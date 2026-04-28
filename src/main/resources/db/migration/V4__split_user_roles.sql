CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT ck_user_roles_role CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

INSERT INTO user_roles (user_id, role)
SELECT user_id, user_role
FROM users;

ALTER TABLE users
    DROP CHECK ck_users_role;

ALTER TABLE users
    DROP COLUMN user_role;
