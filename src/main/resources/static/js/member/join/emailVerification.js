/**
 * 이메일 인증 페이지 JavaScript
 */

// 인증 코드 재발송
async function resendCode() {
    try {
        // 세션에서 이메일 정보를 가져오므로 별도 파라미터 없이 요청
        const result = await ajax.post('/member/email/resend-code');
        
        if (result && result.code === '00') {
            alert('인증 코드가 재발송되었습니다.');
            // 페이지 새로고침하여 메시지 표시
            window.location.reload();
        } else {
            alert(result?.message || '인증 코드 재발송에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('인증 코드 재발송 중 오류가 발생했습니다.');
    }
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 인증 코드 입력 필드 포커스
    const verificationCodeInput = document.querySelector('input[name="verificationCode"]');
    if (verificationCodeInput) {
        verificationCodeInput.focus();
    }
    
    // 인증 코드 입력 시 자동 다음 필드 이동
    verificationCodeInput.addEventListener('input', function(e) {
        if (this.value.length === 6) {
            // 6자리 입력 완료 시 자동 제출
            this.form.submit();
        }
    });
}); 