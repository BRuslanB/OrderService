// src/main/java/kz/bars/order_service/application/services/OrderService.java
package kz.bars.order_service.application.services;

import kz.bars.order_service.application.dto.OrderRequest;
import kz.bars.order_service.application.dto.OrderResponse;
import kz.bars.order_service.application.dto.ProductResponse;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import kz.bars.order_service.domain.repositories.OrderRepository;
import kz.bars.order_service.infrastructure.metrics.CustomMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CustomMetrics customMetrics;

    /**
     * Получение всех заказов в виде DTO с использованием Redis Cache.
     * Успешная операция увеличивает счетчик успешных операций.
     */
    @Cacheable(value = "orderResponses", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<OrderResponse> getAllOrderResponses() {
        try {
            // Извлекаем все заказы, которые не помечены как удалённые
            List<OrderResponse> responses = orderRepository.findAllNotDeleted().stream()
                    .map(this::mapToOrderResponse) // Преобразуем каждый заказ в DTO
                    .collect(Collectors.toList());
            customMetrics.incrementSuccessfulOrders(); // Увеличиваем метрику успешных операций
            return responses;
        } catch (Exception e) {
            customMetrics.incrementFailedOrders(); // Увеличиваем метрику неудачных операций
            throw e; // Пробрасываем исключение дальше
        }
    }

    /**
     * Получение заказа по ID в виде DTO с использованием Redis Cache.
     * Успешная операция увеличивает счетчик успешных операций.
     */
    @Cacheable(value = "orderResponses", key = "#orderId", unless = "#result == null")
    public OrderResponse getOrderResponseById(UUID orderId) {
        try {
            // Находим заказ по ID, исключая удалённые
            Order order = orderRepository.findById(orderId)
                    .filter(o -> !o.isDeleted())
                    .orElseThrow(() -> new IllegalArgumentException("Order not found or deleted with ID: " + orderId));
            customMetrics.incrementSuccessfulOrders(); // Увеличиваем метрику успешных операций
            return mapToOrderResponse(order); // Преобразуем заказ в DTO и возвращаем
        } catch (Exception e) {
            customMetrics.incrementFailedOrders(); // Увеличиваем метрику неудачных операций
            throw e; // Пробрасываем исключение дальше
        }
    }

    /**
     * Создание нового заказа, преобразование в DTO и обновление кэша.
     * Успешная операция увеличивает счетчик успешных операций.
     */
    @CachePut(value = "orderResponses", key = "#result.orderId")
    @CacheEvict(value = "orderResponses", key = "'all'")
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        try {
            // Преобразуем запрос в объект заказа
            Order order = mapToOrder(request);

            // Получаем имя текущего пользователя
            String username = userService.getCurrentUsername();
            if (username == null) {
                throw new IllegalStateException("User is not authenticated");
            }

            // Устанавливаем имя клиента и вычисляем общую стоимость заказа
            order.setCustomerName(username);
            order.calculateTotalPrice();

            // Сохраняем заказ в репозитории
            Order savedOrder = orderRepository.save(order);
            customMetrics.incrementSuccessfulOrders(); // Увеличиваем метрику успешных операций
            return mapToOrderResponse(savedOrder); // Преобразуем сохранённый заказ в DTO и возвращаем
        } catch (Exception e) {
            customMetrics.incrementFailedOrders(); // Увеличиваем метрику неудачных операций
            throw e; // Пробрасываем исключение дальше
        }
    }

    /**
     * Обновление заказа, преобразование в DTO и обновление кэша.
     * Успешная операция увеличивает счетчик успешных операций.
     */
    @CachePut(value = "orderResponses", key = "#orderId")
    @CacheEvict(value = "orderResponses", key = "'all'")
    @Transactional
    public OrderResponse updateOrder(UUID orderId, OrderRequest request) {
        try {
            // Находим существующий заказ
            Order existingOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

            // Очищаем список продуктов и добавляем новые
            existingOrder.getProducts().clear();
            existingOrder.getProducts().addAll(
                    request.getProducts().stream()
                            .map(productRequest -> {
                                Product product = new Product();
                                product.setName(productRequest.getName());
                                product.setPrice(productRequest.getPrice());
                                product.setQuantity(productRequest.getQuantity());
                                product.setOrder(existingOrder); // Устанавливаем связь с заказом
                                return product;
                            })
                            .toList()
            );

            // Пересчитываем общую стоимость и сохраняем изменения
            existingOrder.calculateTotalPrice();
            Order updatedOrder = orderRepository.save(existingOrder);
            customMetrics.incrementSuccessfulOrders(); // Увеличиваем метрику успешных операций
            return mapToOrderResponse(updatedOrder); // Преобразуем заказ в DTO и возвращаем
        } catch (Exception e) {
            customMetrics.incrementFailedOrders(); // Увеличиваем метрику неудачных операций
            throw e; // Пробрасываем исключение дальше
        }
    }

    /**
     * Мягкое удаление заказа и удаление из кэша.
     * Успешная операция увеличивает счетчик успешных операций.
     */
    @CacheEvict(value = "orderResponses", allEntries = true)
    @Transactional
    public UUID deleteOrder(UUID orderId) {
        try {
            // Находим заказ по ID
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

            // Помечаем заказ как удалённый
            order.setDeleted(true);
            orderRepository.save(order);

            customMetrics.incrementSuccessfulOrders(); // Увеличиваем метрику успешных операций
            return orderId; // Возвращаем ID удалённого заказа
        } catch (Exception e) {
            customMetrics.incrementFailedOrders(); // Увеличиваем метрику неудачных операций
            throw e; // Пробрасываем исключение дальше
        }
    }

    /**
     * Изменение статуса заказа и публикация события.
     * Успешная операция увеличивает счетчик успешных операций.
     */
    @CachePut(value = "orderResponses", key = "#orderId")
    @CacheEvict(value = "orderResponses", key = "'all'")
    public Order updateOrderStatus(UUID orderId, Order.Status newStatus) {
        try {
            // Находим заказ по ID
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            // Сохраняем старый статус и обновляем на новый
            Order.Status oldStatus = order.getStatus();
            order.setStatus(newStatus);

            // Генерируем событие изменения статуса
            generateStatusChangeEvent(order.getOrderId(), oldStatus, newStatus);

            // Сохраняем изменения
            Order updatedOrder = orderRepository.save(order);
            customMetrics.incrementSuccessfulOrders(); // Увеличиваем метрику успешных операций
            return updatedOrder; // Возвращаем обновлённый заказ
        } catch (Exception e) {
            customMetrics.incrementFailedOrders(); // Увеличиваем метрику неудачных операций
            throw e; // Пробрасываем исключение дальше
        }
    }

    /**
     * Заглушка, генерирует событие при изменении статуса заказа.
     * @param orderId   ID заказа
     * @param oldStatus Старый статус
     * @param newStatus Новый статус
     */
    private void generateStatusChangeEvent(UUID orderId, Order.Status oldStatus, Order.Status newStatus) {
        System.out.printf("Event generated: order_id=%s, old_status=%s, new_status=%s%n",
                orderId, oldStatus, newStatus);
    }

    /**
     * Проверяет, является ли пользователь владельцем заказа.
     */
    public boolean isOwner(String username, UUID orderId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getCustomerName().equals(username))
                .orElse(false);
    }

    /**
     * Преобразует объект Order в OrderResponse.
     */
    private OrderResponse mapToOrderResponse(Order order) {
        List<ProductResponse> productResponses = order.getProducts().stream()
                .map(product -> new ProductResponse(
                        product.getName(),
                        product.getPrice(),
                        product.getQuantity()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerName(),
                productResponses,
                order.getTotalPrice(),
                order.getStatus()
        );
    }

    /**
     * Преобразует объект OrderRequest в Order.
     */
    private Order mapToOrder(OrderRequest request) {
        Order order = new Order();
        order.setProducts(request.getProducts().stream()
                .map(productRequest -> {
                    Product product = new Product();
                    product.setName(productRequest.getName());
                    product.setPrice(productRequest.getPrice());
                    product.setQuantity(productRequest.getQuantity());
                    product.setOrder(order);
                    return product;
                })
                .collect(Collectors.toList()));
        return order;
    }
}
