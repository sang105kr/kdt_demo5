// 비밀번호 변경 폼 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('비밀번호 변경 폼 로드됨');
    
    const form = document.querySelector('.password-form');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    
    if (form) {
        form.addEventListener('submit', function(e) {
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = newPasswordInput.value;
            const confirmPassword = confirmPasswordInput.value;
            
            // 현재 비밀번호 확인
            if (!currentPassword) {
                alert('현재 비밀번호를 입력해주세요.');
                e.preventDefault();
                return;
            }
            
            // 새 비밀번호 확인
            if (!newPassword) {
                alert('새 비밀번호를 입력해주세요.');
                e.preventDefault();
                return;
            }
            
            // 비밀번호 형식 검사
            const passwordPattern = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]+$/;
            if (!passwordPattern.test(newPassword)) {
                alert('비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.');
                e.preventDefault();
                return;
            }
            
            if (newPassword.length < 8 || newPassword.length > 20) {
                alert('비밀번호는 8~20자 사이로 입력해주세요.');
                e.preventDefault();
                return;
            }
            
            // 비밀번호 확인
            if (newPassword !== confirmPassword) {
                alert('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
                e.preventDefault();
                return;
            }
            
            // 현재 비밀번호와 새 비밀번호가 같은지 확인
            if (currentPassword === newPassword) {
                alert('현재 비밀번호와 다른 비밀번호를 입력해주세요.');
                e.preventDefault();
                return;
            }
        });
    }
    
    // 비밀번호 확인 실시간 검사
    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', function() {
            const newPassword = newPasswordInput.value;
            const confirmPassword = this.value;
            
            if (confirmPassword && newPassword !== confirmPassword) {
                this.setCustomValidity('비밀번호가 일치하지 않습니다.');
            } else {
                this.setCustomValidity('');
            }
        });
    }
    
    // 새 비밀번호 입력 시 확인 필드 초기화
    if (newPasswordInput) {
        newPasswordInput.addEventListener('input', function() {
            if (confirmPasswordInput.value) {
                confirmPasswordInput.value = '';
                confirmPasswordInput.setCustomValidity('');
            }
        });
    }
}); 