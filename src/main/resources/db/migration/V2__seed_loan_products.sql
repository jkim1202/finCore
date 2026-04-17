INSERT INTO loan_product (
    product_name,
    min_amount,
    max_amount,
    base_interest_rate,
    overdue_interest_rate,
    min_term_months,
    max_term_months,
    active
) VALUES
    ('Standard Credit Loan', 1000000.00, 50000000.00, 5.20, 8.20, 6, 36, TRUE),
    ('Starter Salary Loan', 500000.00, 10000000.00, 4.80, 7.80, 6, 24, TRUE),
    ('Emergency Relief Loan', 300000.00, 3000000.00, 3.90, 6.90, 3, 12, TRUE);
