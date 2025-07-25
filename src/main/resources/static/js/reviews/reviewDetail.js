/**
 * 리뷰 상세 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // 댓글 작성 폼 글자 수 카운트
    const commentTextarea = document.querySelector('textarea[name="content"]');
    const commentCharCount = document.getElementById('commentCharCount');
    
    if (commentTextarea && commentCharCount) {
        commentTextarea.addEventListener('input', function() {
            const length = this.value.length;
            commentCharCount.textContent = length;
            
            if (length > 450) {
                commentCharCount.style.color = '#d32f2f';
            } else if (length > 400) {
                commentCharCount.style.color = '#f57c00';
            } else {
                commentCharCount.style.color = '#666';
            }
        });
    }
    
    // 댓글 수정 모달 관련
    const editModal = document.getElementById('editCommentModal');
    const editForm = document.getElementById('editCommentForm');
    const editTextarea = document.getElementById('editCommentContent');
    const editCharCount = document.getElementById('editCommentCharCount');
    
    if (editTextarea && editCharCount) {
        editTextarea.addEventListener('input', function() {
            const length = this.value.length;
            editCharCount.textContent = length;
            
            if (length > 450) {
                editCharCount.style.color = '#d32f2f';
            } else if (length > 400) {
                editCharCount.style.color = '#f57c00';
            } else {
                editCharCount.style.color = '#666';
            }
        });
    }
    
    // 성공/에러 메시지 자동 숨김
    const messages = document.querySelectorAll('.message');
    messages.forEach(message => {
        setTimeout(() => {
            message.style.opacity = '0';
            setTimeout(() => {
                message.style.display = 'none';
            }, 300);
        }, 3000);
    });
});

// 댓글 수정 모달 열기
function editComment(commentId, content) {
    const modal = document.getElementById('editCommentModal');
    const form = document.getElementById('editCommentForm');
    const textarea = document.getElementById('editCommentContent');
    const charCount = document.getElementById('editCommentCharCount');
    
    // 폼 액션 설정
    form.action = `/reviews/comments/${commentId}/edit`;
    
    // 텍스트 영역에 기존 내용 설정
    textarea.value = content;
    charCount.textContent = content.length;
    
    // 모달 표시
    modal.style.display = 'block';
    textarea.focus();
}

// 댓글 수정 모달 닫기
function closeEditModal() {
    const modal = document.getElementById('editCommentModal');
    modal.style.display = 'none';
    
    // 폼 초기화
    const form = document.getElementById('editCommentForm');
    const textarea = document.getElementById('editCommentContent');
    const charCount = document.getElementById('editCommentCharCount');
    
    form.action = '';
    textarea.value = '';
    charCount.textContent = '0';
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('editCommentModal');
    if (event.target === modal) {
        closeEditModal();
    }
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeEditModal();
    }
}); 