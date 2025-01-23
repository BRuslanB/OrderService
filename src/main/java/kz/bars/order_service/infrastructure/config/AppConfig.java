package kz.bars.order_service.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true) // Включение поддержки AspectJ для обработки аспектов (AOP)
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class AppConfig {

    /**
     * Класс конфигурации приложения.
     * Этот класс служит для настройки дополнительных компонентов Spring.
     * Например:
     * - Включение поддержки аспектно-ориентированного программирования (AOP) через аннотацию @EnableAspectJAutoProxy.
     *   Это позволяет использовать аспекты (например, для логирования, метрик и транзакций) в приложении.
     * - Добавление пользовательских бинов, если потребуется расширение функционала.
     */
}
