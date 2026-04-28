ALTER TABLE account
    DROP FOREIGN KEY fk_account_customer;

ALTER TABLE loan_application
    DROP FOREIGN KEY fk_loan_application_customer;

ALTER TABLE loan
    DROP FOREIGN KEY fk_loan_customer;

DROP INDEX idx_account_customer_id ON account;
DROP INDEX idx_loan_application_customer_id ON loan_application;
DROP INDEX idx_loan_customer_id_status ON loan;

RENAME TABLE customer TO users;

ALTER TABLE users
    RENAME COLUMN customer_id TO user_id;

ALTER TABLE users
    ADD COLUMN user_role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' AFTER phone;

ALTER TABLE users
    ADD COLUMN user_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER user_role;

ALTER TABLE users
    DROP INDEX uk_customer_email,
    DROP INDEX uk_customer_phone,
    ADD CONSTRAINT uk_users_email UNIQUE (email),
    ADD CONSTRAINT uk_users_phone UNIQUE (phone),
    ADD CONSTRAINT ck_users_role CHECK (user_role IN ('CUSTOMER', 'ADMIN')),
    ADD CONSTRAINT ck_users_status CHECK (user_status IN ('ACTIVE', 'INACTIVE'));

ALTER TABLE account
    RENAME COLUMN customer_id TO user_id;

ALTER TABLE account
    ADD CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES users (user_id);

CREATE INDEX idx_account_user_id ON account (user_id);

ALTER TABLE loan_application
    RENAME COLUMN customer_id TO user_id;

ALTER TABLE loan_application
    ADD CONSTRAINT fk_loan_application_user FOREIGN KEY (user_id) REFERENCES users (user_id);

CREATE INDEX idx_loan_application_user_id ON loan_application (user_id);

ALTER TABLE loan
    RENAME COLUMN customer_id TO user_id;

ALTER TABLE loan
    ADD CONSTRAINT fk_loan_user FOREIGN KEY (user_id) REFERENCES users (user_id);

CREATE INDEX idx_loan_user_id_status ON loan (user_id, status);
