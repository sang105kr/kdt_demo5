/**
 * 답글 작성 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // 취소 버튼 이벤트 처리
    const cancelBtn = document.querySelector('button[type="reset"]');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', function(e) {
            e.preventDefault();
            showModal({
                title: '작성 취소',
                message: '작성 중인 내용이 사라집니다.\n정말 취소하시겠습니까?',
                onConfirm: () => {
                    // 원글 상세 페이지로 이동
                    const boardId = getBoardIdFromUrl();
                    if (boardId) {
                        window.location.href = `/board/${boardId}`;
                    } else {
                        window.location.href = '/board';
                    }
                },
                onCancel: () => {
                    // 취소 시 아무것도 하지 않음
                }
            });
        });
    }
    
    // 폼 제출 전 유효성 검사
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            if (!validateForm()) {
                e.preventDefault();
            }
        });
    }
    
    // 카테고리 자동 선택 (원글과 동일하게)
    autoSelectCategory();
});

/**
 * URL에서 게시글 ID 추출
 */
function getBoardIdFromUrl() {
    const pathSegments = window.location.pathname.split('/');
    const boardIndex = pathSegments.indexOf('board');
    if (boardIndex !== -1 && pathSegments[boardIndex + 1]) {
        return pathSegments[boardIndex + 1];
    }
    return null;
}

/**
 * 폼 유효성 검사
 */
function validateForm() {
    const title = document.getElementById('title').value.trim();
    const bcontent = document.getElementById('bcontent').value.trim();
    const bcategory = document.getElementById('bcategory').value;
    
    // 제목 검사
    if (!title) {
        alert('제목을 입력해주세요.');
        document.getElementById('title').focus();
        return false;
    }
    
    if (title.length < 2) {
        alert('제목은 2자 이상 입력해주세요.');
        document.getElementById('title').focus();
        return false;
    }
    
    // 내용 검사
    if (!bcontent) {
        alert('내용을 입력해주세요.');
        document.getElementById('bcontent').focus();
        return false;
    }
    
    if (bcontent.length < 10) {
        alert('내용은 10자 이상 입력해주세요.');
        document.getElementById('bcontent').focus();
        return false;
    }
    
    // 카테고리 검사
    if (!bcategory) {
        alert('카테고리를 선택해주세요.');
        document.getElementById('bcategory').focus();
        return false;
    }
    
    return true;
}

/**
 * 카테고리 자동 선택 (원글과 동일하게)
 */
function autoSelectCategory() {
    // 원글 정보에서 카테고리 정보를 가져와서 자동 선택
    // 실제 구현에서는 서버에서 원글 정보를 전달받아 처리
    const originalPostCategory = document.querySelector('[data-original-category]')?.dataset.originalCategory;
    if (originalPostCategory) {
        const categorySelect = document.getElementById('bcategory');
        if (categorySelect) {
            categorySelect.value = originalPostCategory;
        }
    }
}

/**
 * 제목 자동 생성 (선택사항)
 */
function autoGenerateTitle() {
    const titleInput = document.getElementById('title');
    if (titleInput && !titleInput.value.trim()) {
        titleInput.value = 'Re: 답글';
    }
} 