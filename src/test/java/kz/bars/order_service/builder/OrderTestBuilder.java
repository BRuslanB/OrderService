package kz.bars.order_service.builder;

import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class OrderTestBuilder {

    @Builder.Default
    private String customerName = "Default Customer";

    @Builder.Default
    private Order.Status status = Order.Status.PENDING;

    @Builder.Default
    private List<Product> products = new ArrayList<>();

    public Order toOrder() {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setStatus(status);
        order.setProducts(products != null ? products : new ArrayList<>());
        order.calculateTotalPrice();
        return order;
    }
}
