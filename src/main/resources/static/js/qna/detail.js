// Q&A 상세 페이지 JavaScript

// 전역 변수
let currentEditCommentId = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
    // ajax 객체가 로드되었는지 확인
    if (typeof ajax === 'undefined') {
        console.error('ajax 객체가 로드되지 않았습니다.');
        return;
    }
});

// 이벤트 리스너 설정
function setupEventListeners() {
    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal')) {
            e.target.style.display = 'none';
        }
    });
}

// Q&A 도움됨 표시
async function markHelpful(qnaId) {
    if (!isLoggedIn) {
        showLoginRequired();
        return;
    }

    try {
        const result = await ajax.post(`/api/qna/${qnaId}/helpful`);
        if (result.code === '00') {
            // 도움됨 카운트 업데이트
            const helpfulCount = document.querySelector('.helpful-count');
            if (helpfulCount) {
                const currentCount = parseInt(helpfulCount.textContent) || 0;
                helpfulCount.textContent = currentCount + 1;
            }
            showToast('도움됨으로 표시되었습니다.', 'success');
        } else {
            showToast(result.message || '처리에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('도움됨 처리 실패:', error);
        showToast('처리 중 오류가 발생했습니다.', 'error');
    }
}

// Q&A 도움안됨 표시
async function markUnhelpful(qnaId) {
    if (!isLoggedIn) {
        showLoginRequired();
        return;
    }

    try {
        const result = await ajax.post(`/api/qna/${qnaId}/unhelpful`);
        if (result.code === '00') {
            // 도움안됨 카운트 업데이트
            const unhelpfulCount = document.querySelector('.unhelpful-count');
            if (unhelpfulCount) {
                const currentCount = parseInt(unhelpfulCount.textContent) || 0;
                unhelpfulCount.textContent = currentCount + 1;
            }
            showToast('도움안됨으로 표시되었습니다.', 'success');
        } else {
            showToast(result.message || '처리에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('도움안됨 처리 실패:', error);
        showToast('처리 중 오류가 발생했습니다.', 'error');
    }
}

// 댓글 작성
async function writeComment() {
    if (!isLoggedIn) {
        showLoginRequired();
        return;
    }

    const content = document.getElementById('commentContent').value.trim();
    if (!content) {
        showToast('댓글 내용을 입력해주세요.', 'error');
        return;
    }

    try {
        const result = await ajax.post('/api/qna/comments/write', {
            qnaId: qnaId,
            content: content
        });

        if (result.code === '00') {
            showToast('댓글이 작성되었습니다.', 'success');
            document.getElementById('commentContent').value = '';
            // 댓글 목록 새로고침
            location.reload();
        } else {
            showToast(result.message || '댓글 작성에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('댓글 작성 실패:', error);
        showToast('댓글 작성 중 오류가 발생했습니다.', 'error');
    }
}

// 댓글 수정 모달 열기
function editComment(commentId) {
    const commentItem = document.querySelector(`[data-comment-id="${commentId}"]`);
    if (!commentItem) return;

    const contentElement = commentItem.querySelector('.comment-content');
    const currentContent = contentElement.textContent;

    document.getElementById('editCommentContent').value = currentContent;
    currentEditCommentId = commentId;
    document.getElementById('editCommentModal').style.display = 'block';
}

// 댓글 수정 모달 닫기
function closeEditCommentModal() {
    document.getElementById('editCommentModal').style.display = 'none';
    currentEditCommentId = null;
    document.getElementById('editCommentContent').value = '';
}

// 댓글 수정
async function updateComment() {
    if (!currentEditCommentId) return;

    const content = document.getElementById('editCommentContent').value.trim();
    if (!content) {
        showToast('댓글 내용을 입력해주세요.', 'error');
        return;
    }

    try {
        const result = await ajax.put(`/api/qna/comments/${currentEditCommentId}`, {
            content: content
        });

        if (result.code === '00') {
            showToast('댓글이 수정되었습니다.', 'success');
            closeEditCommentModal();
            // 댓글 목록 새로고침
            location.reload();
        } else {
            showToast(result.message || '댓글 수정에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('댓글 수정 실패:', error);
        showToast('댓글 수정 중 오류가 발생했습니다.', 'error');
    }
}

// 댓글 삭제
async function deleteComment(commentId) {
    showModal({
        title: '댓글 삭제',
        message: '댓글을 삭제하시겠습니까?',
        onConfirm: () => {
            deleteCommentConfirm(commentId);
        },
        onCancel: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
}

// 댓글 삭제 확인
async function deleteCommentConfirm(commentId) {
    try {
        const result = await ajax.delete(`/api/qna/comments/${commentId}`);
        if (result.code === '00') {
            showToast('댓글이 삭제되었습니다.', 'success');
            // 댓글 목록 새로고침
            location.reload();
        } else {
            showToast(result.message || '댓글 삭제에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('댓글 삭제 실패:', error);
        showToast('댓글 삭제 중 오류가 발생했습니다.', 'error');
    }
}

// 댓글 도움됨 표시
async function markCommentHelpful(commentId) {
    if (!isLoggedIn) {
        showLoginRequired();
        return;
    }

    try {
        const result = await ajax.post(`/api/qna/comments/${commentId}/helpful`);
        if (result.code === '00') {
            // 도움됨 카운트 업데이트
            const commentItem = document.querySelector(`[data-comment-id="${commentId}"]`);
            if (commentItem) {
                const helpfulCount = commentItem.querySelector('.comment-helpful-count');
                if (helpfulCount) {
                    const currentCount = parseInt(helpfulCount.textContent) || 0;
                    helpfulCount.textContent = currentCount + 1;
                }
            }
            showToast('도움됨으로 표시되었습니다.', 'success');
        } else {
            showToast(result.message || '처리에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('댓글 도움됨 처리 실패:', error);
        showToast('처리 중 오류가 발생했습니다.', 'error');
    }
}

// 댓글 도움안됨 표시
async function markCommentUnhelpful(commentId) {
    if (!isLoggedIn) {
        showLoginRequired();
        return;
    }

    try {
        const result = await ajax.post(`/api/qna/comments/${commentId}/unhelpful`);
        if (result.code === '00') {
            // 도움안됨 카운트 업데이트
            const commentItem = document.querySelector(`[data-comment-id="${commentId}"]`);
            if (commentItem) {
                const unhelpfulCount = commentItem.querySelector('.comment-unhelpful-count');
                if (unhelpfulCount) {
                    const currentCount = parseInt(unhelpfulCount.textContent) || 0;
                    unhelpfulCount.textContent = currentCount + 1;
                }
            }
            showToast('도움안됨으로 표시되었습니다.', 'success');
        } else {
            showToast(result.message || '처리에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('댓글 도움안됨 처리 실패:', error);
        showToast('처리 중 오류가 발생했습니다.', 'error');
    }
}

// Q&A 삭제
async function deleteQna() {
    showModal({
        title: 'Q&A 삭제',
        message: 'Q&A를 삭제하시겠습니까?\n삭제된 Q&A는 복구할 수 없습니다.',
        onConfirm: () => {
            deleteQnaConfirm();
        },
        onCancel: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
}

// Q&A 삭제 확인
async function deleteQnaConfirm() {

    try {
        const result = await ajax.delete(`/api/qna/${qnaId}`);
        if (result.code === '00') {
            showToast('Q&A가 삭제되었습니다.', 'success');
            // 목록 페이지로 이동
            setTimeout(() => {
                location.href = '/qna';
            }, 1000);
        } else {
            showToast(result.message || 'Q&A 삭제에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('Q&A 삭제 실패:', error);
        showToast('Q&A 삭제 중 오류가 발생했습니다.', 'error');
    }
}

// 로그인 필요 알림
function showLoginRequired() {
    showToast('로그인이 필요한 서비스입니다.', 'error');
}

// 전역 함수로 노출
window.markHelpful = markHelpful;
window.markUnhelpful = markUnhelpful;
window.writeComment = writeComment;
window.editComment = editComment;
window.closeEditCommentModal = closeEditCommentModal;
window.updateComment = updateComment;
window.deleteComment = deleteComment;
window.markCommentHelpful = markCommentHelpful;
window.markCommentUnhelpful = markCommentUnhelpful;
window.deleteQna = deleteQna;
