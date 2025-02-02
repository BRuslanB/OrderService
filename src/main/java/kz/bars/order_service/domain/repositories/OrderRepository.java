package kz.bars.order_service.domain.repositories;

import kz.bars.order_service.domain.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    /**
     * Возвращает список всех заказов, с указаным статусом.
     * @return Список заказов, с указаным статусом.
     */
    List<Order> findByStatus(Order.Status status);
}
