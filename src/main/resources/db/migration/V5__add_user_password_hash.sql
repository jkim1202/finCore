ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(255) NULL AFTER birth_date;

UPDATE users
SET password_hash = '{noop}changeme'
WHERE password_hash IS NULL;

ALTER TABLE users
    MODIFY COLUMN password_hash VARCHAR(255) NOT NULL;
