CREATE TABLE settlements (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id   UUID REFERENCES groups(id) ON DELETE CASCADE,
    payer_id   UUID REFERENCES users(id),
    payee_id   UUID REFERENCES users(id),
    amount     DECIMAL(10, 2) NOT NULL,
    status     VARCHAR(20) DEFAULT 'COMPLETED',
    settled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_settlements_group ON settlements(group_id);
CREATE INDEX idx_settlements_payer ON settlements(payer_id);
CREATE INDEX idx_settlements_payee ON settlements(payee_id);
