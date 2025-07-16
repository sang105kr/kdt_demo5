// 주문 목록 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('주문 목록 페이지 로드됨');
    
    // 주문 취소 확인
    setupOrderCancellation();
    
    // 메시지 자동 숨김
    setupMessageAutoHide();
});

/**
 * 주문 취소 기능 설정
 */
function setupOrderCancellation() {
    const cancelForms = document.querySelectorAll('form[action*="/cancel"]');
    
    cancelForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const orderNumber = this.closest('.order-item').querySelector('.order-number').textContent;
            
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
    });
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
 * 주문 상태별 색상 업데이트
 */
function updateOrderStatusColors() {
    const statusBadges = document.querySelectorAll('.status-badge');
    
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
        }
    });
}

/**
 * 주문 항목 애니메이션
 */
function animateOrderItems() {
    const orderItems = document.querySelectorAll('.order-item');
    
    orderItems.forEach((item, index) => {
        item.style.opacity = '0';
        item.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            item.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateY(0)';
        }, index * 100);
    });
}

// 페이지 로드 시 애니메이션 실행
window.addEventListener('load', function() {
    animateOrderItems();
    updateOrderStatusColors();
}); 