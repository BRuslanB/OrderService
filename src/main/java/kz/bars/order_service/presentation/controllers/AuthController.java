package kz.bars.order_service.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kz.bars.order_service.presentation.dto.LoginRequest;
import kz.bars.order_service.presentation.dto.LoginResponse;
import kz.bars.order_service.presentation.dto.SignupRequest;
import kz.bars.order_service.application.services.AuthService;
import kz.bars.order_service.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Authentication API", description = "API for authentication")
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Выполняет аутентификацию пользователя и выдаёт JWT-токен.
     *
     * @param request запрос, содержащий имя пользователя и пароль
     * @return ResponseEntity с токеном авторизации
     */
    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        String token = authService.authenticate(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    /**
     * Регистрация нового пользователя.
     *
     * @param request объект SignupRequest, содержащий данные для регистрации (имя пользователя и пароль)
     * @return ResponseEntity с сообщением об успешной регистрации или ошибкой
     */
    @PostMapping("/signup")
    @Operation(summary = "Sign Up")
    public ResponseEntity<String> signup(@RequestBody @Valid SignupRequest request) {
        try {
            authService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Выполняет выход пользователя из системы, делая его токен недействительным.
     *
     * @param request объект HttpServletRequest, содержащий заголовок Authorization с JWT токеном.
     * @return ResponseEntity с сообщением об успешном выходе.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        authService.logout(token);
        return ResponseEntity.ok("User logged out successfully.");
    }
}
