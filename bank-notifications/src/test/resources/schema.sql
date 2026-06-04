CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    account_id VARCHAR(100) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    version BIGINT DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);
