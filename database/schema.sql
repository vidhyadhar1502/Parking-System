-- =============================================================================
-- SMART PARKING MANAGEMENT SYSTEM
-- Relational Database Schema (MySQL 8.x)
-- Engine: InnoDB | Character Set: utf8mb4 | Collation: utf8mb4_unicode_ci
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `smart_parking`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `smart_parking`;

-- Drop existing tables in reverse foreign-key dependency order
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `reservations`;
DROP TABLE IF EXISTS `parking_slots`;
DROP TABLE IF EXISTS `parking_locations`;
DROP TABLE IF EXISTS `users`;

-- =============================================================================
-- 1. USERS TABLE
-- Stores credentials, role hierarchy (CUSTOMER / ADMIN), and default vehicle info
-- =============================================================================
CREATE TABLE `users` (
    `user_id` INT PRIMARY KEY AUTO_INCREMENT,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(120) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `phone_number` VARCHAR(20) NOT NULL,
    `role` ENUM('CUSTOMER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    `vehicle_number` VARCHAR(30) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `uq_users_email` UNIQUE (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_users_email` ON `users` (`email`);

-- =============================================================================
-- 2. PARKING LOCATIONS TABLE
-- Multi-facility geo-coordinates, capacity limits, and hourly rate configurations
-- =============================================================================
CREATE TABLE `parking_locations` (
    `location_id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `latitude` DECIMAL(10, 8) NOT NULL,
    `longitude` DECIMAL(11, 8) NOT NULL,
    `total_capacity` INT NOT NULL,
    `hourly_rate` DECIMAL(8, 2) NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 3. PARKING SLOTS TABLE
-- Individual bays with floor level, slot classification, and real-time status
-- =============================================================================
CREATE TABLE `parking_slots` (
    `slot_id` INT PRIMARY KEY AUTO_INCREMENT,
    `location_id` INT NOT NULL,
    `slot_number` VARCHAR(20) NOT NULL,
    `floor_level` INT NOT NULL DEFAULT 1,
    `slot_type` ENUM('STANDARD', 'COMPACT', 'EV_CHARGING', 'HANDICAPPED') NOT NULL DEFAULT 'STANDARD',
    `status` ENUM('AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT `fk_slots_location` FOREIGN KEY (`location_id`) 
        REFERENCES `parking_locations` (`location_id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT `uq_location_slot` UNIQUE (`location_id`, `slot_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_slots_location_id` ON `parking_slots` (`location_id`);
CREATE INDEX `idx_slots_status` ON `parking_slots` (`status`);
CREATE INDEX `idx_slots_location_status` ON `parking_slots` (`location_id`, `status`);

-- =============================================================================
-- 4. RESERVATIONS TABLE
-- Pre-booking records, QR verification codes, check-in and check-out timestamps
-- =============================================================================
CREATE TABLE `reservations` (
    `reservation_id` INT PRIMARY KEY AUTO_INCREMENT,
    `reservation_code` VARCHAR(36) NOT NULL,
    `user_id` INT NOT NULL,
    `slot_id` INT NOT NULL,
    `reservation_time` DATETIME NOT NULL,
    `check_in_time` DATETIME NULL,
    `check_out_time` DATETIME NULL,
    `status` ENUM('CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    `qr_code_file_path` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `uq_reservation_code` UNIQUE (`reservation_code`),
    CONSTRAINT `fk_reservations_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`user_id`) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT `fk_reservations_slot` FOREIGN KEY (`slot_id`) 
        REFERENCES `parking_slots` (`slot_id`) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_reservations_code` ON `reservations` (`reservation_code`);
CREATE INDEX `idx_reservations_user_id` ON `reservations` (`user_id`);
CREATE INDEX `idx_reservations_slot_id` ON `reservations` (`slot_id`);
CREATE INDEX `idx_reservations_status` ON `reservations` (`status`);

-- =============================================================================
-- 5. PAYMENTS TABLE
-- Transaction audit, calculated parking duration, amounts, and settlement status
-- =============================================================================
CREATE TABLE `payments` (
    `payment_id` INT PRIMARY KEY AUTO_INCREMENT,
    `reservation_id` INT NOT NULL,
    `duration_hours` DECIMAL(5, 2) NOT NULL,
    `total_amount` DECIMAL(8, 2) NOT NULL,
    `payment_status` ENUM('PAID', 'PENDING', 'WAIVED') NOT NULL DEFAULT 'PENDING',
    `payment_method` ENUM('CASH', 'CARD', 'UPI_SIMULATED') NOT NULL DEFAULT 'CARD',
    `paid_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `uq_payment_reservation` UNIQUE (`reservation_id`),
    CONSTRAINT `fk_payments_reservation` FOREIGN KEY (`reservation_id`) 
        REFERENCES `reservations` (`reservation_id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_payments_reservation_id` ON `payments` (`reservation_id`);
CREATE INDEX `idx_payments_status` ON `payments` (`payment_status`);
