// Q&A 수정 페이지 JavaScript

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
    setupCharacterCounters();
});

// 이벤트 리스너 설정
function setupEventListeners() {
    // 폼 제출 이벤트
    document.getElementById('qnaEditForm').addEventListener('submit', function(e) {
        e.preventDefault();
        submitQnaEditForm();
    });

    // 제목 입력 이벤트
    document.getElementById('qnaTitle').addEventListener('input', function() {
        updateCharacterCount('qnaTitle', 'titleCharCount', 200);
    });

    // 내용 입력 이벤트
    document.getElementById('qnaContent').addEventListener('input', function() {
        updateCharacterCount('qnaContent', 'contentCharCount', 2000);
    });

    // 카테고리 선택 이벤트
    document.getElementById('qnaCategory').addEventListener('change', function() {
        validateCategory();
    });
}

// 문자 수 카운터 설정
function setupCharacterCounters() {
    // 초기 문자 수 설정
    updateCharacterCount('qnaTitle', 'titleCharCount', 200);
    updateCharacterCount('qnaContent', 'contentCharCount', 2000);
}

// 문자 수 업데이트
function updateCharacterCount(inputId, counterId, maxLength) {
    const input = document.getElementById(inputId);
    const counter = document.getElementById(counterId);
    
    if (input && counter) {
        const currentLength = input.value.length;
        counter.textContent = currentLength;
        
        // 최대 길이에 가까워지면 색상 변경
        if (currentLength > maxLength * 0.9) {
            counter.style.color = '#dc3545';
        } else if (currentLength > maxLength * 0.8) {
            counter.style.color = '#ffc107';
        } else {
            counter.style.color = '#666';
        }
    }
}

// 폼 유효성 검사
function validateForm() {
    const title = document.getElementById('qnaTitle').value.trim();
    const category = document.getElementById('qnaCategory').value;
    const content = document.getElementById('qnaContent').value.trim();
    
    // 제목 검사
    if (!title) {
        showFieldError('qnaTitle', '제목을 입력해주세요.');
        return false;
    }
    
    if (title.length > 200) {
        showFieldError('qnaTitle', '제목은 200자 이하여야 합니다.');
        return false;
    }
    
    // 카테고리 검사
    if (!category) {
        showFieldError('qnaCategory', '카테고리를 선택해주세요.');
        return false;
    }
    
    // 내용 검사
    if (!content) {
        showFieldError('qnaContent', '내용을 입력해주세요.');
        return false;
    }
    
    if (content.length > 2000) {
        showFieldError('qnaContent', '내용은 2000자 이하여야 합니다.');
        return false;
    }
    
    return true;
}

// 필드별 유효성 검사
function validateCategory() {
    const category = document.getElementById('qnaCategory').value;
    if (!category) {
        showFieldError('qnaCategory', '카테고리를 선택해주세요.');
    } else {
        clearFieldError('qnaCategory');
    }
}

// 필드 에러 표시
function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    if (!field) return;
    
    // 기존 에러 메시지 제거
    clearFieldError(fieldId);
    
    // 필드에 에러 클래스 추가
    field.classList.add('error');
    
    // 에러 메시지 생성
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    errorDiv.id = `${fieldId}-error`;
    
    // 에러 메시지 삽입
    field.parentNode.appendChild(errorDiv);
    
    // 필드에 포커스
    field.focus();
}

// 필드 에러 제거
function clearFieldError(fieldId) {
    const field = document.getElementById(fieldId);
    if (!field) return;
    
    // 에러 클래스 제거
    field.classList.remove('error');
    
    // 기존 에러 메시지 제거
    const existingError = document.getElementById(`${fieldId}-error`);
    if (existingError) {
        existingError.remove();
    }
}

// Q&A 수정 폼 제출
async function submitQnaEditForm() {
    // 유효성 검사
    if (!validateForm()) {
        return;
    }
    
    // 제출 버튼 비활성화
    const submitBtn = document.querySelector('#qnaEditForm button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = '수정 중...';
    
    try {
        // 폼 데이터 수집
        const formData = {
            title: document.getElementById('qnaTitle').value.trim(),
            categoryId: document.getElementById('qnaCategory').value,
            content: document.getElementById('qnaContent').value.trim()
        };
        
        // 상품 ID가 있는 경우 추가
        const productIdInput = document.querySelector('input[name="productId"]');
        if (productIdInput && productIdInput.value) {
            formData.productId = productIdInput.value;
        }
        
        // API 호출
        const result = await ajax.put(`/api/qna/${qnaId}`, formData);
        
        if (result.code === '00') {
            showToast('Q&A가 성공적으로 수정되었습니다.', 'success');
            
            // 성공 후 상세 페이지로 이동
            setTimeout(() => {
                location.href = `/qna/${qnaId}`;
            }, 1000);
        } else {
            showToast(result.message || 'Q&A 수정에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('Q&A 수정 실패:', error);
        showToast('Q&A 수정 중 오류가 발생했습니다.', 'error');
    } finally {
        // 제출 버튼 활성화
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    }
}

// 뒤로 가기
function goBack() {
    if (confirm('수정 중인 내용이 사라집니다. 정말 나가시겠습니까?')) {
        history.back();
    }
}

// 페이지 떠날 때 경고
window.addEventListener('beforeunload', function(e) {
    // 원본 값과 비교하여 변경사항이 있는지 확인
    const originalTitle = document.getElementById('qnaTitle').getAttribute('data-original') || '';
    const originalContent = document.getElementById('qnaContent').getAttribute('data-original') || '';
    
    const currentTitle = document.getElementById('qnaTitle').value.trim();
    const currentContent = document.getElementById('qnaContent').value.trim();
    
    if (originalTitle !== currentTitle || originalContent !== currentContent) {
        e.preventDefault();
        e.returnValue = '';
    }
});

// 전역 함수로 노출
window.submitQnaEditForm = submitQnaEditForm;
window.goBack = goBack;
