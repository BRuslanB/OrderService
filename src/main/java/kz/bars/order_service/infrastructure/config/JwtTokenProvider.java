package kz.bars.order_service.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import kz.bars.order_service.domain.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final Key key;
    private final long validityInMilliseconds;

    // Конструктор по умолчанию
    public JwtTokenProvider() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Генерируем ключ по умолчанию
        this.validityInMilliseconds = 3600000;                  // Время жизни токена в миллисекундах по умолчанию
    }

    /**
     * Конструктор с внедрением значений из application.yml.
     *
     * @param secretKey  Секретный ключ для подписи JWT.
     * @param expiration Время жизни токена в миллисекундах.
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = expiration;
    }

    /**
     * Извлечение токена из заголовка Authorization.
     *
     * @param request HTTP-запрос
     * @return JWT токен или null, если токен отсутствует
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Убираем "Bearer "
        }
        return null;
    }

    /**
     * Генерация токена JWT на основе пользователя.
     *
     * @param user Объект пользователя
     * @return Сгенерированный JWT токен
     */
    public String generateToken(User user) {
        Claims claims = createClaims(user);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Создание Claims для токена.
     *
     * @param user Объект пользователя
     * @return Claims, включающие имя пользователя и роли
     */
    private Claims createClaims(User user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        return claims;
    }

    /**
     * Извлечение имени пользователя из токена.
     *
     * @param token JWT токен
     * @return Имя пользователя
     */
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Извлечение ролей из токена.
     *
     * @param token JWT токен
     * @return Список ролей
     */
    public List<String> getRoles(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        List<String> roles = (List<String>) claims.get("roles");
        return roles;
    }

    /**
     * Проверка валидности токена.
     *
     * @param token JWT токен
     * @return true, если токен валиден; false в противном случае
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Токен недействителен
            return false;
        }
    }

    /**
     * Извлекает время до истечения срока действия токена.
     *
     * @param token JWT токен.
     * @return оставшееся время в миллисекундах до истечения срока действия токена.
     */
    public long getExpiration(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
