// 비밀번호 찾기 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
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
            
            // 발송 중 버튼 비활성화
            const submitBtn = this.querySelector('button[type="submit"]');
            submitBtn.disabled = true;
            submitBtn.textContent = '발송 중...';
            
            // 3초 후 버튼 재활성화 (실제로는 서버 응답 후 처리)
            setTimeout(() => {
                submitBtn.disabled = false;
                submitBtn.textContent = '비밀번호 재설정 링크 발송';
            }, 3000);
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
    
    // 이메일 입력 필드 자동 포커스
    const emailInput = document.querySelector('input[name="email"]');
    if (emailInput) {
        emailInput.focus();
        
        // Enter 키 이벤트
        emailInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                emailForm.submit();
            }
        });
    }
    
    // 성공 메시지가 있으면 자동으로 다음 단계 안내
    const successMessage = document.querySelector('.alert-success');
    if (successMessage) {
        const message = successMessage.textContent;
        if (message.includes('발송되었습니다')) {
            // 3초 후 안내 메시지 표시
            setTimeout(() => {
                showAlert('이메일을 확인하여 비밀번호 재설정 링크를 클릭해주세요.', 'info');
            }, 3000);
        }
    }
}); 