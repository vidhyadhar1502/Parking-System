-- =============================================================================
-- SMART PARKING MANAGEMENT SYSTEM
-- Realistic Seed Data (College Campus / Tech Hub - Chennai, Tamil Nadu)
-- Passwords are encrypted with jBCrypt ($2a$10$...)
-- =============================================================================

USE `smart_parking`;

-- Clean existing data
DELETE FROM `payments`;
DELETE FROM `reservations`;
DELETE FROM `parking_slots`;
DELETE FROM `parking_locations`;
DELETE FROM `users`;

-- Reset auto increments
ALTER TABLE `users` AUTO_INCREMENT = 1;
ALTER TABLE `parking_locations` AUTO_INCREMENT = 1;
ALTER TABLE `parking_slots` AUTO_INCREMENT = 1;
ALTER TABLE `reservations` AUTO_INCREMENT = 1;
ALTER TABLE `payments` AUTO_INCREMENT = 1;

-- =============================================================================
-- 1. USERS SEED DATA
-- Default Credentials:
-- admin@campus.edu         / Admin@123    (Role: ADMIN)
-- arun.kumar@gmail.com     / Customer@123 (Role: CUSTOMER)
-- priya.sundaram@gmail.com / Student@123  (Role: CUSTOMER)
-- karthik.rajan@annauniv.edu/ Staff@123    (Role: CUSTOMER)
-- =============================================================================
INSERT INTO `users` (`user_id`, `full_name`, `email`, `password_hash`, `phone_number`, `role`, `vehicle_number`, `created_at`) VALUES
(1, 'Dr. Sarah Jenkins', 'admin@campus.edu', '$2a$10$2YDLk6yCPn0JTdje0BthDe/ScvhOBZu8xlstWGJlnp7IVyh8V9jry', '+91 98401 23456', 'ADMIN', 'TN 07 CE 0001', '2026-08-01 08:00:00'),
(2, 'Arun Kumar', 'arun.kumar@gmail.com', '$2a$10$FvJDKbsnT5mlsDZZwZmpTe4KAKVKFzQJG74eAWGXuEOL5nGcVWhvK', '+91 94440 98765', 'CUSTOMER', 'TN 09 BX 4512', '2026-08-15 09:30:00'),
(3, 'Priya Sundaram', 'priya.sundaram@gmail.com', '$2a$10$q92MnZaLcMtB5urvFEWY3.m2GkeXFxzehpX6s7pNJfuPqzBtcxoPu', '+91 98840 55432', 'CUSTOMER', 'TN 07 CA 8924', '2026-08-20 11:15:00'),
(4, 'Prof. Karthik Rajan', 'karthik.rajan@annauniv.edu', '$2a$10$m/AWb1ztlqPniaXnTYRtleGiAJlXfkFleFp97YXoe6/N4VcivlbHO', '+91 97910 11223', 'CUSTOMER', 'TN 22 DQ 3110', '2026-08-25 14:00:00');

-- =============================================================================
-- 2. PARKING LOCATIONS SEED DATA
-- Key institutional & academic facilities in Guindy / Taramani, Chennai
-- =============================================================================
INSERT INTO `parking_locations` (`location_id`, `name`, `address`, `latitude`, `longitude`, `total_capacity`, `hourly_rate`, `is_active`, `created_at`) VALUES
(1, 'CEG Main Academic Deck', 'College of Engineering Guindy, Sardar Patel Road, Guindy, Chennai, Tamil Nadu 600025', 13.01090000, 80.23550000, 16, 30.00, TRUE, '2026-08-01 00:00:00'),
(2, 'IIT Madras Research Park Hub', 'Kanagam Road, Taramani, Chennai, Tamil Nadu 600113', 12.99020000, 80.24250000, 12, 40.00, TRUE, '2026-08-01 00:00:00'),
(3, 'Anna Centenary Library Complex', 'Gandhi Mandapam Road, Surya Nagar, Kotturpuram, Chennai, Tamil Nadu 600085', 13.01350000, 80.23720000, 10, 25.00, TRUE, '2026-08-01 00:00:00');

-- =============================================================================
-- 3. PARKING SLOTS SEED DATA
-- 28 Total slots across 2 floors and 3 facilities
-- Slot Types: STANDARD, COMPACT, EV_CHARGING, HANDICAPPED
-- Statuses: AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE
-- =============================================================================
INSERT INTO `parking_slots` (`slot_id`, `location_id`, `slot_number`, `floor_level`, `slot_type`, `status`) VALUES
-- Location 1 (CEG Main Academic Deck) - Floor 1
(1,  1, 'A-101', 1, 'HANDICAPPED', 'AVAILABLE'),
(2,  1, 'A-102', 1, 'EV_CHARGING', 'OCCUPIED'),
(3,  1, 'A-103', 1, 'EV_CHARGING', 'AVAILABLE'),
(4,  1, 'A-104', 1, 'STANDARD',    'RESERVED'),
(5,  1, 'A-105', 1, 'STANDARD',    'AVAILABLE'),
(6,  1, 'A-106', 1, 'STANDARD',    'AVAILABLE'),
(7,  1, 'A-107', 1, 'COMPACT',     'OCCUPIED'),
(8,  1, 'A-108', 1, 'COMPACT',     'MAINTENANCE'),

