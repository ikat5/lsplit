CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(255)  UNIQUE NOT NULL,
    phone         VARCHAR(20),
    password_hash VARCHAR(255)  NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE groups (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_by  UUID REFERENCES users(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE group_members (
    group_id  UUID REFERENCES groups(id) ON DELETE CASCADE,
    user_id   UUID REFERENCES users(id)  ON DELETE CASCADE,
    role      VARCHAR(20) DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id)
);

CREATE INDEX idx_gm_group ON group_members(group_id);
CREATE INDEX idx_gm_user  ON group_members(user_id);
