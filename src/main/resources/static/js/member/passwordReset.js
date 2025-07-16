// 비밀번호 재설정 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const strengthFill = document.getElementById('strengthFill');
    const strengthText = document.getElementById('strengthText');
    const resetForm = document.querySelector('.reset-form');
    const submitBtn = resetForm.querySelector('button[type="submit"]');
    
    // 비밀번호 강도 체크
    if (newPasswordInput) {
        newPasswordInput.addEventListener('input', function() {
            const password = this.value;
            const strength = checkPasswordStrength(password);
            updatePasswordStrength(strength);
            updateRequirements(password);
        });
    }
    
    // 비밀번호 확인 체크
    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', function() {
            const newPassword = newPasswordInput.value;
            const confirmPassword = this.value;
            
            if (confirmPassword && newPassword !== confirmPassword) {
                this.style.borderColor = '#dc3545';
                showPasswordMismatch();
            } else {
                this.style.borderColor = '#28a745';
                hidePasswordMismatch();
            }
        });
    }
    
    // 폼 제출 유효성 검사
    if (resetForm) {
        resetForm.addEventListener('submit', function(e) {
            const newPassword = newPasswordInput.value;
            const confirmPassword = confirmPasswordInput.value;
            
            if (!newPassword) {
                e.preventDefault();
                showAlert('새 비밀번호를 입력해주세요.', 'error');
                newPasswordInput.focus();
                return;
            }
            
            if (!isValidPassword(newPassword)) {
                e.preventDefault();
                showAlert('비밀번호 요구사항을 확인해주세요.', 'error');
                newPasswordInput.focus();
                return;
            }
            
            if (!confirmPassword) {
                e.preventDefault();
                showAlert('비밀번호 확인을 입력해주세요.', 'error');
                confirmPasswordInput.focus();
                return;
            }
            
            if (newPassword !== confirmPassword) {
                e.preventDefault();
                showAlert('비밀번호와 비밀번호 확인이 일치하지 않습니다.', 'error');
                confirmPasswordInput.focus();
                return;
            }
            
            // 제출 중 버튼 비활성화
            submitBtn.disabled = true;
            submitBtn.textContent = '처리 중...';
        });
    }
    
    // 비밀번호 강도 체크 함수
    function checkPasswordStrength(password) {
        let score = 0;
        
        // 길이 체크
        if (password.length >= 8) score += 1;
        if (password.length >= 12) score += 1;
        
        // 문자 종류 체크
        if (/[a-z]/.test(password)) score += 1;
        if (/[A-Z]/.test(password)) score += 1;
        if (/[0-9]/.test(password)) score += 1;
        if (/[!@#$%^&*]/.test(password)) score += 1;
        
        if (score <= 2) return 'weak';
        if (score <= 4) return 'fair';
        if (score <= 6) return 'good';
        return 'strong';
    }
    
    // 비밀번호 강도 표시 업데이트
    function updatePasswordStrength(strength) {
        strengthFill.className = `strength-fill ${strength}`;
        strengthText.className = `strength-text ${strength}`;
        
        const strengthMessages = {
            weak: '약함',
            fair: '보통',
            good: '좋음',
            strong: '강함'
        };
        
        strengthText.textContent = `비밀번호 강도: ${strengthMessages[strength]}`;
    }
    
    // 비밀번호 요구사항 업데이트
    function updateRequirements(password) {
        const requirements = {
            length: password.length >= 8 && password.length <= 20,
            lowercase: /[a-z]/.test(password),
            uppercase: /[A-Z]/.test(password),
            number: /[0-9]/.test(password),
            special: /[!@#$%^&*]/.test(password)
        };
        
        // 요구사항 리스트 업데이트
        const requirementItems = document.querySelectorAll('.password-requirements li');
        requirementItems.forEach((item, index) => {
            const keys = ['length', 'lowercase', 'uppercase', 'number', 'special'];
            const key = keys[index];
            
            if (key && requirements[key] !== undefined) {
                if (requirements[key]) {
                    item.classList.add('valid');
                } else {
                    item.classList.remove('valid');
                }
            }
        });
    }
    
    // 비밀번호 유효성 검사
    function isValidPassword(password) {
        return password.length >= 8 && 
               password.length <= 20 && 
               /[a-z]/.test(password) && 
               /[A-Z]/.test(password) && 
               /[0-9]/.test(password) && 
               /[!@#$%^&*]/.test(password);
    }
    
    // 비밀번호 불일치 표시
    function showPasswordMismatch() {
        let mismatchMsg = document.getElementById('passwordMismatch');
        if (!mismatchMsg) {
            mismatchMsg = document.createElement('div');
            mismatchMsg.id = 'passwordMismatch';
            mismatchMsg.style.cssText = `
                color: #dc3545;
                font-size: 0.9rem;
                margin-top: 5px;
            `;
            confirmPasswordInput.parentNode.appendChild(mismatchMsg);
        }
        mismatchMsg.textContent = '비밀번호가 일치하지 않습니다.';
    }
    
    // 비밀번호 불일치 숨김
    function hidePasswordMismatch() {
        const mismatchMsg = document.getElementById('passwordMismatch');
        if (mismatchMsg) {
            mismatchMsg.remove();
        }
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
    
    // 초기 포커스
    if (newPasswordInput) {
        newPasswordInput.focus();
    }
}); 