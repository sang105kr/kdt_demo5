# Metal Design System 가이드

## 📋 개요

Metal Design System은 검은색, 회색, 흰색 기반의 모던한 메탈 느낌 디자인 시스템입니다. CSS 프레임워크 없이 순수 CSS와 JavaScript로 구축되어 일관성 있는 UI/UX를 제공합니다.

## 🎨 디자인 토큰

### 색상 팔레트

```css
/* Primary Colors - 메탈 그레이 톤 */
--color-black: #000000;        /* 순수 검은색 */
--color-dark-gray: #1a1a1a;    /* 어두운 회색 */
--color-charcoal: #2d2d2d;     /* 숯색 */
--color-steel: #404040;        /* 강철색 */
--color-silver: #666666;       /* 은색 */
--color-light-gray: #999999;   /* 밝은 회색 */
--color-platinum: #cccccc;     /* 백금색 */
--color-white: #ffffff;        /* 순수 흰색 */

/* Accent Colors - 메탈 하이라이트 */
--color-gold: #d4af37;         /* 금색 */
--color-bronze: #cd7f32;       /* 청동색 */
--color-copper: #b87333;       /* 구리색 */
--color-chrome: #e8e8e8;       /* 크롬색 */
```

### 타이포그래피

```css
/* 폰트 패밀리 */
--font-family-primary: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
--font-family-mono: 'Courier New', Courier, monospace;

/* 폰트 크기 */
--font-size-xs: 0.75rem;    /* 12px */
--font-size-sm: 0.875rem;   /* 14px */
--font-size-base: 1rem;     /* 16px */
--font-size-lg: 1.125rem;   /* 18px */
--font-size-xl: 1.25rem;    /* 20px */
--font-size-2xl: 1.5rem;    /* 24px */
--font-size-3xl: 1.875rem;  /* 30px */
--font-size-4xl: 2.25rem;   /* 36px */
```

### 간격 (Spacing)

```css
--spacing-xs: 0.25rem;   /* 4px */
--spacing-sm: 0.5rem;    /* 8px */
--spacing-md: 1rem;      /* 16px */
--spacing-lg: 1.5rem;    /* 24px */
--spacing-xl: 2rem;      /* 32px */
--spacing-2xl: 3rem;     /* 48px */
--spacing-3xl: 4rem;     /* 64px */
```

## 🧩 컴포넌트

### 버튼 (Buttons)

```html
<!-- 기본 버튼 -->
<button class="btn btn-primary">기본 버튼</button>
<button class="btn btn-secondary">보조 버튼</button>
<button class="btn btn-success">성공 버튼</button>
<button class="btn btn-warning">경고 버튼</button>
<button class="btn btn-danger">위험 버튼</button>
<button class="btn btn-outline">아웃라인 버튼</button>

<!-- 버튼 크기 -->
<button class="btn btn-primary btn-sm">작은 버튼</button>
<button class="btn btn-primary">기본 버튼</button>
<button class="btn btn-primary btn-lg">큰 버튼</button>
```

### 폼 (Forms)

```html
<div class="form-group">
    <label class="form-label" for="email">이메일</label>
    <input type="email" id="email" name="email" class="form-control" required>
    <div class="form-text">이메일 주소를 입력해주세요.</div>
</div>

<div class="form-group">
    <label class="form-label" for="password">비밀번호</label>
    <input type="password" id="password" name="password" class="form-control" required minlength="8">
    <div class="form-error">비밀번호는 8자 이상이어야 합니다.</div>
</div>
```

### 카드 (Cards)

```html
<div class="card">
    <div class="card-header">
        <h3 class="card-title">카드 제목</h3>
    </div>
    <div class="card-body">
        <p>카드 내용이 여기에 들어갑니다.</p>
    </div>
    <div class="card-footer">
        <button class="btn btn-primary">확인</button>
    </div>
</div>
```

### 알림 (Alerts)

```html
<div class="alert alert-success">
    <i class="fas fa-check-circle mr-2"></i>
    성공적으로 처리되었습니다.
</div>

<div class="alert alert-warning">
    <i class="fas fa-exclamation-triangle mr-2"></i>
    주의사항을 확인해주세요.
</div>

<div class="alert alert-danger">
    <i class="fas fa-times-circle mr-2"></i>
    오류가 발생했습니다.
</div>

<div class="alert alert-info">
    <i class="fas fa-info-circle mr-2"></i>
    정보를 확인해주세요.
</div>
```

### 배지 (Badges)

