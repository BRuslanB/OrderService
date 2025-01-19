package kz.bars.order_service.infrastructure.config;

import kz.bars.order_service.domain.models.User;
import kz.bars.order_service.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает информацию о пользователе по имени пользователя (username).
     *
     * @param username имя пользователя
     * @return UserDetails, содержащий данные пользователя для аутентификации
     * @throws UsernameNotFoundException если пользователь с указанным именем не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Получение пользователя из репозитория
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Преобразование ролей пользователя в GrantedAuthority
        Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                // Используем Enum RoleName и добавляем префикс
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());

        // Возврат UserDetails с ролями
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
