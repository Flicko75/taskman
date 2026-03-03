CREATE TABLE team (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'MEMBER')),
    team_id BIGINT,
    deleted BOOLEAN NOT NULL,
    token_version INTEGER NOT NULL,
    CONSTRAINT fk_users_team FOREIGN KEY (team_id) REFERENCES team(id)
);

CREATE INDEX idx_user_deleted ON users(deleted);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_team_deleted ON users(team_id, deleted);

CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(255) NOT NULL CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    priority VARCHAR(255) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    created_at TIMESTAMP,
    due_date TIMESTAMP,
    user_id BIGINT,
    team_id BIGINT NOT NULL,
    CONSTRAINT fk_task_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_task_team FOREIGN KEY (team_id) REFERENCES team(id)
);

CREATE TABLE comment (
    id BIGSERIAL PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES task(id),
    CONSTRAINT fk_comment_users FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO users (name, email, password, role, team_id, deleted, token_version) VALUES (
    'admin_user',
    'admin123@email.com',
    '$2a$10$ZLYoV8WHSqhXX1SowvdQOOrmB74f8zkjZ7EdPAodRgfqwEPGuaNQ6',
    'ADMIN',
    NULL,
    FALSE,
    0
);