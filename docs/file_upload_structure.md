# 파일 업로드 폴더 구조

## 개요
파일 업로드 시 코드 테이블의 `FILE_TYPE` 그룹의 코드값을 기반으로 하위 폴더에 저장하여 파일 관리를 용이하게 합니다.

## 폴더 구조
```
uploads/
├── product_image/      # 상품 이미지 파일
├── product_manual/     # 상품 설명서 파일
├── board_attach/       # 게시판 첨부 파일
├── member_profile/     # 회원 프로필 이미지
├── review_image/       # 리뷰 이미지 파일
├── qna_attach/         # Q&A 첨부 파일
├── report_attach/      # 신고 첨부 파일
└── system_doc/         # 시스템 문서 파일
```

## 코드 테이블 기반 폴더 매핑

### FILE_TYPE 그룹 코드
- **PRODUCT_IMAGE**: 상품 이미지 파일
- **PRODUCT_MANUAL**: 상품 설명서 파일
- **BOARD_ATTACH**: 게시판 첨부 파일
- **MEMBER_PROFILE**: 회원 프로필 이미지
- **REVIEW_IMAGE**: 리뷰 이미지 파일
- **QNA_ATTACH**: Q&A 첨부 파일
- **REPORT_ATTACH**: 신고 첨부 파일
- **SYSTEM_DOC**: 시스템 문서 파일

### 폴더명 생성 규칙
- 코드값을 소문자로 변환하여 폴더명 생성
- 예: `PRODUCT_IMAGE` → `product_image`

## 파일 타입별 설정

### 상품 이미지 (product_image/)
- **지원 형식**: JPG, JPEG, PNG, GIF, WEBP
- **최대 크기**: 10MB
- **용도**: 상품 이미지

### 상품 설명서 (product_manual/)
- **지원 형식**: PDF, DOC, DOCX, TXT
- **최대 크기**: 50MB
- **용도**: 상품 설명서, 사용자 매뉴얼

### 게시판 첨부 (board_attach/)
- **지원 형식**: 모든 파일 형식
- **최대 크기**: 100MB
- **용도**: 게시판 첨부 파일

### 회원 프로필 (member_profile/)
- **지원 형식**: JPG, PNG, GIF, WEBP
- **최대 크기**: 10MB
- **용도**: 사용자 프로필 이미지

### 리뷰 이미지 (review_image/)
- **지원 형식**: JPG, PNG, GIF, WEBP
- **최대 크기**: 10MB
- **용도**: 리뷰 이미지

### Q&A 첨부 (qna_attach/)
- **지원 형식**: 모든 파일 형식
- **최대 크기**: 100MB
- **용도**: Q&A 첨부 파일

### 신고 첨부 (report_attach/)
- **지원 형식**: 모든 파일 형식
- **최대 크기**: 100MB
- **용도**: 신고 첨부 파일

### 시스템 문서 (system_doc/)
- **지원 형식**: 모든 파일 형식
- **최대 크기**: 100MB
- **용도**: 시스템 문서

## 장점

### 1. 체계적인 파일 관리
- 코드 테이블과 연동되어 일관성 있는 폴더 구조
- 새로운 파일 타입 추가 시 코드 테이블만 수정하면 됨

### 2. 확장성
- 새로운 파일 타입을 쉽게 추가할 수 있음
- 코드 테이블을 통한 중앙 집중식 관리

### 3. 보안 강화
- 파일 타입별로 접근 권한을 다르게 설정할 수 있음
- 민감한 파일과 일반 파일을 분리할 수 있음

### 4. 백업 효율성
- 필요한 파일 타입만 선택적으로 백업할 수 있음
- 백업 크기를 줄일 수 있음

### 5. 디스크 공간 관리
- 파일 타입별로 용량을 모니터링할 수 있음
- 용량 제한을 타입별로 설정할 수 있음

## 구현 방식

### 1. 폴더명 생성
```java
// 코드값을 소문자로 변환하여 폴더명 생성
String folderName = codeValue.toLowerCase();
```

### 2. 파일 저장 경로
```java
// 코드 ID를 기반으로 폴더명 생성
String folderName = FileUtils.generateFolderNameFromCodeId(codeId, codeSVC);
String typeSpecificPath = Paths.get(uploadPath, folderName).toString();
```

### 3. 파일 삭제
```java
// 코드 ID를 기반으로 폴더에서 파일 찾기
String folderName = FileUtils.generateFolderNameFromCodeId(codeId, codeSVC);
Path filePath = Paths.get(uploadPath, folderName, storeFilename);
```

## 설정

`application.yml`에서 파일 업로드 설정을 관리합니다:

```yaml
file:
  upload:
    path: ${user.dir}/uploads
    allowed-image-types: jpg,jpeg,png,gif,webp
    allowed-document-types: pdf,doc,docx,txt
    max-image-size: 10MB
    max-document-size: 50MB
```

## 주의사항

1. **코드 테이블 관리**: 새로운 파일 타입 추가 시 `FILE_TYPE` 그룹에 코드를 추가해야 함
2. **권한 설정**: 각 폴더에 대한 적절한 읽기/쓰기 권한을 설정해야 함
3. **백업**: 중요한 파일들은 정기적으로 백업해야 함
4. **용량 모니터링**: 각 폴더별 용량을 정기적으로 확인해야 함

## 마이그레이션

기존 파일들을 새로운 폴더 구조로 마이그레이션할 때는:

1. **코드 테이블 확인**: `FILE_TYPE` 그룹의 코드들이 올바르게 설정되어 있는지 확인
2. **파일 정보 조회**: `uploadfile` 테이블에서 각 파일의 `code` 값을 확인
3. **폴더 생성**: 각 코드값에 해당하는 폴더를 생성
4. **파일 이동**: 기존 파일들을 해당하는 폴더로 이동
