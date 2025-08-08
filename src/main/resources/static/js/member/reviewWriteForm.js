/**
 * 리뷰 작성 폼 JavaScript
 */
document.addEventListener('DOMContentLoaded', function() {
    
    // 평점 선택 시스템
    const ratingStars = document.getElementById('rating-stars');
    const ratingText = document.getElementById('rating-text');
    const ratingInput = document.getElementById('rating');
    const stars = document.querySelectorAll('.rating-stars .star');
    
    const ratingMessages = {
        0: '평점을 선택해주세요',
        1: '매우 나쁨 😞',
        2: '나쁨 😕',
        3: '보통 😐',
        4: '좋음 😊',
        5: '매우 좋음 😍'
    };
    
    let currentRating = 0;
    
    // 별점 클릭 이벤트
    stars.forEach((star, index) => {
        star.addEventListener('click', function() {
            const rating = index + 1;
            updateRating(rating);
        });
        
        star.addEventListener('mouseenter', function() {
            const rating = index + 1;
            highlightStars(rating);
        });
        
        star.addEventListener('mouseleave', function() {
            highlightStars(currentRating);
        });
    });
    
    function updateRating(rating) {
        currentRating = rating;
        ratingInput.value = rating;
        highlightStars(rating);
        ratingText.textContent = ratingMessages[rating];
        
        // 시각적 피드백
        ratingText.style.color = '#333';
        ratingText.style.fontWeight = '600';
    }
    
    function highlightStars(rating) {
        stars.forEach((star, index) => {
            if (index < rating) {
                star.classList.add('filled');
            } else {
                star.classList.remove('filled');
            }
        });
    }
    
    // 문자 수 카운터
    const titleInput = document.getElementById('title');
    const titleCharCount = document.getElementById('titleCharCount');
    
    function updateCharCount(input, counter, maxLength) {
        const length = input.value.length;
        counter.textContent = length;
        
        const charCountContainer = counter.parentElement;
        charCountContainer.classList.remove('warning', 'error');
        
        if (length > maxLength * 0.8) {
            charCountContainer.classList.add('warning');
        }
        if (length > maxLength) {
            charCountContainer.classList.add('error');
        }
    }
    
    // 초기 카운트 설정
    if (titleInput && titleCharCount) {
        updateCharCount(titleInput, titleCharCount, 100);
        titleInput.addEventListener('input', function() {
            updateCharCount(this, titleCharCount, 100);
        });
    }
    
    // Content textarea 이벤트 리스너 설정 - 더 직접적인 방법
    function setupContentCounter() {
        // 모든 textarea를 찾아서 content 관련된 것 찾기
        const textareas = document.querySelectorAll('textarea');
        let contentTextarea = null;
        
        for (let textarea of textareas) {
            if (textarea.name === 'content' || textarea.id === 'content') {
                contentTextarea = textarea;
                break;
            }
        }
        
        const contentCharCount = document.getElementById('contentCharCount');
        
        if (contentTextarea && contentCharCount) {
            updateCharCount(contentTextarea, contentCharCount, 2000);
            
            // 이벤트 리스너 추가
            contentTextarea.addEventListener('input', function() {
                updateCharCount(this, contentCharCount, 2000);
            });
            
            contentTextarea.addEventListener('keyup', function() {
                updateCharCount(this, contentCharCount, 2000);
            });
            
            contentTextarea.addEventListener('paste', function() {
                setTimeout(() => updateCharCount(this, contentCharCount, 2000), 0);
            });
            
            return true;
        } else {
            return false;
        }
    }
    
    // 초기 설정 시도
    if (!setupContentCounter()) {
        // DOM이 완전히 로드되지 않았을 수 있으므로 약간의 지연 후 다시 시도
        setTimeout(() => {
            setupContentCounter();
        }, 100);
    }
    
    // 폼 검증
    const reviewForm = document.querySelector('.review-form');
    
    if (reviewForm) {
        reviewForm.addEventListener('submit', function(e) {
            const title = document.getElementById('title').value.trim();
            const rating = document.getElementById('rating').value;
            const content = document.getElementById('content').value.trim();
            
            let isValid = true;
            let errorMessage = '';
            
            // 제목 검사
            if (title.length < 2) {
                errorMessage = '리뷰 제목은 최소 2자 이상 입력해주세요.';
                isValid = false;
            } else if (title.length > 100) {
                errorMessage = '리뷰 제목은 최대 100자까지 입력 가능합니다.';
                isValid = false;
            }
            
            // 평점 검사
            if (!rating || rating < 1) {
                errorMessage = '평점을 선택해주세요.';
                isValid = false;
            }
            
            // 내용 검사
            if (content.length < 10) {
                errorMessage = '리뷰 내용은 최소 10자 이상 입력해주세요.';
                isValid = false;
            } else if (content.length > 2000) {
                errorMessage = '리뷰 내용은 최대 2000자까지 입력 가능합니다.';
                isValid = false;
            }
            
            if (!isValid) {
                e.preventDefault();
                showNotification(errorMessage, 'warning');
                return false;
            }
            
            // 제출 버튼 비활성화
            const submitBtn = this.querySelector('button[type="submit"]');
            submitBtn.disabled = true;
            submitBtn.textContent = '리뷰 작성 중...';
        });
    }
    
    // 알림 표시 함수
    function showNotification(message, type = 'info') {
        // 기존 알림 제거
        const existingNotifications = document.querySelectorAll('.notification-toast');
        existingNotifications.forEach(notification => {
            notification.remove();
        });
        
        // 알림 요소 생성
        const notification = document.createElement('div');
        notification.className = `notification-toast notification-${type}`;
        
        // 아이콘 설정
        let icon = '';
        switch (type) {
            case 'success':
                icon = '✓';
                break;
            case 'error':
                icon = '✕';
                break;
            case 'warning':
                icon = '⚠';
                break;
            default:
                icon = 'ℹ';
        }
        
        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-icon">${icon}</span>
                <span class="notification-message">${message}</span>
            </div>
        `;
        
        // 스타일 적용
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : type === 'warning' ? '#ffc107' : '#007bff'};
            color: white;
            padding: 12px 20px;
            border-radius: 6px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
            z-index: 10000;
            font-weight: 500;
            opacity: 0;
            transform: translateX(100%);
            transition: all 0.3s ease;
            max-width: 400px;
            word-wrap: break-word;
        `;
        
        // DOM에 추가
        document.body.appendChild(notification);
        
        // 애니메이션
        requestAnimationFrame(() => {
            notification.style.opacity = '1';
            notification.style.transform = 'translateX(0)';
        });
        
        // 자동 제거
        setTimeout(() => {
            notification.style.opacity = '0';
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.remove();
                }
            }, 300);
        }, 3000);
    }
}); 