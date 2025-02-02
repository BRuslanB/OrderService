-- schema.sql

-- Отключаем проверку внешних ключей, чтобы избежать ошибок
SET REFERENTIAL_INTEGRITY FALSE;

-- Удаляем все записи из связанных таблиц
DELETE FROM user_roles;
DELETE FROM users;
DELETE FROM roles;

-- Сбрасываем автоинкрементные ID
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE roles ALTER COLUMN id RESTART WITH 1;

-- Включаем проверку внешних ключей обратно
SET REFERENTIAL_INTEGRITY TRUE;

-- Вставляем роли
INSERT INTO roles (id, name) VALUES (1, 'USER'); -- Роль обычного пользователя
INSERT INTO roles (id, name) VALUES (2, 'ADMIN'); -- Роль администратора

-- Вставляем пользователей
INSERT INTO users (id, username, password, email, enabled) -- Пароль password
VALUES (1, 'admin', '$2a$12$phy5GI5ySQ0WqGy.nyzRMOVp0UcIgRmHtH28fctf0HSo6G1tHwina', 'admin@example.com', TRUE);
INSERT INTO users (id, username, password, email, enabled) -- Пароль password
VALUES (2, 'testuser', '$2a$12$CiflwxQn36Z5zQsiQFqAuuj.hBRd.7H5/FyHc2hLl3WYVrcpMu0rK', 'testuser@example.com', TRUE);

-- Связываем пользователя с ролью
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2); -- Администратор имеет роль ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1); -- Тестовый пользователь имеет роль USER
