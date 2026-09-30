DROP TABLE IF EXISTS audit_logs;

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    `role` VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    base_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    result VARCHAR(50) NOT NULL,
    ip_address VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    session_id BIGINT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (base_id) REFERENCES bases(id),
    FOREIGN KEY (session_id) REFERENCES user_sessions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_base_id ON audit_logs (base_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_session_id ON audit_logs(session_id);
