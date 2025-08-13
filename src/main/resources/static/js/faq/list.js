/**
 * FAQ 목록 페이지 JavaScript
 */

// DOM 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', function() {
    console.log('FAQ 목록 페이지 로드됨');
    
    // FAQ 토글 기능 초기화
    initFaqToggle();
    
    // 검색 폼 초기화
    initSearchForm();
    
    // FAQ 상세 페이지 이동 기능 초기화
    initFaqDetailNavigation();
});

/**
 * FAQ 토글 기능 초기화
 */
function initFaqToggle() {
    // FAQ 내용 클릭 이벤트 (상세 페이지 이동과 구분)
    document.querySelectorAll('.faq-content').forEach(content => {
        content.addEventListener('click', function(e) {
            // 액션 버튼 클릭 시 토글 방지
            if (e.target.closest('.faq-actions')) {
                return;
            }
            toggleAnswer(this);
        });
    });
}

/**
 * FAQ 답변 토글
 */
function toggleAnswer(contentElement) {
    const faqItem = contentElement.closest('.faq-item');
    const answerElement = faqItem.querySelector('.faq-answer');
    const questionElement = faqItem.querySelector('.faq-question');
    
    // 현재 상태 확인
    const isActive = faqItem.classList.contains('active');
    
    // 모든 FAQ 닫기
    document.querySelectorAll('.faq-item').forEach(item => {
        item.classList.remove('active');
        const itemAnswer = item.querySelector('.faq-answer');
        if (itemAnswer) {
            itemAnswer.style.display = 'none';
        }
    });
    
    // 클릭한 FAQ가 닫혀있었다면 열기
    if (!isActive) {
        faqItem.classList.add('active');
        
        // 답변 표시
        if (answerElement) {
            answerElement.style.display = 'block';
            
            // 부드러운 애니메이션을 위한 스타일 적용
            answerElement.style.opacity = '0';
            answerElement.style.transform = 'translateY(-10px)';
            
            // 애니메이션 실행
            setTimeout(() => {
                answerElement.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                answerElement.style.opacity = '1';
                answerElement.style.transform = 'translateY(0)';
            }, 10);
        }
    }
}

/**
 * 도움됨/도움안됨 버튼 초기화 (상세 페이지에서만 사용)
 */
function initHelpfulButtons() {
    // 목록 페이지에서는 평가 버튼이 없으므로 아무것도 하지 않음
    console.log('목록 페이지에서는 평가 기능을 제공하지 않습니다.');
}

/**
 * FAQ 상세 페이지 이동 기능 초기화
 */
function initFaqDetailNavigation() {
    // FAQ 제목 클릭 시 상세 페이지로 이동
    document.querySelectorAll('.faq-question').forEach(question => {
        question.addEventListener('click', function(e) {
            e.stopPropagation();
            const faqItem = this.closest('.faq-item');
            const faqId = faqItem.dataset.faqId;
            
            if (faqId) {
                window.location.href = `/faq/${faqId}`;
            }
        });
    });
    
    // FAQ 제목에 커서 포인터 스타일 적용
    document.querySelectorAll('.faq-question').forEach(question => {
        question.style.cursor = 'pointer';
        question.title = '클릭하여 상세보기';
    });
}

/**
 * 검색 폼 초기화
 */
function initSearchForm() {
    const searchForm = document.querySelector('.search-form');
    const searchInput = document.querySelector('.search-input');
    const categorySelect = document.querySelector('.filter-select');
    
    // 검색 입력 필드 엔터 키 이벤트
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchForm.submit();
            }
        });
    }
    
    // 카테고리 선택 시 자동 제출
    if (categorySelect) {
        categorySelect.addEventListener('change', function() {
            searchForm.submit();
        });
    }
}

/**
 * 도움됨 수 증가
 */
async function incrementHelpful(faqId) {
    if (!faqId) {
        console.error('FAQ ID가 없습니다.');
        return;
    }
    
    try {
        showLoading();
        
        const response = await fetch(`/api/faq/${faqId}/helpful`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        });
        
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        
        const result = await response.json();
        
        if (result.code === 'SUCCESS') {
            // 성공 시 카운트 업데이트 (서버에서 받은 실제 값으로 업데이트)
            if (result.details && result.details.helpfulCount !== undefined) {
                updateHelpfulCount(faqId, result.details.helpfulCount);
            }
            
            // 성공 메시지 표시 (common.js의 showToast 사용)
            if (typeof showToast === 'function') {
                showToast('도움됨으로 표시되었습니다.', 'success');
            } else {
                showMessage('도움됨으로 표시되었습니다.', 'success');
            }
            
            // 버튼 상태 업데이트
            updateHelpfulButton(faqId, true);
        } else {
            throw new Error(result.message || '도움됨 수 증가에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('도움됨 수 증가 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || '도움됨 수 증가에 실패했습니다.', 'error');
        } else {
            showMessage(error.message || '도움됨 수 증가에 실패했습니다.', 'error');
        }
    } finally {
        hideLoading();
    }
}

/**
 * 도움안됨 수 증가
 */
