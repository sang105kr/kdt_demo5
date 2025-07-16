/**
 * 관리자 주문 상세 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자 주문 상세 페이지 로드됨');
    
    // 메시지 자동 숨김
    autoHideMessages();
    
    // 폼 유효성 검사
    initFormValidation();
    
    // 상태 변경 확인
    initStatusChangeConfirmation();
    
    // 주문 취소 확인
    initCancelConfirmation();
});

/**
 * 메시지 자동 숨김
 */
function autoHideMessages() {
    const messages = document.querySelectorAll('.message');
    messages.forEach(message => {
        setTimeout(() => {
            message.style.opacity = '0';
            setTimeout(() => {
                message.style.display = 'none';
            }, 300);
        }, 3000);
    });
}

/**
 * 폼 유효성 검사 초기화
 */
function initFormValidation() {
    const statusForm = document.querySelector('.status-form');
    const paymentForm = document.querySelector('.payment-form');
    
    if (statusForm) {
        statusForm.addEventListener('submit', function(e) {
            const select = this.querySelector('select[name="orderStatus"]');
            if (!select.value) {
                e.preventDefault();
                alert('주문 상태를 선택해주세요.');
                return false;
            }
            
            // 상태 변경 확인
            const currentStatus = select.getAttribute('data-current-status');
            const newStatus = select.value;
            
            if (currentStatus && currentStatus !== newStatus) {
                if (!confirm(`주문 상태를 "${getStatusText(currentStatus)}"에서 "${getStatusText(newStatus)}"로 변경하시겠습니까?`)) {
                    e.preventDefault();
                    return false;
                }
            }
        });
    }
    
    if (paymentForm) {
        paymentForm.addEventListener('submit', function(e) {
            const select = this.querySelector('select[name="paymentStatus"]');
            if (!select.value) {
                e.preventDefault();
                alert('결제 상태를 선택해주세요.');
                return false;
            }
            
            // 결제 상태 변경 확인
            const currentStatus = select.getAttribute('data-current-status');
            const newStatus = select.value;
            
            if (currentStatus && currentStatus !== newStatus) {
                if (!confirm(`결제 상태를 "${getPaymentStatusText(currentStatus)}"에서 "${getPaymentStatusText(newStatus)}"로 변경하시겠습니까?`)) {
                    e.preventDefault();
                    return false;
                }
            }
        });
    }
}

/**
 * 상태 변경 확인 초기화
 */
function initStatusChangeConfirmation() {
    const statusSelect = document.querySelector('select[name="orderStatus"]');
    if (statusSelect) {
        // 현재 상태 저장
        statusSelect.setAttribute('data-current-status', statusSelect.value);
        
        statusSelect.addEventListener('change', function() {
            const currentStatus = this.getAttribute('data-current-status');
            const newStatus = this.value;
            
            if (currentStatus && currentStatus !== newStatus) {
                const confirmed = confirm(`주문 상태를 "${getStatusText(currentStatus)}"에서 "${getStatusText(newStatus)}"로 변경하시겠습니까?`);
                if (!confirmed) {
                    this.value = currentStatus;
                }
            }
        });
    }
    
    const paymentSelect = document.querySelector('select[name="paymentStatus"]');
    if (paymentSelect) {
        // 현재 상태 저장
        paymentSelect.setAttribute('data-current-status', paymentSelect.value);
        
        paymentSelect.addEventListener('change', function() {
            const currentStatus = this.getAttribute('data-current-status');
            const newStatus = this.value;
            
            if (currentStatus && currentStatus !== newStatus) {
                const confirmed = confirm(`결제 상태를 "${getPaymentStatusText(currentStatus)}"에서 "${getPaymentStatusText(newStatus)}"로 변경하시겠습니까?`);
                if (!confirmed) {
                    this.value = currentStatus;
                }
            }
        });
    }
}

/**
 * 주문 취소 확인 초기화
 */
function initCancelConfirmation() {
    const cancelForm = document.querySelector('.cancel-form');
    if (cancelForm) {
        cancelForm.addEventListener('submit', function(e) {
            const confirmed = confirm('정말로 이 주문을 취소하시겠습니까?\n\n취소 시 재고가 복구되며, 이 작업은 되돌릴 수 없습니다.');
            if (!confirmed) {
                e.preventDefault();
                return false;
            }
        });
    }
}

/**
 * 주문 상태 텍스트 반환
 */
function getStatusText(status) {
    const statusMap = {
        'PENDING': '주문대기',
        'CONFIRMED': '주문확정',
        'SHIPPED': '배송중',
        'DELIVERED': '배송완료',
        'CANCELLED': '주문취소'
    };
    return statusMap[status] || status;
}

/**
 * 결제 상태 텍스트 반환
 */
function getPaymentStatusText(status) {
    const statusMap = {
        'PENDING': '결제대기',
        'COMPLETED': '결제완료',
        'FAILED': '결제실패',
        'REFUNDED': '환불완료'
    };
    return statusMap[status] || status;
}

/**
 * 주문 정보 복사
 */
function copyOrderInfo() {
    const orderNumber = document.querySelector('.order-info .info-item span').textContent;
    const recipientName = document.querySelector('.shipping-info .info-item span').textContent;
    const shippingAddress = document.querySelector('.shipping-info .info-item.full-width span').textContent;
    
    const orderInfo = `주문번호: ${orderNumber}\n수령인: ${recipientName}\n배송주소: ${shippingAddress}`;
    
    navigator.clipboard.writeText(orderInfo).then(() => {
        showToast('주문 정보가 클립보드에 복사되었습니다.');
    }).catch(() => {
        showToast('복사에 실패했습니다.');
    });
}

/**
 * 토스트 메시지 표시
 */
function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #333;
        color: white;
        padding: 12px 20px;
        border-radius: 4px;
        z-index: 1000;
        opacity: 0;
        transition: opacity 0.3s ease;
    `;
    
    document.body.appendChild(toast);
    
    // 표시
    setTimeout(() => {
        toast.style.opacity = '1';
    }, 100);
    
    // 숨김
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => {
            document.body.removeChild(toast);
        }, 300);
    }, 3000);
}

/**
 * 주문 통계 정보 표시 (선택적)
 */
function showOrderStatistics() {
    const orderItems = document.querySelectorAll('.items-table tbody tr');
    let totalItems = 0;
    let totalAmount = 0;
    
    orderItems.forEach(item => {
        const quantity = parseInt(item.cells[2].textContent);
        const subtotal = parseInt(item.cells[3].textContent.replace(/[^\d]/g, ''));
        
        totalItems += quantity;
        totalAmount += subtotal;
    });
    
    console.log('주문 통계:', {
        totalItems: totalItems,
        totalAmount: totalAmount,
        averagePrice: totalAmount / totalItems
    });
}

/**
 * 페이지 인쇄
 */
function printOrder() {
    window.print();
}

// 전역 함수로 노출 (HTML에서 호출 가능)
window.copyOrderInfo = copyOrderInfo;
window.printOrder = printOrder; 