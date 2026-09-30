DELIMITER $$

CREATE PROCEDURE safe_migration_v4()
BEGIN
    -- Create user_sessions if not exists
    CREATE TABLE IF NOT EXISTS user_sessions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL,
        login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        logout_time TIMESTAMP NULL,
        status ENUM('ACTIVE','LOGGED_OUT','EXPIRED') NOT NULL DEFAULT 'ACTIVE',
        ip_address VARCHAR(45) NULL,
        FOREIGN KEY (user_id) REFERENCES users(id)
    );

    -- Add session_id to audit_logs
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = DATABASE() AND table_name = 'audit_logs' AND column_name = 'session_id'
    ) THEN
        ALTER TABLE audit_logs ADD COLUMN session_id BIGINT NULL;
    END IF;

    -- Add fk_audit_session
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.key_column_usage 
        WHERE table_schema = DATABASE() AND table_name = 'audit_logs' AND constraint_name = 'fk_audit_session'
    ) THEN
        ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_session FOREIGN KEY (session_id) REFERENCES user_sessions(id);
    END IF;
END $$

DELIMITER ;

CALL safe_migration_v4();
DROP PROCEDURE safe_migration_v4;