async function incrementUnhelpful(faqId) {
    if (!faqId) {
        console.error('FAQ ID가 없습니다.');
        return;
    }
    
    try {
        showLoading();
        
        const response = await fetch(`/api/faq/${faqId}/unhelpful`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        });
        
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        
        const result = await response.json();
        
        if (result.code === 'SUCCESS') {
            // 성공 시 카운트 업데이트 (서버에서 받은 실제 값으로 업데이트)
            if (result.details && result.details.unhelpfulCount !== undefined) {
                updateUnhelpfulCount(faqId, result.details.unhelpfulCount);
            }
            
            // 성공 메시지 표시 (common.js의 showToast 사용)
            if (typeof showToast === 'function') {
                showToast('도움안됨으로 표시되었습니다.', 'success');
            } else {
                showMessage('도움안됨으로 표시되었습니다.', 'success');
            }
            
            // 버튼 상태 업데이트
            updateUnhelpfulButton(faqId, true);
        } else {
            throw new Error(result.message || '도움안됨 수 증가에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('도움안됨 수 증가 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || '도움안됨 수 증가에 실패했습니다.', 'error');
        } else {
            showMessage(error.message || '도움안됨 수 증가에 실패했습니다.', 'error');
        }
    } finally {
        hideLoading();
    }
}

/**
 * 도움됨 수 UI 업데이트
 */
function updateHelpfulCount(faqId, increment) {
    const faqItem = document.querySelector(`[data-faq-id="${faqId}"]`);
    if (!faqItem) return;
    
    const helpfulSpans = faqItem.querySelectorAll('.helpful span');
    helpfulSpans.forEach(span => {
        const currentCount = parseInt(span.textContent) || 0;
        span.textContent = currentCount + increment;
    });
}

/**
 * 도움안됨 수 UI 업데이트
 */
function updateUnhelpfulCount(faqId, increment) {
    const faqItem = document.querySelector(`[data-faq-id="${faqId}"]`);
    if (!faqItem) return;
    
    const unhelpfulSpans = faqItem.querySelectorAll('.unhelpful span');
    unhelpfulSpans.forEach(span => {
        const currentCount = parseInt(span.textContent) || 0;
        span.textContent = currentCount + increment;
    });
}

/**
 * 도움됨 버튼 상태 업데이트
 */
function updateHelpfulButton(faqId, isClicked) {
    const helpfulBtn = document.querySelector(`.helpful-btn[data-faq-id="${faqId}"]`);
    if (helpfulBtn && isClicked) {
        helpfulBtn.classList.add('clicked');
        helpfulBtn.disabled = true;
        helpfulBtn.innerHTML = '<i class="fas fa-thumbs-up"></i> 도움됨 ✓';
    }
}

/**
 * 도움안됨 버튼 상태 업데이트
 */
function updateUnhelpfulButton(faqId, isClicked) {
    const unhelpfulBtn = document.querySelector(`.unhelpful-btn[data-faq-id="${faqId}"]`);
    if (unhelpfulBtn && isClicked) {
        unhelpfulBtn.classList.add('clicked');
        unhelpfulBtn.disabled = true;
        unhelpfulBtn.innerHTML = '<i class="fas fa-thumbs-down"></i> 도움안됨 ✓';
    }
}

/**
 * 메시지 표시 (common.js의 showToast와 호환)
 */
function showMessage(message, type = 'info') {
    // common.js의 showToast 함수가 있으면 사용
    if (typeof showToast === 'function') {
        showToast(message, type);
    } else {
        // fallback 메시지 표시
        const existingMessage = document.querySelector('.message-toast');
        if (existingMessage) {
            existingMessage.remove();
        }
        
        const messageDiv = document.createElement('div');
        messageDiv.className = `message-toast message-${type}`;
        messageDiv.innerHTML = `
            <div class="message-content">
                <i class="fas ${getMessageIcon(type)}"></i>
                <span>${message}</span>
            </div>
        `;
        
        messageDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${getMessageColor(type)};
            color: white;
            padding: 1rem 1.5rem;
            border-radius: 4px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.2);
            z-index: 1000;
            max-width: 300px;
            animation: slideIn 0.3s ease-out;
        `;
        
        const messageContent = messageDiv.querySelector('.message-content');
        messageContent.style.cssText = `
            display: flex;
            align-items: center;
            gap: 0.5rem;
        `;
        
        document.body.appendChild(messageDiv);
        
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.style.animation = 'slideOut 0.3s ease-in';
                setTimeout(() => {
                    if (messageDiv.parentNode) {
                        messageDiv.remove();
                    }
                }, 300);
            }
        }, 3000);
    }
}

/**
 * 메시지 타입별 아이콘 반환
 */
function getMessageIcon(type) {
    switch (type) {
        case 'success': return 'fa-check-circle';
        case 'error': return 'fa-exclamation-circle';
        case 'warning': return 'fa-exclamation-triangle';
        default: return 'fa-info-circle';
    }
}

/**
 * 메시지 타입별 색상 반환
 */
function getMessageColor(type) {
    switch (type) {
        case 'success': return '#28a745';
        case 'error': return '#dc3545';
        case 'warning': return '#ffc107';
        default: return '#17a2b8';
    }
}

/**
 * 로딩 표시
 */
function showLoading() {
    // 로딩 인디케이터 표시 (필요시 구현)
    console.log('로딩 중...');
}

/**
 * 로딩 숨김
 */
function hideLoading() {
    // 로딩 인디케이터 숨김 (필요시 구현)
    console.log('로딩 완료');
}

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    .helpful-btn.clicked,
    .unhelpful-btn.clicked {
        background: #e9ecef !important;
        color: #6c757d !important;
        cursor: not-allowed;
    }
    
    .faq-answer {
        display: none;
        padding: 1rem 0;
        border-top: 1px solid #e9ecef;
        margin-top: 1rem;
    }
    
    .faq-item.active .faq-answer {
        display: block;
    }
`;
document.head.appendChild(style);

// 전역 함수로 내보내기
window.toggleAnswer = toggleAnswer;
