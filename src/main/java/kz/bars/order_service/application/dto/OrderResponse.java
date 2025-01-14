package kz.bars.order_service.application.dto;

import kz.bars.order_service.domain.models.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;
    private String customerName;
    private List<ProductResponse> products; // DTO для продуктов
    private BigDecimal totalPrice;
    private Order.Status status; // Статус заказа
}
