package kz.bars.order_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test") // Используем тестовый профиль для тестирования
@SpringBootTest // Загружаем контекст Spring
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
