// src/main/java/kz/bars/order_service/application/dto/ProductRequest.java
package kz.bars.order_service.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    /**
     * Название продукта. Поле не может быть пустым или содержать только пробелы.
     * Используется аннотация @NotBlank
     */
    @NotBlank
    private String name;

    /**
     * Цена продукта. Поле должно быть больше 0.
     * Используется аннотация @DecimalMin с параметрами:
     * - value = "0.0": минимальное значение.
     * - inclusive = false: значение должно быть строго больше указанного минимума.
     */
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    /**
     * Количество продукта. Поле должно быть целым числом >= 1.
     * Используется аннотация @Min
     */
    @Min(1)
    private Integer quantity;
}
