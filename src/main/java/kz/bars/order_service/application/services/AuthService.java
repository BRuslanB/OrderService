package kz.bars.order_service.application.services;

import kz.bars.order_service.presentation.dto.SignupRequest;
import kz.bars.order_service.domain.models.Role;
import kz.bars.order_service.domain.models.User;
import kz.bars.order_service.domain.repositories.RoleRepository;
import kz.bars.order_service.domain.repositories.UserRepository;
import kz.bars.order_service.infrastructure.security.CustomUserDetailsService;
import kz.bars.order_service.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;  // Кодировщик паролей
    private final JwtTokenProvider jwtTokenProvider;    // Провайдер JWT токенов
    private final CustomUserDetailsService customUserDetailsService; // Сервис для загрузки данных пользователя
    private final RedisTemplate<String, String> tokenRedisTemplate; // Хранилище для недействительных токенов

    /**
     * Аутентифицирует пользователя и генерирует JWT токен.
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return сгенерированный JWT токен
     * @throws BadCredentialsException если логин или пароль неверные
     */
    public String authenticate(String username, String password) {
        // Загружаем данные пользователя через CustomUserDetailsService
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Проверяем пароль
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Создаём объект Authentication и устанавливаем его в SecurityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Если нужен доступ к сущности User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Генерация JWT токена на основе сущности User
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
