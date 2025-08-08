// 장바구니 주문 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('장바구니 주문 페이지 로드됨');
    
    // 폼 유효성 검사
    setupFormValidation();
    
    // 전화번호 포맷팅
    setupPhoneFormatting();
    
    // 메시지 자동 숨김
    setupMessageAutoHide();
    
    // 페이지 애니메이션
    animatePageContent();
    
    // 주소 검색 결과 처리
    setupAddressSearch();
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
    const sections = document.querySelectorAll('.order-items, .order-form');
    
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
 * 주소 검색 결과 처리 설정
 */
function setupAddressSearch() {
    // 주소 검색 결과 이벤트 리스너
    document.addEventListener('addressSelected', function(event) {
        const addressData = event.detail.addressData;
        const shippingAddressField = document.getElementById('shippingAddress');
        
        if (shippingAddressField && addressData) {
            // 주소 정보를 배송주소 필드에 설정
            let fullAddress = '';
            
            if (addressData.roadAddr) {
                fullAddress += addressData.roadAddr;
            } else if (addressData.jibunAddr) {
                fullAddress += addressData.jibunAddr;
            }
            
            if (addressData.addrDetail) {
                fullAddress += ' ' + addressData.addrDetail;
            }
            
            shippingAddressField.value = fullAddress;
            
            // 주소 필드 에러 제거
            clearFieldError(shippingAddressField);
            
            console.log('배송주소가 설정되었습니다:', fullAddress);
        }
    });
}

// 페이지 로드 시 추가 기능 실행
window.addEventListener('load', function() {
    setupTableHoverEffects();
}); 