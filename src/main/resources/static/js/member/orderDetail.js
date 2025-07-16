// 주문 상세 조회 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('주문 상세 조회 페이지 로드됨');
    
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
    
    // 주문 상태별 색상 적용
    applyOrderStatusColors();
    
    // 주문 정보 복사 기능 (주문번호)
    const orderNumber = document.querySelector('.order-info-grid .info-item span');
    if (orderNumber) {
        orderNumber.style.cursor = 'pointer';
        orderNumber.title = '클릭하여 주문번호 복사';
        orderNumber.addEventListener('click', function() {
            copyToClipboard(this.textContent);
            showCopyMessage('주문번호가 복사되었습니다.');
        });
    }
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

// 클립보드 복사 함수
function copyToClipboard(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
            console.log('클립보드에 복사됨:', text);
        }).catch(err => {
            console.error('클립보드 복사 실패:', err);
            fallbackCopyToClipboard(text);
        });
    } else {
        fallbackCopyToClipboard(text);
    }
}

// 폴백 클립보드 복사 함수
function fallbackCopyToClipboard(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
        document.execCommand('copy');
        console.log('폴백 방식으로 클립보드에 복사됨:', text);
    } catch (err) {
        console.error('폴백 클립보드 복사 실패:', err);
    }
    
    document.body.removeChild(textArea);
}

// 복사 완료 메시지 표시
function showCopyMessage(message) {
    const notification = document.createElement('div');
    notification.className = 'copy-notification';
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #333;
        color: white;
        padding: 10px 15px;
        border-radius: 4px;
        z-index: 1000;
        font-size: 14px;
        opacity: 0;
        transition: opacity 0.3s ease;
    `;
    
    document.body.appendChild(notification);
    
    // 애니메이션
    setTimeout(() => {
        notification.style.opacity = '1';
    }, 100);
    
    setTimeout(() => {
        notification.style.opacity = '0';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 2000);
} 