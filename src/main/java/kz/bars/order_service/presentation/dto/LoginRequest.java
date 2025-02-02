package kz.bars.order_service.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Логин пользователя. Поле не может быть пустым или содержать только пробелы.
     * Используется аннотация @NotBlank
     */
    @NotBlank
    private String username;

    /**
     * Пароль пользователя. Поле не может быть пустым или содержать только пробелы.
     * Используется аннотация @NotBlank
     */
    @NotBlank
    private String password;
}
