// src/main/java/kz/bars/order_service/presentation/controllers/OrderController.java
package kz.bars.order_service.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.bars.order_service.application.dto.OrderRequest;
import kz.bars.order_service.application.dto.OrderResponse;
import kz.bars.order_service.application.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
     * Возвращает список всех заказов.
     * Только для администраторов.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders")
    public ResponseEntity<List<OrderResponse>> getOrders() {
        List<OrderResponse> responses = orderService.getAllOrderResponses();
        return ResponseEntity.ok(responses);
    }

    /**
     * Возвращает заказ по его ID.
     * Пользователи могут получить только свои заказы, администраторы - любые.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @orderService.isOwner(authentication.name, #orderId))")
    @Operation(summary = "Get order ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        try {
            OrderResponse response = orderService.getOrderResponseById(orderId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Создаёт новый заказ.
     * Пользователи и администраторы могут создавать заказы.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create order")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        try {
            OrderResponse response = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Обновляет существующий заказ.
     * Пользователи могут обновлять только свои заказы, администраторы - любые.
     */
    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @orderService.isOwner(authentication.name, #orderId))")
    @Operation(summary = "Update order")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable UUID orderId, @RequestBody @Valid OrderRequest request) {
        try {
            OrderResponse response = orderService.updateOrder(orderId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Удаляет заказ по ID (мягкое удаление).
     * Пользователи могут удалять только свои заказы, администраторы - любые.
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @orderService.isOwner(authentication.name, #orderId))")
    @Operation(summary = "Soft delete order")
    public ResponseEntity<UUID> deleteOrder(@PathVariable UUID orderId) {
        try {
            UUID deletedId = orderService.deleteOrder(orderId);
            return ResponseEntity.ok(deletedId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Обрабатывает все исключения IllegalArgumentException и возвращает корректный HTTP-ответ.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
