package kz.bars.order_service.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * Токен JWT, выпущенный после успешной аутентификации.
     */
    private String token;
}
