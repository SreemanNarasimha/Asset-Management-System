CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    base_id BIGINT,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (base_id) REFERENCES bases(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE equipment_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(100),
    unit VARCHAR(50),
    description TEXT,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE personnel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_number VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    rank_name VARCHAR(100),
    base_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (base_id) REFERENCES bases(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inventory_opening_balances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_id BIGINT NOT NULL,
    equipment_type_id BIGINT NOT NULL,
    effective_date DATE NOT NULL,
    quantity BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (base_id, equipment_type_id),
    CHECK (quantity >= 0),
    FOREIGN KEY (base_id) REFERENCES bases(id),
    FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purchases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_id BIGINT NOT NULL,
    equipment_type_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    purchase_date DATE NOT NULL,
    supplier VARCHAR(255),
    reference_number VARCHAR(100) UNIQUE,
    notes TEXT,
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHECK (quantity > 0),
    FOREIGN KEY (base_id) REFERENCES bases(id),
    FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_purchases_base_equip_date ON purchases (base_id, equipment_type_id, purchase_date);

CREATE TABLE transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_base_id BIGINT NOT NULL,
    destination_base_id BIGINT NOT NULL,
    equipment_type_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    transfer_date DATE NOT NULL,
    reference_number VARCHAR(100),
    notes TEXT,
    created_by BIGINT NOT NULL,
    approved_by BIGINT,
    received_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (quantity > 0),
    CHECK (source_base_id <> destination_base_id),
    FOREIGN KEY (source_base_id) REFERENCES bases(id),
    FOREIGN KEY (destination_base_id) REFERENCES bases(id),
    FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    FOREIGN KEY (received_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_transfers_source_equip_date ON transfers (source_base_id, equipment_type_id, transfer_date);
CREATE INDEX idx_transfers_dest_equip_date ON transfers (destination_base_id, equipment_type_id, transfer_date);

CREATE TABLE assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_id BIGINT NOT NULL,
    personnel_id BIGINT NOT NULL,
    equipment_type_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    assignment_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    returned_at DATETIME,
    returned_by BIGINT,
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (quantity > 0),
    FOREIGN KEY (base_id) REFERENCES bases(id),
    FOREIGN KEY (personnel_id) REFERENCES personnel(id),
    FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id),
    FOREIGN KEY (returned_by) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_assignments_base_equip_status ON assignments (base_id, equipment_type_id, status);

CREATE TABLE expenditures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_id BIGINT NOT NULL,
    equipment_type_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    expenditure_date DATE NOT NULL,
    category VARCHAR(100) NOT NULL,
    reference_number VARCHAR(100),
    notes TEXT,
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHECK (quantity > 0),
    FOREIGN KEY (base_id) REFERENCES bases(id),
    FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_expenditures_base_equip_date ON expenditures (base_id, equipment_type_id, expenditure_date);

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    base_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    result VARCHAR(50) NOT NULL,
    ip_address VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (base_id) REFERENCES bases(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_base_id ON audit_logs (base_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
