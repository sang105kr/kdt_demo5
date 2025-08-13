/**
 * 관리자 FAQ 목록 페이지 JavaScript
 */

// DOM 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자 FAQ 목록 페이지 로드됨');
    
    // FAQ 액션 버튼 이벤트 초기화
    initFaqActions();
});

/**
 * FAQ 액션 버튼 초기화
 */
function initFaqActions() {
    // 상태 변경 버튼 클릭 이벤트
    document.querySelectorAll('.status-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const faqId = this.dataset.faqId;
            const currentStatus = this.dataset.currentStatus;
            toggleStatus(faqId, currentStatus);
        });
    });
    
    // 삭제 버튼 클릭 이벤트
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const faqId = this.dataset.faqId;
            const faqTitle = this.dataset.faqTitle;
            confirmDeleteFaq(faqId, faqTitle);
        });
    });
}

/**
 * FAQ 상태 변경
 */
async function toggleStatus(faqId, currentStatus) {
    if (!faqId) {
        console.error('FAQ ID가 없습니다.');
        return;
    }
    
    const newStatus = currentStatus === 'Y' ? 'N' : 'Y';
    const statusText = newStatus === 'Y' ? '활성화' : '비활성화';
    
    try {
        showLoading();
        
        // common.js의 ajax 객체 사용
        if (typeof ajax !== 'undefined' && ajax.put) {
            const result = await ajax.put(`/admin/faq/${faqId}/status?isActive=${newStatus}`);
            
            if (result && result.code === '00') {
                if (typeof showToast === 'function') {
                    showToast(`FAQ가 ${statusText}되었습니다.`, 'success');
                } else {
                    alert(`FAQ가 ${statusText}되었습니다.`);
                }
                
                // 페이지 새로고침
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
                
            } else {
                throw new Error(result?.message || 'FAQ 상태 변경에 실패했습니다.');
            }
        } else {
            throw new Error('AJAX 기능을 사용할 수 없습니다.');
        }
        
    } catch (error) {
        console.error('FAQ 상태 변경 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || 'FAQ 상태 변경에 실패했습니다.', 'error');
        } else {
            alert(error.message || 'FAQ 상태 변경에 실패했습니다.');
        }
    } finally {
        hideLoading();
    }
}

/**
 * FAQ 삭제 확인
 */
function confirmDeleteFaq(faqId, faqTitle) {
    if (!faqId) {
        console.error('FAQ ID가 없습니다.');
        return;
    }
    
    const message = `정말로 "${faqTitle}" FAQ를 삭제하시겠습니까?\n\n삭제된 FAQ는 복구할 수 없습니다.`;
    
    if (typeof showModal === 'function') {
        showModal({
            title: 'FAQ 삭제 확인',
            message: message,
            confirmText: '삭제',
            cancelText: '취소',
            confirmClass: 'btn--danger',
            onConfirm: () => deleteFaq(faqId),
            onCancel: () => {
                // 취소 시 아무것도 하지 않음
            }
        });
    } else {
        if (confirm(message)) {
            deleteFaq(faqId);
        }
    }
}

/**
 * FAQ 삭제
 */
async function deleteFaq(faqId) {
    if (!faqId) {
        console.error('FAQ ID가 없습니다.');
        return;
    }
    
    try {
        showLoading();
        
        // common.js의 ajax 객체 사용
        if (typeof ajax !== 'undefined' && ajax.delete) {
            const result = await ajax.delete(`/admin/faq/${faqId}`);
            
            if (result && result.code === '00') {
                if (typeof showToast === 'function') {
                    showToast('FAQ가 성공적으로 삭제되었습니다.', 'success');
                } else {
                    alert('FAQ가 성공적으로 삭제되었습니다.');
                }
                
                // 페이지 새로고침
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
                
            } else {
                throw new Error(result?.message || 'FAQ 삭제에 실패했습니다.');
            }
        } else {
            throw new Error('AJAX 기능을 사용할 수 없습니다.');
        }
        
    } catch (error) {
        console.error('FAQ 삭제 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || 'FAQ 삭제에 실패했습니다.', 'error');
        } else {
            alert(error.message || 'FAQ 삭제에 실패했습니다.');
        }
    } finally {
        hideLoading();
    }
}

/**
 * 로딩 표시
 */
function showLoading() {
    // 로딩 인디케이터 표시 로직
    if (typeof showLoadingIndicator === 'function') {
        showLoadingIndicator();
    }
}

/**
 * 로딩 숨김
 */
function hideLoading() {
    // 로딩 인디케이터 숨김 로직
    if (typeof hideLoadingIndicator === 'function') {
        hideLoadingIndicator();
    }
}

// 전역 함수로 내보내기
window.toggleStatus = toggleStatus;
window.confirmDeleteFaq = confirmDeleteFaq;
window.deleteFaq = deleteFaq;
