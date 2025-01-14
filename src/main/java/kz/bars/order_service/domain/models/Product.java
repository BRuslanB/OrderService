// src/main/java/kz/bars/order_service/domain/models/Product.java
package kz.bars.order_service.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId; // Уникальный идентификатор для Product

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    // Устанавливаем связь между Product и Order многие к одному
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
