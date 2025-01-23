-- init.sql

-- Создаем базу данных
DO
$$
BEGIN
   IF NOT EXISTS (
       SELECT FROM pg_database
       WHERE datname = 'orders_db'
   ) THEN
       CREATE DATABASE orders_db;
   END IF;
END
$$;

-- Создаем таблицу orders
CREATE TABLE IF NOT EXISTS orders (
    order_id UUID PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    total_price NUMERIC NOT NULL
);

-- Создаем таблицу products
CREATE TABLE IF NOT EXISTS products (
    product_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC NOT NULL,
    quantity INT NOT NULL,
    order_id UUID REFERENCES orders(order_id)
);

-- Создаем таблицу roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY, -- Уникальный идентификатор роли
    name VARCHAR(20) NOT NULL UNIQUE -- Название роли (например, USER, ADMIN)
);

-- Создаем таблицу users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY, -- Уникальный идентификатор пользователя
    username VARCHAR(50) NOT NULL UNIQUE, -- Уникальное имя пользователя
    password VARCHAR(255) NOT NULL, -- Зашифрованный пароль пользователя
    email VARCHAR(100) NOT NULL UNIQUE, -- Уникальная электронная почта пользователя
    enabled BOOLEAN NOT NULL DEFAULT TRUE -- Признак активности пользователя (по умолчанию TRUE)
);

-- Создаем таблицу user_roles для связи пользователей и их ролей
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL, -- Идентификатор пользователя
    role_id BIGINT NOT NULL, -- Идентификатор роли
    PRIMARY KEY (user_id, role_id), -- Составной первичный ключ
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE, -- Связь с таблицей users
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE -- Связь с таблицей roles
);

-- Вставляем роли
INSERT INTO roles (id, name) VALUES (1, 'USER');
INSERT INTO roles (id, name) VALUES (2, 'ADMIN');

-- Вставляем пользователей
INSERT INTO users (id, username, password, email, enabled) -- Пароль password
VALUES (1, 'admin', '$2a$12$phy5GI5ySQ0WqGy.nyzRMOVp0UcIgRmHtH28fctf0HSo6G1tHwina', 'admin@example.com', TRUE);
INSERT INTO users (id, username, password, email, enabled) -- Пароль password
VALUES (2, 'testuser', '$2a$12$CiflwxQn36Z5zQsiQFqAuuj.hBRd.7H5/FyHc2hLl3WYVrcpMu0rK', 'testuser@example.com', TRUE);

-- Связываем пользователя с ролью
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2); -- Администратор (admin) получает роль ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1); -- Пользователь (testuser) получает роль USER
