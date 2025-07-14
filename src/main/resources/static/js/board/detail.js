/**
 * 게시글 상세 페이지 JavaScript - 무한 스크롤 댓글 시스템
 */

// common.js에서 showModal 함수 import
import { showModal, ajax } from '../common.js';

// 전역 변수
let currentPage = 1;
let isLoading = false;
let hasMore = true;
const pageSize = 10;
let boardId = null;

document.addEventListener('DOMContentLoaded', function() {
    // 게시글 ID 가져오기
    const boardIdInput = document.getElementById('boardId');
    if (boardIdInput) {
        boardId = boardIdInput.value;
    }
    
    // 초기 댓글 로드
    loadReplies();
    
    // 삭제 버튼 이벤트 처리
    const deleteBtn = document.getElementById('deleteBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function() {
            showDeleteModal();
        });
    }
    
    // 댓글 등록 버튼 이벤트 처리
    const submitReplyBtn = document.getElementById('submitReplyBtn');
    if (submitReplyBtn) {
        submitReplyBtn.addEventListener('click', function() {
            submitReply();
        });
    }
    
    // 더 보기 버튼 이벤트 처리
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', function() {
            loadReplies();
        });
    }
    
    // 스크롤 이벤트 처리 (무한 스크롤)
    window.addEventListener('scroll', function() {
        if (isLoading || !hasMore) return;
        
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const windowHeight = window.innerHeight;
        const documentHeight = document.documentElement.scrollHeight;
        
        if (scrollTop + windowHeight >= documentHeight - 100) {
            loadReplies();
        }
    });
});

/**
 * 댓글 목록 로드
 */
async function loadReplies() {
    if (isLoading || !hasMore || !boardId) return;
    
    isLoading = true;
    showLoading(true);
    
    // 최소 로딩 시간 보장 (500ms)
    const startTime = Date.now();
    const minLoadingTime = 500;
    
    try {
        const response = await ajax.get(`/api/replies?boardId=${boardId}&pageNo=${currentPage}&pageSize=${pageSize}`);
        
        if (response && (response.code === 'SUCCESS' || response.code === '00') && Array.isArray(response.data)) {
            const replies = response.data;
            
            if (replies.length === 0) {
                if (currentPage === 1) {
                    showNoReplies();
                }
                hasMore = false;
            } else {
                renderReplies(replies);
                currentPage++;
                
                // 10개 미만이면 더 이상 데이터가 없음
                if (replies.length < pageSize) {
                    hasMore = false;
                }
            }
        } else {
            console.error('댓글 로드 실패:', response?.message, response);
        }
    } catch (error) {
        console.error('댓글 로드 중 오류:', error);
    } finally {
        // 최소 로딩 시간 보장
        const elapsedTime = Date.now() - startTime;
        if (elapsedTime < minLoadingTime) {
            await new Promise(resolve => setTimeout(resolve, minLoadingTime - elapsedTime));
        }
        
        isLoading = false;
        showLoading(false);
        updateLoadMoreButton();
    }
}

/**
 * 댓글 렌더링
 */
function renderReplies(replies) {
    const replyList = document.getElementById('replyList');
    const noReplies = document.getElementById('noReplies');
    
    // 첫 페이지인 경우 기존 내용 초기화
    if (currentPage === 1) {
        replyList.innerHTML = '';
        noReplies.style.display = 'none';
    }
    
    replies.forEach(reply => {
        const replyElement = createReplyElement(reply);
        replyList.appendChild(replyElement);
    });
}

/**
 * 댓글 요소 생성
 */
function createReplyElement(reply) {
    const replyDiv = document.createElement('div');
    replyDiv.className = 'reply-item';
    replyDiv.style.paddingLeft = `${reply.rindent * 20}px`;
    replyDiv.dataset.replyId = reply.replyId;
    
    const formattedDate = formatDate(reply.cdate);
    const isLoggedIn = checkLoginStatus();
    const isAuthor = isLoggedIn && checkIsAuthor(reply.email);
    
    replyDiv.innerHTML = `
        <div class="reply-header">
            <span class="reply-author">${reply.nickname}</span>
            <span class="reply-date">${formattedDate}</span>
        </div>
        <div class="reply-content">${escapeHtml(reply.rcontent)}</div>
        <div class="reply-actions">
            ${isLoggedIn ? `<button class="btn btn--small btn--outline reply-reply-btn" 
                data-reply-id="${reply.replyId}" 
                data-reply-group="${reply.rgroup}" 
                data-reply-step="${reply.rstep}" 
                data-reply-indent="${reply.rindent}">답글</button>` : ''}
            ${isAuthor ? `<button type="button" class="btn btn--small btn--outline delete-reply-btn" 
                data-reply-id="${reply.replyId}" 
                data-board-id="${boardId}">삭제</button>` : ''}
        </div>
        <div class="reply-reply-form" style="display: none;">
            <div class="form-row">
                <textarea placeholder="답글을 입력하세요..." required></textarea>
            </div>
            <div class="btn-area">
                <button type="button" class="btn btn--small btn--primary submit-reply-reply-btn">답글 등록</button>
                <button type="button" class="btn btn--small btn--outline cancel-reply-btn">취소</button>
            </div>
        </div>
    `;
    
    // 이벤트 리스너 추가
    const replyReplyBtn = replyDiv.querySelector('.reply-reply-btn');
    if (replyReplyBtn) {
        replyReplyBtn.addEventListener('click', function() {
            showReplyForm(reply.replyId, reply.rgroup, reply.rstep, reply.rindent);
        });
    }
    
    const deleteReplyBtn = replyDiv.querySelector('.delete-reply-btn');
    if (deleteReplyBtn) {
        deleteReplyBtn.addEventListener('click', function() {
            showDeleteReplyModal(reply.replyId, boardId);
        });
    }
    
    const submitReplyReplyBtn = replyDiv.querySelector('.submit-reply-reply-btn');
    if (submitReplyReplyBtn) {
        submitReplyReplyBtn.addEventListener('click', function() {
            submitReplyReply(reply.replyId, reply.rgroup, reply.rstep, reply.rindent);
        });
    }
    
    const cancelReplyBtn = replyDiv.querySelector('.cancel-reply-btn');
    if (cancelReplyBtn) {
        cancelReplyBtn.addEventListener('click', function() {
            hideReplyForm(this);
        });
    }
    
    return replyDiv;
}

