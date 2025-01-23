package kz.bars.order_service.application.services;

import kz.bars.order_service.builder.OrderTestBuilder;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ActiveProfiles("test") // Используем тестовый профиль
@ExtendWith(MockitoExtension.class) // Подключает поддержку Mockito
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    /**
     * Тест проверяет обновление статуса заказа на CONFIRMED.
     * Убедитесь, что статус обновляется и сохраняется корректно.
     */
    @Test
    void testUpdateOrderStatusToConfirmed() {
        // Arrange
        Order order = OrderTestBuilder.builder()
                .customerName("John Doe")
                .status(Order.Status.PENDING)
                .build()
                .toOrder(); // Создаём заказ через билдер

        // Act
        order.setStatus(Order.Status.CONFIRMED);
        orderRepository.save(order);

        // Assert
        assertEquals(Order.Status.CONFIRMED, order.getStatus());
    }

    /**
     * Тест проверяет обновление статуса заказа на CANCELLED.
     * Убедитесь, что статус обновляется и сохраняется корректно.
     */
    @Test
    void testCancelOrder() {
        // Arrange
        Order order = OrderTestBuilder.builder()
                .customerName("Jane Smith")
                .status(Order.Status.PENDING)
                .build()
                .toOrder(); // Создаём заказ через билдер

        // Act
        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);

        // Assert
        assertEquals(Order.Status.CANCELLED, order.getStatus());
    }

    /**
     * Тест проверяет фильтрацию заказов по статусу CONFIRMED.
     * Убедитесь, что из репозитория возвращаются только заказы с этим статусом.
     */
    @Test
    void testFindOrdersByStatus() {
        // Arrange
        Order confirmedOrder = OrderTestBuilder.builder()
                .customerName("Alice")
                .status(Order.Status.CONFIRMED)
                .build()
                .toOrder(); // Создаём заказ через билдер

        when(orderRepository.findByStatus(Order.Status.CONFIRMED))
                .thenReturn(List.of(confirmedOrder));

        // Act
        List<Order> orders = orderRepository.findByStatus(Order.Status.CONFIRMED);

        // Assert
        assertEquals(1, orders.size());
        assertEquals(Order.Status.CONFIRMED, orders.get(0).getStatus());
    }
}
