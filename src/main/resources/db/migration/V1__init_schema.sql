CREATE TABLE customer (
    customer_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    birth_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (customer_id),
    CONSTRAINT uk_customer_email UNIQUE (email),
    CONSTRAINT uk_customer_phone UNIQUE (phone)
);

CREATE TABLE account (
    account_id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    account_number VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    balance DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (account_id),
    CONSTRAINT uk_account_number UNIQUE (account_number),
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT ck_account_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    CONSTRAINT ck_account_balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE account_transaction (
    transaction_id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    balance_after DECIMAL(18, 2) NOT NULL,
    description VARCHAR(255) NULL,
    reference_type VARCHAR(30) NULL,
    reference_id BIGINT NULL,
    idempotency_key VARCHAR(100) NULL,
    transacted_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (transaction_id),
    CONSTRAINT fk_account_transaction_account FOREIGN KEY (account_id) REFERENCES account (account_id),
    CONSTRAINT ck_account_transaction_type CHECK (
        transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'LOAN_DISBURSEMENT', 'LOAN_REPAYMENT')
    ),
    CONSTRAINT ck_account_transaction_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_account_transaction_balance_non_negative CHECK (balance_after >= 0)
);

CREATE TABLE loan_product (
    loan_product_id BIGINT NOT NULL AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    min_amount DECIMAL(18, 2) NOT NULL,
    max_amount DECIMAL(18, 2) NOT NULL,
    base_interest_rate DECIMAL(5, 2) NOT NULL,
    overdue_interest_rate DECIMAL(5, 2) NOT NULL,
    min_term_months INT NOT NULL,
    max_term_months INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (loan_product_id),
    CONSTRAINT ck_loan_product_amount_range CHECK (min_amount > 0 AND max_amount >= min_amount),
    CONSTRAINT ck_loan_product_term_range CHECK (min_term_months > 0 AND max_term_months >= min_term_months),
    CONSTRAINT ck_loan_product_interest_rate CHECK (base_interest_rate >= 0 AND overdue_interest_rate >= 0)
);

