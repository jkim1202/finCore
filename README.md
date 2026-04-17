# finCore

Spring Boot 기반 금융 코어 백엔드 프로젝트의 초기 실행 환경을 정리한 저장소다. 현재 단계에서는 MySQL, Redis, Flyway를 기준으로 로컬 개발용 설정과 커밋 가능한 기본 설정을 분리해 두었다.

## Stack

- Java 21
- Spring Boot
- Spring Data JPA
- MySQL
- Redis
- Flyway
- Docker Compose
- JUnit 5 / Testcontainers

## Configuration Strategy

커밋되는 공통 설정:

- `build.gradle`
- `src/main/resources/application.yml`
- `src/main/resources/application-docker.yml`
- `.env.example`
- `docker-compose.yml`

로컬 전용 설정:

- `application-local.yml`
- `.env.local`

`application-local.yml`과 `.env.local`은 `.gitignore`에 포함되어 원격 저장소에 올라가지 않는다.

## Local Setup

### 1. 로컬 설정 파일 만들기

```bash
cp application-local.example.yml application-local.yml
cp .env.example .env.local
```

필요하면 `application-local.yml`에 개인 DB 계정, 포트, 로컬 환경별 값을 수정한다.

### 2. 인프라 실행

```bash
docker compose --env-file .env.local up -d
```

기본으로 다음 서비스가 올라온다.

- MySQL: `localhost:3306`
- Redis: `localhost:6379`

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 프로필은 `local`이며, `application.yml`이 루트의 `application-local.yml`을 선택적으로 import 한다.

## Profiles

- `local`: 기본 로컬 개발 프로필
- `docker`: 애플리케이션을 컨테이너 네트워크 내부에서 띄울 때 사용할 수 있는 프로필

예시:

```bash
SPRING_PROFILES_ACTIVE=docker ./gradlew bootRun
```

## Database And Migration

- 기본 DB 드라이버는 MySQL이다.
- Flyway는 `classpath:db/migration` 경로를 사용한다.
- 현재 포함된 마이그레이션:
  - `V1__init_schema.sql`: customer, account, loan, repayment 관련 초기 스키마
  - `V2__seed_loan_products.sql`: 기본 대출 상품 시드 데이터

## Useful Commands

```bash
./gradlew test
./gradlew bootRun
docker compose --env-file .env.local up -d
docker compose --env-file .env.local down
```

## Next

- 도메인 엔티티 및 패키지 구조 추가
- JPA 엔티티와 Flyway 스키마 매핑 정리
- Account / Loan API 구현
