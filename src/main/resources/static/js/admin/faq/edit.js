/**
 * 관리자 FAQ 수정 페이지 JavaScript
 */

// DOM 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자 FAQ 수정 페이지 로드됨');
    
    // FAQ 수정 폼 초기화
    initFaqEditForm();
});

/**
 * FAQ 수정 폼 초기화
 */
function initFaqEditForm() {
    const form = document.getElementById('faqEditForm');
    
    if (!form) {
        console.error('FAQ 수정 폼을 찾을 수 없습니다.');
        return;
    }
    
    // 폼 제출 이벤트
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        submitFaqForm(this);
    });
    
    // 실시간 유효성 검사
    setupFormValidation();
    
    // 텍스트 영역 자동 크기 조정
    setupTextareaAutoResize();
}

/**
 * 폼 유효성 검사 설정
 */
function setupFormValidation() {
    const form = document.getElementById('faqEditForm');
    
    // 카테고리 선택 검사
    const categorySelect = form.querySelector('#categoryId');
    if (categorySelect) {
        categorySelect.addEventListener('change', function() {
            validateCategory(this);
        });
    }
    
    // 질문 입력 검사
    const questionInput = form.querySelector('#question');
    if (questionInput) {
        questionInput.addEventListener('input', function() {
            validateQuestion(this);
        });
    }
    
    // 답변 입력 검사
    const answerTextarea = form.querySelector('#answer');
    if (answerTextarea) {
        answerTextarea.addEventListener('input', function() {
            validateAnswer(this);
        });
    }
    
    // 키워드 입력 검사
    const keywordsInput = form.querySelector('#keywords');
    if (keywordsInput) {
        keywordsInput.addEventListener('input', function() {
            validateKeywords(this);
        });
    }
    
    // 정렬 순서 입력 검사
    const sortOrderInput = form.querySelector('#sortOrder');
    if (sortOrderInput) {
        sortOrderInput.addEventListener('input', function() {
            validateSortOrder(this);
        });
    }
}

/**
 * 텍스트 영역 자동 크기 조정
 */
function setupTextareaAutoResize() {
    const textarea = document.getElementById('answer');
    if (textarea) {
        textarea.addEventListener('input', function() {
            this.style.height = 'auto';
            this.style.height = Math.min(this.scrollHeight, 400) + 'px';
        });
        
        // 초기 크기 조정
        setTimeout(() => {
            textarea.style.height = 'auto';
            textarea.style.height = Math.min(textarea.scrollHeight, 400) + 'px';
        }, 100);
    }
}

/**
 * 카테고리 유효성 검사
 */
function validateCategory(select) {
    const errorElement = document.getElementById('categoryIdError');
    const value = select.value.trim();
    
    if (!value) {
        showFieldError(errorElement, '카테고리를 선택해주세요.');
        return false;
    }
    
    clearFieldError(errorElement);
    return true;
}

/**
 * 질문 유효성 검사
 */
function validateQuestion(input) {
    const errorElement = document.getElementById('questionError');
    const value = input.value.trim();
    
    if (!value) {
        showFieldError(errorElement, '질문을 입력해주세요.');
        return false;
    }
    
    if (value.length > 200) {
        showFieldError(errorElement, '질문은 200자 이내로 입력해주세요.');
        return false;
    }
    
    clearFieldError(errorElement);
    return true;
}

/**
 * 답변 유효성 검사
 */
function validateAnswer(textarea) {
    const errorElement = document.getElementById('answerError');
    const value = textarea.value.trim();
    
    if (!value) {
        showFieldError(errorElement, '답변을 입력해주세요.');
        return false;
    }
    
    if (value.length > 4000) {
        showFieldError(errorElement, '답변은 4000자 이내로 입력해주세요.');
        return false;
    }
    
    clearFieldError(errorElement);
    return true;
}

/**
 * 키워드 유효성 검사
 */
function validateKeywords(input) {
    const errorElement = document.getElementById('keywordsError');
    const value = input.value.trim();
    
    if (value && value.length > 500) {
        showFieldError(errorElement, '키워드는 500자 이내로 입력해주세요.');
        return false;
    }
    
    clearFieldError(errorElement);
    return true;
}

/**
 * 정렬 순서 유효성 검사
 */
