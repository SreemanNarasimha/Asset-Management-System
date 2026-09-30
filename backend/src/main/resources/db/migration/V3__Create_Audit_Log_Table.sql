CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    details TEXT,
    timestamp DATETIME NOT NULL
);
