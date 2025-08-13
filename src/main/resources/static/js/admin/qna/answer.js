// 관리자 Q&A 답변 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeAnswerPage();
});

/**
 * 답변 페이지 초기화
 */
function initializeAnswerPage() {
    setupTextarea();
    setupForm();
    setupAccessibility();
}

/**
 * 텍스트 영역 설정
 */
function setupTextarea() {
    const textarea = document.getElementById('answer');
    const charCount = document.querySelector('.char-count .current-count');
    const maxCount = document.querySelector('.char-count .max-count');
    
    if (textarea && charCount && maxCount) {
        const maxLength = 2000;
        maxCount.textContent = maxLength;
        
        // 초기 글자 수 설정
        updateCharCount(textarea, charCount, maxLength);
        
        // 입력 시 글자 수 업데이트
        textarea.addEventListener('input', function() {
            updateCharCount(this, charCount, maxLength);
        });
        
        // 포커스 효과
        textarea.addEventListener('focus', function() {
            this.style.borderColor = '#000000';
            this.style.boxShadow = '0 0 0 3px rgba(0, 0, 0, 0.1)';
        });
        
        textarea.addEventListener('blur', function() {
            this.style.borderColor = '';
            this.style.boxShadow = '';
        });
    }
}

/**
 * 글자 수 업데이트
 */
function updateCharCount(textarea, charCountElement, maxLength) {
    const currentLength = textarea.value.length;
    charCountElement.textContent = currentLength;
    
    if (currentLength > maxLength) {
        charCountElement.style.color = '#dc3545';
        textarea.value = textarea.value.substring(0, maxLength);
        charCountElement.textContent = maxLength;
    } else if (currentLength > maxLength * 0.9) {
        charCountElement.style.color = '#ffc107';
    } else {
        charCountElement.style.color = '';
    }
}

/**
 * 폼 설정
 */
function setupForm() {
    const form = document.querySelector('.answer-form');
    
    if (form) {
        form.addEventListener('submit', function(e) {
            // 일반 폼 제출 사용 (AJAX 비활성화)
            // e.preventDefault();
            // submitAnswer(this);
            
            // 폼 유효성 검사만 수행
            const answer = this.querySelector('#answer').value.trim();
            
            if (!answer) {
                e.preventDefault();
                if (typeof showToast === 'function') {
                    showToast('답변 내용을 입력해주세요.', 'error');
                } else {
                    alert('답변 내용을 입력해주세요.');
                }
                return;
            }
            
            if (answer.length > 2000) {
                e.preventDefault();
                if (typeof showToast === 'function') {
                    showToast('답변 내용은 2000자를 초과할 수 없습니다.', 'error');
                } else {
                    alert('답변 내용은 2000자를 초과할 수 없습니다.');
                }
                return;
            }
            
            // 폼 제출 진행 (기본 동작)
        });
    }
}

/**
 * 답변 제출 (AJAX 방식 - 현재 비활성화)
 */
/*
async function submitAnswer(form) {
    const answer = form.querySelector('#answer').value.trim();
    
    if (!answer) {
        if (typeof showToast === 'function') {
            showToast('답변 내용을 입력해주세요.', 'error');
        } else {
            alert('답변 내용을 입력해주세요.');
        }
        return;
    }
    
    if (answer.length > 2000) {
        if (typeof showToast === 'function') {
            showToast('답변 내용은 2000자를 초과할 수 없습니다.', 'error');
        } else {
            alert('답변 내용은 2000자를 초과할 수 없습니다.');
        }
        return;
    }
    
    try {
        showLoading();
        
        // FormData를 직접 사용하여 전송
        const formData = new FormData(form);

        // common.js의 ajax 객체 사용
        if (typeof ajax !== 'undefined' && ajax.post) {
            const result = await ajax.post(form.action, formData);
            
            if (result && result.code === '00') {
                if (typeof showToast === 'function') {
                    showToast('답변이 성공적으로 등록되었습니다.', 'success');
                } else {
                    alert('답변이 성공적으로 등록되었습니다.');
                }
                
                // 관리자 Q&A 목록으로 리다이렉트
                setTimeout(() => {
                    window.location.href = '/admin/qna';
                }, 1000);
                
            } else {
                throw new Error(result?.message || '답변 등록에 실패했습니다.');
            }
        } else {
            throw new Error('AJAX 기능을 사용할 수 없습니다.');
        }
        
    } catch (error) {
        console.error('답변 등록 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || '답변 등록에 실패했습니다.', 'error');
        } else {
            alert(error.message || '답변 등록에 실패했습니다.');
        }
    } finally {
        hideLoading();
    }
}
*/

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
 * 뒤로가기 버튼 클릭 처리
 */
function handleBackButton() {
    const backBtn = document.querySelector('.back-btn');
    if (backBtn) {
        backBtn.addEventListener('click', function(e) {
            // 클릭 효과
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
    }
}

// 페이지 로드 완료 후 추가 설정
window.addEventListener('load', function() {
    handleBackButton();
});
