package kz.bars.order_service.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CustomMetrics {

    private final Counter successfulOrdersCounter; // Счётчик успешных заказов
    private final Counter failedOrdersCounter; // Счётчик неудачных заказов

    /**
     * Инициализация кастомных метрик с использованием MeterRegistry.
     *
     * @param meterRegistry Реестр метрик для регистрации кастомных счётчиков.
     */
    public CustomMetrics(MeterRegistry meterRegistry) {
        successfulOrdersCounter = Counter.builder("custom.successful.orders")
                .description("Number of successful orders") // Описание метрики
                .register(meterRegistry);

        failedOrdersCounter = Counter.builder("custom.failed.orders")
                .description("Number of failed orders") // Описание метрики
                .register(meterRegistry);
    }

    /**
     * Увеличивает счётчик успешных заказов.
     */
    public void incrementSuccessfulOrders() {
        successfulOrdersCounter.increment();
    }

    /**
     * Увеличивает счётчик неудачных заказов.
     */
    public void incrementFailedOrders() {
        failedOrdersCounter.increment();
    }
}
