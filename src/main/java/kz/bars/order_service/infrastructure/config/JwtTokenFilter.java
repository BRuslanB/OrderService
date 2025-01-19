package kz.bars.order_service.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для проверки JWT-токенов в каждом запросе.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider; // Провайдер токенов
    private final CustomUserDetailsService userDetailsService; // Сервис для загрузки пользователей
    private final RedisTemplate<String, String> redisTemplate; // RedisTemplate для проверки токенов в черном списке

    /**
     * Фильтр для проверки JWT токенов и настройки контекста безопасности.
     *
     * @param request     текущий HTTP-запрос.
     * @param response    текущий HTTP-ответ.
     * @param filterChain цепочка фильтров безопасности.
     * @throws ServletException если возникает ошибка при обработке запроса.
     * @throws IOException      если возникает ошибка ввода-вывода.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Получаем токен из заголовка запроса
            String token = jwtTokenProvider.resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {

                // Проверяем, находится ли токен в Redis (черный список)
                if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
                    throw new ServletException("Token is invalid.");
                }

                // Получаем имя пользователя из токена
                String username = jwtTokenProvider.getUsername(token);

                // Загружаем данные пользователя
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Создаем объект аутентификации
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // Устанавливаем аутентификацию в контекст безопасности
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            // Логируем исключения для диагностики
            log.error("Error during token validation", e);
            throw e;
        }

        // Передаем запрос следующему фильтру в цепочке
        filterChain.doFilter(request, response);
    }
}