-- Location 1 (CEG Main Academic Deck) - Floor 2
(9,  1, 'A-201', 2, 'STANDARD',    'AVAILABLE'),
(10, 1, 'A-202', 2, 'STANDARD',    'AVAILABLE'),
(11, 1, 'A-203', 2, 'STANDARD',    'AVAILABLE'),
(12, 1, 'A-204', 2, 'STANDARD',    'AVAILABLE'),
(13, 1, 'A-205', 2, 'COMPACT',     'AVAILABLE'),
(14, 1, 'A-206', 2, 'COMPACT',     'AVAILABLE'),
(15, 1, 'A-207', 2, 'EV_CHARGING', 'AVAILABLE'),
(16, 1, 'A-208', 2, 'HANDICAPPED', 'AVAILABLE'),

-- Location 2 (IIT Madras Research Park Hub) - Floor 1
(17, 2, 'B-101', 1, 'HANDICAPPED', 'AVAILABLE'),
(18, 2, 'B-102', 1, 'EV_CHARGING', 'OCCUPIED'),
(19, 2, 'B-103', 1, 'EV_CHARGING', 'AVAILABLE'),
(20, 2, 'B-104', 1, 'STANDARD',    'AVAILABLE'),
(21, 2, 'B-105', 1, 'STANDARD',    'RESERVED'),
(22, 2, 'B-106', 1, 'COMPACT',     'AVAILABLE'),

-- Location 2 (IIT Madras Research Park Hub) - Floor 2
(23, 2, 'B-201', 2, 'STANDARD',    'AVAILABLE'),
(24, 2, 'B-202', 2, 'STANDARD',    'AVAILABLE'),
(25, 2, 'B-203', 2, 'COMPACT',     'AVAILABLE'),
(26, 2, 'B-204', 2, 'COMPACT',     'AVAILABLE'),
(27, 2, 'B-205', 2, 'EV_CHARGING', 'AVAILABLE'),
(28, 2, 'B-206', 2, 'STANDARD',    'AVAILABLE');

-- =============================================================================
-- 4. RESERVATIONS SEED DATA
-- Active & completed bookings linked to slots and users
-- =============================================================================
INSERT INTO `reservations` (`reservation_id`, `reservation_code`, `user_id`, `slot_id`, `reservation_time`, `check_in_time`, `check_out_time`, `status`, `qr_code_file_path`, `created_at`) VALUES
-- Active Check-in on A-102 (Priya Sundaram)
(1, 'RES-2026-98124', 3, 2, '2026-09-06 08:30:00', '2026-09-06 08:45:00', NULL, 'CHECKED_IN', 'qr_codes/RES-2026-98124.png', '2026-09-06 08:30:00'),

-- Confirmed Upcoming Reservation on A-104 (Arun Kumar)
(2, 'RES-2026-44319', 2, 4, '2026-09-06 14:00:00', NULL, NULL, 'CONFIRMED', 'qr_codes/RES-2026-44319.png', '2026-09-06 10:15:00'),

-- Active Check-in on A-107 (Prof. Karthik Rajan)
(3, 'RES-2026-77821', 4, 7, '2026-09-06 09:00:00', '2026-09-06 09:10:00', NULL, 'CHECKED_IN', 'qr_codes/RES-2026-77821.png', '2026-09-06 09:00:00'),

-- Confirmed Upcoming Reservation on B-105 (Prof. Karthik Rajan)
(4, 'RES-2026-11902', 4, 21, '2026-09-06 16:30:00', NULL, NULL, 'CONFIRMED', 'qr_codes/RES-2026-11902.png', '2026-09-06 11:45:00'),

-- Completed Historical Reservation on A-101 (Arun Kumar)
(5, 'RES-2026-30219', 2, 1, '2026-09-05 10:00:00', '2026-09-05 10:05:00', '2026-09-05 13:05:00', 'COMPLETED', 'qr_codes/RES-2026-30219.png', '2026-09-05 09:50:00');

-- =============================================================================
-- 5. PAYMENTS SEED DATA
-- Corresponds to reservations above
-- =============================================================================
INSERT INTO `payments` (`payment_id`, `reservation_id`, `duration_hours`, `total_amount`, `payment_status`, `payment_method`, `paid_at`) VALUES
-- Pending ongoing session for RES-2026-98124 (A-102: Rate Rs. 30/hr for 2.0 hrs)
(1, 1, 2.00, 60.00, 'PENDING', 'UPI_SIMULATED', '2026-09-06 08:30:00'),

-- Pending upcoming booking for RES-2026-44319 (A-104: Rate Rs. 30/hr for 3.0 hrs)
(2, 2, 3.00, 90.00, 'PENDING', 'CARD', '2026-09-06 10:15:00'),

-- Pending ongoing session for RES-2026-77821 (A-107: Rate Rs. 30/hr for 4.0 hrs)
(3, 3, 4.00, 120.00, 'PENDING', 'CASH', '2026-09-06 09:00:00'),

-- Settled completed historical session for RES-2026-30219 (A-101: Rate Rs. 30/hr for 3.0 hrs)
(4, 5, 3.00, 90.00, 'PAID', 'UPI_SIMULATED', '2026-09-05 13:05:00');
