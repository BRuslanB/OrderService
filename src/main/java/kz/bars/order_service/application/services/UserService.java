package kz.bars.order_service.application.services;

import kz.bars.order_service.application.dto.UserDto;
import kz.bars.order_service.domain.models.User;
import kz.bars.order_service.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Получает данные пользователя по его имени.
     *
     * @param username имя пользователя, для которого требуется получить данные
     * @return объект {@link UserDto}, содержащий имя пользователя и его роли
     * @throws UsernameNotFoundException если пользователь с указанным именем не найден
     */
    public UserDto getUserByUsername(String username) {
        // Поиск пользователя в репозитории по имени
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Преобразование Set<Role> в List<Role> для дальнейшего использования
        return new UserDto(
                user.getUsername(),                  // Устанавливаем имя пользователя
                new ArrayList<>(user.getRoles())     // Приведение ролей пользователя к List<Role>
        );
    }

    /**
     * Получение имени текущего пользователя из SecurityContext.
     *
     * @return имя пользователя или null, если пользователь не аутентифицирован
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return null;
    }
}
