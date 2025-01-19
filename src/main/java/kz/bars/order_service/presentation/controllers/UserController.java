package kz.bars.order_service.presentation.controllers;

import kz.bars.order_service.application.dto.UserDto;
import kz.bars.order_service.application.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Возвращает информацию о текущем пользователе на основе Principal.
     *
     * @param principal объект Principal, содержащий информацию о текущем пользователе
     * @return ResponseEntity с информацией о пользователе
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(userService.getUserByUsername(principal.getName()));
    }
}
