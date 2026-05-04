# finCore

Spring Boot 기반 금융 코어 백엔드 프로젝트다.

## Stack

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
- MySQL
- Redis
- Flyway
- Docker Compose
- JUnit 5 / Mockito / Testcontainers

## Current Scope

- 사용자 도메인
  - `User`, `UserRole`, `UserStatus`
  - 다중 권한 구조
  - 비밀번호 해시 기반 인증 준비
- 인증 도메인
  - 회원가입
  - 로그인
  - refresh token 기반 토큰 재발급
  - JWT 기반 인증 필터와 Security 설정
- 계좌 도메인
  - 계좌 생성
  - 계좌 상세 조회
  - 입금
  - 출금
  - 계좌 거래내역 목록 조회
  - 거래 단건 조회
  - 계좌번호 sequence 기반 생성
- 공통 처리
  - `BusinessException`
  - `ErrorCode`
  - `GlobalExceptionHandler`

## Database And Migration

- 기본 DB 드라이버는 MySQL이다.
- Flyway는 `classpath:db/migration` 경로를 사용한다.
- 현재 포함된 마이그레이션:
  - `V1__init_schema.sql`: 사용자, 계좌, 계좌 거래, 대출, 상환 관련 초기 코어 스키마와 기본 대출 상품 seed
  - `V2__create_account_number_sequence_table.sql`: 계좌번호 생성을 위한 sequence 테이블

## API Scope

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/accounts`
- `GET /api/v1/accounts/{accountId}`
- `POST /api/v1/accounts/{accountId}/deposit`
- `POST /api/v1/accounts/{accountId}/withdraw`
- `GET /api/v1/accounts/{accountId}/transactions`
- `GET /api/v1/accounts/transactions/{transactionId}`

## Testing

- 인증 controller/service 테스트
- 계좌 controller/service/usecase 테스트
- 사용자 조회 service 테스트
- 테스트 케이스에는 `@DisplayName`으로 시나리오 설명을 기록한다.

## Next

- Account API 통합 테스트 보강
- Redis 기반 idempotency 적용
- Loan Product 조회 API 구현
- Loan Application / Review 흐름 구현
