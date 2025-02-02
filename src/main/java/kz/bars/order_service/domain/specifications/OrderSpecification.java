package kz.bars.order_service.domain.specifications;

import kz.bars.order_service.domain.models.Order;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.math.BigDecimal;

/**
 * Класс для динамической фильтрации заказов с использованием Spring Data JPA Specification.
 */
public class OrderSpecification {

    /**
     * Фильтр по статусу заказа.
     * @param status статус заказа (может быть null)
     * @return спецификация для фильтрации
     */
    public static Specification<Order> hasStatus(Order.Status status) {
        return (Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                status != null ? cb.equal(root.get("status"), status) : cb.conjunction();
    }

    /**
     * Фильтр по минимальной цене заказа.
     * @param minPrice минимальная цена (может быть null)
     * @return спецификация для фильтрации
     */
    public static Specification<Order> hasMinPrice(BigDecimal minPrice) {
        return (Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                minPrice != null ? cb.greaterThanOrEqualTo(root.get("totalPrice"), minPrice) : cb.conjunction();
    }

    /**
     * Фильтр по максимальной цене заказа.
     * @param maxPrice максимальная цена (может быть null)
     * @return спецификация для фильтрации
     */
    public static Specification<Order> hasMaxPrice(BigDecimal maxPrice) {
        return (Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                maxPrice != null ? cb.lessThanOrEqualTo(root.get("totalPrice"), maxPrice) : cb.conjunction();
    }
}
