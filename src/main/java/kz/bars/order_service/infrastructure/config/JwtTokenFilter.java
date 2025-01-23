package kz.bars.order_service.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> tokenRedisTemplate;

    /**
     * Фильтр для проверки JWT токенов и настройки контекста безопасности.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Получаем токен из заголовка запроса
            String token = jwtTokenProvider.resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {

                // Проверяем, находится ли токен в Redis (черный список)
                if (Boolean.TRUE.equals(tokenRedisTemplate.hasKey("tokens::" + token))) {
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
            throw new ServletException("Authentication failed", e);
        }

        // Передаем запрос следующему фильтру в цепочке
        filterChain.doFilter(request, response);
    }
}
