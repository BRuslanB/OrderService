package kz.bars.order_service.builder;

import kz.bars.order_service.application.dto.OrderRequest;
import kz.bars.order_service.application.dto.ProductRequest;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class OrderRequestTestBuilder {

    @Builder.Default
    private List<ProductRequest> products = new ArrayList<>();

    public OrderRequest toOrderRequest() {
        OrderRequest request = new OrderRequest();
        request.setProducts(products != null ? products : new ArrayList<>());
        return request;
    }
}
