package kz.bars.order_service.domain.repositories;

import kz.bars.order_service.builder.OrderTestBuilder;
import kz.bars.order_service.builder.ProductTestBuilder;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test") // Используем тестовый профиль для тестирования
@DataJpaTest // Конфигурирует JPA-тесты с использованием встроенной базы данных
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Заменяем базу данных на H2
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository; // Внедрение зависимости через поле

    /**
     * Этот тест проверяет сохранение и последующее извлечение заказа из репозитория.
     * Убедитесь, что заказ сохраняется корректно, включая продукты, имя клиента, общую стоимость и статус.
     */
    @Test
    void testSaveAndFindOrder() {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .customerName("John Doe")
                .status(Order.Status.PENDING)
                .build()
                .toOrder();

        // Устанавливаем связь между заказом и продуктом
        product.setOrder(order);
        order.setProducts(List.of(product));

        // Пересчитываем общую стоимость
        order.calculateTotalPrice();

        // Act
        Order savedOrder = orderRepository.save(order);
        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getOrderId());

        // Assert
        assertTrue(retrievedOrder.isPresent());
        assertEquals("John Doe", retrievedOrder.get().getCustomerName());
        assertEquals(Order.Status.PENDING, retrievedOrder.get().getStatus());
        assertEquals(BigDecimal.valueOf(200), retrievedOrder.get().getTotalPrice());

        // Проверяем продукт
        Product foundProduct = retrievedOrder.get().getProducts().get(0);
        assertEquals("Product A", foundProduct.getName());
        assertEquals(BigDecimal.valueOf(100), foundProduct.getPrice());
        assertEquals(2, foundProduct.getQuantity());
    }
}
