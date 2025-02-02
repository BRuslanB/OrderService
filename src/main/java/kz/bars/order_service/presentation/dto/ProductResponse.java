package kz.bars.order_service.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    /**
     * Название продукта.
     */
    private String name;

    /**
     * Цена продукта.
     */
    private BigDecimal price;

    /**
     * Количество продукта.
     */
    private int quantity;
}
