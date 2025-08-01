/**
 * Admin Member Detail Page JavaScript
 * 회원 상세 페이지의 클라이언트 사이드 기능
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Member Detail Page loaded');
    
    // 상태 변경 폼 처리
    const statusForm = document.querySelector('.status-form');
    if (statusForm) {
        statusForm.addEventListener('submit', function(e) {
            if (!confirmStatusChange()) {
                e.preventDefault();
                return false;
            }
        });
    }
    
    // 회원 탈퇴 폼 처리
    const deleteForm = document.querySelector('.delete-form');
    if (deleteForm) {
        deleteForm.addEventListener('submit', function(e) {
            if (!confirmDelete()) {
                e.preventDefault();
                return false;
            }
        });
    }
    
    // 상태 변경 셀렉트 이벤트
    const statusSelect = document.querySelector('.status-form select[name="status"]');
    if (statusSelect) {
        statusSelect.addEventListener('change', function() {
            updateStatusPreview(this.value);
        });
    }
    
    // 페이지 로드 시 초기 상태 미리보기
    if (statusSelect) {
        updateStatusPreview(statusSelect.value);
    }
});

/**
 * 상태 변경 확인
 */
function confirmStatusChange() {
    const statusSelect = document.querySelector('.status-form select[name="status"]');
    const selectedOption = statusSelect.options[statusSelect.selectedIndex];
    const statusName = selectedOption.textContent;
    
    return confirm(`회원 상태를 "${statusName}"로 변경하시겠습니까?`);
}

/**
 * 회원 탈퇴 확인
 */
function confirmDelete() {
    return confirm('정말로 이 회원을 탈퇴(삭제)하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.');
}

/**
 * 상태 미리보기 업데이트
 */
function updateStatusPreview(statusValue) {
    const statusSelect = document.querySelector('.status-form select[name="status"]');
    const selectedOption = statusSelect.options[statusSelect.selectedIndex];
    const statusName = selectedOption.textContent;
    
    // 상태 미리보기 요소 찾기 또는 생성
    let previewElement = document.querySelector('.status-preview');
    if (!previewElement) {
        previewElement = document.createElement('div');
        previewElement.className = 'status-preview';
        previewElement.style.marginTop = '8px';
        previewElement.style.fontSize = '0.875rem';
        previewElement.style.color = '#666';
        
        const statusForm = document.querySelector('.status-form');
        statusForm.appendChild(previewElement);
    }
    
    // 현재 상태와 비교
    const currentStatusElement = document.querySelector('.status-badge');
    const currentStatus = currentStatusElement ? currentStatusElement.textContent.trim() : '';
    
    if (statusName === currentStatus) {
        previewElement.textContent = '현재 상태와 동일합니다.';
        previewElement.style.color = '#666';
    } else {
        previewElement.textContent = `상태를 "${statusName}"로 변경합니다.`;
        previewElement.style.color = '#222';
    }
}

/**
 * 회원 정보 수정 페이지로 이동
 */
function goToEditPage(memberId) {
    if (confirm('회원 정보를 수정하시겠습니까?')) {
        window.location.href = `/admin/members/${memberId}/edit`;
    }
}

/**
 * 회원 목록으로 돌아가기
 */
function goToList() {
    if (confirm('회원 목록으로 돌아가시겠습니까?')) {
        window.location.href = '/admin/members';
    }
}

/**
 * 상태 배지 색상 업데이트
 */
function updateStatusBadgeColors() {
    const statusBadges = document.querySelectorAll('.status-badge');
    statusBadges.forEach(badge => {
        const status = badge.textContent.trim();
        badge.className = 'status-badge ' + status.toLowerCase();
    });
}

// 페이지 로드 시 상태 배지 색상 업데이트
document.addEventListener('DOMContentLoaded', function() {
    updateStatusBadgeColors();
});

// 전역 함수로 노출
window.confirmStatusChange = confirmStatusChange;
window.confirmDelete = confirmDelete;
window.goToEditPage = goToEditPage;
window.goToList = goToList; 