```html
<span class="badge badge-primary">기본</span>
<span class="badge badge-secondary">보조</span>
<span class="badge badge-success">성공</span>
<span class="badge badge-warning">경고</span>
<span class="badge badge-danger">위험</span>
```

### 테이블 (Tables)

```html
<table class="table">
    <thead>
        <tr>
            <th>이름</th>
            <th>이메일</th>
            <th>역할</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>홍길동</td>
            <td>hong@example.com</td>
            <td>관리자</td>
        </tr>
        <tr>
            <td>김철수</td>
            <td>kim@example.com</td>
            <td>사용자</td>
        </tr>
    </tbody>
</table>
```

### 모달 (Modals)

```html
<!-- 모달 트리거 버튼 -->
<button class="btn btn-primary" data-modal='{"title": "확인", "content": "정말 삭제하시겠습니까?", "onConfirm": "deleteItem()"}'>
    삭제
</button>

<!-- JavaScript로 모달 생성 -->
<script>
const modal = MetalUI.createModal({
    title: '사용자 정보',
    content: '<p>사용자 정보를 확인해주세요.</p>',
    onConfirm: () => {
        console.log('확인됨');
    },
    onClose: () => {
        console.log('취소됨');
    }
});

MetalUI.showModal(modal);
</script>
```

### 로딩 스피너 (Loading Spinner)

```html
<div class="spinner"></div>
<div class="spinner spinner-sm"></div>
<div class="spinner spinner-lg"></div>
```

### 툴팁 (Tooltips)

```html
<div class="tooltip" data-tooltip="이것은 툴팁입니다">
    마우스를 올려보세요
</div>
```

### 프로그레스 바 (Progress Bars)

```html
<div class="progress">
    <div class="progress-bar" style="width: 75%"></div>
</div>
```

## 🎯 유틸리티 클래스

### 텍스트 유틸리티

```html
<p class="text-xs">매우 작은 텍스트</p>
<p class="text-sm">작은 텍스트</p>
<p class="text-base">기본 텍스트</p>
<p class="text-lg">큰 텍스트</p>
<p class="text-xl">매우 큰 텍스트</p>

<p class="font-light">얇은 폰트</p>
<p class="font-normal">기본 폰트</p>
<p class="font-medium">중간 폰트</p>
<p class="font-semibold">반굵은 폰트</p>
<p class="font-bold">굵은 폰트</p>

<p class="text-center">가운데 정렬</p>
<p class="text-left">왼쪽 정렬</p>
<p class="text-right">오른쪽 정렬</p>
```

### 색상 유틸리티

```html
<p class="text-black">검은색 텍스트</p>
<p class="text-white">흰색 텍스트</p>
<p class="text-silver">은색 텍스트</p>
<p class="text-gold">금색 텍스트</p>

<div class="bg-black">검은색 배경</div>
<div class="bg-white">흰색 배경</div>
<div class="bg-charcoal">숯색 배경</div>
<div class="bg-steel">강철색 배경</div>
```

### 간격 유틸리티

```html
<div class="m-0">마진 0</div>
<div class="m-1">마진 4px</div>
<div class="m-2">마진 8px</div>
<div class="m-3">마진 16px</div>
<div class="m-4">마진 24px</div>
<div class="m-5">마진 32px</div>

<div class="p-0">패딩 0</div>
<div class="p-1">패딩 4px</div>
<div class="p-2">패딩 8px</div>
<div class="p-3">패딩 16px</div>
<div class="p-4">패딩 24px</div>
<div class="p-5">패딩 32px</div>
```

### 플렉스박스 유틸리티

```html
<div class="flex">
    <div>아이템 1</div>
    <div>아이템 2</div>
</div>

<div class="flex flex-col">
    <div>세로 아이템 1</div>
    <div>세로 아이템 2</div>
</div>

<div class="flex items-center justify-center">
    <div>가운데 정렬</div>
</div>

<div class="flex justify-between">
    <div>왼쪽</div>
    <div>오른쪽</div>
</div>
```

### 그리드 유틸리티

```html
<div class="grid grid-cols-1 gap-3">
    <div>1열 그리드</div>
</div>

<div class="grid grid-cols-2 gap-3">
    <div>2열 그리드 1</div>
    <div>2열 그리드 2</div>
</div>

<div class="grid grid-cols-3 gap-3">
    <div>3열 그리드 1</div>
    <div>3열 그리드 2</div>
    <div>3열 그리드 3</div>
</div>
```

## 🔧 JavaScript API

### MetalUI 객체

