// index.html 전용 JavaScript

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    console.log('홈페이지 로드됨');
    
    // 퀵메뉴 아이템에 클릭 이벤트 추가
    const quickMenuItems = document.querySelectorAll('.quick-menu-item');
    quickMenuItems.forEach(item => {
        item.addEventListener('click', function(e) {
            // 클릭 효과
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
    });
    
    // 로그인 상태 확인
    checkLoginStatus();
});

// 로그인 상태 확인 및 UI 업데이트
function checkLoginStatus() {
    const loginMember = document.querySelector('[data-s-nickname]');
    if (loginMember) {
        const nickname = loginMember.getAttribute('data-s-nickname');
        console.log('로그인된 사용자:', nickname);
        
        // 로그인 후 콘텐츠가 있는지 확인
        const afterLoginContent = document.querySelector('.welcome-section');
        if (afterLoginContent) {
            // 사용자 이름 강조 효과
            const userNickname = afterLoginContent.querySelector('.user-nickname');
            if (userNickname) {
                userNickname.style.animation = 'pulse 2s infinite';
            }
        }
    } else {
        console.log('로그인되지 않은 사용자');
    }
}

// 페이지 새로고침 시 스크롤 위치 복원
window.addEventListener('beforeunload', function() {
    sessionStorage.setItem('scrollPosition', window.scrollY);
});

window.addEventListener('load', function() {
    const scrollPosition = sessionStorage.getItem('scrollPosition');
    if (scrollPosition) {
        window.scrollTo(0, parseInt(scrollPosition));
        sessionStorage.removeItem('scrollPosition');
    }
});

// 스크롤 시 헤더 효과 (선택사항)
let lastScrollTop = 0;
window.addEventListener('scroll', function() {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    
    // 스크롤 방향에 따른 효과
    if (scrollTop > lastScrollTop) {
        // 아래로 스크롤
        document.body.classList.add('scrolling-down');
    } else {
        // 위로 스크롤
        document.body.classList.remove('scrolling-down');
    }
    
    lastScrollTop = scrollTop;
});

// 키보드 접근성 개선
document.addEventListener('keydown', function(e) {
    // Tab 키로 포커스 이동 시 시각적 피드백
    if (e.key === 'Tab') {
        document.body.classList.add('keyboard-navigation');
    }
});

document.addEventListener('mousedown', function() {
    document.body.classList.remove('keyboard-navigation');
});

// 성능 최적화: Intersection Observer 사용
if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
            }
        });
    }, {
        threshold: 0.1
    });
    
    // 퀵메뉴 아이템들을 관찰
    document.querySelectorAll('.quick-menu-item').forEach(item => {
        observer.observe(item);
    });
}

// 에러 처리
window.addEventListener('error', function(e) {
    console.error('홈페이지 에러:', e.error);
});

// 네트워크 상태 확인
window.addEventListener('online', function() {
    console.log('네트워크 연결됨');
});

window.addEventListener('offline', function() {
    console.log('네트워크 연결 끊어짐');
}); 