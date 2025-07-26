-- 테스트용 데이터베이스 스키마 (H2)

-- 게시글 테이블
CREATE TABLE IF NOT EXISTS boards (
    board_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bcategory BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    bcontent CLOB,
    status VARCHAR(1) DEFAULT 'A',
    cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    udate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 댓글 테이블
CREATE TABLE IF NOT EXISTS replies (
    reply_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    email VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    rcontent VARCHAR(500) NOT NULL,
    parent_id BIGINT,
    rgroup BIGINT,
    rstep INT DEFAULT 0,
    rindent INT DEFAULT 0,
    status VARCHAR(1) DEFAULT 'A',
    cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    udate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (board_id) REFERENCES boards(board_id),
    FOREIGN KEY (parent_id) REFERENCES replies(reply_id)
);

-- 코드 테이블
CREATE TABLE IF NOT EXISTS codes (
    code_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gcode VARCHAR(50) NOT NULL,
    pcode BIGINT DEFAULT 0,
    code_name VARCHAR(100) NOT NULL,
    code_value VARCHAR(100),
    sort_order INT DEFAULT 0,
    status VARCHAR(1) DEFAULT 'A',
    cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    udate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 시퀀스 생성 (H2에서는 AUTO_INCREMENT 사용)
-- seq_board_id, seq_reply_id는 AUTO_INCREMENT로 대체

-- 코드 테이블 (H2용)
CREATE TABLE IF NOT EXISTS code (
    code_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gcode VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    decode VARCHAR(100) NOT NULL,
    pcode BIGINT,
    sort_order INT DEFAULT 0,
    use_yn VARCHAR(1) DEFAULT 'Y',
    cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    udate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 검색 로그 테이블 (H2용)
CREATE TABLE IF NOT EXISTS search_logs (
    search_log_id BIGINT IDENTITY PRIMARY KEY,
    member_id BIGINT,
    keyword VARCHAR(200) NOT NULL,
    search_type_id BIGINT NOT NULL,
    result_count INT DEFAULT 0,
    search_ip VARCHAR(50),
    cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    udate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (search_type_id) REFERENCES code(code_id)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_search_logs_keyword ON search_logs(keyword);
CREATE INDEX IF NOT EXISTS idx_search_logs_member_date ON search_logs(member_id, cdate);
CREATE INDEX IF NOT EXISTS idx_search_logs_date ON search_logs(cdate);
CREATE INDEX IF NOT EXISTS idx_search_logs_type ON search_logs(search_type);
CREATE INDEX IF NOT EXISTS idx_boards_bcategory ON boards(bcategory);
CREATE INDEX IF NOT EXISTS idx_boards_status ON boards(status);
CREATE INDEX IF NOT EXISTS idx_replies_board_id ON replies(board_id);
CREATE INDEX IF NOT EXISTS idx_replies_parent_id ON replies(parent_id);
CREATE INDEX IF NOT EXISTS idx_replies_rgroup ON replies(rgroup);
CREATE INDEX IF NOT EXISTS idx_codes_gcode ON codes(gcode);
CREATE INDEX IF NOT EXISTS idx_codes_pcode ON codes(pcode); 