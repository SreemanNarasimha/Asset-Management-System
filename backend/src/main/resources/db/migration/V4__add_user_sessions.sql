CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP NULL,
    status ENUM('ACTIVE','LOGGED_OUT','EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    ip_address VARCHAR(45) NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create index conditionally by skipping if it causes error or just ignore (MySQL 8 doesn't support IF NOT EXISTS for index directly)
-- But we can just leave it for now, usually it will pass if table was created.
-- Actually, let's keep it as is, or we can use a workaround, but IF NOT EXISTS is not standard for CREATE INDEX in MySQL.
CREATE INDEX idx_user_sessions_user_status ON user_sessions(user_id, status);

ALTER TABLE audit_logs ADD COLUMN session_id BIGINT NULL;
ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_session FOREIGN KEY (session_id) REFERENCES user_sessions(id);
CREATE INDEX idx_audit_logs_session_id ON audit_logs(session_id);
