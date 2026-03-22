-- ============================================================
-- V1__initial_schema.sql (H2-compatible)
-- Sun King PAYG Solar System - Initial Database Schema
-- H2 version: Uses autoincrement UUID via hibernate-managed IDs
-- ============================================================

CREATE TABLE IF NOT EXISTS customers (
    id              CHARACTER VARYING(36) PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone_number    VARCHAR(20)  NOT NULL UNIQUE,
    email           VARCHAR(150),
    national_id     VARCHAR(50),
    region          VARCHAR(100),
    address         VARCHAR(255),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_customer_phone       ON customers (phone_number);
CREATE UNIQUE INDEX IF NOT EXISTS uidx_customer_email       ON customers (email);
CREATE UNIQUE INDEX IF NOT EXISTS uidx_customer_national_id ON customers (national_id);
CREATE INDEX IF NOT EXISTS idx_customer_status              ON customers (status);

CREATE TABLE IF NOT EXISTS devices (
    id                  CHARACTER VARYING(36) PRIMARY KEY,
    serial_number       VARCHAR(100)  NOT NULL UNIQUE,
    model               VARCHAR(100)  NOT NULL,
    description         VARCHAR(500),
    total_cost          DECIMAL(15,2) NOT NULL,
    daily_rate          DECIMAL(15,2) NOT NULL,
    grace_period_days   INTEGER       NOT NULL DEFAULT 3,
    status              VARCHAR(20)   NOT NULL DEFAULT 'INACTIVE',
    locked_at           TIMESTAMP,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_device_serial ON devices (serial_number);
CREATE INDEX IF NOT EXISTS idx_device_status          ON devices (status);

CREATE TABLE IF NOT EXISTS device_assignments (
    id                          CHARACTER VARYING(36) PRIMARY KEY,
    customer_id                 CHARACTER VARYING(36) NOT NULL REFERENCES customers(id),
    device_id                   CHARACTER VARYING(36) NOT NULL REFERENCES devices(id),
    assigned_at                 TIMESTAMP     NOT NULL,
    unassigned_at               TIMESTAMP,
    is_active                   BOOLEAN       NOT NULL DEFAULT TRUE,
    total_cost_at_assignment    DECIMAL(15,2) NOT NULL,
    daily_rate_at_assignment    DECIMAL(15,2) NOT NULL,
    amount_paid                 DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    next_payment_due_date       DATE,
    last_payment_date           DATE,
    days_overdue                INTEGER       NOT NULL DEFAULT 0,
    is_fully_paid               BOOLEAN       NOT NULL DEFAULT FALSE,
    fully_paid_at               TIMESTAMP,
    created_at                  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_assignment_customer     ON device_assignments (customer_id);
CREATE INDEX IF NOT EXISTS idx_assignment_device       ON device_assignments (device_id);
CREATE INDEX IF NOT EXISTS idx_assignment_active       ON device_assignments (is_active);
CREATE INDEX IF NOT EXISTS idx_assignment_next_payment ON device_assignments (next_payment_due_date);

CREATE TABLE IF NOT EXISTS payments (
    id                      CHARACTER VARYING(36) PRIMARY KEY,
    customer_id             CHARACTER VARYING(36) NOT NULL REFERENCES customers(id),
    assignment_id           CHARACTER VARYING(36) NOT NULL REFERENCES device_assignments(id),
    amount                  DECIMAL(15,2) NOT NULL,
    status                  VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    payment_method          VARCHAR(30)   NOT NULL,
    transaction_reference   VARCHAR(100),
    external_transaction_id VARCHAR(150),
    mobile_number           VARCHAR(20),
    failure_reason          VARCHAR(500),
    retry_count             INTEGER       NOT NULL DEFAULT 0,
    processed_at            TIMESTAMP,
    created_at              TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_payment_txn_ref  ON payments (transaction_reference);
CREATE INDEX IF NOT EXISTS idx_payment_customer          ON payments (customer_id);
CREATE INDEX IF NOT EXISTS idx_payment_assignment        ON payments (assignment_id);
CREATE INDEX IF NOT EXISTS idx_payment_status            ON payments (status);
CREATE INDEX IF NOT EXISTS idx_payment_created_at        ON payments (created_at);

CREATE TABLE IF NOT EXISTS app_users (
    id              CHARACTER VARYING(36) PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_user_username ON app_users (username);
CREATE INDEX IF NOT EXISTS idx_user_role              ON app_users (role);
