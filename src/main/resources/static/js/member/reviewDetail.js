// 리뷰 상세 조회 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('리뷰 상세 조회 페이지 로드됨');
    
    // 메시지 자동 숨김
    const messages = document.querySelectorAll('.message');
    messages.forEach(message => {
        setTimeout(() => {
            message.style.opacity = '0';
            setTimeout(() => {
                message.style.display = 'none';
            }, 300);
        }, 3000);
    });
    
    // 리뷰 상태별 색상 적용
    applyReviewStatusColors();
    
    // 평점별 별점 표시
    applyRatingStars();
    
    // 댓글 목록 애니메이션
    animateCommentItems();
    
    // 댓글 작성 폼 문자 수 카운터
    setupCommentCharCounter();
    
    // 댓글 수정 모달 문자 수 카운터
    setupEditCommentCharCounter();
});

// 리뷰 상태별 색상 적용 함수
function applyReviewStatusColors() {
    const statusBadges = document.querySelectorAll('.status-badge');
    statusBadges.forEach(badge => {
        const status = badge.textContent.toLowerCase();
        badge.className = `status-badge status-${status}`;
    });
}

// 평점별 별점 표시 함수
function applyRatingStars() {
    const ratingElement = document.querySelector('.rating-display');
    if (ratingElement) {
        const rating = parseFloat(ratingElement.querySelector('span').textContent);
        const starIcon = ratingElement.querySelector('i');
        
        // 평점에 따라 별점 색상 조정
        if (rating >= 4.5) {
            starIcon.style.color = '#f39c12';
        } else if (rating >= 3.5) {
            starIcon.style.color = '#f1c40f';
        } else if (rating >= 2.5) {
            starIcon.style.color = '#e67e22';
        } else {
            starIcon.style.color = '#e74c3c';
        }
    }
}

// 댓글 목록 애니메이션 함수
function animateCommentItems() {
    const commentItems = document.querySelectorAll('.comment-item');
    commentItems.forEach((item, index) => {
        item.style.opacity = '0';
        item.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            item.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateY(0)';
        }, index * 100);
    });
}

// 댓글 작성 폼 문자 수 카운터 설정
function setupCommentCharCounter() {
    const contentTextarea = document.querySelector('.comment-form textarea');
    const charCount = document.getElementById('commentCharCount');
    
    if (contentTextarea && charCount) {
        // 초기 문자 수 설정
        charCount.textContent = contentTextarea.value.length;
        
        // 입력 시 문자 수 업데이트
        contentTextarea.addEventListener('input', function() {
            const length = this.value.length;
            charCount.textContent = length;
            
            // 500자에 가까워지면 색상 변경
            if (length >= 450) {
                charCount.style.color = '#e74c3c';
            } else if (length >= 400) {
                charCount.style.color = '#f39c12';
            } else {
                charCount.style.color = '#666';
            }
        });
    }
}

// 댓글 수정 모달 문자 수 카운터 설정
function setupEditCommentCharCounter() {
    const editTextarea = document.getElementById('editCommentContent');
    const editCharCount = document.getElementById('editCommentCharCount');
    
    if (editTextarea && editCharCount) {
        editTextarea.addEventListener('input', function() {
            const length = this.value.length;
            editCharCount.textContent = length;
            
            // 500자에 가까워지면 색상 변경
            if (length >= 450) {
                editCharCount.style.color = '#e74c3c';
            } else if (length >= 400) {
                editCharCount.style.color = '#f39c12';
            } else {
                editCharCount.style.color = '#666';
            }
        });
    }
}

// 리뷰 삭제 함수 (공통 모달 사용)
function showDeleteReviewModal(reviewId) {
    showModal({
        title: '리뷰 삭제',
        message: '정말로 이 리뷰를 삭제하시겠습니까? 삭제된 리뷰는 복구할 수 없습니다.',
        onConfirm: function() {
            // 삭제 폼 생성 및 제출
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = `/member/mypage/reviews/${reviewId}/delete`;
            document.body.appendChild(form);
            form.submit();
        },
        onCancel: function() {}
    });
}

// 댓글 수정 모달 열기
function editComment(commentId, content) {
    const modal = document.getElementById('editCommentModal');
    const textarea = document.getElementById('editCommentContent');
    const form = document.getElementById('editCommentForm');
    const charCount = document.getElementById('editCommentCharCount');
    
    // 폼 액션 설정
    form.action = `/member/mypage/reviews/comments/${commentId}/edit`;
    
    // 내용 설정
    textarea.value = content;
    charCount.textContent = content.length;
    
    // 모달 표시
    modal.style.display = 'block';
    
    // 텍스트 영역에 포커스
    textarea.focus();
}

// 댓글 수정 모달 닫기
function closeEditModal() {
    const modal = document.getElementById('editCommentModal');
    modal.style.display = 'none';
}

// 댓글 삭제 함수
function deleteComment(commentId) {
    if (confirm('정말로 이 댓글을 삭제하시겠습니까?\n삭제된 댓글은 복구할 수 없습니다.')) {
        // 삭제 폼 생성 및 제출
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = `/member/mypage/reviews/comments/${commentId}/delete`;
        
        document.body.appendChild(form);
        form.submit();
    }
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('editCommentModal');
    if (event.target === modal) {
        closeEditModal();
    }
} 