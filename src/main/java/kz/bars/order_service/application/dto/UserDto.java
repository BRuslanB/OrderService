package kz.bars.order_service.application.dto;

import kz.bars.order_service.domain.models.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * Имя пользователя.
     */
    private String username;

    /**
     * Список ролей, назначенных пользователю.
     */
    private List<Role> roles;
}
