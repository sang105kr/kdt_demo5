/**
 * Admin Member Edit Page JavaScript
 * 회원 정보 수정 페이지의 클라이언트 사이드 기능
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Member Edit Page loaded');
    
    // 폼 유효성 검사
    const editForm = document.querySelector('.edit-form');
    if (editForm) {
        editForm.addEventListener('submit', function(e) {
            if (!validateForm()) {
                e.preventDefault();
                return false;
            }
        });
    }
    
    // 입력 필드 실시간 유효성 검사
    const inputs = document.querySelectorAll('.form-input');
    inputs.forEach(input => {
        input.addEventListener('blur', function() {
            validateField(this);
        });
        
        input.addEventListener('input', function() {
            clearFieldError(this);
        });
    });
});

/**
 * 폼 전체 유효성 검사
 */
function validateForm() {
    let isValid = true;
    const requiredFields = document.querySelectorAll('[required]');
    
    requiredFields.forEach(field => {
        if (!validateField(field)) {
            isValid = false;
        }
    });
    
    return isValid;
}

/**
 * 개별 필드 유효성 검사
 */
function validateField(field) {
    const value = field.value.trim();
    const fieldName = field.name;
    
    // 필수 필드 검사
    if (field.hasAttribute('required') && !value) {
        showFieldError(field, '이 필드는 필수입니다.');
        return false;
    }
    
    // 필드별 특정 검사
    switch (fieldName) {
        case 'nickname':
            if (value && value.length < 2) {
                showFieldError(field, '닉네임은 2자 이상이어야 합니다.');
                return false;
            }
            if (value && value.length > 20) {
                showFieldError(field, '닉네임은 20자 이하여야 합니다.');
                return false;
            }
            break;
            
        case 'tel':
            if (value && !/^[0-9-]+$/.test(value)) {
                showFieldError(field, '올바른 전화번호 형식을 입력해주세요.');
                return false;
            }
            break;
            
        case 'hobby':
            if (value && value.length > 100) {
                showFieldError(field, '취미는 100자 이하여야 합니다.');
                return false;
            }
            break;
    }
    
    clearFieldError(field);
    return true;
}

/**
 * 필드 오류 표시
 */
function showFieldError(field, message) {
    clearFieldError(field);
    
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error';
    errorDiv.style.color = '#dc3545';
    errorDiv.style.fontSize = '0.75rem';
    errorDiv.style.marginTop = '4px';
    errorDiv.textContent = message;
    
    field.parentNode.appendChild(errorDiv);
    field.style.borderColor = '#dc3545';
}

/**
 * 필드 오류 제거
 */
function clearFieldError(field) {
    const errorDiv = field.parentNode.querySelector('.field-error');
    if (errorDiv) {
        errorDiv.remove();
    }
    field.style.borderColor = '';
}

/**
 * 저장 확인 다이얼로그
 */
function confirmSave() {
    return confirm('회원 정보를 수정하시겠습니까?');
}

/**
 * 취소 확인 다이얼로그
 */
function confirmCancel() {
    return confirm('수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.');
}

// 전역 함수로 노출 (HTML에서 직접 호출 가능)
window.confirmSave = confirmSave;
window.confirmCancel = confirmCancel; 