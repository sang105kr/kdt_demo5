/**
 * 관리자 공지사항 작성 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('공지사항 작성 페이지 로드됨');
    
    // 폼 요소들
    const form = document.querySelector('.notice-form');
    const titleInput = document.getElementById('title');
    const categorySelect = document.getElementById('categoryId');
    const statusSelect = document.getElementById('statusId');
    const contentTextarea = document.getElementById('content');
    const importantCheckbox = document.querySelector('input[name="isImportant"]');
    const fixedCheckbox = document.querySelector('input[name="isFixed"]');
    
    // 자동 저장 기능
    let autoSaveTimer;
    
    // 폼 데이터 자동 저장
    function autoSave() {
        const formData = {
            title: titleInput.value,
            categoryId: categorySelect.value,
            statusId: statusSelect.value,
            content: contentTextarea.value,
            isImportant: importantCheckbox.checked ? 'Y' : 'N',
            isFixed: fixedCheckbox.checked ? 'Y' : 'N'
        };
        
        localStorage.setItem('noticeWriteDraft', JSON.stringify(formData));
        console.log('자동 저장 완료');
    }
    
    // 자동 저장 타이머 설정
    function setupAutoSave() {
        [titleInput, categorySelect, statusSelect, contentTextarea, importantCheckbox, fixedCheckbox].forEach(element => {
            element.addEventListener('input', () => {
                clearTimeout(autoSaveTimer);
                autoSaveTimer = setTimeout(autoSave, 2000); // 2초 후 자동 저장
            });
            
            element.addEventListener('change', () => {
                clearTimeout(autoSaveTimer);
                autoSaveTimer = setTimeout(autoSave, 1000); // 1초 후 자동 저장
            });
        });
    }
    
    // 저장된 데이터 복원
    function restoreDraft() {
        const savedData = localStorage.getItem('noticeWriteDraft');
        if (savedData) {
            try {
                const data = JSON.parse(savedData);
                titleInput.value = data.title || '';
                categorySelect.value = data.categoryId || '';
                statusSelect.value = data.statusId || '';
                contentTextarea.value = data.content || '';
                importantCheckbox.checked = data.isImportant === 'Y';
                fixedCheckbox.checked = data.isFixed === 'Y';
                console.log('임시 저장 데이터 복원됨');
            } catch (error) {
                console.error('임시 저장 데이터 복원 실패:', error);
            }
        }
    }
    
    // 폼 제출 처리
    function handleFormSubmit(event) {
        event.preventDefault();
        
        const contentValue = contentTextarea.value || contentTextarea.textContent || contentTextarea.innerText || '';
        
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
        
        // 자동 저장 데이터 삭제
        localStorage.removeItem('noticeWriteDraft');
        
        // 폼 제출
        form.submit();
    }
    
    // 페이지 이탈 경고 (비활성화)
    function setupBeforeUnload() {
        // 페이지 이탈 경고 비활성화
    }
    
    // 초기화
    function init() {
        setupAutoSave();
        restoreDraft();
        setupBeforeUnload();
        
        // 폼 제출 이벤트
        form.addEventListener('submit', handleFormSubmit);
        
        // 취소 버튼 클릭 시 임시 저장 데이터 삭제
        const cancelBtn = document.querySelector('.btn--outline');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', function() {
                localStorage.removeItem('noticeWriteDraft');
            });
        }
    }
    
    // 페이지 로드 시 초기화
    init();
});
