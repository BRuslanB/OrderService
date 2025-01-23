package kz.bars.order_service.infrastructure.info;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class MyInfoContributor implements InfoContributor {

    final OpenAPI openAPI; // Объект OpenAPI для извлечения информации

    /**
     * Конструктор для передачи объекта OpenAPI.
     *
     * @param openAPI Объект OpenAPI, содержащий метаинформацию.
     */
    @Autowired
    public MyInfoContributor(OpenAPI openAPI) {
        this.openAPI = openAPI;
    }

    /**
     * Добавляет метаинформацию об API в эндпоинт /info.
     *
     * @param builder Объект Builder для добавления данных.
     */
    @Override
    public void contribute(Builder builder) {
        Info info = openAPI.getInfo();
        builder.withDetail("title", info.getTitle()); // Название API
        builder.withDetail("description", info.getDescription()); // Описание API
        builder.withDetail("version", info.getVersion()); // Версия API
        builder.withDetail("author", info.getContact().getName()); // Автор API
        builder.withDetail("email", info.getContact().getEmail()); // Email автора
    }
}
