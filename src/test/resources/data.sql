-- Вставляем роли
INSERT INTO roles (id, name) VALUES (1, 'USER'); -- Роль обычного пользователя
INSERT INTO roles (id, name) VALUES (2, 'ADMIN'); -- Роль администратора

-- Вставляем пользователей
INSERT INTO users (id, username, password, email, enabled)
VALUES (1, 'admin', 'password', 'admin@example.com', TRUE); -- Администратор
INSERT INTO users (id, username, password, email, enabled)
VALUES (2, 'testuser', 'password', 'testuser@example.com', TRUE); -- Тестовый пользователь

-- Связываем пользователя с ролью
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2); -- Администратор имеет роль ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1); -- Тестовый пользователь имеет роль USER
