# Schema Notes

이 문서는 `src/main/resources/db/migration` 기준의 현재 스키마를 설명한다.
DDL 자체는 Flyway SQL이 기준이고, 여기서는 각 테이블과 컬럼의 도메인 의미를 짧게 정리한다.

## Conventions

- 금액 컬럼은 KRW 기준 `DECIMAL(18, 2)`를 사용한다.
- `*_id`는 기본적으로 각 테이블의 PK 또는 FK다.
- `created_at`, `updated_at`, `transacted_at`, `paid_at`은 이벤트 발생 시각 또는 생성 시각이다.
- 상태값은 문자열 enum 방식으로 저장한다.

## customer

고객 기본 정보다. 계좌와 대출의 소유 주체다.

| Column | Meaning |
| --- | --- |
| `customer_id` | 고객 식별자 |
| `name` | 고객 이름 |
| `email` | 고객 이메일. 중복 불가 |
| `phone` | 고객 연락처. 중복 불가 |
| `birth_date` | 생년월일 |
| `created_at` | 고객 등록 시각 |

## account

고객 명의의 계좌다. 현재 잔액은 이 테이블에 있고, 거래 이력은 `account_transaction`에 남긴다.

| Column | Meaning |
| --- | --- |
| `account_id` | 계좌 식별자 |
| `customer_id` | 계좌 소유 고객 |
| `account_number` | 외부 노출용 계좌번호. 중복 불가 |
| `status` | 계좌 상태. `ACTIVE`, `FROZEN`, `CLOSED` |
| `balance` | 현재 잔액 |
| `created_at` | 계좌 생성 시각 |
| `updated_at` | 계좌 정보 또는 잔액 마지막 변경 시각 |

## account_transaction

계좌 거래 원장이다. 금융 거래 이력을 저장한다.

| Column | Meaning |
| --- | --- |
| `transaction_id` | 거래 이력 식별자 |
| `account_id` | 거래가 발생한 계좌 |
| `transaction_type` | 거래 종류. `DEPOSIT`, `WITHDRAWAL`, `LOAN_DISBURSEMENT`, `LOAN_REPAYMENT` |
| `amount` | 거래 금액 |
| `balance_after` | 거래 처리 직후 계좌 잔액 |
| `description` | 거래 설명 또는 표시 문구 |
| `reference_type` | 거래를 발생시킨 업무 객체 종류. 예: `LOAN`, `REPAYMENT` |
| `reference_id` | 업무 객체 식별자 |
| `idempotency_key` | 중복 요청 방지용 요청 식별자 |
| `transacted_at` | 거래 발생 시각 |

## loan_product

대출 상품 마스터다. 신청 가능한 금액 범위, 기간, 금리 조건을 가진다.

| Column | Meaning |
| --- | --- |
| `loan_product_id` | 대출 상품 식별자 |
| `product_name` | 상품명 |
| `min_amount` | 최소 신청 가능 금액 |
| `max_amount` | 최대 신청 가능 금액 |
| `base_interest_rate` | 기본 약정 금리 |
| `overdue_interest_rate` | 연체 시 적용 금리 |
| `min_term_months` | 최소 대출 기간(월) |
| `max_term_months` | 최대 대출 기간(월) |
| `active` | 현재 신청 가능 여부 |
| `created_at` | 상품 등록 시각 |

## loan_application

고객이 제출한 대출 신청서다. 심사 전후 상태를 갖고, 실행 계좌도 여기서 지정한다.

