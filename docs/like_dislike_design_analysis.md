# 호감/비호감 설계 분석: 별도 테이블 vs 기존 테이블 컬럼 추가

## 1. 현재 설계 (별도 테이블)

### 현재 구조
```sql
CREATE TABLE like_dislike (
    like_dislike_id   NUMBER(10) PRIMARY KEY,
    target_type       VARCHAR2(20) NOT NULL,  -- 'BOARD' 또는 'REPLY'
    target_id         NUMBER(10) NOT NULL,    -- 게시글 ID 또는 댓글 ID
    member_id         NUMBER(10) NOT NULL,    -- 평가한 회원 ID
    like_type         VARCHAR2(10) NOT NULL,  -- 'LIKE' 또는 'DISLIKE'
    cdate             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    udate             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_like_dislike_unique UNIQUE (target_type, target_id, member_id)
);
```

### 장점
1. **정규화**: 데이터 중복 없음
2. **확장성**: 새로운 대상 타입 추가 용이
3. **상세 정보**: 각 평가의 시간 정보 보존
4. **복잡한 쿼리**: 사용자별 평가 이력 조회 가능
5. **데이터 무결성**: UNIQUE 제약으로 중복 평가 방지

### 단점
1. **JOIN 필요**: 게시글/댓글 조회 시 추가 JOIN
2. **성능 오버헤드**: COUNT 쿼리 시 GROUP BY 필요
3. **복잡성**: 코드가 더 복잡해짐

## 2. 대안 설계 (기존 테이블에 컬럼 추가)

### 제안 구조
```sql
-- boards 테이블에 추가
ALTER TABLE boards ADD (
    like_count    NUMBER(5) DEFAULT 0,        -- 좋아요 수
    dislike_count NUMBER(5) DEFAULT 0         -- 비호감 수
);

-- replies 테이블에 추가
ALTER TABLE replies ADD (
    like_count    NUMBER(5) DEFAULT 0,        -- 좋아요 수
    dislike_count NUMBER(5) DEFAULT 0         -- 비호감 수
);
```

### 장점
1. **단순성**: JOIN 없이 바로 조회 가능
2. **성능**: COUNT 쿼리 불필요
3. **직관성**: 코드가 단순해짐

### 단점
1. **비정규화**: 데이터 중복 발생
2. **동시성 문제**: 동시 업데이트 시 경합 조건
3. **상세 정보 손실**: 개별 평가 시간 정보 없음
4. **확장성 제한**: 새로운 평가 타입 추가 어려움
5. **데이터 무결성**: 중복 평가 방지 어려움

## 3. 하이브리드 설계 (추천)

### 구조
```sql
-- 메인 테이블에 카운트 컬럼 추가
ALTER TABLE boards ADD (
    like_count    NUMBER(5) DEFAULT 0,        -- 좋아요 수
    dislike_count NUMBER(5) DEFAULT 0         -- 비호감 수
);

ALTER TABLE replies ADD (
    like_count    NUMBER(5) DEFAULT 0,        -- 좋아요 수
    dislike_count NUMBER(5) DEFAULT 0         -- 비호감 수
);

-- 상세 정보는 별도 테이블 유지 (현재 like_dislike 테이블)
```

### 장점
1. **성능 최적화**: 자주 조회되는 카운트는 메인 테이블에
2. **상세 정보 보존**: 개별 평가 정보는 별도 테이블에
3. **확장성**: 새로운 기능 추가 가능
4. **동시성 제어**: 트랜잭션으로 일관성 보장

### 단점
1. **복잡성**: 두 테이블 동기화 필요
2. **저장 공간**: 약간의 중복 데이터

## 4. 성능 비교 분석

### 현재 설계 (별도 테이블)
```sql
-- 게시글 조회 시 좋아요 수 계산
SELECT b.*, 
       (SELECT COUNT(*) FROM like_dislike 
        WHERE target_type = 'BOARD' AND target_id = b.board_id AND like_type = 'LIKE') as like_count,
       (SELECT COUNT(*) FROM like_dislike 
        WHERE target_type = 'BOARD' AND target_id = b.board_id AND like_type = 'DISLIKE') as dislike_count
FROM boards b
WHERE b.board_id = :boardId;
```

### 대안 설계 (컬럼 추가)
```sql
-- 게시글 조회 시 바로 카운트 조회
SELECT b.*, b.like_count, b.dislike_count
FROM boards b
WHERE b.board_id = :boardId;
```

### 하이브리드 설계
```sql
-- 게시글 조회 시 바로 카운트 조회
SELECT b.*, b.like_count, b.dislike_count
FROM boards b
WHERE b.board_id = :boardId;

-- 상세 정보 필요시 별도 조회
SELECT * FROM like_dislike 
WHERE target_type = 'BOARD' AND target_id = :boardId;
```

## 5. 실제 사용 시나리오 분석

### 5.1 게시글 목록 조회
- **현재**: 각 게시글마다 서브쿼리 실행 (N+1 문제)
- **대안**: 바로 카운트 조회 (빠름)
- **하이브리드**: 바로 카운트 조회 (빠름)

