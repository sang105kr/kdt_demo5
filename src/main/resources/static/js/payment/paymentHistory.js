// 결제 내역 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('결제 내역 페이지 로드됨');
    
    // 상태 뱃지 색상 동적 적용
    applyStatusBadgeStyles();
    
    // 결제 아이템 애니메이션
    animatePaymentItems();
    
    // 영수증 보기 버튼 이벤트
    setupReceiptButtons();
    
    // 결제 금액 포맷팅
    formatPaymentAmounts();
    
    // 빈 상태 애니메이션
    animateEmptyState();
});

/**
 * 결제 상태에 따른 뱃지 스타일 적용
 */
function applyStatusBadgeStyles() {
    const statusBadges = document.querySelectorAll('.status-badge');
    
    statusBadges.forEach(badge => {
        const status = badge.textContent.trim().toLowerCase();
        
        // 기존 클래스 제거
        badge.classList.remove('completed', 'pending', 'failed', 'cancelled');
        
        // 상태에 따른 클래스 적용
        if (status.includes('완료') || status.includes('성공')) {
            badge.classList.add('completed');
        } else if (status.includes('대기') || status.includes('진행')) {
            badge.classList.add('pending');
        } else if (status.includes('실패') || status.includes('오류')) {
            badge.classList.add('failed');
        } else if (status.includes('취소') || status.includes('환불')) {
            badge.classList.add('cancelled');
        }
    });
}

/**
 * 결제 아이템 목록 애니메이션
 */
function animatePaymentItems() {
    const paymentItems = document.querySelectorAll('.payment-item');
    
    paymentItems.forEach((item, index) => {
        // 순차적으로 나타나는 애니메이션
        item.style.opacity = '0';
        item.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            item.style.transition = 'all 0.5s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateY(0)';
        }, index * 100);
    });
}

/**
 * 영수증 보기 버튼 이벤트 설정
 */
function setupReceiptButtons() {
    const receiptButtons = document.querySelectorAll('a[href*="/payment/receipt"]');
    
    receiptButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            
            const href = this.getAttribute('href');
            
            // 새 창으로 영수증 열기
            window.open(href, 'receipt', 'width=600,height=800,scrollbars=yes,resizable=yes');
        });
    });
}

/**
 * 결제 금액 포맷팅 (천단위 구분)
 */
function formatPaymentAmounts() {
    const amountElements = document.querySelectorAll('.detail-value.amount');
    
    amountElements.forEach(element => {
        const text = element.textContent;
        const numberMatch = text.match(/[\d,]+/);
        
        if (numberMatch) {
            const number = numberMatch[0].replace(/,/g, '');
            const formatted = Number(number).toLocaleString('ko-KR');
            element.textContent = text.replace(numberMatch[0], formatted);
        }
    });
}

/**
 * 빈 상태 애니메이션
 */
function animateEmptyState() {
    const emptyState = document.querySelector('.empty-state');
    
    if (emptyState) {
        const icon = emptyState.querySelector('.empty-icon');
        
        if (icon) {
            // 아이콘 펄스 애니메이션
            setInterval(() => {
                icon.style.transform = 'scale(1.1)';
                setTimeout(() => {
                    icon.style.transform = 'scale(1)';
                }, 300);
            }, 2000);
        }
    }
}

// 페이지 떠날 때 스크롤 위치 저장
window.addEventListener('beforeunload', function() {
    sessionStorage.setItem('paymentHistoryScrollPosition', window.scrollY);
});

// 페이지 로드 시 스크롤 위치 복원
window.addEventListener('load', function() {
    if (sessionStorage.getItem('paymentHistoryScrollPosition')) {
        window.scrollTo(0, sessionStorage.getItem('paymentHistoryScrollPosition'));
        sessionStorage.removeItem('paymentHistoryScrollPosition');
    }
}); 