```javascript
// DOM 요소 선택
const element = MetalUI.$('.my-class');
const elements = MetalUI.$$('.my-class');

// 애니메이션
MetalUI.fadeIn(element, 300);
MetalUI.fadeOut(element, 300);
MetalUI.slideDown(element, 300);
MetalUI.slideUp(element, 300);

// 토스트 알림
MetalUI.showToast('성공했습니다!', 'success', 3000);
MetalUI.showToast('오류가 발생했습니다.', 'danger', 5000);

// 폼 검증
const form = document.querySelector('form');
const { isValid, errors } = MetalUI.validateForm(form);

// AJAX 요청
const response = await MetalUI.fetch('/api/data', {
    method: 'POST',
    body: JSON.stringify(data)
});

// 로컬 스토리지
MetalUI.setStorage('key', value);
const value = MetalUI.getStorage('key', defaultValue);
MetalUI.removeStorage('key');

// 유틸리티 함수
const debouncedFn = MetalUI.debounce(myFunction, 300);
const throttledFn = MetalUI.throttle(myFunction, 1000);

// 이벤트 위임
MetalUI.on('click', '.btn', function(e) {
    console.log('버튼 클릭됨');
});
```

## 📱 반응형 디자인

### 브레이크포인트

```css
/* 모바일: 640px 이하 */
@media (max-width: 640px) {
    .sm\:hidden { display: none; }
    .sm\:block { display: block; }
}

/* 태블릿: 768px 이하 */
@media (max-width: 768px) {
    .md\:hidden { display: none; }
    .md\:flex { display: flex; }
}

/* 데스크톱: 1024px 이하 */
@media (max-width: 1024px) {
    .lg\:hidden { display: none; }
    .lg\:grid { display: grid; }
}
```

### 반응형 예제

```html
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
    <div class="card">카드 1</div>
    <div class="card">카드 2</div>
    <div class="card">카드 3</div>
</div>

<div class="hidden md:block">데스크톱에서만 보임</div>
<div class="block md:hidden">모바일에서만 보임</div>
```

## 🎨 커스터마이징

### CSS 변수 재정의

```css
:root {
    /* 색상 커스터마이징 */
    --color-primary: #your-color;
    --color-secondary: #your-color;
    
    /* 폰트 커스터마이징 */
    --font-family-primary: 'Your Font', sans-serif;
    
    /* 간격 커스터마이징 */
    --spacing-md: 1.5rem;
}
```

### 컴포넌트 확장

```css
/* 커스텀 버튼 스타일 */
.btn-custom {
    background: linear-gradient(135deg, var(--color-gold), var(--color-bronze));
    border: 2px solid var(--color-gold);
    box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3);
}

.btn-custom:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(212, 175, 55, 0.4);
}
```

## 📋 베스트 프랙티스

### 1. 일관성 유지
- 항상 디자인 토큰을 사용하세요
- 커스텀 스타일보다는 유틸리티 클래스를 우선 사용하세요
- 색상은 CSS 변수를 통해 관리하세요

### 2. 접근성 고려
- 충분한 색상 대비를 유지하세요
- 키보드 네비게이션을 지원하세요
- 스크린 리더를 위한 적절한 라벨을 사용하세요

### 3. 성능 최적화
- 불필요한 CSS를 제거하세요
- 이미지 최적화를 수행하세요
- JavaScript 번들 크기를 최소화하세요

### 4. 반응형 디자인
- 모바일 우선 접근법을 사용하세요
- 터치 친화적인 인터페이스를 설계하세요
- 다양한 화면 크기를 테스트하세요

## 🚀 시작하기

### 1. CSS 파일 포함

```html
<link rel="stylesheet" href="/css/design-system.css">
<link rel="stylesheet" href="/css/components.css">
```

### 2. JavaScript 파일 포함

```html
<script src="/js/design-system.js"></script>
```

### 3. 기본 템플릿 사용

```html
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Metal UI</title>
    <link rel="stylesheet" href="/css/design-system.css">
    <link rel="stylesheet" href="/css/components.css">
</head>
<body>
    <!-- 콘텐츠 -->
    <script src="/js/design-system.js"></script>
</body>
</html>
```

## 📚 추가 리소스

- [CSS 변수 가이드](https://developer.mozilla.org/en-US/docs/Web/CSS/Using_CSS_custom_properties)
- [Flexbox 가이드](https://css-tricks.com/snippets/css/a-guide-to-flexbox/)
- [Grid 가이드](https://css-tricks.com/snippets/css/complete-guide-grid/)
- [접근성 가이드](https://www.w3.org/WAI/WCAG21/quickref/)

---

**Metal Design System** - 메탈 느낌의 모던한 웹 경험을 제공합니다. 