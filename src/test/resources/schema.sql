-- schema.sql

-- Создаем таблицу orders
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(36) PRIMARY KEY, -- UUID в виде строки
    customer_name VARCHAR(255) NOT NULL, -- Имя клиента
    total_price NUMERIC(10,2) NOT NULL, -- Общая стоимость заказа
    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL, -- Статус заказа
    deleted BOOLEAN DEFAULT FALSE NOT NULL -- Флаг для мягкого удаления
);

-- Создаем таблицу products
CREATE TABLE IF NOT EXISTS products (
    product_id VARCHAR(36) PRIMARY KEY, -- UUID в виде строки
    name VARCHAR(255) NOT NULL, -- Название продукта
    price NUMERIC(10,2) NOT NULL, -- Цена продукта
    quantity INT NOT NULL, -- Количество продукта
    order_id VARCHAR(36), -- Внешний ключ как строка
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- Создаем таблицу roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Уникальный идентификатор роли
    name VARCHAR(20) NOT NULL UNIQUE      -- Название роли (например, USER, ADMIN)
);

-- Создаем таблицу users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,   -- Уникальный идентификатор пользователя
    username VARCHAR(50) NOT NULL UNIQUE,   -- Уникальное имя пользователя
    password VARCHAR(255) NOT NULL,         -- Зашифрованный пароль пользователя
    email VARCHAR(100) NOT NULL UNIQUE,     -- Уникальная электронная почта пользователя
    enabled BOOLEAN NOT NULL DEFAULT TRUE   -- Признак активности пользователя (по умолчанию TRUE)
);

-- Создаем таблицу user_roles для связи пользователей и их ролей
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL, -- Идентификатор пользователя
    role_id BIGINT NOT NULL, -- Идентификатор роли
    PRIMARY KEY (user_id, role_id), -- Составной первичный ключ
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE, -- Связь с таблицей users
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE  -- Связь с таблицей roles
);
