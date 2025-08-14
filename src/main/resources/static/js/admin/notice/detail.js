/**
 * 관리자 공지사항 상세 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자 공지사항 상세 페이지 로드됨');
    
    // 페이지 초기화
    initializeDetailPage();
});

/**
 * 상세 페이지 초기화
 */
function initializeDetailPage() {
    console.log('상세 페이지 초기화');
    
    // 조회수 증가 (필요한 경우)
    // incrementViewCount();
    
    // 내용 하이라이트
    highlightContent();
    
    // 버튼 이벤트 설정
    setupButtonEvents();
}

/**
 * 내용 하이라이트
 */
function highlightContent() {
    const contentElement = document.querySelector('.content-text');
    if (!contentElement) return;
    
    // 코드 블록 하이라이트 (간단한 버전)
    const codeBlocks = contentElement.querySelectorAll('pre code');
    codeBlocks.forEach(block => {
        block.style.backgroundColor = '#f8f9fa';
        block.style.padding = '8px';
        block.style.borderRadius = '4px';
        block.style.display = 'block';
        block.style.overflowX = 'auto';
    });
    
    // 인라인 코드 하이라이트
    const inlineCodes = contentElement.querySelectorAll('code:not(pre code)');
    inlineCodes.forEach(code => {
        code.style.backgroundColor = '#f8f9fa';
        code.style.padding = '2px 4px';
        code.style.borderRadius = '3px';
        code.style.fontFamily = 'monospace';
    });
}

/**
 * 버튼 이벤트 설정
 */
function setupButtonEvents() {
    // 수정 버튼
    const editButton = document.querySelector('a[href*="/edit"]');
    if (editButton) {
        editButton.addEventListener('click', function(e) {
            console.log('수정 버튼 클릭');
        });
    }
    
    // 목록 버튼
    const listButton = document.querySelector('a[href*="/admin/notice"]');
    if (listButton) {
        listButton.addEventListener('click', function(e) {
            console.log('목록 버튼 클릭');
        });
    }
}

/**
 * 조회수 증가 (필요한 경우)
 */
function incrementViewCount() {
    // AJAX로 조회수 증가 요청
    const noticeId = getNoticeIdFromUrl();
    if (!noticeId) return;
    
    fetch(`/admin/notice/${noticeId}/view`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        console.log('조회수 증가 완료:', data);
    })
    .catch(error => {
        console.error('조회수 증가 오류:', error);
    });
}

/**
 * URL에서 공지사항 ID 추출
 */
function getNoticeIdFromUrl() {
    const pathParts = window.location.pathname.split('/');
    const noticeIndex = pathParts.indexOf('notice');
    if (noticeIndex !== -1 && pathParts[noticeIndex + 1]) {
        return pathParts[noticeIndex + 1];
    }
    return null;
}

/**
 * 페이지 로드 완료 후 실행
 */
window.addEventListener('load', function() {
    console.log('페이지 로드 완료');
    
    // 추가 초기화 작업
    setupKeyboardShortcuts();
});

/**
 * 키보드 단축키 설정
 */
function setupKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + E: 수정 페이지로 이동
        if ((e.ctrlKey || e.metaKey) && e.key === 'e') {
            e.preventDefault();
            const editButton = document.querySelector('a[href*="/edit"]');
            if (editButton) {
                editButton.click();
            }
        }
        
        // Ctrl/Cmd + L: 목록으로 이동
        if ((e.ctrlKey || e.metaKey) && e.key === 'l') {
            e.preventDefault();
            const listButton = document.querySelector('a[href*="/admin/notice"]');
            if (listButton) {
                listButton.click();
            }
        }
        
        // ESC: 목록으로 이동
        if (e.key === 'Escape') {
            const listButton = document.querySelector('a[href*="/admin/notice"]');
            if (listButton) {
                listButton.click();
            }
        }
    });
}
