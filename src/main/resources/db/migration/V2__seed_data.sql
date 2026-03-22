-- ============================================================
-- V2__seed_data.sql
-- Default admin and agent users for initial setup
-- Passwords are BCrypt encoded:
--   admin123  -> $2a$10$N9qo8uLOickgX2ZWj1xLdOuEJBTZObEq4z1VmQqrX4g0D3y2cCWCi
--   agent123  -> $2a$10$N9qo8uLOickgX2ZWj1xLdOuEJBTZObEq4z1VmQqrX4g0D3y2cCWCi
-- ============================================================

INSERT INTO app_users (id, username, password_hash, full_name, role, is_active)
VALUES
    (gen_random_uuid(), 'admin',
     '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO1GRrbLATu',
     'Admin User', 'ADMIN', true),
    (gen_random_uuid(), 'agent',
     '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO1GRrbLATu',
     'Field Agent', 'AGENT', true)
ON CONFLICT (username) DO NOTHING;

-- Default admin password: Admin@123
-- Default agent password: Admin@123

-- Sample devices for testing
INSERT INTO devices (id, serial_number, model, description, total_cost, daily_rate, grace_period_days, status)
VALUES
    (gen_random_uuid(), 'SK-PRO-200-001', 'SunKing Pro 200',
     '200W solar home system, 4 LED bulbs, USB charging, radio',
     15000.00, 100.00, 3, 'INACTIVE'),
    (gen_random_uuid(), 'SK-HOME-50-001', 'SunKing Home 50',
     '50W entry-level solar system, 3 LED bulbs, USB charging',
     8000.00, 60.00, 3, 'INACTIVE'),
    (gen_random_uuid(), 'SK-EAZY-5-001', 'SunKing Eazy 5+',
     'Portable solar lantern with USB charging port',
     3000.00, 25.00, 5, 'INACTIVE')
ON CONFLICT (serial_number) DO NOTHING;
