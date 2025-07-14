# 계층형 게시판 베스트 프랙티스 설계

## 1. 현재 모델 분석 (Adjacency List)

### 현재 구조
```sql
CREATE TABLE boards(
    board_id    NUMBER(10)     NOT NULL,         -- 게시글 번호
    pboard_id   NUMBER(10),                     -- 부모 게시글번호
    bgroup      NUMBER(10),                     -- 답글그룹
    step        NUMBER(3)      DEFAULT 0,        -- 답글단계
    bindent     NUMBER(3)      DEFAULT 0,        -- 답글들여쓰기
    -- ... 기타 필드
);
```

### 장단점
**장점:**
- 구조가 단순하고 이해하기 쉬움
- INSERT/UPDATE가 간단함
- 저장 공간이 적음

**단점:**
- 깊은 계층 조회 시 성능 저하
- 계층 이동 시 복잡한 업데이트 필요
- 삭제 시 하위 노드 처리 복잡

## 2. 개선된 모델: Nested Set Model

### 구조
```sql
CREATE TABLE boards(
    board_id    NUMBER(10)     NOT NULL,         -- 게시글 번호
    left_bound  NUMBER(10)     NOT NULL,         -- 왼쪽 경계값
    right_bound NUMBER(10)     NOT NULL,         -- 오른쪽 경계값
    depth       NUMBER(3)      DEFAULT 0,        -- 깊이 레벨
    bgroup      NUMBER(10),                     -- 그룹 ID (원글 기준)
    -- ... 기타 필드
);
```

### 장점
- 계층 조회 성능이 매우 우수
- 전체 하위 트리 조회가 간단
- 계층 이동이 상대적으로 간단

### 단점
- INSERT/UPDATE 시 많은 레코드 업데이트 필요
- 구조가 복잡함

## 3. 하이브리드 모델 (추천)

### 구조
```sql
CREATE TABLE boards(
    board_id    NUMBER(10)     NOT NULL,         -- 게시글 번호
    pboard_id   NUMBER(10),                     -- 부모 게시글번호
    bgroup      NUMBER(10),                     -- 답글그룹
    step        NUMBER(3)      DEFAULT 0,        -- 답글단계
    bindent     NUMBER(3)      DEFAULT 0,        -- 답글들여쓰기
    path        VARCHAR2(1000),                  -- 경로 (예: /1/5/12)
    depth       NUMBER(3)      DEFAULT 0,        -- 깊이 레벨
    max_depth   NUMBER(3)      DEFAULT 5,        -- 최대 깊이 제한
    -- ... 기타 필드
);
```

### 장점
- Adjacency List의 단순성 유지
- Path 기반 빠른 조회 가능
- 깊이 제한으로 성능 보장
- 기존 코드와 호환성 유지

## 4. 최적화된 현재 모델 개선안

### 4.1 스키마 개선
```sql
-- 기존 테이블에 추가할 컬럼들
ALTER TABLE boards ADD (
    path        VARCHAR2(1000),                  -- 경로 (예: /1/5/12)
    depth       NUMBER(3)      DEFAULT 0,        -- 깊이 레벨
    max_depth   NUMBER(3)      DEFAULT 5,        -- 최대 깊이 제한
    reply_count NUMBER(5)      DEFAULT 0,        -- 하위 답글 수
    last_reply_date TIMESTAMP                    -- 마지막 답글 날짜
);

-- 인덱스 추가
CREATE INDEX idx_boards_path ON boards(path);
CREATE INDEX idx_boards_depth ON boards(depth);
CREATE INDEX idx_boards_group_step_depth ON boards(bgroup, step, depth);
```

### 4.2 서비스 로직 개선

#### 계층 제한 로직
```java
private static final int MAX_DEPTH = 5;
private static final int STEP_INTERVAL = 10;

private void saveReplyPost(Boards boards) {
    Boards parentBoard = boardDAO.findById(boards.getPboardId())
            .orElseThrow(() -> new BusinessValidationException("부모 게시글을 찾을 수 없습니다."));
    
    // 깊이 제한 확인
    if (parentBoard.getDepth() >= MAX_DEPTH) {
        throw new BusinessValidationException("최대 답글 깊이(" + MAX_DEPTH + ")를 초과할 수 없습니다.");
    }
    
    // 계층 구조 설정
    boards.setBgroup(parentBoard.getBgroup());
    boards.setDepth(parentBoard.getDepth() + 1);
    boards.setStep(parentBoard.getStep() + STEP_INTERVAL);
    boards.setBindent(parentBoard.getBindent() + 1);
    boards.setPath(parentBoard.getPath() + "/" + boards.getBoardId());
    boards.setStatus("A");
    
    // 답글 저장
    Long boardId = boardDAO.save(boards);
    boards.setBoardId(boardId);
    
    // 부모 게시글의 답글 수 증가
    boardDAO.incrementReplyCount(parentBoard.getBoardId());
}
```

