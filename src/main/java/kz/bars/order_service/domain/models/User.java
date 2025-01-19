package kz.bars.order_service.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Уникальное имя пользователя.
     * Не может быть null.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Пароль пользователя.
     * Не может быть null.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Электронная почта пользователя.
     * Должна быть уникальной и не null.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Признак того, активен ли пользователь.
     * Значение по умолчанию — true (активен).
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Роли пользователя. Связь с сущностью Role через таблицу "user_roles".
     * Загрузка ролей происходит сразу вместе с пользователем (FetchType.EAGER).
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_roles", // Название таблицы для связи пользователей и ролей
            joinColumns = @JoinColumn(name = "user_id"), // Колонка для пользователя
            inverseJoinColumns = @JoinColumn(name = "role_id") // Колонка для роли
    )
    private Set<Role> roles = new HashSet<>();
}
