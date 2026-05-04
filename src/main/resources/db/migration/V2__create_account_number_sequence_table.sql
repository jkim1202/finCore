/**
  계좌번호 생성을 위한 테이블
 */
CREATE TABLE account_number_sequence (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);