#### 성능 최적화된 조회
```java
// 계층별 조회 (깊이 제한으로 성능 보장)
public List<Boards> findByBgroupWithDepthLimit(Long bgroup, int maxDepth) {
    return boardDAO.findByBgroupWithDepthLimit(bgroup, maxDepth);
}

// Path 기반 빠른 조회
public List<Boards> findByPath(String path) {
    return boardDAO.findByPath(path);
}
```

### 4.3 DAO 개선
```java
// 깊이 제한이 있는 계층 조회
public List<Boards> findByBgroupWithDepthLimit(Long bgroup, int maxDepth) {
    String sql = """
        SELECT * FROM boards 
        WHERE bgroup = :bgroup AND depth <= :maxDepth
        ORDER BY step ASC, cdate ASC
        """;
    MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("bgroup", bgroup)
            .addValue("maxDepth", maxDepth);
    
    return template.query(sql, param, boardRowMapper);
}

// Path 기반 조회
public List<Boards> findByPath(String path) {
    String sql = """
        SELECT * FROM boards 
        WHERE path LIKE :path || '%'
        ORDER BY path ASC, cdate ASC
        """;
    MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("path", path);
    
    return template.query(sql, param, boardRowMapper);
}

// 답글 수 증가
public void incrementReplyCount(Long boardId) {
    String sql = """
        UPDATE boards 
        SET reply_count = reply_count + 1, 
            last_reply_date = CURRENT_TIMESTAMP
        WHERE board_id = :boardId
        """;
    MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("boardId", boardId);
    
    template.update(sql, param);
}
```

## 5. 프론트엔드 최적화

### 5.1 무한 스크롤 개선
```javascript
// 깊이별 그룹화된 댓글 로딩
class HierarchicalCommentLoader {
    constructor() {
        this.currentDepth = 0;
        this.maxDepth = 5;
        this.loadingDepth = new Set();
    }
    
    async loadCommentsByDepth(boardId, depth, offset = 0, limit = 10) {
        if (this.loadingDepth.has(depth)) return;
        
        this.loadingDepth.add(depth);
        try {
            const response = await fetch(`/api/boards/${boardId}/comments?depth=${depth}&offset=${offset}&limit=${limit}`);
            const data = await response.json();
            
            if (data.success) {
                this.renderCommentsByDepth(data.comments, depth);
            }
        } finally {
            this.loadingDepth.delete(depth);
        }
    }
    
    renderCommentsByDepth(comments, depth) {
        const container = document.querySelector(`[data-depth="${depth}"]`);
        if (!container) return;
        
        comments.forEach(comment => {
            const commentElement = this.createCommentElement(comment);
            container.appendChild(commentElement);
        });
    }
}
```

### 5.2 계층 시각화 개선
```css
/* 깊이별 들여쓰기 */
.comment-depth-0 { margin-left: 0; }
.comment-depth-1 { margin-left: 20px; border-left: 2px solid #e0e0e0; padding-left: 10px; }
.comment-depth-2 { margin-left: 40px; border-left: 2px solid #d0d0d0; padding-left: 10px; }
.comment-depth-3 { margin-left: 60px; border-left: 2px solid #c0c0c0; padding-left: 10px; }
.comment-depth-4 { margin-left: 80px; border-left: 2px solid #b0b0b0; padding-left: 10px; }
.comment-depth-5 { margin-left: 100px; border-left: 2px solid #a0a0a0; padding-left: 10px; }

/* 깊이 제한 시 스타일 */
.comment-max-depth {
    background-color: #f8f8f8;
    border: 1px solid #ddd;
    padding: 10px;
    margin: 5px 0;
    font-style: italic;
    color: #666;
}
```

## 6. 권장사항

### 6.1 단기 개선 (현재 모델 기반)
1. **깊이 제한 추가**: MAX_DEPTH = 5로 제한
2. **Step 간격 확대**: STEP_INTERVAL = 10으로 설정
3. **Path 컬럼 추가**: 빠른 조회를 위한 경로 저장
4. **답글 수 카운트**: 성능 최적화를 위한 메타데이터 추가

### 6.2 중기 개선 (하이브리드 모델)
1. **Path 기반 조회**: 계층 조회 성능 향상
2. **캐싱 도입**: Redis를 활용한 인기 게시글 캐싱
3. **비동기 처리**: 대용량 데이터 처리를 위한 비동기 로직

### 6.3 장기 개선 (필요시)
1. **Nested Set Model**: 대용량 데이터 처리를 위한 모델 변경
2. **분산 처리**: 마이크로서비스 아키텍처 도입
3. **검색 엔진**: Elasticsearch를 활용한 고급 검색 기능

## 7. 성능 최적화 체크리스트

- [ ] 깊이 제한 설정 (MAX_DEPTH = 5)
- [ ] Step 간격 확대 (STEP_INTERVAL = 10)
- [ ] Path 컬럼 추가 및 인덱스 생성
- [ ] 답글 수 카운트 컬럼 추가
- [ ] 적절한 인덱스 설정
- [ ] 프론트엔드 무한 스크롤 최적화
- [ ] 캐싱 전략 수립
- [ ] 모니터링 및 로깅 추가 