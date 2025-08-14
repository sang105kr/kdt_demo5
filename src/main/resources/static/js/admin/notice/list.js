/**
 * 관리자 공지사항 목록 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자 공지사항 목록 페이지 로드됨');
    
    // 필터 변경 시 자동 제출
    initializeFilters();
    
    // 삭제 확인 다이얼로그
    initializeDeleteConfirm();
});

/**
 * 필터 초기화
 */
function resetFilters() {
    console.log('필터 초기화 실행');
    
    // 현재 URL에서 기본 경로만 가져오기
    const baseUrl = window.location.pathname;
    
    // 페이지를 1로 리셋하고 모든 필터 제거
    const resetUrl = baseUrl + '?page=1';
    
    console.log('리셋 URL:', resetUrl);
    window.location.href = resetUrl;
}

/**
 * 필터 초기화
 */
function initializeFilters() {
    // 셀렉트 박스 변경 시 자동 제출
    const filterSelects = document.querySelectorAll('.form-select');
    filterSelects.forEach(select => {
        select.addEventListener('change', function() {
            console.log('필터 변경:', this.name, this.value);
            this.closest('form').submit();
        });
    });
    
    // 체크박스 변경 시 자동 제출
    const filterCheckboxes = document.querySelectorAll('input[type="checkbox"]');
    filterCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            console.log('체크박스 변경:', this.name, this.checked);
            this.closest('form').submit();
        });
    });
}

/**
 * 삭제 확인 다이얼로그 초기화
 */
function initializeDeleteConfirm() {
    // 전역 함수로 삭제 함수 등록
    window.deleteNotice = function(noticeId) {
        console.log('공지사항 삭제 요청:', noticeId);
        
        if (confirm('정말로 이 공지사항을 삭제하시겠습니까?\n삭제된 공지사항은 복구할 수 없습니다.')) {
            console.log('삭제 확인됨, 삭제 요청 전송');
            
            // 삭제 요청 전송
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = `/admin/notice/${noticeId}/delete`;
            
            // CSRF 토큰 추가 (필요한 경우)
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            if (csrfToken) {
                const csrfInput = document.createElement('input');
                csrfInput.type = 'hidden';
                csrfInput.name = '_csrf';
                csrfInput.value = csrfToken;
                form.appendChild(csrfInput);
            }
            
            document.body.appendChild(form);
            form.submit();
        } else {
            console.log('삭제 취소됨');
        }
    };
}

/**
 * 페이지 로드 완료 후 실행
 */
window.addEventListener('load', function() {
    console.log('페이지 로드 완료');
    
    // 테이블 행 호버 효과
    const tableRows = document.querySelectorAll('.notice-table tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.backgroundColor = '#f8f9fa';
        });
        
        row.addEventListener('mouseleave', function() {
            this.style.backgroundColor = '';
        });
    });
    
    // 액션 버튼 툴팁
    const actionButtons = document.querySelectorAll('.action-buttons .btn');
    actionButtons.forEach(button => {
        const title = button.getAttribute('title');
        if (title) {
            button.addEventListener('mouseenter', function(e) {
                showTooltip(e.target, title);
            });
            
            button.addEventListener('mouseleave', function() {
                hideTooltip();
            });
        }
    });
});

/**
 * 툴팁 표시
 */
function showTooltip(element, text) {
    const tooltip = document.createElement('div');
    tooltip.className = 'tooltip';
    tooltip.textContent = text;
    tooltip.style.cssText = `
        position: absolute;
        background: #333;
        color: white;
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 12px;
        z-index: 1000;
        pointer-events: none;
        white-space: nowrap;
    `;
    
    document.body.appendChild(tooltip);
    
    const rect = element.getBoundingClientRect();
    tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
    tooltip.style.top = rect.top - tooltip.offsetHeight - 5 + 'px';
}

/**
 * 툴팁 숨기기
 */
function hideTooltip() {
    const tooltip = document.querySelector('.tooltip');
    if (tooltip) {
        tooltip.remove();
    }
}

/**
 * 성공 메시지 표시
 */
function showSuccessMessage(message) {
    showMessage(message, 'success');
}

/**
 * 오류 메시지 표시
 */
function showErrorMessage(message) {
    showMessage(message, 'error');
}

/**
 * 메시지 표시
 */
function showMessage(message, type) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        <span>${message}</span>
    `;
    
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 20px;
        border-radius: 4px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        display: flex;
        align-items: center;
        gap: 8px;
        background: ${type === 'success' ? '#28a745' : '#dc3545'};
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        animation: slideIn 0.3s ease-out;
    `;
    
    document.body.appendChild(messageDiv);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        messageDiv.style.animation = 'slideOut 0.3s ease-in';
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
        }, 300);
    }, 3000);
}

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);
