// 아이디 찾기 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
    // 전화번호 입력 필드 자동 포맷팅
    const telInput = document.getElementById('tel');
    if (telInput) {
        telInput.addEventListener('input', function(e) {
            let value = this.value.replace(/[^0-9]/g, '');
            
            // 전화번호 형식 적용 (010-1234-5678)
            if (value.length >= 3 && value.length <= 7) {
                value = value.replace(/(\d{3})(\d{0,4})/, '$1-$2');
            } else if (value.length >= 8) {
                value = value.replace(/(\d{3})(\d{4})(\d{0,4})/, '$1-$2-$3');
            }
            
            this.value = value;
        });
    }
    
    // 아이디 찾기 폼 유효성 검사
    const findIdForm = document.querySelector('.find-id-form');
    if (findIdForm) {
        findIdForm.addEventListener('submit', function(e) {
            const telInput = this.querySelector('input[name="tel"]');
            const birthDateInput = this.querySelector('input[name="birthDate"]');
            
            const tel = telInput.value.trim();
            const birthDate = birthDateInput.value.trim();
            
            if (!tel) {
                e.preventDefault();
                showAlert('전화번호를 입력해주세요.', 'error');
                telInput.focus();
                return;
            }
            
            if (!/^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$/.test(tel)) {
                e.preventDefault();
                showAlert('올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)', 'error');
                telInput.focus();
                return;
            }
            
            if (!birthDate) {
                e.preventDefault();
                showAlert('생년월일을 입력해주세요.', 'error');
                birthDateInput.focus();
                return;
            }
            
            // 제출 중 버튼 비활성화
            const submitBtn = this.querySelector('button[type="submit"]');
            submitBtn.disabled = true;
            submitBtn.textContent = '검색 중...';
            
            // 3초 후 버튼 재활성화 (실제로는 서버 응답 후 처리)
            setTimeout(() => {
                submitBtn.disabled = false;
                submitBtn.textContent = '아이디 찾기';
            }, 3000);
        });
    }
    
    // 전체 이메일 보기 함수
    window.showFullEmail = function() {
        const fullEmailDiv = document.getElementById('fullEmail');
        const showButton = document.querySelector('.btn-small');
        
        if (fullEmailDiv.style.display === 'none') {
            fullEmailDiv.style.display = 'block';
            showButton.textContent = '숨기기';
        } else {
            fullEmailDiv.style.display = 'none';
            showButton.textContent = '전체 보기';
        }
    };
    
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
    if (telInput) {
        telInput.focus();
    }
    
    // Enter 키 이벤트
    const inputs = document.querySelectorAll('.find-id-form input');
    inputs.forEach((input, index) => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                if (index < inputs.length - 1) {
                    inputs[index + 1].focus();
                } else {
                    findIdForm.submit();
                }
            }
        });
    });
}); 