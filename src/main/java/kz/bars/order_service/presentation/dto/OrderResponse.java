package kz.bars.order_service.presentation.dto;

import kz.bars.order_service.domain.models.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /**
     * Уникальный идентификатор заказа.
     */
    private UUID orderId;

    /**
     * Имя клиента, сделавшего заказ.
     */
    private String customerName;

    /**
     * Список продуктов в заказе.
     * Используется DTO ProductResponse для представления каждого продукта.
     */
    private List<ProductResponse> products;

    /**
     * Общая стоимость заказа.
     */
    private BigDecimal totalPrice;

    /**
     * Статус заказа (например, PENDING, CONFIRMED, CANCELLED).
     */
    private Order.Status status;
}
