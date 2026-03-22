-- ============================================================
-- V2__seed_data.sql (H2-compatible)
-- NOTE: For H2 local/test profiles, default users are created
-- programmatically by DataInitializer.java (using BCrypt).
-- This file inserts only sample device data.
-- ============================================================

INSERT INTO devices (id, serial_number, model, description, total_cost, daily_rate, grace_period_days, status)
VALUES
    (REPLACE(RANDOM_UUID()::VARCHAR, '-', ''), 'SK-PRO-200-001', 'SunKing Pro 200',
     '200W solar home system, 4 LED bulbs, USB charging, radio',
     15000.00, 100.00, 3, 'INACTIVE'),
    (REPLACE(RANDOM_UUID()::VARCHAR, '-', ''), 'SK-HOME-50-001', 'SunKing Home 50',
     '50W entry-level solar system, 3 LED bulbs', 8000.00, 60.00, 3, 'INACTIVE'),
    (REPLACE(RANDOM_UUID()::VARCHAR, '-', ''), 'SK-EAZY-5-001', 'SunKing Eazy 5+',
     'Portable solar lantern with USB charging', 3000.00, 25.00, 5, 'INACTIVE');
