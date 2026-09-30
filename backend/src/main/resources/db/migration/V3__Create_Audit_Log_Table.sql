-- V3 replaced to avoid conflict with V1 which already creates audit_logs
CREATE TABLE IF NOT EXISTS audit_logs_v3_dummy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY
);
