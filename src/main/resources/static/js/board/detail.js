/**
 * 게시글 상세 페이지 JavaScript
 */

// 전역 변수
let boardId = null;

document.addEventListener('DOMContentLoaded', function() {
    // 게시글 ID 가져오기
    const boardIdInput = document.getElementById('boardId');
    if (boardIdInput) {
        boardId = boardIdInput.value;
    }
    
    // 게시글 좋아요/싫어요 최초 렌더링
    const likeCount = Number(document.getElementById('likeCount')?.textContent) || 0;
    const dislikeCount = Number(document.getElementById('dislikeCount')?.textContent) || 0;
    updateLikeDislikeCounts(likeCount, dislikeCount);
    
    // 좋아요/싫어요 버튼 이벤트 처리
    initializeLikeDislikeButtons();
    
    // 삭제 버튼 이벤트 처리
    const deleteBtn = document.getElementById('deleteBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function() {
            showDeleteModal();
        });
    }
    
    // 댓글 SPA 시스템 초기화
    const replyContainer = document.getElementById('replySectionContainer');
    if (replyContainer) {
        initReplySPA('replySectionContainer');
    }
});

/**
 * 좋아요/싫어요 버튼 초기화
 */
function initializeLikeDislikeButtons() {
    const likeBtn = document.getElementById('likeBtn');
    const dislikeBtn = document.getElementById('dislikeBtn');
    
    if (likeBtn) {
        likeBtn.addEventListener('click', function() {
            const boardId = this.dataset.boardId;
            likeBoard(boardId);
        });
    }
    
    if (dislikeBtn) {
        dislikeBtn.addEventListener('click', function() {
            const boardId = this.dataset.boardId;
            dislikeBoard(boardId);
        });
    }
}

/**
 * 게시글 좋아요
 */
async function likeBoard(boardId) {
    try {
        const response = await ajax.post(`/board/${boardId}/like`);
        
        if (response && response.success) {
            updateLikeDislikeCounts(response.likeCount, response.dislikeCount);
            showLikeDislikeStatus(response.message || '좋아요가 등록되었습니다.');
        } else {
            showLikeDislikeStatus(response?.message || '좋아요 처리에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('좋아요 처리 중 오류:', error);
        showLikeDislikeStatus('오류가 발생했습니다.', 'error');
    }
}

/**
 * 게시글 싫어요
 */
async function dislikeBoard(boardId) {
    try {
        const response = await ajax.post(`/board/${boardId}/dislike`);
        
        if (response && response.success) {
            updateLikeDislikeCounts(response.likeCount, response.dislikeCount);
            showLikeDislikeStatus(response.message || '싫어요가 등록되었습니다.');
        } else {
            showLikeDislikeStatus(response?.message || '싫어요 처리에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('싫어요 처리 중 오류:', error);
        showLikeDislikeStatus('오류가 발생했습니다.', 'error');
    }
}

/**
 * 좋아요/싫어요 카운트 업데이트
 */
function updateLikeDislikeCounts(likeCount, dislikeCount) {
    const likeCountElement = document.getElementById('likeCount');
    const dislikeCountElement = document.getElementById('dislikeCount');
    
    if (likeCountElement) likeCountElement.textContent = likeCount || 0;
    if (dislikeCountElement) dislikeCountElement.textContent = dislikeCount || 0;
}

/**
 * 좋아요/싫어요 상태 메시지 표시
 */
function showLikeDislikeStatus(message, type = 'info') {
    const statusElement = document.getElementById('likeDislikeStatus');
    if (statusElement) {
        statusElement.textContent = message;
        statusElement.className = `like-dislike-status ${type}`;
        statusElement.style.display = 'block';
        
        // 3초 후 메시지 숨기기
        setTimeout(() => {
            statusElement.textContent = '';
            statusElement.style.display = 'none';
        }, 3000);
    }
}

/**
 * 삭제 확인 모달 표시
 */
function showDeleteModal() {
    showModal({
        title: '게시글 삭제',
        message: '정말로 이 게시글을 삭제하시겠습니까?\n삭제된 게시글은 복구할 수 없습니다.',
        onConfirm: () => deleteBoard(boardId),
        onCancel: () => {}
    });
}

/**
 * 게시글 삭제 처리
 */
function deleteBoard(boardId) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/board/${boardId}/delete`;
    
    // CSRF 토큰이 있다면 추가
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
} 