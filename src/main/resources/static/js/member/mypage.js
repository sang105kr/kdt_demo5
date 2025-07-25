// 마이페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('마이페이지 로드됨');
    
    // 메시지 자동 숨김
    const messages = document.querySelectorAll('.message');
    messages.forEach(message => {
        setTimeout(() => {
            message.style.opacity = '0';
            setTimeout(() => {
                message.style.display = 'none';
            }, 300);
        }, 3000);
    });
    
    // 회원 탈퇴 버튼 클릭 이벤트
    const withdrawBtn = document.querySelector('.btn-danger');
    if (withdrawBtn) {
        withdrawBtn.addEventListener('click', function(e) {
            e.preventDefault();
            showModal({
                title: '회원 탈퇴',
                message: '정말로 회원 탈퇴를 진행하시겠습니까?\n이 작업은 되돌릴 수 없습니다.',
                onConfirm: () => {
                    // 원래 링크로 이동 (회원 탈퇴 페이지)
                    window.location.href = withdrawBtn.href;
                },
                onCancel: () => {
                    // 취소 시 아무것도 하지 않음
                }
            });
        });
    }
}); 