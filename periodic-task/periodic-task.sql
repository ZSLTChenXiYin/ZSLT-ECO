-- 选择 periodic-task 数据库
USE `periodic-task`;

-- 创建 messages 表
CREATE TABLE messages (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    created_at DATETIME(3) NULL,
    updated_at DATETIME(3) NULL,
    deleted_at DATETIME(3) NULL,
    sender VARCHAR(255) NOT NULL,
    message_type ENUM('private', 'public') NOT NULL,
    content JSON NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_messages_deleted_at (deleted_at)
) ENGINE=InnoDB;

-- 创建 message_consumers 表
CREATE TABLE message_consumers (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    created_at DATETIME(3) NULL,
    updated_at DATETIME(3) NULL,
    deleted_at DATETIME(3) NULL,
    message_id BIGINT UNSIGNED NOT NULL,
    consumer_service VARCHAR(255) NOT NULL,
    status ENUM('success', 'failure', 'progress') NOT NULL DEFAULT 'progress',
    consumed_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_message_consumers_deleted_at (deleted_at),
    INDEX idx_message_consumers_message_id (message_id)
) ENGINE=InnoDB;

-- 创建 services 表
CREATE TABLE services (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    created_at DATETIME(3) NULL,
    updated_at DATETIME(3) NULL,
    deleted_at DATETIME(3) NULL,
    name VARCHAR(255) NOT NULL,
    secret CHAR(32) NOT NULL,
    scheme VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_name_secret (name, secret),
    INDEX idx_services_deleted_at (deleted_at)
) ENGINE=InnoDB;