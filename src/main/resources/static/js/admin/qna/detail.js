// 관리자 Q&A 상세 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeDetailPage();
});

/**
 * 상세 페이지 초기화
 */
function initializeDetailPage() {
    setupAccessibility();
    setupEventListeners();
}

/**
 * 접근성 설정
 */
function setupAccessibility() {
    // 포커스 표시 개선
    const focusableElements = document.querySelectorAll('a, button, input, select, textarea, [tabindex]');
    focusableElements.forEach(element => {
        element.addEventListener('focus', function() {
            this.style.outline = '2px solid #000000';
            this.style.outlineOffset = '2px';
        });
        
        element.addEventListener('blur', function() {
            this.style.outline = '';
            this.style.outlineOffset = '';
        });
    });
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // 뒤로가기 버튼 클릭 효과
    const backBtn = document.querySelector('.back-btn');
    if (backBtn) {
        backBtn.addEventListener('click', function(e) {
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
    }
}

/**
 * Q&A 삭제
 */
async function deleteQna(qnaId) {
    if (!qnaId) {
        if (typeof showToast === 'function') {
            showToast('삭제할 Q&A를 찾을 수 없습니다.', 'error');
        } else {
            alert('삭제할 Q&A를 찾을 수 없습니다.');
        }
        return;
    }
    
    // 삭제 확인
    if (typeof showModal === 'function') {
        showModal({
            title: 'Q&A 삭제',
            message: '이 Q&A를 삭제하시겠습니까?\n\n삭제된 Q&A는 복구할 수 없습니다.',
            confirmText: '삭제',
            cancelText: '취소',
            confirmClass: 'btn--danger',
            onConfirm: () => {
                performDeleteQna(qnaId);
            },
            onCancel: () => {
                // 취소 시 아무것도 하지 않음
            }
        });
    } else {
        if (confirm('이 Q&A를 삭제하시겠습니까?')) {
            performDeleteQna(qnaId);
        }
    }
}

/**
 * Q&A 삭제 실행
 */
async function performDeleteQna(qnaId) {
    try {
        showLoading();
        
        // common.js의 ajax 객체 사용
        if (typeof ajax !== 'undefined' && ajax.delete) {
            const result = await ajax.delete(`/api/qna/${qnaId}`);
            
            if (result && result.code === '00') {
                if (typeof showToast === 'function') {
                    showToast('Q&A가 성공적으로 삭제되었습니다.', 'success');
                } else {
                    alert('Q&A가 성공적으로 삭제되었습니다.');
                }
                
                // 관리자 Q&A 목록으로 리다이렉트
                setTimeout(() => {
                    window.location.href = '/admin/qna';
                }, 1000);
                
            } else {
                throw new Error(result?.message || 'Q&A 삭제에 실패했습니다.');
            }
        } else {
            throw new Error('AJAX 기능을 사용할 수 없습니다.');
        }
        
    } catch (error) {
        console.error('Q&A 삭제 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || 'Q&A 삭제에 실패했습니다.', 'error');
        } else {
            alert(error.message || 'Q&A 삭제에 실패했습니다.');
        }
    } finally {
        hideLoading();
    }
}

/**
 * 댓글 삭제
 */
async function deleteComment(commentId) {
    if (!commentId) {
        if (typeof showToast === 'function') {
            showToast('삭제할 댓글을 찾을 수 없습니다.', 'error');
        } else {
            alert('삭제할 댓글을 찾을 수 없습니다.');
        }
        return;
    }
    
    // 삭제 확인
    if (typeof showModal === 'function') {
        showModal({
            title: '댓글 삭제',
            message: '이 댓글을 삭제하시겠습니까?\n\n삭제된 댓글은 복구할 수 없습니다.',
            confirmText: '삭제',
            cancelText: '취소',
            confirmClass: 'btn--danger',
            onConfirm: () => {
                performDeleteComment(commentId);
            },
            onCancel: () => {
                // 취소 시 아무것도 하지 않음
            }
        });
    } else {
        if (confirm('이 댓글을 삭제하시겠습니까?')) {
            performDeleteComment(commentId);
        }
    }
}

/**
 * 댓글 삭제 실행
 */
async function performDeleteComment(commentId) {
    try {
        showLoading();
        
        // common.js의 ajax 객체 사용
        if (typeof ajax !== 'undefined' && ajax.delete) {
            const result = await ajax.delete(`/api/qna/comments/${commentId}`);
            
            if (result && result.code === '00') {
                if (typeof showToast === 'function') {
                    showToast('댓글이 성공적으로 삭제되었습니다.', 'success');
                } else {
                    alert('댓글이 성공적으로 삭제되었습니다.');
                }
                
                // 페이지 새로고침
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
                
            } else {
                throw new Error(result?.message || '댓글 삭제에 실패했습니다.');
            }
        } else {
            throw new Error('AJAX 기능을 사용할 수 없습니다.');
        }
        
    } catch (error) {
        console.error('댓글 삭제 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || '댓글 삭제에 실패했습니다.', 'error');
        } else {
            alert(error.message || '댓글 삭제에 실패했습니다.');
        }
    } finally {
        hideLoading();
    }
}

/**
 * 로딩 상태 표시
 */
function showLoading() {
    const loadingOverlay = document.createElement('div');
    loadingOverlay.id = 'loading-overlay';
    loadingOverlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(255, 255, 255, 0.8);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 9999;
    `;
    
    const spinner = document.createElement('div');
    spinner.style.cssText = `
        width: 40px;
        height: 40px;
        border: 3px solid #f3f3f3;
        border-top: 3px solid #000000;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    `;
    
    const style = document.createElement('style');
    style.textContent = `
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    `;
    
    document.head.appendChild(style);
    loadingOverlay.appendChild(spinner);
    document.body.appendChild(loadingOverlay);
}

/**
 * 로딩 상태 숨김
 */
function hideLoading() {
    const loadingOverlay = document.getElementById('loading-overlay');
    if (loadingOverlay) {
        loadingOverlay.remove();
    }
}

/**
 * 관리자 댓글 작성
 */
async function writeAdminComment() {
    const content = document.getElementById('adminCommentContent').value.trim();
    
    if (!content) {
        if (typeof showToast === 'function') {
            showToast('댓글 내용을 입력해주세요.', 'error');
        } else {
            alert('댓글 내용을 입력해주세요.');
        }
        return;
    }
    
    try {
        showLoading();
        
        const commentData = {
            qnaId: qnaId,
            content: content,
            commentType: 'ADMIN'
        };
        
        // common.js의 ajax 객체 사용
        if (typeof ajax !== 'undefined' && ajax.post) {
            const result = await ajax.post('/api/qna/comments', commentData);
            
            if (result && result.code === '00') {
                if (typeof showToast === 'function') {
                    showToast('댓글이 성공적으로 작성되었습니다.', 'success');
                } else {
                    alert('댓글이 성공적으로 작성되었습니다.');
                }
                
                // 페이지 새로고침
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
                
            } else {
                throw new Error(result?.message || '댓글 작성에 실패했습니다.');
            }
        } else {
            throw new Error('AJAX 기능을 사용할 수 없습니다.');
        }
        
    } catch (error) {
        console.error('댓글 작성 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || '댓글 작성에 실패했습니다.', 'error');
        } else {
            alert(error.message || '댓글 작성에 실패했습니다.');
        }
    } finally {
        hideLoading();
    }
}

/**
 * 댓글 수정 모달 열기
 */
function editComment(commentId) {
    const commentItem = document.querySelector(`[data-comment-id="${commentId}"]`);
    if (commentItem) {
        const content = commentItem.querySelector('.comment-content').textContent;
        document.getElementById('editCommentContent').value = content;
        document.getElementById('editCommentModal').style.display = 'block';
        
        // 현재 수정 중인 댓글 ID 저장
        window.currentEditCommentId = commentId;
    }
}

/**
 * 댓글 수정 모달 닫기
 */
function closeEditCommentModal() {
    document.getElementById('editCommentModal').style.display = 'none';
    document.getElementById('editCommentContent').value = '';
    window.currentEditCommentId = null;
}

/**
 * 댓글 수정
 */
async function updateComment() {
    const content = document.getElementById('editCommentContent').value.trim();
    const commentId = window.currentEditCommentId;
    
    if (!content) {
        if (typeof showToast === 'function') {
            showToast('댓글 내용을 입력해주세요.', 'error');
        } else {
            alert('댓글 내용을 입력해주세요.');
        }
        return;
    }
    
    if (!commentId) {
        if (typeof showToast === 'function') {
            showToast('수정할 댓글을 찾을 수 없습니다.', 'error');
        } else {
            alert('수정할 댓글을 찾을 수 없습니다.');
        }
        return;
    }
    
    try {
        showLoading();
        
        const commentData = {
            content: content
        };
        
        // common.js의 ajax 객체 사용
        if (typeof ajax !== 'undefined' && ajax.put) {
            const result = await ajax.put(`/api/qna/comments/${commentId}`, commentData);
            
            if (result && result.code === '00') {
                if (typeof showToast === 'function') {
                    showToast('댓글이 성공적으로 수정되었습니다.', 'success');
                } else {
                    alert('댓글이 성공적으로 수정되었습니다.');
                }
                
                // 페이지 새로고침
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
                
            } else {
                throw new Error(result?.message || '댓글 수정에 실패했습니다.');
            }
        } else {
            throw new Error('AJAX 기능을 사용할 수 없습니다.');
        }
        
    } catch (error) {
        console.error('댓글 수정 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || '댓글 수정에 실패했습니다.', 'error');
        } else {
            alert(error.message || '댓글 수정에 실패했습니다.');
        }
    } finally {
        hideLoading();
        closeEditCommentModal();
    }
}

/**
 * 답변 수정 모달 열기
 */
function editAnswer(qnaId, currentAnswer) {
    if (!qnaId) {
        if (typeof showToast === 'function') {
            showToast('수정할 Q&A를 찾을 수 없습니다.', 'error');
        } else {
            alert('수정할 Q&A를 찾을 수 없습니다.');
        }
        return;
    }
    
    // 현재 답변 내용을 모달에 설정
    document.getElementById('editAnswerContent').value = currentAnswer || '';
    document.getElementById('editAnswerModal').style.display = 'block';
    
    // 현재 수정 중인 Q&A ID 저장
    window.currentEditQnaId = qnaId;
}

/**
 * 답변 수정 모달 닫기
 */
function closeEditAnswerModal() {
    document.getElementById('editAnswerModal').style.display = 'none';
    document.getElementById('editAnswerContent').value = '';
    window.currentEditQnaId = null;
}

/**
 * 답변 수정
 */
async function updateAnswer() {
    const content = document.getElementById('editAnswerContent').value.trim();
    const qnaId = window.currentEditQnaId;
    
    if (!content) {
        if (typeof showToast === 'function') {
            showToast('답변 내용을 입력해주세요.', 'error');
        } else {
            alert('답변 내용을 입력해주세요.');
        }
        return;
    }
    
    if (!qnaId) {
        if (typeof showToast === 'function') {
            showToast('수정할 Q&A를 찾을 수 없습니다.', 'error');
        } else {
            alert('수정할 Q&A를 찾을 수 없습니다.');
        }
        return;
    }
    
    try {
        showLoading();
        
        const answerData = {
            answer: content
        };
        
        // common.js의 ajax 객체 사용
        if (typeof ajax !== 'undefined' && ajax.put) {
            const result = await ajax.put(`/admin/qna/${qnaId}/answer`, answerData);
            
            if (result && result.code === '00') {
                if (typeof showToast === 'function') {
                    showToast('답변이 성공적으로 수정되었습니다.', 'success');
                } else {
                    alert('답변이 성공적으로 수정되었습니다.');
                }
                
                // 페이지 새로고침
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
                
            } else {
                throw new Error(result?.message || '답변 수정에 실패했습니다.');
            }
        } else {
            throw new Error('AJAX 기능을 사용할 수 없습니다.');
        }
        
    } catch (error) {
        console.error('답변 수정 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || '답변 수정에 실패했습니다.', 'error');
        } else {
            alert(error.message || '답변 수정에 실패했습니다.');
        }
    } finally {
        hideLoading();
        closeEditAnswerModal();
    }
}

// 전역 함수로 내보내기
window.deleteQna = deleteQna;
window.deleteComment = deleteComment;
window.writeAdminComment = writeAdminComment;
window.editComment = editComment;
window.closeEditCommentModal = closeEditCommentModal;
window.updateComment = updateComment;
window.editAnswer = editAnswer;
window.closeEditAnswerModal = closeEditAnswerModal;
window.updateAnswer = updateAnswer;