### 5.2 게시글 상세 조회
- **현재**: 서브쿼리로 카운트 계산
- **대안**: 바로 카운트 조회
- **하이브리드**: 바로 카운트 조회

### 5.3 좋아요/비호감 처리
- **현재**: 별도 테이블 INSERT/UPDATE
- **대안**: 메인 테이블 UPDATE + 동시성 제어 필요
- **하이브리드**: 두 테이블 모두 UPDATE (트랜잭션)

### 5.4 사용자 평가 이력 조회
- **현재**: 별도 테이블에서 조회
- **대안**: 불가능 (정보 손실)
- **하이브리드**: 별도 테이블에서 조회

## 6. 권장사항

### 6.1 현재 상황에서의 권장사항

**하이브리드 설계를 추천합니다:**

1. **즉시 적용**: boards, replies 테이블에 카운트 컬럼 추가
2. **기존 테이블 유지**: like_dislike 테이블은 그대로 유지
3. **트랜잭션 처리**: 좋아요/비호감 시 두 테이블 동시 업데이트

### 6.2 구현 예시

#### 스키마 변경
```sql
-- boards 테이블에 카운트 컬럼 추가
ALTER TABLE boards ADD (
    like_count    NUMBER(5) DEFAULT 0,
    dislike_count NUMBER(5) DEFAULT 0
);

-- replies 테이블에 카운트 컬럼 추가
ALTER TABLE replies ADD (
    like_count    NUMBER(5) DEFAULT 0,
    dislike_count NUMBER(5) DEFAULT 0
);

-- 인덱스 추가
CREATE INDEX idx_boards_like_count ON boards(like_count);
CREATE INDEX idx_boards_dislike_count ON boards(dislike_count);
CREATE INDEX idx_replies_like_count ON replies(like_count);
CREATE INDEX idx_replies_dislike_count ON replies(dislike_count);
```

#### 서비스 로직 개선
```java
@Transactional
public Long like(String targetType, Long targetId, Long memberId) {
    // 1. 기존 평가 확인
    Optional<LikeDislike> existing = likeDislikeDAO.findByTargetAndMember(targetType, targetId, memberId);
    
    if (existing.isPresent()) {
        LikeDislike ld = existing.get();
        if ("LIKE".equals(ld.getLikeType())) {
            return ld.getLikeDislikeId(); // 이미 좋아요
        } else {
            // DISLIKE → LIKE로 변경
            likeDislikeDAO.delete(ld.getLikeDislikeId());
            // 메인 테이블 카운트 조정
            decrementDislikeCount(targetType, targetId);
        }
    }
    
    // 2. 새로운 좋아요 저장
    LikeDislike newLike = new LikeDislike();
    newLike.setTargetType(targetType);
    newLike.setTargetId(targetId);
    newLike.setMemberId(memberId);
    newLike.setLikeType("LIKE");
    newLike.setCdate(LocalDateTime.now());
    newLike.setUdate(LocalDateTime.now());
    Long likeId = likeDislikeDAO.save(newLike);
    
    // 3. 메인 테이블 카운트 증가
    incrementLikeCount(targetType, targetId);
    
    return likeId;
}

private void incrementLikeCount(String targetType, Long targetId) {
    if ("BOARD".equals(targetType)) {
        boardDAO.incrementLikeCount(targetId);
    } else if ("REPLY".equals(targetType)) {
        replyDAO.incrementLikeCount(targetId);
    }
}

private void decrementDislikeCount(String targetType, Long targetId) {
    if ("BOARD".equals(targetType)) {
        boardDAO.decrementDislikeCount(targetId);
    } else if ("REPLY".equals(targetType)) {
        replyDAO.decrementDislikeCount(targetId);
    }
}
```

### 6.3 DAO 메서드 추가
```java
// BoardDAO에 추가
public void incrementLikeCount(Long boardId) {
    String sql = "UPDATE boards SET like_count = like_count + 1 WHERE board_id = :boardId";
    MapSqlParameterSource param = new MapSqlParameterSource().addValue("boardId", boardId);
    template.update(sql, param);
}

public void decrementDislikeCount(Long boardId) {
    String sql = "UPDATE boards SET dislike_count = dislike_count - 1 WHERE board_id = :boardId AND dislike_count > 0";
    MapSqlParameterSource param = new MapSqlParameterSource().addValue("boardId", boardId);
    template.update(sql, param);
}
```

## 7. 결론

**하이브리드 설계를 추천하는 이유:**

1. **성능 향상**: 자주 조회되는 카운트는 메인 테이블에서 바로 조회
2. **기능 보존**: 상세 정보와 사용자 이력은 별도 테이블에서 관리
3. **점진적 개선**: 기존 코드를 크게 변경하지 않고 성능 개선 가능
4. **확장성**: 향후 새로운 기능 추가 시 유연성 확보

이 방식은 **성능과 기능성의 균형**을 맞추는 가장 실용적인 접근법입니다. 