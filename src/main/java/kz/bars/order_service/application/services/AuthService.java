// src/main/java/kz/bars/order_service/application/services/AuthService.java
package kz.bars.order_service.application.services;

import kz.bars.order_service.application.dto.SignupRequest;
import kz.bars.order_service.domain.models.Role;
import kz.bars.order_service.domain.models.User;
import kz.bars.order_service.domain.repositories.RoleRepository;
import kz.bars.order_service.domain.repositories.UserRepository;
import kz.bars.order_service.infrastructure.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> tokenRedisTemplate; // Хранилище для недействительных токенов

    /**
     * Аутентифицирует пользователя и генерирует JWT токен.
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return сгенерированный JWT токен
     * @throws UsernameNotFoundException если пользователь с указанным именем не найден
     */
    public String authenticate(String username, String password) {
        // Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // Получение пользователя из репозитория
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Генерация JWT токена
        return jwtTokenProvider.generateToken(user);
    }

    /**
     * Регистрирует нового пользователя с базовой ролью USER.
     *
     * @param request объект, содержащий данные для регистрации (имя пользователя, пароль, email)
     * @throws IllegalArgumentException если имя пользователя уже занято или роль USER не найдена
     */
    public void registerUser(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already taken.");
        }

        // Проверяем, существует ли роль USER
        Role userRole = roleRepository.findByName(Role.RoleName.USER)
                .orElseThrow(() -> new IllegalArgumentException("Default role USER not found."));

        // Создаём нового пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setEnabled(true);
        user.setRoles(Set.of(userRole));

        userRepository.save(user);
    }

    /**
     * Удаляет JWT токен, добавляя его в список недействительных (черный список).
     *
     * @param token JWT токен, который необходимо сделать недействительным
     * @throws IllegalArgumentException если токен недействителен
     */
    public void logout(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            long expiration = jwtTokenProvider.getExpiration(token);
            tokenRedisTemplate.opsForValue().set("tokens::" + token, "invalid", expiration, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalArgumentException("Invalid token.");
        }
    }
}
