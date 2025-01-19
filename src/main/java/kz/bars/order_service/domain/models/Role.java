package kz.bars.order_service.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /**
     * Уникальный идентификатор роли.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название роли. Хранится в виде строки (например, "USER" или "ADMIN").
     * Значение должно быть уникальным и не null.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private RoleName name;

    /**
     * Перечисление, содержащее возможные роли.
     */
    public enum RoleName {
        USER, ADMIN
    }

    // Конструктор для установки роли
    public Role(RoleName name) {
        this.name = name;
    }
}
