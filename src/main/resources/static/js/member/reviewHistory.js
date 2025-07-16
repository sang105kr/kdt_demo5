// 리뷰 내역 조회 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('리뷰 내역 조회 페이지 로드됨');
    
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
    
    // 리뷰 항목 클릭 이벤트 (상세보기 버튼 외 영역 클릭 시)
    const reviewItems = document.querySelectorAll('.review-item');
    reviewItems.forEach(item => {
        item.addEventListener('click', function(e) {
            // 상세보기 버튼 클릭이 아닌 경우에만 처리
            if (!e.target.closest('.review-actions')) {
                const detailLink = this.querySelector('.review-actions a');
                if (detailLink) {
                    detailLink.click();
                }
            }
        });
    });
    
    // 리뷰 상태별 색상 적용
    applyReviewStatusColors();
    
    // 평점별 별점 표시
    applyRatingStars();
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
    const ratingElements = document.querySelectorAll('.review-rating');
    ratingElements.forEach(element => {
        const rating = parseFloat(element.querySelector('span').textContent);
        const starIcon = element.querySelector('i');
        
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
    });
} 