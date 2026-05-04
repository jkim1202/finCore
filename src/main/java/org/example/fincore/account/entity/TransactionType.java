package org.example.fincore.account.entity;

public enum TransactionType {
    DEPOSIT,             // 일반 입금
    WITHDRAWAL,          // 일반 출금
    LOAN_DISBURSEMENT,   // 대출 실행으로 인한 입금
    LOAN_REPAYMENT       // 대출 상환으로 인한 출금
}
