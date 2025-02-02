package kz.bars.order_service.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.bars.order_service.application.services.OrderService;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.presentation.dto.OrderRequest;
import kz.bars.order_service.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Orders API", description = "API for managing orders")
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class OrderController {

    private final OrderService orderService;

    /**
     * Получение списка заказов с фильтрацией.
     * Доступно только администраторам.
     *
     * @param status   статус заказа (опционально)
     * @param minPrice минимальная цена (опционально)
     * @param maxPrice максимальная цена (опционально)
     * @return список заказов
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders by filter")
    public ResponseEntity<List<OrderResponse>> getOrdersFiltered(
            @RequestParam(value = "status", required = false) Order.Status status,
            @RequestParam(value = "min_price", required = false) BigDecimal minPrice,
            @RequestParam(value = "max_price", required = false) BigDecimal maxPrice) {

        // Получение отфильтрованных заказов через сервис
        List<OrderResponse> orders = orderService.getOrdersFiltered(status, minPrice, maxPrice);

        // Возврат списка заказов с HTTP статусом OK
        return ResponseEntity.ok(orders);
    }

    /**
     * Возвращает заказ по его ID.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get order ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        // Получение заказа через сервис
        OrderResponse response = orderService.getOrderResponseById(orderId);

        // Возврат заказа с HTTP статусом OK
        return ResponseEntity.ok(response);
    }

    /**
     * Создаёт новый заказ.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create order")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        // Создание заказа через сервис
        OrderResponse response = orderService.createOrder(request);

        // Возврат созданного заказа с HTTP статусом CREATED
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Обновляет существующий заказ.
     */
    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update order")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable UUID orderId, @RequestBody @Valid OrderRequest request) {
        // Обновление заказа через сервис
        OrderResponse response = orderService.updateOrder(orderId, request);

        // Возврат обновленного заказа с HTTP статусом OK
        return ResponseEntity.ok(response);
    }

    /**
     * Удаляет заказ по ID (мягкое удаление).
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Soft delete order")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID orderId) {
        // Удаление заказа через сервис
        orderService.deleteOrder(orderId);

        // Возврат пустого контента с HTTP статусом NO_CONTENT
        return ResponseEntity.noContent().build();
    }
}
