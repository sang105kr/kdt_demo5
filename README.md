# KDT Demo 프로젝트 구조 및 아키텍처 문서

이 문서는 **Mermaid**와 **PlantUML** 두 가지 다이어그램 문법을 활용해
프로젝트의 전체 구조(아키텍처, ERD, 주요 프로세스)를 비교 설명합니다.

---

## 1. 시스템 아키텍처 (컴포넌트/레이어)

### Mermaid
```mermaid
flowchart TD
  Client["브라우저/클라이언트"] -->|HTTP| Web["Spring Boot Web (Controller)"]
  Web --> Service["Service Layer"]
  Service --> DAO["DAO/Repository"]
  DAO --> DB[("Oracle DB")]
  Service --> ES[("Elasticsearch")]
  Web --> Thymeleaf["Thymeleaf View"]
  Web --> API["REST API"]
```

### PlantUML
```plantuml
@startuml
actor Client
rectangle "Web (Controller)" as Web
rectangle "Service Layer" as Service
rectangle "DAO/Repository" as DAO
database "Oracle DB" as DB
rectangle "Elasticsearch" as ES
rectangle "Thymeleaf View" as Thymeleaf
rectangle "REST API" as API
Client --> Web : HTTP
Web --> Service
Service --> DAO
DAO --> DB
Service --> ES
Web --> Thymeleaf
Web --> API
@enduml
```

---

## 2. ERD (주요 테이블 관계)

### Mermaid
```mermaid
erDiagram
  MEMBER ||--o{ BOARD : "작성"
  BOARD ||--o{ REPLY : "댓글"
  MEMBER ||--o{ ORDER : "주문"
  ORDER ||--|{ ORDER_ITEM : "구성"
  MEMBER ||--o{ REVIEW : "리뷰"
  PRODUCT ||--o{ ORDER_ITEM : "주문상품"
  PRODUCT ||--o{ REVIEW : "리뷰"
```

### PlantUML
```plantuml
@startuml
entity MEMBER {
  member_id : int
  email : varchar
}
entity BOARD {
  board_id : int
  title : varchar
}
entity REPLY {
  reply_id : int
  board_id : int
}
entity ORDER {
  order_id : int
  member_id : int
}
entity ORDER_ITEM {
  order_item_id : int
  order_id : int
  product_id : int
}
entity PRODUCT {
  product_id : int
  pname : varchar
}
entity REVIEW {
  review_id : int
  member_id : int
  product_id : int
}
MEMBER ||--o{ BOARD : "작성"
BOARD ||--o{ REPLY : "댓글"
MEMBER ||--o{ ORDER : "주문"
ORDER ||--|{ ORDER_ITEM : "구성"
MEMBER ||--o{ REVIEW : "리뷰"
PRODUCT ||--o{ ORDER_ITEM : "주문상품"
PRODUCT ||--o{ REVIEW : "리뷰"
@enduml
```

---

## 3. 주요 프로세스 (회원가입/게시글/댓글)

### Mermaid (회원가입 시퀀스)
```mermaid
sequenceDiagram
  participant User
  participant Web as WebController
  participant Service
  participant DAO
  participant DB
  User->>Web: 회원가입 폼 제출
  Web->>Service: 회원가입 요청
  Service->>DAO: 회원 정보 저장
  DAO->>DB: INSERT member
  DB-->>DAO: 저장 결과
  DAO-->>Service: 저장 결과
  Service-->>Web: 처리 결과
  Web-->>User: 가입 완료 응답
```

### PlantUML (회원가입 시퀀스)
```plantuml
@startuml
actor User
participant WebController as Web
participant Service
participant DAO
database DB
User -> Web : 회원가입 폼 제출
Web -> Service : 회원가입 요청
Service -> DAO : 회원 정보 저장
DAO -> DB : INSERT member
DB --> DAO : 저장 결과
DAO --> Service : 저장 결과
Service --> Web : 처리 결과
Web --> User : 가입 완료 응답
@enduml
```

---

## 4. Mermaid vs PlantUML 용도/차이
- **Mermaid**: 마크다운 친화, 빠른 시각화, GitHub/Notion 등에서 바로 지원, 간단한 구조/플로우에 적합
- **PlantUML**: UML 표준, 복잡한 설계, 다양한 다이어그램, 별도 렌더러 필요, 복잡한 시스템 설계에 적합

---

> 위 다이어그램들은 실제 코드/DB 구조와 100% 일치하지 않을 수 있습니다. 
> 상세한 구조/관계/프로세스는 실제 소스와 DB를 참고하세요. 