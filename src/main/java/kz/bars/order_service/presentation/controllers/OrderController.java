// src/main/java/kz/bars/order_service/presentation/controllers/OrderController.java
package kz.bars.order_service.presentation.controllers;

import jakarta.validation.Valid;
import kz.bars.order_service.application.dto.OrderRequest;
import kz.bars.order_service.application.dto.OrderResponse;
import kz.bars.order_service.application.dto.ProductResponse;
import kz.bars.order_service.application.services.OrderService;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class OrderController {

    private final OrderService orderService;

    /**
     * Возвращает список всех заказов.
     * Только для администраторов.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrders() {
        List<Order> orders = orderService.getOrders();
        List<OrderResponse> responses = orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Возвращает заказ по его ID.
     * Пользователи могут получить только свои заказы, администраторы - любые.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @orderService.isOwner(authentication.name, #orderId))")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            OrderResponse response = mapToOrderResponse(order);
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
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        try {
            Order order = mapToOrder(request);
            Order createdOrder = orderService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToOrderResponse(createdOrder));
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
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long orderId, @RequestBody @Valid OrderRequest request) {
        try {
            Order order = mapToOrder(request);
            Order updatedOrder = orderService.updateOrder(orderId, order);
            return ResponseEntity.ok(mapToOrderResponse(updatedOrder));
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
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Преобразует объект OrderRequest в объект Order.
     */
    private Order mapToOrder(OrderRequest request) {
        Order order = new Order();
        order.setProducts(request.getProducts().stream()
                .map(productRequest -> {
                    Product product = new Product();
                    product.setName(productRequest.getName());
                    product.setPrice(productRequest.getPrice());
                    product.setQuantity(productRequest.getQuantity());
                    product.setOrder(order); // Устанавливаем связь с заказом
                    return product;
                })
                .collect(Collectors.toList()));
        return order;
    }

    /**
     * Преобразует объект Order в объект OrderResponse.
     */
    private OrderResponse mapToOrderResponse(Order order) {
        List<ProductResponse> productResponses = Optional.ofNullable(order.getProducts())
                .orElse(Collections.emptyList())
                .stream()
                .map(product -> new ProductResponse(
                        product.getName(),
                        product.getPrice(),
                        product.getQuantity()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerName(),
                productResponses,
                order.getTotalPrice(),
                order.getStatus()
        );
    }

    /**
     * Обрабатывает все исключения IllegalArgumentException и возвращает корректный HTTP-ответ.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
