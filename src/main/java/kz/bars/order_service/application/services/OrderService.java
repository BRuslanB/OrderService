// src/main/java/kz/bars/order_service/application/services/OrderService.java
package kz.bars.order_service.application.services;

import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import kz.bars.order_service.domain.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Возвращает список всех заказов, которые не были удалены.
     * @return список заказов.
     */
    public List<Order> getOrders() {
        return orderRepository.findAllNotDeleted();
    }

    /**
     * Возвращает заказ по идентификатору, если он не помечен как удалённый.
     * @param orderId идентификатор заказа.
     * @return заказ.
     * @throws RuntimeException если заказ не найден или удалён.
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .filter(order -> !order.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Order not found or deleted with ID: " + orderId));
    }

    /**
     * Создаёт новый заказ и вычисляет его общую стоимость.
     * @param order заказ для сохранения.
     * @return созданный заказ.
     */
    @Transactional
    public Order createOrder(Order order) {
        order.calculateTotalPrice(); // Вычисляем общую стоимость заказа
        return orderRepository.save(order);
    }

    /**
     * Обновляет существующий заказ: обновляет имя клиента, список продуктов и статус.
     * Также пересчитывает общую стоимость заказа.
     * @param orderId идентификатор заказа.
     * @param updatedOrder данные для обновления.
     * @return обновлённый заказ.
     */
    @Transactional
    public Order updateOrder(Long orderId, Order updatedOrder) {
        // Извлекаем существующий заказ из базы данных
        Order existingOrder = getOrderById(orderId);

        // Обновляем имя клиента и статус
        existingOrder.setCustomerName(updatedOrder.getCustomerName());
        existingOrder.setStatus(updatedOrder.getStatus());

        // Удаляем старые продукты
        existingOrder.getProducts().clear();

        // Добавляем новые продукты
        if (updatedOrder.getProducts() != null && !updatedOrder.getProducts().isEmpty()) {
            List<Product> updatedProducts = updatedOrder.getProducts().stream()
                    .peek(product -> {
                        product.setOrder(existingOrder); // Устанавливаем связь с заказом
                    })
                    .toList();
            existingOrder.getProducts().addAll(updatedProducts);
        }

        // Пересчитываем общую стоимость заказа
        existingOrder.calculateTotalPrice();

        // Сохраняем и возвращаем обновленный заказ
        return orderRepository.save(existingOrder);
    }

    /**
     * Мягко удаляет заказ, устанавливая флаг deleted = true.
     * @param orderId идентификатор заказа.
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = getOrderById(orderId);
        order.setDeleted(true); // Помечаем заказ как удалённый
        orderRepository.save(order);
    }

    /**
     * Изменяет статус заказа и генерирует событие.
     * @param orderId   ID заказа
     * @param newStatus Новый статус заказа
     * @return Обновлённый заказ
     */
    @SuppressWarnings("unused") // Подавляем предупреждение о неиспользуемом методе
    public Order updateOrderStatus(Long orderId, Order.Status newStatus) {
        // Находим заказ по ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Сохраняем старый статус
        Order.Status oldStatus = order.getStatus();

        // Обновляем статус
        order.setStatus(newStatus);

        // Генерируем событие
        generateStatusChangeEvent(order.getOrderId(), oldStatus, newStatus);

        // Сохраняем обновлённый заказ
        return orderRepository.save(order);
    }

    /**
     * Заглушка, генерирует событие изменения статуса заказа.
     * @param orderId   ID заказа
     * @param oldStatus Старый статус
     * @param newStatus Новый статус
     */
    private void generateStatusChangeEvent(Long orderId, Order.Status oldStatus, Order.Status newStatus) {
        System.out.printf("Event generated: order_id=%d, old_status=%s, new_status=%s%n",
                orderId, oldStatus, newStatus);
    }
}
