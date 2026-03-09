CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    group_id BIGINT NOT NULL,
    paid_by VARCHAR(255) NOT NULL,
    created_by VARCHAR(255),
    category VARCHAR(50),
    split_type VARCHAR(20) NOT NULL,
    receipt_url VARCHAR(500),
    date TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS expense_splits (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    percentage NUMERIC(5, 2),
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    expense_id BIGINT NOT NULL,
    CONSTRAINT fk_expense_splits_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE
);