/**
 * 댓글 등록
 */
async function submitReply() {
    const rcontent = document.getElementById('rcontent').value.trim();
    const rcontentError = document.getElementById('rcontentError');
    
    // 유효성 검사
    if (!rcontent) {
        showFieldError('rcontentError', '댓글 내용을 입력해주세요.');
        return;
    }
    
    try {
        const response = await ajax.post('/api/replies', {
            boardId: parseInt(boardId),
            rcontent: rcontent
        });
        
        if (response && (response.code === 'SUCCESS' || response.code === '00')) {
            // 입력 필드 초기화
            document.getElementById('rcontent').value = '';
            hideFieldError('rcontentError');
            
            // 댓글 목록 새로고침
            refreshReplies();
            
            showToastMessage('댓글이 등록되었습니다.', 'success');
        } else {
            showFieldError('rcontentError', response?.message || '댓글 등록에 실패했습니다.');
        }
    } catch (error) {
        console.error('댓글 등록 중 오류:', error);
        showFieldError('rcontentError', '댓글 등록 중 오류가 발생했습니다.');
    }
}

/**
 * 답글 등록
 */
async function submitReplyReply(parentId, rgroup, rstep, rindent) {
    const replyForm = document.querySelector(`[data-reply-id="${parentId}"] .reply-reply-form`);
    const textarea = replyForm.querySelector('textarea');
    const rcontent = textarea.value.trim();
    
    if (!rcontent) {
        showToastMessage('답글 내용을 입력해주세요.', 'error');
        return;
    }
    
    try {
        const response = await ajax.post('/api/replies', {
            boardId: parseInt(boardId),
            parentId: parentId,
            rcontent: rcontent
        });
        
        if (response && (response.code === 'SUCCESS' || response.code === '00')) {
            // 입력 필드 초기화
            textarea.value = '';
            hideReplyForm(replyForm.querySelector('.cancel-reply-btn'));
            
            // 댓글 목록 새로고침
            refreshReplies();
            
            showToastMessage('답글이 등록되었습니다.', 'success');
        } else {
            showToastMessage(response?.message || '답글 등록에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('답글 등록 중 오류:', error);
        showToastMessage('답글 등록 중 오류가 발생했습니다.', 'error');
    }
}

/**
 * 댓글 목록 새로고침
 */
function refreshReplies() {
    // 기존 목록 초기화
    const replyList = document.getElementById('replyList');
    const noReplies = document.getElementById('noReplies');
    
    if (replyList) {
        replyList.innerHTML = '';
    }
    if (noReplies) {
        noReplies.style.display = 'none';
    }
    
    // 상태 초기화
    currentPage = 1;
    hasMore = true;
    isLoading = false;
    
    // 새로 로드
    loadReplies();
}

/**
 * 로딩 표시/숨김
 */
function showLoading(show) {
    const loadingIndicator = document.getElementById('loadingIndicator');
    if (loadingIndicator) {
        loadingIndicator.style.display = show ? 'block' : 'none';
    }
}

/**
 * 댓글 없음 표시
 */
function showNoReplies() {
    const noReplies = document.getElementById('noReplies');
    if (noReplies) {
        noReplies.style.display = 'block';
    }
}

/**
 * 더 보기 버튼 업데이트
 */
function updateLoadMoreButton() {
    const loadMore = document.getElementById('loadMore');
    if (loadMore) {
        loadMore.style.display = hasMore ? 'block' : 'none';
    }
}

/**
 * 필드 에러 표시
 */
function showFieldError(fieldId, message) {
    const errorElement = document.getElementById(fieldId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

/**
 * 필드 에러 숨김
 */
function hideFieldError(fieldId) {
    const errorElement = document.getElementById(fieldId);
    if (errorElement) {
        errorElement.style.display = 'none';
    }
}

/**
 * 토스트 메시지 표시
 */
function showToastMessage(message, type = 'info') {
    // 기존 토스트 메시지 제거
    const existingToast = document.getElementById('toast-message');
    if (existingToast) {
        existingToast.remove();
    }
    
    // 토스트 메시지 생성
    const toast = document.createElement('div');
    toast.id = 'toast-message';
    toast.className = `toast toast--${type}`;
    toast.textContent = message;
    
    // 스타일 적용
    Object.assign(toast.style, {
        position: 'fixed',
        top: '20px',
        right: '20px',
        padding: '12px 20px',
        borderRadius: '4px',
        color: '#fff',
        fontSize: '14px',
        fontWeight: '500',
        zIndex: '10000',
        maxWidth: '300px',
        wordWrap: 'break-word',
        boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
        transform: 'translateX(100%)',
        transition: 'transform 0.3s ease-in-out'
    });
    
    // 타입별 색상 설정
    const colors = {
        success: '#28a745',
        error: '#dc3545',
        warning: '#ffc107',
        info: '#17a2b8'
    };
    toast.style.backgroundColor = colors[type] || colors.info;
    
    // DOM에 추가
    document.body.appendChild(toast);
    
    // 애니메이션 표시
    setTimeout(() => {
        toast.style.transform = 'translateX(0)';
    }, 100);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.remove();
            }
        }, 300);
    }, 3000);
}

