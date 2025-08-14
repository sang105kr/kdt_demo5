/**
 * 관리자 공지사항 수정 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('공지사항 수정 페이지 로드됨');
    
    // 폼 요소들
    const form = document.querySelector('.notice-form');
    const titleInput = document.getElementById('title');
    const categorySelect = document.getElementById('categoryId');
    const statusSelect = document.getElementById('statusId');
    const contentTextarea = document.getElementById('content');
    const importantCheckbox = document.querySelector('input[name="isImportant"]');
    const fixedCheckbox = document.querySelector('input[name="isFixed"]');
    
    // DOM 요소 존재 확인
    if (!form) {
        console.error('폼을 찾을 수 없습니다.');
        return;
    }
    
    if (!titleInput || !categorySelect || !statusSelect || !contentTextarea || !importantCheckbox || !fixedCheckbox) {
        console.error('필수 폼 요소를 찾을 수 없습니다.');
        return;
    }
    
    // 원본 데이터 저장
    let originalData = {
        title: titleInput.value || '',
        categoryId: categorySelect.value || '',
        statusId: statusSelect.value || '',
        content: contentTextarea.value || contentTextarea.textContent || contentTextarea.innerText || '',
        isImportant: importantCheckbox.checked ? 'Y' : 'N',
        isFixed: fixedCheckbox.checked ? 'Y' : 'N'
    };
    
    console.log('원본 데이터:', originalData);
    console.log('제목 값:', titleInput.value);
    console.log('내용 값 (value):', contentTextarea.value);
    console.log('내용 값 (textContent):', contentTextarea.textContent);
    console.log('내용 값 (innerText):', contentTextarea.innerText);
    console.log('내용 길이:', contentTextarea.value ? contentTextarea.value.length : 0);
    
    // 변경 감지
    function hasChanges() {
        const currentData = {
            title: titleInput.value || '',
            categoryId: categorySelect.value || '',
            statusId: statusSelect.value || '',
            content: contentTextarea.value || contentTextarea.textContent || contentTextarea.innerText || '',
            isImportant: importantCheckbox.checked ? 'Y' : 'N',
            isFixed: fixedCheckbox.checked ? 'Y' : 'N'
        };
        
        const hasChanged = JSON.stringify(originalData) !== JSON.stringify(currentData);
        console.log('변경사항 감지:', hasChanged);
        return hasChanged;
    }
    
    // 폼 제출 처리
    function handleFormSubmit(event) {
        event.preventDefault();
        console.log('폼 제출 처리 시작');
        
        const contentValue = contentTextarea.value || contentTextarea.textContent || contentTextarea.innerText || '';
        
        console.log('제출 시 제목 값:', titleInput.value);
        console.log('제출 시 내용 값 (value):', contentTextarea.value);
        console.log('제출 시 내용 값 (textContent):', contentTextarea.textContent);
        console.log('제출 시 내용 값 (innerText):', contentTextarea.innerText);
        console.log('제출 시 내용 값 (최종):', contentValue);
        console.log('제출 시 내용 길이:', contentValue.length);
        
        // 필수 필드 검증
        if (!titleInput.value || !titleInput.value.trim()) {
            alert('제목을 입력해주세요.');
            titleInput.focus();
            return;
        }
        
        if (!categorySelect.value) {
            alert('카테고리를 선택해주세요.');
            categorySelect.focus();
            return;
        }
        
        if (!contentValue || !contentValue.trim()) {
            alert('내용을 입력해주세요.');
            contentTextarea.focus();
            return;
        }
        
        // 변경사항 확인
        if (!hasChanges()) {
            alert('변경된 내용이 없습니다.');
            return;
        }
        
        console.log('폼 제출 실행');
        // 폼 제출
        form.submit();
    }
    
    // 페이지 이탈 경고 (비활성화)
    function setupBeforeUnload() {
        // 페이지 이탈 경고 비활성화
    }
    
    // 취소 버튼 처리
    function handleCancel() {
        history.back();
    }
    
    // 초기화
    function init() {
        console.log('초기화 시작');
        
        setupBeforeUnload();
        
        // 폼 제출 이벤트
        form.addEventListener('submit', handleFormSubmit);
        
        // 취소 버튼 클릭 시
        const cancelBtn = document.querySelector('.btn--outline');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', handleCancel);
        }
        
        // 키보드 단축키
        document.addEventListener('keydown', function(event) {
            // Ctrl+S: 저장
            if (event.ctrlKey && event.key === 's') {
                event.preventDefault();
                form.dispatchEvent(new Event('submit'));
            }
            
            // Esc: 취소
            if (event.key === 'Escape') {
                handleCancel();
            }
        });
        
        console.log('초기화 완료');
    }
    
    // 페이지 로드 시 초기화
    init();
});
