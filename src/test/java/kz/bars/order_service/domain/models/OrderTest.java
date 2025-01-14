package kz.bars.order_service.domain.models;

import kz.bars.order_service.builder.ProductTestBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderTest {

    /**
     * Этот тест проверяет правильное вычисление общей стоимости заказа
     * с несколькими продуктами.
     */
    @Test
    void testCalculateTotalPrice() {
        // Arrange
        Order order = new Order();

        Product product1 = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .quantity(2)
                .build() // Завершает настройку билдера
                .toProduct(); // Преобразует в Product

        Product product2 = ProductTestBuilder.builder()
                .name("Product B")
                .price(BigDecimal.valueOf(50))
                .quantity(3)
                .build() // Завершает настройку билдера
                .toProduct(); // Преобразует в Product

        order.setProducts(List.of(product1, product2));

        // Act
        order.calculateTotalPrice();

        // Assert
        assertEquals(BigDecimal.valueOf(350), order.getTotalPrice());
    }

    /**
     * Этот тест проверяет, что общая стоимость заказа равна нулю,
     * если список продуктов пуст.
     */
    @Test
    void testCalculateTotalPriceWithNoProducts() {
        // Arrange
        Order order = new Order();
        order.setProducts(List.of());

        // Act
        order.calculateTotalPrice();

        // Assert
        assertEquals(BigDecimal.ZERO, order.getTotalPrice());
    }

    /**
     * Этот тест проверяет, что общая стоимость заказа равна нулю,
     * если список продуктов равен null.
     */
    @Test
    void testCalculateTotalPriceWithNullProducts() {
        // Arrange
        Order order = new Order();
        order.setProducts(null);

        // Act
        order.calculateTotalPrice();

        // Assert
        assertEquals(BigDecimal.ZERO, order.getTotalPrice());
    }

    /**
     * Этот тест проверяет, что метод выбрасывает исключение,
     * если у продукта отрицательное количество.
     */
    @Test
    void testInvalidProductQuantity() {
        // Arrange
        Order order = new Order();

        Product invalidProduct = ProductTestBuilder.builder()
                .name("Product C")
                .price(BigDecimal.valueOf(100))
                .quantity(-1)
                .build() // Завершает настройку билдера
                .toProduct(); // Преобразует в Product

        order.setProducts(List.of(invalidProduct));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, order::calculateTotalPrice);
    }
}
