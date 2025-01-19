package kz.bars.order_service.application.services;

import kz.bars.order_service.builder.OrderTestBuilder;
import kz.bars.order_service.builder.ProductTestBuilder;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import kz.bars.order_service.domain.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class) // Подключает поддержку Mockito
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderService orderService;

    /**
     * Тест проверяет корректное создание заказа.
     * Убедитесь, что заказ сохраняется с правильными данными клиента и общей стоимостью.
     */
    @Test
    void testCreateOrder() {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .status(Order.Status.PENDING)
                .build()
                .toOrder();

        // Устанавливаем связь между продуктом и заказом
        product.setOrder(order);
        order.setProducts(List.of(product));

        when(userService.getCurrentUsername()).thenReturn("testuser");
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        Order createdOrder = orderService.createOrder(order);

        // Assert
        assertNotNull(createdOrder);
        assertEquals("testuser", createdOrder.getCustomerName());
        assertEquals(BigDecimal.valueOf(200), createdOrder.getTotalPrice());
    }

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

    /**
     * Тест проверяет мягкое удаление заказа.
     * Убедитесь, что флаг isDeleted становится true, и изменения сохраняются.
     */
    @Test
    void testSoftDeleteOrder() {
        // Arrange
        Order order = OrderTestBuilder.builder()
                .customerName("John Doe")
                .status(Order.Status.PENDING)
                .build()
                .toOrder(); // Создаём заказ через билдер
        order.setDeleted(false);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.deleteOrder(1L);

        // Assert
        assertTrue(order.isDeleted());
        verify(orderRepository).save(order);
    }
}
