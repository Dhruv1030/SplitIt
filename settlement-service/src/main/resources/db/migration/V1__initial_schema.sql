CREATE TABLE IF NOT EXISTS settlements (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    payer_id VARCHAR(255) NOT NULL,
    payee_id VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(255),
    transaction_id VARCHAR(255),
    notes VARCHAR(255),
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);
