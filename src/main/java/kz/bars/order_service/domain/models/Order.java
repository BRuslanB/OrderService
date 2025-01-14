// src/main/java/kz/bars/order_service/domain/models/Order.java
package kz.bars.order_service.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId; // Уникальный идентификатор для Order

    @NotBlank // Поле не может быть пустым или содержать только пробелы
    private String customerName;

    @DecimalMin(value = "0.0", inclusive = false) // Поле должно быть больше 0.
    private BigDecimal totalPrice;

    // Устанавливаем связь между Order и Product один ко многим
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    @Enumerated(EnumType.STRING)            // Используем список статусов в качестве значений
    @NotNull                                // Поле не может быть null
    private Status status = Status.PENDING; // Значение по умолчанию PENDING

    private boolean deleted = false; // Флаг для мягкого удаления. Значение по умолчанию false.

    public enum Status {    // Список статусов заказа
        PENDING,            // Ожидает обработки
        CONFIRMED,          // Подтвержден
        CANCELLED           // Отменен
    }

    /**
     * Вычисляет общую стоимость заказа на основе списка продуктов.
     * Умножает цену каждого продукта на его количество и суммирует все значения.
     */
    public void calculateTotalPrice() {
        if (products == null || products.isEmpty()) {
            totalPrice = BigDecimal.ZERO;
            return;
        }

        for (Product product : products) {
            if (product.getQuantity() < 0) {
                throw new IllegalArgumentException("Количество продукта не может быть отрицательным: " + product.getName());
            }
        }

        totalPrice = products.stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
