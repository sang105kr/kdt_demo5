// 회원정보 수정 폼 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('회원정보 수정 폼 로드됨');
    
    // 폼 유효성 검사
    const form = document.querySelector('.edit-form');
    if (form) {
        form.addEventListener('submit', function(e) {
            const nickname = document.getElementById('nickname').value.trim();
            const tel = document.getElementById('tel').value.trim();
            
            if (!nickname) {
                alert('별칭을 입력해주세요.');
                e.preventDefault();
                return;
            }
            
            if (!tel) {
                alert('연락처를 입력해주세요.');
                e.preventDefault();
                return;
            }
            
            // 연락처 형식 검사
            const telPattern = /^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$/;
            if (!telPattern.test(tel)) {
                alert('올바른 연락처 형식이 아닙니다. (예: 010-1234-5678)');
                e.preventDefault();
                return;
            }
        });
    }
    
    // 연락처 자동 포맷팅
    const telInput = document.getElementById('tel');
    if (telInput) {
        telInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/[^0-9]/g, '');
            
            if (value.length <= 3) {
                value = value;
            } else if (value.length <= 7) {
                value = value.slice(0, 3) + '-' + value.slice(3);
            } else {
                value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11);
            }
            
            e.target.value = value;
        });
    }
}); 