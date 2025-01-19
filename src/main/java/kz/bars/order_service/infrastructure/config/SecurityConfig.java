package kz.bars.order_service.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

     /**
     * Предоставляет AuthenticationManager для управления процессом аутентификации.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Создаёт объект PasswordEncoder для шифрования паролей.
     *
     * @return объект PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Настраивает цепочку фильтров безопасности.
     *
     * @param http объект HttpSecurity
     * @return объект SecurityFilterChain
     * @throws Exception если настройка не удалась
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Отключение CSRF
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Настройка сессий
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(HttpMethod.POST, "/auth/login").permitAll() // Разрешение для login
                                .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll() // Разрешение для signup
                                .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**")
                                    .permitAll() // Разрешение для документации
                                .anyRequest().authenticated() // Все остальные запросы требуют аутентификации
                )
                .addFilterBefore(new JwtTokenFilter(jwtTokenProvider, userDetailsService, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class); // Добавление JWT фильтра перед стандартным фильтром

        return http.build();
    }
}
