// src/main/java/kz/bars/order_service/application/dto/OrderRequest.java
package kz.bars.order_service.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    /**
     * Список продуктов. Поле не может быть null и должно содержать валидные объекты ProductRequest.
     * Используется аннотация @NotNull
     * Добавлена @Valid для проверки элементов списка
     */
    @NotNull
    private List<@Valid ProductRequest> products;
}
