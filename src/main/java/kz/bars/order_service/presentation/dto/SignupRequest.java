package kz.bars.order_service.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank // Поле не должно быть пустым или содержать только пробелы
    @Size(min = 3, max = 20) // Устанавливаем минимальную и максимальную длину строки
    private String username;

    @NotBlank // Поле не должно быть пустым
    @Size(min = 6, max = 40) // Устанавливаем минимальную и максимальную длину пароля
    private String password;

    @NotBlank // Поле не должно быть пустым
    @Email // Валидатор для проверки корректности формата email
    private String email;
}
