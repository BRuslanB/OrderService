package kz.bars.order_service.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AppConfig {
    // Здесь можно указать другие бины, если требуется
}
