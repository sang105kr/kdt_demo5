// 이메일 인증 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
    // 인증 코드 입력 필드 자동 포커스
    const verificationCodeInput = document.getElementById('verificationCode');
    if (verificationCodeInput) {
        verificationCodeInput.addEventListener('input', function(e) {
            // 숫자만 입력 허용
            this.value = this.value.replace(/[^0-9]/g, '');
            
            // 6자리 입력 시 자동으로 다음 단계로
            if (this.value.length === 6) {
                this.blur();
            }
        });
    }
    
    // 이메일 발송 폼 유효성 검사
    const emailForm = document.querySelector('.email-form');
    if (emailForm) {
        emailForm.addEventListener('submit', function(e) {
            const emailInput = this.querySelector('input[name="email"]');
            const email = emailInput.value.trim();
            
            if (!email) {
                e.preventDefault();
                showAlert('이메일을 입력해주세요.', 'error');
                emailInput.focus();
                return;
            }
            
            if (!isValidEmail(email)) {
                e.preventDefault();
                showAlert('올바른 이메일 형식이 아닙니다.', 'error');
                emailInput.focus();
                return;
            }
        });
    }
    
    // 인증 코드 확인 폼 유효성 검사
    const verificationForm = document.querySelector('.verification-form');
    if (verificationForm) {
        verificationForm.addEventListener('submit', function(e) {
            const emailInput = this.querySelector('input[name="email"]');
            const codeInput = this.querySelector('input[name="verificationCode"]');
            
            const email = emailInput.value.trim();
            const code = codeInput.value.trim();
            
            if (!email) {
                e.preventDefault();
                showAlert('이메일을 입력해주세요.', 'error');
                emailInput.focus();
                return;
            }
            
            if (!isValidEmail(email)) {
                e.preventDefault();
                showAlert('올바른 이메일 형식이 아닙니다.', 'error');
                emailInput.focus();
                return;
            }
            
            if (!code) {
                e.preventDefault();
                showAlert('인증 코드를 입력해주세요.', 'error');
                codeInput.focus();
                return;
            }
            
            if (code.length !== 6) {
                e.preventDefault();
                showAlert('인증 코드는 6자리여야 합니다.', 'error');
                codeInput.focus();
                return;
            }
            
            if (!/^\d{6}$/.test(code)) {
                e.preventDefault();
                showAlert('인증 코드는 숫자만 입력 가능합니다.', 'error');
                codeInput.focus();
                return;
            }
        });
    }
    
    // 이메일 유효성 검사 함수
    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }
    
    // 알림 메시지 표시 함수
    function showAlert(message, type) {
        // 기존 알림 제거
        const existingAlert = document.querySelector('.custom-alert');
        if (existingAlert) {
            existingAlert.remove();
        }
        
        // 새 알림 생성
        const alert = document.createElement('div');
        alert.className = `custom-alert alert-${type}`;
        alert.innerHTML = `
            <span>${message}</span>
            <button type="button" class="alert-close">&times;</button>
        `;
        
        // 스타일 적용
        alert.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 4px;
            color: white;
            font-weight: 500;
            z-index: 1000;
            display: flex;
            align-items: center;
            gap: 10px;
            max-width: 400px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        `;
        
        if (type === 'error') {
            alert.style.backgroundColor = '#dc3545';
        } else if (type === 'success') {
            alert.style.backgroundColor = '#28a745';
        } else {
            alert.style.backgroundColor = '#17a2b8';
        }
        
        // 닫기 버튼 이벤트
        const closeBtn = alert.querySelector('.alert-close');
        closeBtn.style.cssText = `
            background: none;
            border: none;
            color: white;
            font-size: 18px;
            cursor: pointer;
            padding: 0;
            margin-left: 10px;
        `;
        
        closeBtn.addEventListener('click', function() {
            alert.remove();
        });
        
        // 페이지에 추가
        document.body.appendChild(alert);
        
        // 5초 후 자동 제거
        setTimeout(() => {
            if (alert.parentNode) {
                alert.remove();
            }
        }, 5000);
    }
    
    // 인증 코드 입력 필드 자동 포커스 및 탭 이동
    const emailInput = document.querySelector('input[name="email"]');
    if (emailInput && verificationCodeInput) {
        emailInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                verificationCodeInput.focus();
            }
        });
    }
    
    // 인증 코드 입력 시 자동 제출
    if (verificationCodeInput) {
        verificationCodeInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && this.value.length === 6) {
                e.preventDefault();
                verificationForm.submit();
            }
        });
    }
}); 