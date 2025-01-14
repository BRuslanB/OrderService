package kz.bars.order_service.builder;

import kz.bars.order_service.domain.models.Product;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public class ProductTestBuilder {

    @Builder.Default
    private String name = "Default Product";

    @Builder.Default
    private BigDecimal price = BigDecimal.valueOf(100);

    @Builder.Default
    private int quantity = 1;

    public Product toProduct() {
        Product product = new Product();
        product.setName(name != null ? name : "Default Product");
        product.setPrice(price);
        product.setQuantity(quantity);
        return product;
    }
}