/**
 * 성공 메시지 표시 (기존 함수 유지)
 */
function showSuccessMessage(message) {
    showToastMessage(message, 'success');
}

/**
 * 날짜 포맷팅
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear().toString().slice(-2);
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}년 ${month}월 ${day}일 ${hours}시 ${minutes}분`;
}

/**
 * HTML 이스케이프
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * 로그인 상태 확인
 */
function checkLoginStatus() {
    // 세션 정보 확인 (실제 구현에서는 서버에서 확인)
    return document.querySelector('[data-s-email]') !== null;
}

/**
 * 작성자 확인
 */
function checkIsAuthor(replyEmail) {
    const userEmail = document.querySelector('[data-s-email]')?.dataset.sEmail;
    return userEmail === replyEmail;
}

/**
 * 댓글 답글 폼 표시
 */
function showReplyForm(replyId, replyGroup, replyStep, replyIndent) {
    // 기존에 열린 답글 폼들 숨기기
    const openForms = document.querySelectorAll('.reply-reply-form');
    openForms.forEach(form => {
        form.style.display = 'none';
    });
    
    // 해당 댓글의 답글 폼 표시
    const replyItem = document.querySelector(`[data-reply-id="${replyId}"]`);
    const replyForm = replyItem.querySelector('.reply-reply-form');
    
    if (replyForm) {
        replyForm.style.display = 'block';
        
        // 텍스트 영역에 포커스
        const textarea = replyForm.querySelector('textarea');
        if (textarea) {
            textarea.focus();
        }
    }
}

/**
 * 댓글 답글 폼 숨기기
 */
function hideReplyForm(button) {
    const replyForm = button.closest('.reply-reply-form');
    if (replyForm) {
        replyForm.style.display = 'none';
        
        // 폼 내용 초기화
        const textarea = replyForm.querySelector('textarea');
        if (textarea) {
            textarea.value = '';
        }
    }
}

/**
 * 삭제 확인 모달 표시
 */
function showDeleteModal() {
    const boardId = document.querySelector('input[name="boardId"]')?.value || 
                   document.getElementById('boardId')?.value;
    
    if (!boardId) {
        console.error('게시글 ID를 찾을 수 없습니다.');
        return;
    }
    
    showModal({
        title: '게시글 삭제',
        message: '정말로 이 게시글을 삭제하시겠습니까?\n삭제된 게시글은 복구할 수 없습니다.',
        onConfirm: () => {
            deleteBoard(boardId);
        },
        onCancel: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
}

/**
 * 게시글 삭제 처리
 */
function deleteBoard(boardId) {
    // POST 요청으로 삭제 처리
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

/**
 * 댓글 삭제 확인 모달 표시
 */
function showDeleteReplyModal(replyId, boardId) {
    showModal({
        title: '댓글 삭제',
        message: '정말로 이 댓글을 삭제하시겠습니까?\n삭제된 댓글은 복구할 수 없습니다.',
        onConfirm: () => {
            deleteReply(replyId, boardId);
        },
        onCancel: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
}

/**
 * 댓글 삭제 처리
 */
async function deleteReply(replyId, boardId) {
    try {
        const response = await ajax.delete(`/api/replies/${replyId}`);
        
        if (response && (response.code === 'SUCCESS' || response.code === '00')) {
            // 댓글 목록 새로고침
            refreshReplies();
            
            showToastMessage('댓글이 삭제되었습니다.', 'success');
        } else {
            showToastMessage(response?.message || '댓글 삭제에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('댓글 삭제 중 오류:', error);
        showToastMessage('댓글 삭제 중 오류가 발생했습니다.', 'error');
    }
} 