# Top 메뉴 시스템 관리 가이드

## 개요
Top 메뉴의 프로필 드롭다운 시스템은 중앙 집중식으로 관리됩니다. 모든 관련 스타일, JavaScript, HTML 구조는 지정된 파일에서만 관리되어 일관성을 보장합니다.

## 파일 구조

### 1. CSS 관리
- **파일**: `src/main/resources/static/css/top.css`
- **관리 내용**: 모든 드롭다운 관련 스타일
- **주요 클래스**:
  - `.profile-dropdown`: 기본 드롭다운 컨테이너
  - `.customer-profile`: 고객용 드롭다운
  - `.admin-profile`: 관리자용 드롭다운
  - `.active`: 활성화 상태

### 2. JavaScript 관리
- **파일**: `src/main/resources/static/js/top.js`
- **관리 내용**: 모든 드롭다운 관련 함수
- **주요 함수**:
  - `toggleCustomerDropdown()`: 고객 드롭다운 토글
  - `toggleAdminDropdown()`: 관리자 드롭다운 토글
  - `openDropdown()`: 드롭다운 열기
  - `closeAllDropdowns()`: 모든 드롭다운 닫기

### 3. HTML 구조 관리
- **파일**: `src/main/resources/templates/fragment/top.html`
- **관리 내용**: 드롭다운 HTML 구조
- **주요 구조**:
  - 고객용: `.profile-dropdown.customer-profile`
  - 관리자용: `.profile-dropdown.admin-profile`

## 관리 규칙

### 1. 중앙 집중식 관리
- ✅ **허용**: `top.css`, `top.js`, `top.html`에서만 관리
- ❌ **금지**: 다른 파일에서 드롭다운 스타일/함수 추가

### 2. 일관성 보장
- 모든 페이지에서 동일한 스타일 적용
- 클래스명과 함수명 통일
- 동작 방식 일관성 유지

### 3. 변경 시 주의사항
1. 관련 파일의 주석 섹션 업데이트
2. 모든 페이지에서 테스트
3. 브라우저 호환성 확인

## 문제 해결

### 스타일이 페이지마다 다른 경우
1. `top.css`에서 스타일 확인
2. 다른 CSS 파일에서 중복 스타일 검색
3. CSS 우선순위 확인
4. 필요시 `!important` 사용 (최후의 수단)

### JavaScript 동작이 다른 경우
1. `top.js`에서 함수 확인
2. 다른 JS 파일에서 중복 함수 검색
3. 이벤트 리스너 충돌 확인
4. 콘솔 에러 확인

## 개발 가이드

### 새로운 드롭다운 추가 시
1. `top.html`에 HTML 구조 추가
2. `top.css`에 스타일 추가
3. `top.js`에 함수 추가
4. 주석 섹션 업데이트

### 스타일 수정 시
1. `top.css`에서만 수정
2. 다른 CSS 파일 확인하여 중복 제거
3. 모든 페이지에서 테스트

### JavaScript 수정 시
1. `top.js`에서만 수정
2. 다른 JS 파일 확인하여 중복 제거
3. 브라우저 콘솔에서 에러 확인

## 검증 체크리스트

- [ ] 모든 드롭다운 스타일이 `top.css`에만 있음
- [ ] 모든 드롭다운 함수가 `top.js`에만 있음
- [ ] 모든 드롭다운 HTML이 `top.html`에만 있음
- [ ] 다른 파일에서 중복 코드 없음
- [ ] 모든 페이지에서 일관된 동작
- [ ] 주석 섹션이 최신 상태

## 연락처
문제 발생 시 이 문서를 참조하여 중앙 집중식 관리 원칙을 준수하세요. 