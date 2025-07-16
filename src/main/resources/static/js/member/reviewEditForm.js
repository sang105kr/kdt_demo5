// 리뷰 수정 폼 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('리뷰 수정 폼 페이지 로드됨');
    
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
    
    // 문자 수 카운터
    const contentTextarea = document.getElementById('content');
    const charCount = document.getElementById('charCount');
    
    if (contentTextarea && charCount) {
        // 초기 문자 수 설정
        charCount.textContent = contentTextarea.value.length;
        
        // 입력 시 문자 수 업데이트
        contentTextarea.addEventListener('input', function() {
            const length = this.value.length;
            charCount.textContent = length;
            
            // 2000자에 가까워지면 색상 변경
            if (length >= 1800) {
                charCount.style.color = '#e74c3c';
            } else if (length >= 1500) {
                charCount.style.color = '#f39c12';
            } else {
                charCount.style.color = '#666';
            }
        });
    }
    
    // 폼 유효성 검사
    const reviewForm = document.querySelector('.review-form');
    if (reviewForm) {
        reviewForm.addEventListener('submit', function(e) {
            const title = document.getElementById('title').value.trim();
            const rating = document.getElementById('rating').value;
            const content = document.getElementById('content').value.trim();
            
            // 제목 검사
            if (title.length < 2) {
                e.preventDefault();
                alert('리뷰 제목은 최소 2자 이상 입력해주세요.');
                document.getElementById('title').focus();
                return;
            }
            
            if (title.length > 100) {
                e.preventDefault();
                alert('리뷰 제목은 최대 100자까지 입력 가능합니다.');
                document.getElementById('title').focus();
                return;
            }
            
            // 평점 검사
            if (!rating) {
                e.preventDefault();
                alert('평점을 선택해주세요.');
                document.getElementById('rating').focus();
                return;
            }
            
            // 내용 검사
            if (content.length < 10) {
                e.preventDefault();
                alert('리뷰 내용은 최소 10자 이상 입력해주세요.');
                document.getElementById('content').focus();
                return;
            }
            
            if (content.length > 2000) {
                e.preventDefault();
                alert('리뷰 내용은 최대 2000자까지 입력 가능합니다.');
                document.getElementById('content').focus();
                return;
            }
        });
    }
    
    // 평점 선택 시 시각적 피드백
    const ratingSelect = document.getElementById('rating');
    if (ratingSelect) {
        // 초기 평점에 따른 색상 설정
        const initialRating = parseFloat(ratingSelect.value);
        if (initialRating) {
            applyRatingColor(ratingSelect, initialRating);
        }
        
        ratingSelect.addEventListener('change', function() {
            const selectedRating = parseFloat(this.value);
            applyRatingColor(this, selectedRating);
        });
    }
});

// 평점에 따른 색상 적용 함수
function applyRatingColor(selectElement, rating) {
    if (rating >= 4.5) {
        selectElement.style.backgroundColor = '#d4edda';
        selectElement.style.color = '#155724';
    } else if (rating >= 3.5) {
        selectElement.style.backgroundColor = '#fff3cd';
        selectElement.style.color = '#856404';
    } else if (rating >= 2.5) {
        selectElement.style.backgroundColor = '#f8d7da';
        selectElement.style.color = '#721c24';
    } else {
        selectElement.style.backgroundColor = '#fff';
        selectElement.style.color = '#333';
    }
} 