CREATE TABLE loan_application (
    application_id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    loan_product_id BIGINT NOT NULL,
    disbursement_account_id BIGINT NOT NULL,
    requested_amount DECIMAL(18, 2) NOT NULL,
    requested_term_months INT NOT NULL,
    annual_income DECIMAL(18, 2) NOT NULL,
    existing_debt_amount DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL,
    submitted_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    reviewed_at DATETIME(6) NULL,
    PRIMARY KEY (application_id),
    CONSTRAINT fk_loan_application_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT fk_loan_application_loan_product FOREIGN KEY (loan_product_id) REFERENCES loan_product (loan_product_id),
    CONSTRAINT fk_loan_application_disbursement_account FOREIGN KEY (disbursement_account_id) REFERENCES account (account_id),
    CONSTRAINT ck_loan_application_status CHECK (
        status IN ('SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'CANCELLED')
    ),
    CONSTRAINT ck_loan_application_requested_amount CHECK (requested_amount > 0),
    CONSTRAINT ck_loan_application_requested_term CHECK (requested_term_months > 0),
    CONSTRAINT ck_loan_application_annual_income CHECK (annual_income >= 0),
    CONSTRAINT ck_loan_application_existing_debt CHECK (existing_debt_amount >= 0)
);

CREATE TABLE loan_review (
    review_id BIGINT NOT NULL AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    decision VARCHAR(20) NOT NULL,
    approved_limit DECIMAL(18, 2) NULL,
    approved_amount DECIMAL(18, 2) NULL,
    reject_reason VARCHAR(255) NULL,
    rule_snapshot TEXT NULL,
    reviewed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (review_id),
    CONSTRAINT uk_loan_review_application UNIQUE (application_id),
    CONSTRAINT fk_loan_review_application FOREIGN KEY (application_id) REFERENCES loan_application (application_id),
    CONSTRAINT ck_loan_review_decision CHECK (decision IN ('APPROVED', 'REJECTED')),
    CONSTRAINT ck_loan_review_amounts_non_negative CHECK (
        approved_limit IS NULL OR approved_limit >= 0
    ),
    CONSTRAINT ck_loan_review_approved_amount_non_negative CHECK (
        approved_amount IS NULL OR approved_amount >= 0
    )
);

CREATE TABLE loan (
    loan_id BIGINT NOT NULL AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    loan_product_id BIGINT NOT NULL,
    disbursement_account_id BIGINT NOT NULL,
    principal_amount DECIMAL(18, 2) NOT NULL,
    outstanding_principal DECIMAL(18, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    overdue_interest_rate DECIMAL(5, 2) NOT NULL,
    term_months INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    disbursed_at DATETIME(6) NULL,
    maturity_date DATE NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (loan_id),
    CONSTRAINT uk_loan_application UNIQUE (application_id),
    CONSTRAINT fk_loan_application FOREIGN KEY (application_id) REFERENCES loan_application (application_id),
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT fk_loan_loan_product FOREIGN KEY (loan_product_id) REFERENCES loan_product (loan_product_id),
    CONSTRAINT fk_loan_disbursement_account FOREIGN KEY (disbursement_account_id) REFERENCES account (account_id),
    CONSTRAINT ck_loan_status CHECK (
        status IN ('PENDING_DISBURSEMENT', 'ACTIVE', 'OVERDUE', 'PAID_OFF', 'DEFAULTED', 'RESTRUCTURED')
    ),
    CONSTRAINT ck_loan_principal_amount CHECK (principal_amount > 0),
    CONSTRAINT ck_loan_outstanding_principal CHECK (
        outstanding_principal >= 0 AND outstanding_principal <= principal_amount
    ),
    CONSTRAINT ck_loan_interest_rate CHECK (interest_rate >= 0 AND overdue_interest_rate >= 0),
    CONSTRAINT ck_loan_term_months CHECK (term_months > 0)
);

CREATE TABLE repayment_schedule (
    schedule_id BIGINT NOT NULL AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    installment_no INT NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(18, 2) NOT NULL,
    interest_due DECIMAL(18, 2) NOT NULL,
    late_interest_due DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    total_due DECIMAL(18, 2) NOT NULL,
    paid_total DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL,
    paid_at DATETIME(6) NULL,
    PRIMARY KEY (schedule_id),
    CONSTRAINT uk_repayment_schedule_installment UNIQUE (loan_id, installment_no),
    CONSTRAINT fk_repayment_schedule_loan FOREIGN KEY (loan_id) REFERENCES loan (loan_id),
    CONSTRAINT ck_repayment_schedule_amounts CHECK (
        principal_due >= 0
        AND interest_due >= 0
        AND late_interest_due >= 0
        AND total_due >= 0
        AND paid_total >= 0
        AND paid_total <= total_due
    ),
    CONSTRAINT ck_repayment_schedule_status CHECK (status IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'OVERDUE')),
    CONSTRAINT ck_repayment_schedule_installment_no CHECK (installment_no > 0)
);

CREATE TABLE repayment_history (
    repayment_id BIGINT NOT NULL AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    payment_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    payment_amount DECIMAL(18, 2) NOT NULL,
    principal_paid DECIMAL(18, 2) NOT NULL,
    interest_paid DECIMAL(18, 2) NOT NULL,
    late_interest_paid DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    paid_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (repayment_id),
    CONSTRAINT fk_repayment_history_loan FOREIGN KEY (loan_id) REFERENCES loan (loan_id),
    CONSTRAINT fk_repayment_history_schedule FOREIGN KEY (schedule_id) REFERENCES repayment_schedule (schedule_id),
    CONSTRAINT fk_repayment_history_account FOREIGN KEY (account_id) REFERENCES account (account_id),
    CONSTRAINT ck_repayment_history_payment_type CHECK (
        payment_type IN ('REGULAR', 'PARTIAL', 'LATE_PAYMENT', 'PREPAYMENT', 'EARLY_PAYOFF')
    ),
    CONSTRAINT ck_repayment_history_payment_amount CHECK (payment_amount > 0),
    CONSTRAINT ck_repayment_history_component_amounts CHECK (
        principal_paid >= 0
        AND interest_paid >= 0
        AND late_interest_paid >= 0
        AND payment_amount = principal_paid + interest_paid + late_interest_paid
    )
);

-- Customer -> accounts lookup
CREATE INDEX idx_account_customer_id ON account (customer_id);

-- Account transaction history in reverse chronological order
CREATE INDEX idx_account_transaction_account_id_transacted_at ON account_transaction (account_id, transacted_at DESC);

-- Reverse lookup from business object to account ledger entries
CREATE INDEX idx_account_transaction_reference ON account_transaction (reference_type, reference_id);

-- Customer loan application list
CREATE INDEX idx_loan_application_customer_id ON loan_application (customer_id);

-- Review queue / status-based application lookup
CREATE INDEX idx_loan_application_status_submitted_at ON loan_application (status, submitted_at DESC);

-- Customer active or closed loan lookup
CREATE INDEX idx_loan_customer_id_status ON loan (customer_id, status);

-- Per-loan repayment schedule lookup
CREATE INDEX idx_repayment_schedule_loan_id_status_due_date ON repayment_schedule (loan_id, status, due_date);

-- Overdue batch scan
CREATE INDEX idx_repayment_schedule_due_date_status ON repayment_schedule (due_date, status);

-- Loan repayment history in reverse chronological order
CREATE INDEX idx_repayment_history_loan_id_paid_at ON repayment_history (loan_id, paid_at DESC);
