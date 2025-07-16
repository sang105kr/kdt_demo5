-- 이메일 인증 토큰 테이블 생성
CREATE TABLE email_verification_tokens (
    token_id NUMBER PRIMARY KEY,
    email VARCHAR2(100) NOT NULL,
    verification_code VARCHAR2(6) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,
    cdate TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    udate TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

-- 시퀀스 생성
CREATE SEQUENCE seq_email_verification_token_id
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 인덱스 생성
CREATE INDEX idx_email_verification_email ON email_verification_tokens(email);
CREATE INDEX idx_email_verification_code ON email_verification_tokens(verification_code);
CREATE INDEX idx_email_verification_status ON email_verification_tokens(status);
CREATE INDEX idx_email_verification_expiry ON email_verification_tokens(expiry_date);

-- 비밀번호 재설정 토큰 테이블 생성
CREATE TABLE password_reset_tokens (
    token_id NUMBER PRIMARY KEY,
    email VARCHAR2(100) NOT NULL,
    token VARCHAR2(100) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,
    cdate TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    udate TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

-- 시퀀스 생성
CREATE SEQUENCE seq_password_reset_token_id
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 인덱스 생성
CREATE INDEX idx_password_reset_email ON password_reset_tokens(email);
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_status ON password_reset_tokens(status);
CREATE INDEX idx_password_reset_expiry ON password_reset_tokens(expiry_date);

-- 샘플 데이터 (테스트용)
INSERT INTO email_verification_tokens (token_id, email, verification_code, expiry_date, status)
VALUES (seq_email_verification_token_id.nextval, 'test@example.com', '123456', SYSTIMESTAMP + INTERVAL '10' MINUTE, 'ACTIVE');

INSERT INTO password_reset_tokens (token_id, email, token, expiry_date, status)
VALUES (seq_password_reset_token_id.nextval, 'test@example.com', 'test-token-123456789', SYSTIMESTAMP + INTERVAL '30' MINUTE, 'ACTIVE'); 