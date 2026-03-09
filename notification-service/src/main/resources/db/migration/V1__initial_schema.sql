CREATE TABLE IF NOT EXISTS activities (
    id BIGSERIAL PRIMARY KEY,
    activity_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    user_name VARCHAR(255),
    group_id BIGINT NOT NULL,
    group_name VARCHAR(255),
    description VARCHAR(500) NOT NULL,
    metadata TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    target_user_id VARCHAR(255),
    target_user_name VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_group_id ON activities(group_id);
CREATE INDEX IF NOT EXISTS idx_user_id ON activities(user_id);
CREATE INDEX IF NOT EXISTS idx_timestamp ON activities(timestamp);
