// 회원 탈퇴 폼 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('회원 탈퇴 폼 로드됨');
    
    const confirmCheckbox = document.getElementById('confirmWithdraw');
    const withdrawBtn = document.getElementById('withdrawBtn');
    const form = document.querySelector('.withdraw-form');
    
    // 체크박스 상태에 따른 버튼 활성화/비활성화
    if (confirmCheckbox && withdrawBtn) {
        confirmCheckbox.addEventListener('change', function() {
            withdrawBtn.disabled = !this.checked;
        });
    }
    
    // 폼 제출 시 최종 확인
    if (form) {
        form.addEventListener('submit', function(e) {
            if (!confirmCheckbox.checked) {
                alert('회원 탈퇴 동의에 체크해주세요.');
                e.preventDefault();
                return;
            }
            
            e.preventDefault();
            
            showModal({
                title: '회원 탈퇴 최종 확인',
                message: '정말로 회원 탈퇴를 진행하시겠습니까?\n\n• 모든 개인정보가 삭제됩니다.\n• 주문 내역, 리뷰 등 모든 데이터가 삭제됩니다.\n• 이 작업은 되돌릴 수 없습니다.\n\n탈퇴를 진행하시려면 "확인"을 클릭하세요.',
                onConfirm: () => {
                    // 실제 폼 제출
                    form.submit();
                },
                onCancel: () => {
                    // 취소 시 아무것도 하지 않음
                }
            });
        });
    }
}); 