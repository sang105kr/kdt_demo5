// 바로 주문 폼 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('바로 주문 폼 로드됨');
    
    // 폼 유효성 검사
    setupFormValidation();
    
    // 전화번호 포맷팅
    setupPhoneFormatting();
    
    // 메시지 자동 숨김
    setupMessageAutoHide();
    
    // 페이지 애니메이션
    animatePageContent();
});

/**
 * 폼 유효성 검사 설정
 */
function setupFormValidation() {
    const form = document.querySelector('.form');
    
    if (form) {
        form.addEventListener('submit', function(e) {
            if (!validateForm()) {
                e.preventDefault();
                return false;
            }
            
            // 제출 버튼 비활성화
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = '주문 처리 중...';
            }
        });
    }
}

/**
 * 폼 유효성 검사
 */
function validateForm() {
    let isValid = true;
    
    // 필수 필드 검사
    const requiredFields = ['paymentMethod', 'recipientName', 'recipientPhone', 'shippingAddress'];
    
    requiredFields.forEach(fieldName => {
        const field = document.querySelector(`[name="${fieldName}"]`);
        if (field && !field.value.trim()) {
            showFieldError(field, '이 필드는 필수입니다.');
            isValid = false;
        } else if (field) {
            clearFieldError(field);
        }
    });
    
    // 전화번호 형식 검사
    const phoneField = document.querySelector('[name="recipientPhone"]');
    if (phoneField && phoneField.value.trim()) {
        const phonePattern = /^01[0-9]-[0-9]{3,4}-[0-9]{4}$/;
        if (!phonePattern.test(phoneField.value.trim())) {
            showFieldError(phoneField, '올바른 전화번호 형식을 입력하세요. (예: 010-1234-5678)');
            isValid = false;
        }
    }
    
    // 배송주소 길이 검사
    const addressField = document.querySelector('[name="shippingAddress"]');
    if (addressField && addressField.value.trim().length < 10) {
        showFieldError(addressField, '배송주소는 10자 이상 입력해주세요.');
        isValid = false;
    }
    
    return isValid;
}

/**
 * 필드 에러 표시
 */
function showFieldError(field, message) {
    field.classList.add('error');
    
    // 기존 에러 메시지 제거
    const existingError = field.parentNode.querySelector('.error-message');
    if (existingError) {
        existingError.remove();
    }
    
    // 새 에러 메시지 추가
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    field.parentNode.appendChild(errorDiv);
}

/**
 * 필드 에러 제거
 */
function clearFieldError(field) {
    field.classList.remove('error');
    
    const errorDiv = field.parentNode.querySelector('.error-message');
    if (errorDiv) {
        errorDiv.remove();
    }
}

/**
 * 전화번호 포맷팅 설정
 */
function setupPhoneFormatting() {
    const phoneField = document.querySelector('[name="recipientPhone"]');
    
    if (phoneField) {
        phoneField.addEventListener('input', function(e) {
            let value = e.target.value.replace(/[^0-9]/g, '');
            
            if (value.length >= 3 && value.length <= 7) {
                value = value.replace(/(\d{3})(\d{0,4})/, '$1-$2');
            } else if (value.length >= 8) {
                value = value.replace(/(\d{3})(\d{3,4})(\d{0,4})/, '$1-$2-$3');
            }
            
            e.target.value = value;
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
    const sections = document.querySelectorAll('.product-info, .order-form');
    
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
 * 실시간 총액 계산
 */
function updateTotalAmount() {
    const priceElement = document.querySelector('.product-meta .price');
    const quantityInput = document.querySelector('input[name="quantity"]');
    const totalElement = document.querySelector('.summary-item.total span:last-child');
    
    if (priceElement && quantityInput && totalElement) {
        const price = parseInt(priceElement.textContent.replace(/[^0-9]/g, ''));
        const quantity = parseInt(quantityInput.value) || 1;
        const total = price * quantity;
        
        totalElement.textContent = total.toLocaleString() + '원';
    }
}

/**
 * 수량 변경 이벤트
 */
function setupQuantityChange() {
    const quantityInput = document.querySelector('input[name="quantity"]');
    
    if (quantityInput) {
        quantityInput.addEventListener('change', updateTotalAmount);
        quantityInput.addEventListener('input', updateTotalAmount);
    }
}

// 페이지 로드 시 추가 기능 실행
window.addEventListener('load', function() {
    setupQuantityChange();
    updateTotalAmount();
}); 