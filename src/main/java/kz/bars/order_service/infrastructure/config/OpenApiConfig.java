package kz.bars.order_service.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class OpenApiConfig {

    /**
     * Настройка OpenAPI для метаинформации об API.
     *
     * @return Объект OpenAPI с метаинформацией.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API") // Название API
                        .description("Order Service Information") // Описание API
                        .version("1.4.0") // Версия API
                        .contact(new Contact()
                                .name("BRuslanB") // Имя автора
                                .email("kz.bars.prod@gmail.com") // Email автора
                        )
                );
    }
}
