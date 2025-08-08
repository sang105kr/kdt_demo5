/**
 * 관리자 회원 수정 페이지 JavaScript
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    // CSSManager와 NotificationManager 사용
    const cssManager = window.CSSManager;
    const notify = window.notify;

    // 관리자 회원 수정 전용 스타일 생성
    cssManager.addStyle('admin-member-edit', `
        .admin-member-edit-page .member-edit-container {
            opacity: 0;
            transform: translateY(20px);
            transition: all var(--transition-normal);
        }

        .admin-member-edit-page .member-edit-container.loaded {
            opacity: 1;
            transform: translateY(0);
        }

        .admin-member-edit-page .edit-form {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-xl);
            margin-bottom: var(--space-lg);
        }

        .admin-member-edit-page .form-group {
            margin-bottom: var(--space-lg);
        }

        .admin-member-edit-page .form-label {
            display: block;
            font-weight: 600;
            color: var(--color-text);
            margin-bottom: var(--space-xs);
        }

        .admin-member-edit-page .form-input,
        .admin-member-edit-page .form-select {
            width: 100%;
            padding: var(--space-sm);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            font-size: var(--font-size-base);
            transition: border-color var(--transition-fast);
        }

        .admin-member-edit-page .form-input:focus,
        .admin-member-edit-page .form-select:focus {
            outline: none;
            border-color: var(--color-primary);
            box-shadow: 0 0 0 2px rgba(34, 40, 49, 0.1);
        }

        .admin-member-edit-page .form-input.error {
            border-color: var(--color-error);
            box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
        }

        .admin-member-edit-page .form-input.success {
            border-color: var(--color-success);
            box-shadow: 0 0 0 2px rgba(34, 40, 49, 0.1);
        }

        .admin-member-edit-page .field-error {
            color: var(--color-error);
            font-size: var(--font-size-xs);
            margin-top: var(--space-xs);
            display: block;
        }

        .admin-member-edit-page .member-info-card {
            background: var(--color-light-gray);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-md);
            margin-bottom: var(--space-lg);
        }

        .admin-member-edit-page .info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: var(--space-xs) 0;
            border-bottom: 1px solid var(--color-border);
        }

        .admin-member-edit-page .info-item:last-child {
            border-bottom: none;
        }

        .admin-member-edit-page .info-label {
            font-weight: 600;
            color: var(--color-text);
            min-width: 120px;
        }

        .admin-member-edit-page .info-value {
            color: var(--color-text-secondary);
            flex: 1;
            text-align: right;
        }

        .admin-member-edit-page .form-actions {
            display: flex;
            gap: var(--space-sm);
            justify-content: flex-end;
            margin-top: var(--space-xl);
        }

        .admin-member-edit-page .btn--save {
            background: var(--color-primary);
            color: var(--color-white);
        }

        .admin-member-edit-page .btn--cancel {
            background: var(--color-secondary);
            color: var(--color-white);
        }

        .admin-member-edit-page .btn--delete {
            background: var(--color-error);
            color: var(--color-white);
        }

        .admin-member-edit-page .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: var(--z-modal);
        }

        .admin-member-edit-page .loading-spinner {
            width: 40px;
            height: 40px;
            border: 4px solid var(--color-border);
            border-top: 4px solid var(--color-primary);
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    `);

    /**
     * 초기화
     */
    function init() {
        initializeFormEvents();
        animatePageLoad();
    }

    /**
     * 폼 이벤트 초기화
     */
    function initializeFormEvents() {
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
    }

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
        
        if (!isValid) {
            notify.warning('입력 오류가 있습니다. 확인해주세요.', '유효성 검사');
        }
        
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
        errorDiv.textContent = message;
        
        field.parentNode.appendChild(errorDiv);
        field.classList.add('error');
    }

    /**
     * 필드 오류 제거
     */
    function clearFieldError(field) {
        const errorDiv = field.parentNode.querySelector('.field-error');
        if (errorDiv) {
            errorDiv.remove();
        }
        field.classList.remove('error');
    }

    /**
     * 저장 확인 다이얼로그
     */
    function confirmSave() {
        if (!validateForm()) {
            return false;
        }
        
        if (confirm('회원 정보를 수정하시겠습니까?')) {
            notify.info('회원 정보를 수정하고 있습니다...', '처리 중');
            return true;
        }
        return false;
    }

    /**
     * 취소 확인 다이얼로그
     */
    function confirmCancel() {
        if (confirm('수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.')) {
            notify.info('수정이 취소되었습니다.', '취소');
            return true;
        }
        return false;
    }

    /**
     * 회원 삭제 확인
     */
    function confirmDelete() {
        if (confirm('정말로 이 회원을 삭제하시겠습니까?\n\n삭제된 회원은 복구할 수 없습니다.')) {
            notify.warning('회원 삭제를 진행합니다...', '삭제 진행');
            return true;
        }
        return false;
    }

    /**
     * 페이지 로드 애니메이션
     */
    function animatePageLoad() {
        const container = document.querySelector('.member-edit-container');
        if (container) {
            setTimeout(() => {
                container.classList.add('loaded');
            }, 100);
        }
    }

    // 전역 함수로 노출
    window.confirmSave = confirmSave;
    window.confirmCancel = confirmCancel;
    window.confirmDelete = confirmDelete;

    // 초기화 실행
    init();
}); 