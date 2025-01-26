// src/main/java/kz/bars/order_service/domain/repositories/OrderRepository.java
package kz.bars.order_service.domain.repositories;

import kz.bars.order_service.domain.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Возвращает список всех заказов, которые не были мягко удалены.
     * Используется для фильтрации только актуальных заказов.
     * @return Список заказов, где поле deleted = false.
     */
    @Query("SELECT o FROM Order o WHERE o.deleted = false")
    List<Order> findAllNotDeleted();

    /**
     * Возвращает список заказов с указанным статусом.
     * @param status Статус заказа (например, PENDING, CONFIRMED, CANCELLED).
     * @return Список заказов с данным статусом.
     */
    List<Order> findByStatus(Order.Status status);

    /**
     * Возвращает список заказов по указанным параметрам.
     * @param status Статус заказа (может быть null, если фильтр по статусу не нужен)
     * @param minPrice Минимальная цена (может быть null, если фильтр по минимальной цене не нужен)
     * @param maxPrice Максимальная цена (может быть null, если фильтр по максимальной цене не нужен)
     * @return список заказов, соответствующих фильтрам
     */
    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:minPrice IS NULL OR o.totalPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR o.totalPrice <= :maxPrice)")
    List<Order> findOrdersFiltered(
            @Param("status") Order.Status status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);
}
