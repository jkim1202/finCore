# finCore

Spring Boot 기반 금융 코어 백엔드 프로젝트다.

## Stack

- Java 21
- Spring Boot
- Spring Data JPA
- MySQL
- Redis
- Flyway
- Docker Compose
- JUnit 5 / Testcontainers

## Database And Migration

- 기본 DB 드라이버는 MySQL이다.
- Flyway는 `classpath:db/migration` 경로를 사용한다.
- 현재 포함된 마이그레이션:
  - `V1__init_schema.sql`: account, loan, repayment를 포함한 초기 코어 스키마
  - `V2__seed_loan_products.sql`: 기본 대출 상품 시드 데이터
  - `V3__rename_customer_to_users.sql`: `users`, `user_role`, `user_status` 기준의 사용자 도메인으로 전환
  - `V4__split_user_roles.sql`: 단일 사용자 권한 컬럼을 다중 권한 테이블로 분리
  - `V5__add_user_password_hash.sql`: 사용자 비밀번호 해시 컬럼 추가

## Next

- 도메인 엔티티 및 패키지 구조 추가
- JPA 엔티티와 Flyway 스키마 매핑 정리
- Account / Loan API 구현