| Column | Meaning |
| --- | --- |
| `application_id` | 신청 식별자 |
| `customer_id` | 신청 고객 |
| `loan_product_id` | 신청 대상 상품 |
| `disbursement_account_id` | 승인 시 대출금이 입금될 계좌 |
| `requested_amount` | 신청 금액 |
| `requested_term_months` | 신청 기간(월) |
| `annual_income` | 신청 시점 연 소득 |
| `existing_debt_amount` | 신청 시점 기존 부채 금액 |
| `status` | 신청 상태. `SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `CANCELLED` |
| `submitted_at` | 신청 접수 시각 |
| `reviewed_at` | 심사 완료 시각 |

## loan_review

대출 심사 결과다. 신청 1건당 최대 1건을 가진다.

| Column | Meaning |
| --- | --- |
| `review_id` | 심사 결과 식별자 |
| `application_id` | 대상 신청서 |
| `decision` | 심사 결과. `APPROVED`, `REJECTED` |
| `approved_limit` | 심사 기준상 허용 가능한 한도 |
| `approved_amount` | 실제 승인 금액 |
| `reject_reason` | 거절 사유 |
| `rule_snapshot` | 심사 규칙/판단 근거 기록 |
| `reviewed_at` | 심사 완료 시각 |

## loan

승인된 신청이 실제 대출로 생성된 엔티티다. 실행 이후 잔여 원금과 상태를 추적한다.

| Column | Meaning |
| --- | --- |
| `loan_id` | 대출 식별자 |
| `application_id` | 원본 신청서 |
| `customer_id` | 차주 |
| `loan_product_id` | 적용 상품 |
| `disbursement_account_id` | 대출금 입금 계좌 |
| `principal_amount` | 실행된 원금 |
| `outstanding_principal` | 현재 남은 원금 |
| `interest_rate` | 약정 금리 |
| `overdue_interest_rate` | 연체 금리 |
| `term_months` | 총 기간(월) |
| `status` | 대출 상태. `PENDING_DISBURSEMENT`, `ACTIVE`, `OVERDUE`, `PAID_OFF`, `DEFAULTED`, `RESTRUCTURED` |
| `disbursed_at` | 실제 실행 시각 |
| `maturity_date` | 만기일 |
| `created_at` | 대출 레코드 생성 시각 |

## repayment_schedule

회차별 상환 계획이다. 납부 예정 금액과 납부 상태를 관리한다.

| Column | Meaning |
| --- | --- |
| `schedule_id` | 상환 회차 식별자 |
| `loan_id` | 대상 대출 |
| `installment_no` | 회차 번호 |
| `due_date` | 납부 예정일 |
| `principal_due` | 해당 회차 원금 예정액 |
| `interest_due` | 해당 회차 이자 예정액 |
| `late_interest_due` | 현재까지 반영된 연체 이자 |
| `total_due` | 총 납부 예정액 |
| `paid_total` | 현재까지 납부된 누적 금액 |
| `status` | 회차 상태. `PENDING`, `PARTIALLY_PAID`, `PAID`, `OVERDUE` |
| `paid_at` | 회차가 전액 납부된 시각 |

## repayment_history

실제 납부 이력이다. 어떤 계좌에서 얼마를 냈는지와 원금/이자 분해값을 저장한다.

| Column | Meaning |
| --- | --- |
| `repayment_id` | 납부 이력 식별자 |
| `loan_id` | 대상 대출 |
| `schedule_id` | 대상 회차 |
| `account_id` | 납부 출금 계좌 |
| `payment_type` | 상환 유형. `REGULAR`, `PARTIAL`, `LATE_PAYMENT`, `PREPAYMENT`, `EARLY_PAYOFF` |
| `payment_amount` | 실제 납부 총액 |
| `principal_paid` | 납부액 중 원금 |
| `interest_paid` | 납부액 중 일반 이자 |
| `late_interest_paid` | 납부액 중 연체 이자 |
| `paid_at` | 납부 처리 시각 |

## Status Notes

### account.status

- `ACTIVE`: 정상 사용 가능
- `FROZEN`: 지급정지 또는 사용 제한 상태
- `CLOSED`: 해지 완료 상태

### loan_application.status

- `SUBMITTED`: 신청 접수 완료
- `UNDER_REVIEW`: 심사 진행 중
- `APPROVED`: 승인 완료
- `REJECTED`: 거절 완료
- `CANCELLED`: 신청 취소

### loan.status

- `PENDING_DISBURSEMENT`: 승인됐지만 아직 실행 전
- `ACTIVE`: 정상 상환 중
- `OVERDUE`: 연체 회차 존재
- `PAID_OFF`: 전액 상환 완료
- `DEFAULTED`: 부실 또는 장기 연체 처리
- `RESTRUCTURED`: 조건 재조정 상태

### repayment_schedule.status

- `PENDING`: 아직 납부 전
- `PARTIALLY_PAID`: 일부만 납부된 상태
- `PAID`: 전액 납부 완료
- `OVERDUE`: 납부일 경과 후 미납 상태
