package kz.bars.order_service.domain.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders") // Указываем таблицу в базе данных для сущности
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // Версия для сериализации

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Современный способ генерации UUID
    @Column(name = "order_id", updatable = false, nullable = false, columnDefinition = "UUID") // Настройки для столбца
    private UUID orderId; // Уникальный идентификатор заказа

    @NotBlank // Поле не может быть пустым или содержать только пробелы
    private String customerName; // Имя клиента, сделавшего заказ

    @DecimalMin(value = "0.0", inclusive = false) // Цена должна быть больше 0
    private BigDecimal totalPrice; // Общая стоимость заказа

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference // Обеспечивает корректную сериализацию в JSON
    private List<Product> products = new ArrayList<>(); // Список продуктов, связанных с заказом

    @Enumerated(EnumType.STRING) // Хранит значение статуса в виде строки
    @NotNull // Поле не должно быть null
    private Status status = Status.PENDING; // Статус заказа. По умолчанию - PENDING (ожидает обработки)

    private boolean deleted = false; // Флаг для мягкого удаления. По умолчанию - false (не удалён)

    public enum Status { // Возможные статусы заказа
        PENDING,    // Ожидает обработки
        CONFIRMED,  // Подтверждён
        CANCELLED   // Отменён
    }

    /**
     * Метод вычисляет общую стоимость заказа на основе списка продуктов.
     * Суммирует цену каждого продукта, умноженную на его количество.
     * Если список продуктов пуст, общая стоимость устанавливается в 0.
     */
    public void calculateTotalPrice() {
        if (products == null || products.isEmpty()) {
            totalPrice = BigDecimal.ZERO; // Если продуктов нет, общая стоимость равна 0
            return;
        }

        for (Product product : products) {
            // Проверяем, чтобы количество продукта не было отрицательным
            if (product.getQuantity() < 0) {
                throw new IllegalArgumentException("Количество продукта не может быть отрицательным: " + product.getName());
            }
        }

        // Вычисляем общую стоимость заказа
        totalPrice = products.stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
