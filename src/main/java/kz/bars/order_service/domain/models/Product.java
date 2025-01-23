// src/main/java/kz/bars/order_service/domain/models/Product.java
package kz.bars.order_service.domain.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products") // Указываем таблицу в базе данных для сущности
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // Версия для сериализации

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Современный способ генерации UUID
    @Column(name = "product_id", updatable = false, nullable = false, columnDefinition = "UUID") // Настройки для столбца
    private UUID productId; // Уникальный идентификатор продукта

    @Column(nullable = false) // Поле обязательно для заполнения
    private String name; // Название продукта

    @Column(nullable = false) // Поле обязательно для заполнения
    private BigDecimal price; // Цена продукта

    @Column(nullable = false) // Поле обязательно для заполнения
    private Integer quantity; // Количество продукта

    @ManyToOne // Устанавливаем связь многие-к-одному с заказом
    @JoinColumn(name = "order_id") // Внешний ключ для связи с таблицей заказов
    @JsonBackReference // Обеспечивает корректную сериализацию в JSON
    private Order order; // Ссылка на заказ, к которому принадлежит продукт
}
