CREATE TABLE events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id    UUID REFERENCES groups(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    event_date  DATE,
    created_by  UUID REFERENCES users(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_events_group ON events(group_id);

CREATE TABLE expense_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    UUID REFERENCES events(id) ON DELETE CASCADE,
    description VARCHAR(255)    NOT NULL,
    amount      DECIMAL(10, 2)  NOT NULL,
    split_type  VARCHAR(20)     DEFAULT 'EQUAL',
    paid_by     UUID REFERENCES users(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expenses_event ON expense_items(event_id);

CREATE TABLE expense_shares (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expense_item_id UUID REFERENCES expense_items(id) ON DELETE CASCADE,
    user_id         UUID REFERENCES users(id),
    share_amount    DECIMAL(10, 2) NOT NULL,
    is_settled      BOOLEAN DEFAULT FALSE,
    settled_at      TIMESTAMP
);

CREATE INDEX idx_shares_expense ON expense_shares(expense_item_id);
CREATE INDEX idx_shares_user    ON expense_shares(user_id);
