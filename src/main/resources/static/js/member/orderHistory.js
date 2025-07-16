// 주문 내역 조회 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('주문 내역 조회 페이지 로드됨');
    
    // 메시지 자동 숨김
    const messages = document.querySelectorAll('.message');
    messages.forEach(message => {
        setTimeout(() => {
            message.style.opacity = '0';
            setTimeout(() => {
                message.style.display = 'none';
            }, 300);
        }, 3000);
    });
    
    // 주문 항목 클릭 이벤트 (상세보기 버튼 외 영역 클릭 시)
    const orderItems = document.querySelectorAll('.order-item');
    orderItems.forEach(item => {
        item.addEventListener('click', function(e) {
            // 상세보기 버튼 클릭이 아닌 경우에만 처리
            if (!e.target.closest('.order-actions')) {
                const detailLink = this.querySelector('.order-actions a');
                if (detailLink) {
                    detailLink.click();
                }
            }
        });
    });
    
    // 주문 상태별 색상 적용
    applyOrderStatusColors();
});

// 주문 상태별 색상 적용 함수
function applyOrderStatusColors() {
    const statusBadges = document.querySelectorAll('.status-badge');
    statusBadges.forEach(badge => {
        const status = badge.textContent.toLowerCase();
        badge.className = `status-badge status-${status}`;
    });
    
    const paymentStatuses = document.querySelectorAll('.payment-status');
    paymentStatuses.forEach(status => {
        const paymentStatus = status.textContent.toLowerCase();
        status.className = `payment-status payment-${paymentStatus}`;
    });
} 