function validateSortOrder(input) {
    const errorElement = document.getElementById('sortOrderError');
    const value = input.value.trim();
    
    if (value) {
        const numValue = parseInt(value);
        if (isNaN(numValue) || numValue < 0 || numValue > 9999) {
            showFieldError(errorElement, '정렬 순서는 0~9999 사이의 숫자로 입력해주세요.');
            return false;
        }
    }
    
    clearFieldError(errorElement);
    return true;
}

/**
 * 필드 에러 표시
 */
function showFieldError(errorElement, message) {
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'flex';
    }
}

/**
 * 필드 에러 제거
 */
function clearFieldError(errorElement) {
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }
}

/**
 * 전체 폼 유효성 검사
 */
function validateForm(form) {
    let isValid = true;
    
    // 각 필드 검사
    const categorySelect = form.querySelector('#categoryId');
    const questionInput = form.querySelector('#question');
    const answerTextarea = form.querySelector('#answer');
    const keywordsInput = form.querySelector('#keywords');
    const sortOrderInput = form.querySelector('#sortOrder');
    
    if (categorySelect && !validateCategory(categorySelect)) {
        isValid = false;
    }
    
    if (questionInput && !validateQuestion(questionInput)) {
        isValid = false;
    }
    
    if (answerTextarea && !validateAnswer(answerTextarea)) {
        isValid = false;
    }
    
    if (keywordsInput && !validateKeywords(keywordsInput)) {
        isValid = false;
    }
    
    if (sortOrderInput && !validateSortOrder(sortOrderInput)) {
        isValid = false;
    }
    
    return isValid;
}

/**
 * FAQ 폼 제출
 */
async function submitFaqForm(form) {
    // 폼 유효성 검사
    if (!validateForm(form)) {
        if (typeof showToast === 'function') {
            showToast('입력 내용을 확인해주세요.', 'error');
        } else {
            alert('입력 내용을 확인해주세요.');
        }
        return;
    }
    
    // FAQ ID 가져오기
    const faqId = form.querySelector('#faqId').value;
    if (!faqId) {
        console.error('FAQ ID가 없습니다.');
        return;
    }
    
    // 폼 데이터 수집
    const formData = {
        categoryId: form.querySelector('#categoryId').value,
        question: form.querySelector('#question').value.trim(),
        answer: form.querySelector('#answer').value.trim(),
        keywords: form.querySelector('#keywords').value.trim(),
        sortOrder: form.querySelector('#sortOrder').value || 0,
        isActive: form.querySelector('input[name="isActive"]:checked').value
    };
    
    try {
        showLoading();
        
        // common.js의 ajax 객체 사용
        if (typeof ajax !== 'undefined' && ajax.put) {
            const result = await ajax.put(`/admin/faq/${faqId}`, formData);
            
            if (result && result.code === '00') {
                if (typeof showToast === 'function') {
                    showToast('FAQ가 성공적으로 수정되었습니다.', 'success');
                } else {
                    alert('FAQ가 성공적으로 수정되었습니다.');
                }
                
                // FAQ 상세 페이지로 이동
                setTimeout(() => {
                    window.location.href = `/admin/faq/${faqId}`;
                }, 1000);
                
            } else {
                throw new Error(result?.message || 'FAQ 수정에 실패했습니다.');
            }
        } else {
            throw new Error('AJAX 기능을 사용할 수 없습니다.');
        }
        
    } catch (error) {
        console.error('FAQ 수정 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || 'FAQ 수정에 실패했습니다.', 'error');
        } else {
            alert(error.message || 'FAQ 수정에 실패했습니다.');
        }
    } finally {
        hideLoading();
    }
}

/**
 * 로딩 표시
 */
function showLoading() {
    // 로딩 인디케이터 표시 로직
    if (typeof showLoadingIndicator === 'function') {
        showLoadingIndicator();
    }
    
    // 제출 버튼 비활성화
    const submitBtn = document.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 수정 중...';
    }
}

/**
 * 로딩 숨김
 */
function hideLoading() {
    // 로딩 인디케이터 숨김 로직
    if (typeof hideLoadingIndicator === 'function') {
        hideLoadingIndicator();
    }
    
    // 제출 버튼 활성화
    const submitBtn = document.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-save"></i> FAQ 수정';
    }
}

// 전역 함수로 내보내기
window.submitFaqForm = submitFaqForm;
