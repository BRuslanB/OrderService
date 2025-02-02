package kz.bars.order_service.application.services;

import kz.bars.order_service.domain.specifications.OrderSpecification;
import kz.bars.order_service.presentation.dto.OrderRequest;
import kz.bars.order_service.presentation.dto.OrderResponse;
import kz.bars.order_service.presentation.dto.ProductResponse;
import kz.bars.order_service.application.dto.UserDto;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import kz.bars.order_service.domain.models.Role;
import kz.bars.order_service.domain.repositories.OrderRepository;
import kz.bars.order_service.infrastructure.exception.ApiException;
import kz.bars.order_service.infrastructure.metrics.CustomMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
     * Получение всех заказов с фильтрацией по статусу и диапазону цен.
     * Результаты кешируются в Redis.
     *
     * @param status   статус заказа (может быть null)
     * @param minPrice минимальная цена (может быть null)
     * @param maxPrice максимальная цена (может быть null)
     * @return список заказов в формате DTO
     */
    @Cacheable(value = "orderResponses", key = "'filtered:' + #status?.name() + ':' + #minPrice + ':' + #maxPrice", unless = "#result == null || #result.isEmpty()")
    public List<OrderResponse> getOrdersFiltered(Order.Status status, BigDecimal minPrice, BigDecimal maxPrice) {
        try {
            // Создаем динамическую спецификацию
            Specification<Order> spec = Specification.where(OrderSpecification.hasStatus(status))
                    .and(OrderSpecification.hasMinPrice(minPrice))
                    .and(OrderSpecification.hasMaxPrice(maxPrice));

            // Фильтруем заказы
            List<Order> orders = orderRepository.findAll(spec).stream()
                    .filter(order -> !order.isDeleted()) // Исключаем удалённые заказы
                    .toList();

            // Преобразование сущностей Order в DTO OrderResponse
            List<OrderResponse> responses = orders.stream()
                    .map(this::mapToOrderResponse)
                    .toList();

            // Увеличиваем метрику успешных операций
            customMetrics.incrementSuccessfulOrders();
            return responses;
        } catch (Exception e) {
            customMetrics.incrementFailedOrders();
            throw e;
        }
    }

    /**
     * Получение заказа по ID в виде DTO с использованием Redis Cache.
     * Успешная операция увеличивает счетчик успешных операций.
     */
    @Cacheable(value = "orderResponses", key = "#orderId", unless = "#result == null")
    public OrderResponse getOrderResponseById(UUID orderId) {
        try {
            // Получаем имя текущего пользователя
            String currentUser = userService.getCurrentUsername();
            if (currentUser == null) {
                throw new IllegalStateException("User is not authenticated");
            }

            // Находим заказ по ID, исключая удалённые
            Order order = orderRepository.findById(orderId)
                    .filter(o -> !o.isDeleted())
                    .orElseThrow(() -> new ApiException("Order not found or deleted with ID: " + orderId, HttpStatus.NOT_FOUND));

            // Проверяем доступ пользователя
            if (isAccessDeniedToOrder(currentUser, order)) {
                throw new AccessDeniedException("You do not have permission to modify this order.");
            }

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
    @CacheEvict(value = "orderResponses", allEntries = true)
    @CachePut(value = "orderResponses", key = "#result.orderId")
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        try {
            // Получаем имя текущего пользователя
            String currentUser = userService.getCurrentUsername();
            if (currentUser == null) {
                throw new IllegalStateException("User is not authenticated");
            }

            // Преобразуем запрос в объект заказа
            Order order = mapToOrder(request);

            // Проверяем, переданы ли данные для создания заказа
            if (request.getProducts() == null || request.getProducts().isEmpty()) {
                throw new IllegalArgumentException("The order must contain at least one product.");
            }

            // Устанавливаем имя клиента и вычисляем общую стоимость заказа
            order.setCustomerName(currentUser);
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
    @CacheEvict(value = "orderResponses", allEntries = true)
    @CachePut(value = "orderResponses", key = "#orderId")
    @Transactional
    public OrderResponse updateOrder(UUID orderId, OrderRequest request) {
        try {
            // Получаем имя текущего пользователя
            String currentUser = userService.getCurrentUsername();
            if (currentUser == null) {
                throw new IllegalStateException("User is not authenticated");
            }

            // Находим существующий заказ
            Order existingOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ApiException("Order not found with ID: " + orderId, HttpStatus.NOT_FOUND));

            // Проверяем доступ пользователя
            if (isAccessDeniedToOrder(currentUser, existingOrder)) {
                throw new AccessDeniedException("You do not have permission to modify this order.");
            }

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

            // Сохраняем заказ в репозитории
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
    public void deleteOrder(UUID orderId) {
        try {
            // Получаем имя текущего пользователя
            String currentUser = userService.getCurrentUsername();
            if (currentUser == null) {
                throw new IllegalStateException("User is not authenticated");
            }

            // Находим заказ по ID
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ApiException("Order not found with ID: " + orderId, HttpStatus.NOT_FOUND));

            // Проверяем доступ пользователя
            if (isAccessDeniedToOrder(currentUser, order)) {
                throw new AccessDeniedException("You do not have permission to modify this order.");
            }

            // Помечаем заказ как удалённый
            order.setDeleted(true);

            // Сохраняем заказ в репозитории
            orderRepository.save(order);

            customMetrics.incrementSuccessfulOrders(); // Увеличиваем метрику успешных операций
        } catch (Exception e) {
            customMetrics.incrementFailedOrders(); // Увеличиваем метрику неудачных операций
            throw e; // Пробрасываем исключение дальше
        }
    }

    /**
     * Изменение статуса заказа и публикация события.
     * Успешная операция увеличивает счетчик успешных операций.
     */
    @CacheEvict(value = "orderResponses", allEntries = true)
    @CachePut(value = "orderResponses", key = "#orderId")
    public Order updateOrderStatus(UUID orderId, Order.Status newStatus) {
        try {
            // Находим заказ по ID
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

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
     * Проверяет, запрещен ли доступ пользователя к заказу.
     *
     * @param order объект заказа
     * @return true, если доступ запрещен, иначе false
     */
    public boolean isAccessDeniedToOrder(String currentUser, Order order) {
        // Получает данные пользователя по его имени
        UserDto userDto = userService.getUserByUsername(currentUser);

        // Проверяем, является ли пользователь администратором или владельцем заказа, если да то false
        return userService.getUserByUsername(currentUser)
                .getRoles()
                .stream()
                .noneMatch(role -> role.getName().equals(Role.RoleName.ADMIN))
                && !order.getCustomerName().equals(currentUser);
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
