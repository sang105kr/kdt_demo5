/**
 * 관리자 회원 목록 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자 회원 목록 페이지 로드됨');
    
    // 회원 행 클릭 이벤트
    const memberRows = document.querySelectorAll('.member-row');
    memberRows.forEach(row => {
        row.addEventListener('click', function() {
            const memberId = this.querySelector('.member-id').textContent;
            window.location.href = `/admin/members/${memberId}`;
        });
    });
    
    // 검색 폼 엔터키 이벤트
    const searchInput = document.querySelector('.filter-input');
    if (searchInput) {
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.closest('form').submit();
            }
        });
    }
    
    // 상태 필터 변경 시 자동 제출
    const statusSelect = document.querySelector('.filter-select');
    if (statusSelect) {
        statusSelect.addEventListener('change', function() {
            this.closest('form').submit();
        });
    }
    
    // 메시지 자동 숨김
    const messages = document.querySelectorAll('.message');
    messages.forEach(message => {
        setTimeout(() => {
            message.style.opacity = '0';
            setTimeout(() => {
                message.remove();
            }, 300);
        }, 3000);
    });
}); 