// 내 Q&A 목록 페이지 JavaScript

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
});

// 이벤트 리스너 설정
function setupEventListeners() {
    // Q&A 아이템 클릭 이벤트 (이미 HTML에서 처리됨)
    // 추가적인 이벤트가 필요한 경우 여기에 추가
}

// Q&A 삭제 확인
function confirmDelete(qnaId, event) {
    if (event) {
        event.stopPropagation(); // 상위 요소 클릭 이벤트 방지
    }
    
    showModal({
        title: 'Q&A 삭제',
        message: 'Q&A를 삭제하시겠습니까?\n삭제된 Q&A는 복구할 수 없습니다.',
        onConfirm: () => {
            deleteQna(qnaId);
        },
        onCancel: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
}

// Q&A 삭제
async function deleteQna(qnaId) {
    try {
        const result = await ajax.delete(`/api/qna/${qnaId}`);
        if (result.code === '00') {
            showToast('Q&A가 삭제되었습니다.', 'success');
            // 페이지 새로고침
            setTimeout(() => {
                location.reload();
            }, 1000);
        } else {
            showToast(result.message || 'Q&A 삭제에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('Q&A 삭제 실패:', error);
        showToast('Q&A 삭제 중 오류가 발생했습니다.', 'error');
    }
}

// 전역 함수로 노출
window.confirmDelete = confirmDelete;
window.deleteQna = deleteQna;
