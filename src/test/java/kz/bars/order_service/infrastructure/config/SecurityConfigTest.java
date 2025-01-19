package kz.bars.order_service.infrastructure.config;

import kz.bars.order_service.domain.repositories.UserRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SecurityConfigTest {
    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        JwtTokenProvider mockProvider = Mockito.mock(JwtTokenProvider.class);

        // Настраиваем поведение мока
        Mockito.when(mockProvider.validateToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(mockProvider.getUsername(Mockito.anyString())).thenReturn("test-user");

        return mockProvider;
    }

    /**
     * Бин для CustomUserDetailsService, который использует автоматически созданный UserRepository.
     */
    @Bean
    public CustomUserDetailsService customUserDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }
}
