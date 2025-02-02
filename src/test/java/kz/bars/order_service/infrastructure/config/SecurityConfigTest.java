package kz.bars.order_service.infrastructure.config;

import kz.bars.order_service.domain.repositories.UserRepository;
import kz.bars.order_service.infrastructure.security.CustomUserDetailsService;
import kz.bars.order_service.infrastructure.security.JwtTokenProvider;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test") // Указывает использование тестового профиля
@TestConfiguration // Аннотация указывает, что класс предоставляет тестовую конфигурацию
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class SecurityConfigTest {

    /**
     * Тестовый бин для JwtTokenProvider.
     *
     * Создаёт мок-объект для JwtTokenProvider с предопределённым поведением.
     * Это позволяет тестировать компоненты, зависящие от JWT, без необходимости
     * генерировать и проверять настоящие токены.
     *
     * @return Мок-объект JwtTokenProvider с настройкой поведения.
     */
    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        JwtTokenProvider mockProvider = Mockito.mock(JwtTokenProvider.class);

        // Настраиваем поведение мока: всегда возвращает true для валидации токена
        Mockito.when(mockProvider.validateToken(Mockito.anyString())).thenReturn(true);

        // Возвращает предопределённое имя пользователя при запросе через токен
        Mockito.when(mockProvider.getUsername(Mockito.anyString())).thenReturn("test-user");

        return mockProvider;
    }

    /**
     * Тестовый бин для CustomUserDetailsService.
     *
     * Этот бин подключает CustomUserDetailsService, который использует тестовый
     * UserRepository. Это позволяет проверять логику авторизации и аутентификации
     * в тестовом окружении.
     *
     * @param userRepository Реальный или мокированный UserRepository.
     * @return Экземпляр CustomUserDetailsService для тестов.
     */
    @Bean
    public CustomUserDetailsService customUserDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }
}
