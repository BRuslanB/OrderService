package kz.bars.order_service.infrastructure.logging;

import kz.bars.order_service.application.services.UserService;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.presentation.dto.OrderResponse;
import kz.bars.order_service.presentation.dto.SignupRequest;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Aspect
@Component
@Log4j2
@AllArgsConstructor
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class LoggingAspect {


    private final UserService userService;


    /**
     * Логирование действий пользователя: пользователь зарегистрирован.
     */
    @AfterReturning("execution(* kz.bars.order_service.application.services.AuthService.registerUser(..))")
    public void logUserRegistration(JoinPoint joinPoint) {
        // Извлечение имени пользователя из аргументов метода
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof SignupRequest signupRequest) {
            log.info("User registered: {}", signupRequest.getUsername()); // Логируем только имя пользователя
        } else {
            log.warn("Unable to log user registration: invalid arguments.");
        }
    }

    /**
     * Логирование действий пользователя: пользователь вошел в систему.
     */
    @AfterReturning("execution(* kz.bars.order_service.application.services.AuthService.authenticate(..))")
    public void logUserLogin(JoinPoint joinPoint) {
        String username = userService.getCurrentUsername();
        log.info("User logged in: {}", username != null ? username : "Unknown");
    }

    /**
     * Логирование действий пользователя: пользователь вышел из системы.
     */
    @AfterReturning("execution(* kz.bars.order_service.application.services.AuthService.logout(..))")
    public void logUserLogout(JoinPoint joinPoint) {
        log.info("User logged out: {}", userService.getCurrentUsername());
    }

    /**
     * Логирование действий пользователя: создание заказа.
     */
    @AfterReturning(pointcut = "execution(* kz.bars.order_service.application.services.OrderService.createOrder(..))", returning = "result")
    public void logOrderCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof OrderResponse response) {
            log.info("Order with ID {} was created by user: {}", response.getOrderId(), userService.getCurrentUsername());
        }
    }

    /**
     * Логирование действий пользователя: обновление заказа.
     */
    @AfterReturning(pointcut = "execution(* kz.bars.order_service.application.services.OrderService.updateOrder(..))", returning = "result")
    public void logOrderUpdate(JoinPoint joinPoint, Object result) {
        if (result instanceof OrderResponse response) {
            log.info("Order with ID {} was updated by user: {}", response.getOrderId(), userService.getCurrentUsername());
        }
    }

    /**
     * Логирование действий пользователя: удаление заказа.
     */
    @After("execution(* kz.bars.order_service.application.services.OrderService.deleteOrder(..))")
    public void logOrderDeletion(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof UUID orderId) {
            log.info("Order with ID {} was marked as deleted by user: {}", orderId, userService.getCurrentUsername());
        }
    }

    /**
     * Логирование действий пользователя: получение заказа по ID.
     */
    @AfterReturning(pointcut = "execution(* kz.bars.order_service.application.services.OrderService.getOrderResponseById(..))", returning = "result")
    public void logGetOrderResponseById(JoinPoint joinPoint, Object result) {
        if (result instanceof OrderResponse response) {
            log.info("Order with ID {} was retrieved by user: {}", response.getOrderId(), userService.getCurrentUsername());
        }
    }

    /**
     * Логирование действий пользователя: получение всех заказов.
     */
    @AfterReturning(pointcut = "execution(* kz.bars.order_service.application.services.OrderService.getAllOrderResponses(..))", returning = "result")
    public void logGetAllOrderResponses(JoinPoint joinPoint, Object result) {
        if (result instanceof List<?> responses) {
            log.info("User {} retrieved all orders. Total count: {}", userService.getCurrentUsername(), responses.size());
        }
    }

    /**
     * Логирование действий пользователя: получение всех заказов по фильтрам.
     */
    @AfterReturning(pointcut = "execution(* kz.bars.order_service.application.services.OrderService.getOrdersFiltered(..))", returning = "result")
    public void logGetFilteredOrders(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        if (result instanceof List<?> responses) {
            log.info("User {} retrieved filtered orders. Total count: {}, Filters: status={}, minPrice={}, maxPrice={}",
                    userService.getCurrentUsername(),
                    responses.size(),
                    args.length > 0 ? args[0] : "N/A",
                    args.length > 1 ? args[1] : "N/A",
                    args.length > 2 ? args[2] : "N/A"
            );
        }
    }

    /**
     * Логирование изменения статуса заказа.
     */
    @AfterReturning(pointcut = "execution(* kz.bars.order_service.application.services.OrderService.updateOrderStatus(..))", returning = "result")
    public void logOrderStatusUpdate(JoinPoint joinPoint, Object result) {
        if (result instanceof Order order) {
            log.info("Order status was updated by user: {}. Order ID: {}, Old Status: {}, New Status: {}",
                    userService.getCurrentUsername(),
                    order.getOrderId(),
                    joinPoint.getArgs()[1], // Новый статус передаётся как второй аргумент метода
                    order.getStatus());
        }
    }

    /**
     * Логирование вызова метода generateStatusChangeEvent.
     */
    @Before("execution(* kz.bars.order_service.application.services.OrderService.generateStatusChangeEvent(..)) && args(orderId, oldStatus, newStatus)")
    public void logGenerateStatusChangeEvent(Long orderId, Order.Status oldStatus, Order.Status newStatus) {
        log.info("Order status change event: Order ID: {}, Old Status: {}, New Status: {}", orderId, oldStatus, newStatus);
    }
}
