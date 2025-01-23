package kz.bars.order_service.infrastructure.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class MyHealthIndicator implements HealthIndicator {

    final DataSource dataSource; // Источник данных для проверки состояния базы данных

    /**
     * Конструктор для передачи источника данных.
     *
     * @param dataSource Источник данных (например, подключение к базе данных).
     */
    public MyHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Проверяет состояние компонента (например, базы данных).
     *
     * @return Состояние компонента (вверх или вниз).
     */
    @Override
    public Health health() {
        int errorCode = check(); // Выполняем проверку
        if (errorCode != 0) {
            // Если код ошибки не равен 0, возвращаем состояние "вниз"
            return Health.down().withDetail("Error Code", errorCode).build();
        }
        // Если всё в порядке, возвращаем состояние "вверх"
        return Health.up().build();
    }

    /**
     * Выполняет проверку подключения к базе данных.
     *
     * @return 0, если подключение успешно; 1, если возникла ошибка.
     */
    private int check() {
        try {
            // Пробуем подключиться к базе данных и выполнить запрос
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT 1");
            statement.close();
            connection.close();
            return 0; // Успешное подключение
        } catch (SQLException e) {
            // Если возникает ошибка, возвращаем код ошибки
            return 1;
        }
    }
}
