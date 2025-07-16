// 주문 상세 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('주문 상세 페이지 로드됨');
    
    // 주문 취소 확인
    setupOrderCancellation();
    
    // 메시지 자동 숨김
    setupMessageAutoHide();
    
    // 페이지 애니메이션
    animatePageContent();
});

/**
 * 주문 취소 기능 설정
 */
function setupOrderCancellation() {
    const cancelForm = document.querySelector('.cancel-form');
    
    if (cancelForm) {
        cancelForm.addEventListener('submit', function(e) {
            const orderNumber = document.querySelector('.order-info .info-item span').textContent;
            
            if (!confirm(`주문번호 ${orderNumber}을(를) 정말로 취소하시겠습니까?\n\n취소 시 재고가 복구되며, 이 작업은 되돌릴 수 없습니다.`)) {
                e.preventDefault();
                return false;
            }
            
            // 버튼 비활성화
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = '취소 처리 중...';
            }
        });
    }
}

/**
 * 메시지 자동 숨김 설정
 */
function setupMessageAutoHide() {
    const messages = document.querySelectorAll('.message');
    
    messages.forEach(message => {
        setTimeout(() => {
            message.style.opacity = '0';
            setTimeout(() => {
                message.style.display = 'none';
            }, 300);
        }, 5000);
    });
}

/**
 * 페이지 콘텐츠 애니메이션
 */
function animatePageContent() {
    const sections = document.querySelectorAll('.order-info, .shipping-info, .order-items, .order-actions');
    
    sections.forEach((section, index) => {
        section.style.opacity = '0';
        section.style.transform = 'translateY(30px)';
        
        setTimeout(() => {
            section.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            section.style.opacity = '1';
            section.style.transform = 'translateY(0)';
        }, index * 200);
    });
}

/**
 * 주문 상태별 색상 업데이트
 */
function updateOrderStatusColors() {
    const statusBadges = document.querySelectorAll('.status-badge, .payment-badge');
    
    statusBadges.forEach(badge => {
        const status = badge.textContent.toLowerCase();
        
        // 상태별 추가 스타일 적용
        switch(status) {
            case 'pending':
                badge.style.border = '1px solid #ffc107';
                break;
            case 'processing':
                badge.style.border = '1px solid #17a2b8';
                break;
            case 'shipped':
                badge.style.border = '1px solid #28a745';
                break;
            case 'delivered':
                badge.style.border = '1px solid #20c997';
                break;
            case 'cancelled':
                badge.style.border = '1px solid #dc3545';
                break;
            case 'completed':
                badge.style.border = '1px solid #28a745';
                break;
            case 'failed':
                badge.style.border = '1px solid #dc3545';
                break;
        }
    });
}

/**
 * 테이블 행 호버 효과
 */
function setupTableHoverEffects() {
    const tableRows = document.querySelectorAll('.items-table tbody tr');
    
    tableRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.backgroundColor = '#f8f9fa';
            this.style.transform = 'scale(1.01)';
        });
        
        row.addEventListener('mouseleave', function() {
            this.style.backgroundColor = '';
            this.style.transform = 'scale(1)';
        });
    });
}

/**
 * 뒤로가기 버튼 기능
 */
function setupBackButton() {
    const backBtn = document.querySelector('.back-section .btn');
    
    if (backBtn) {
        backBtn.addEventListener('click', function(e) {
            // 브라우저 히스토리가 있으면 뒤로가기, 없으면 주문 목록으로
            if (window.history.length > 1) {
                e.preventDefault();
                window.history.back();
            }
        });
    }
}

// 페이지 로드 시 추가 기능 실행
window.addEventListener('load', function() {
    updateOrderStatusColors();
    setupTableHoverEffects();
    setupBackButton();
}); 