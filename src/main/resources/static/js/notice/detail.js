/**
 * 공지사항 상세 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    
    // 뒤로가기 버튼 이벤트
    const backBtn = document.querySelector('.back-btn');
    if (backBtn) {
        backBtn.addEventListener('click', function(e) {
            e.preventDefault();
            history.back();
        });
    }
    
    // 공유하기 버튼 이벤트
    const shareBtn = document.querySelector('.share-btn');
    if (shareBtn) {
        shareBtn.addEventListener('click', function(e) {
            e.preventDefault();
            shareNotice();
        });
    }
    
    // 목록보기 버튼 이벤트
    const listBtn = document.querySelector('.list-btn');
    if (listBtn) {
        listBtn.addEventListener('click', function(e) {
            // 기본 링크 동작 사용
        });
    }
    
    // 중요/고정 공지사항 스타일링
    const noticeHeader = document.querySelector('.notice-header');
    if (noticeHeader) {
        if (noticeHeader.classList.contains('important')) {
            noticeHeader.style.borderLeftColor = '#000';
        }
        if (noticeHeader.classList.contains('fixed')) {
            noticeHeader.style.borderLeftColor = '#666';
        }
        if (noticeHeader.classList.contains('important') && noticeHeader.classList.contains('fixed')) {
            noticeHeader.style.borderLeftColor = '#333';
        }
    }
    
    // 카테고리 배지 스타일링
    const categoryBadges = document.querySelectorAll('.category-badge');
    categoryBadges.forEach(badge => {
        badge.style.backgroundColor = '#000';
        badge.style.color = '#fff';
    });
    
    // 플래그 스타일링
    const flags = document.querySelectorAll('.flag');
    flags.forEach(flag => {
        if (flag.classList.contains('important')) {
            flag.style.color = '#000';
            flag.style.fontWeight = 'bold';
        }
        if (flag.classList.contains('fixed')) {
            flag.style.color = '#666';
            flag.style.fontWeight = 'bold';
        }
    });
    
    // 내용 영역 스타일링
    const contentBody = document.querySelector('.content-body');
    if (contentBody) {
        // 테이블 스타일링
        const tables = contentBody.querySelectorAll('table');
        tables.forEach(table => {
            table.style.width = '100%';
            table.style.borderCollapse = 'collapse';
            table.style.margin = '15px 0';
            table.style.backgroundColor = '#fff';
            table.style.border = '1px solid #ddd';
            table.style.borderRadius = '4px';
            table.style.overflow = 'hidden';
        });
        
        // 테이블 헤더 스타일링
        const tableHeaders = contentBody.querySelectorAll('th');
        tableHeaders.forEach(th => {
            th.style.backgroundColor = '#f8f9fa';
            th.style.padding = '10px';
            th.style.border = '1px solid #dee2e6';
            th.style.fontWeight = '600';
            th.style.color = '#333';
        });
        
        // 테이블 셀 스타일링
        const tableCells = contentBody.querySelectorAll('td');
        tableCells.forEach(td => {
            td.style.padding = '10px';
            td.style.border = '1px solid #dee2e6';
        });
        
        // 인용구 스타일링
        const blockquotes = contentBody.querySelectorAll('blockquote');
        blockquotes.forEach(blockquote => {
            blockquote.style.margin = '20px 0';
            blockquote.style.padding = '16px 20px';
            blockquote.style.background = '#f8f9fa';
            blockquote.style.borderLeft = '4px solid #000';
            blockquote.style.borderRadius = '0 4px 4px 0';
            blockquote.style.fontStyle = 'italic';
            blockquote.style.color = '#333';
        });
        
        // 코드 블록 스타일링
        const codeBlocks = contentBody.querySelectorAll('code');
        codeBlocks.forEach(code => {
            code.style.background = '#f8f9fa';
            code.style.padding = '2px 6px';
            code.style.borderRadius = '4px';
            code.style.fontFamily = "'Courier New', monospace";
            code.style.fontSize = '14px';
            code.style.color = '#333';
            code.style.border = '1px solid #ddd';
        });
        
        // 미리보기 블록 스타일링
        const preBlocks = contentBody.querySelectorAll('pre');
        preBlocks.forEach(pre => {
            pre.style.background = '#000';
            pre.style.color = '#fff';
            pre.style.padding = '16px';
            pre.style.borderRadius = '4px';
            pre.style.overflowX = 'auto';
            pre.style.margin = '16px 0';
        });
        
        // 제목 스타일링
        const headings = contentBody.querySelectorAll('h1, h2, h3, h4, h5, h6');
        headings.forEach(heading => {
            heading.style.color = '#000';
            heading.style.fontWeight = '600';
            heading.style.margin = '24px 0 16px 0';
        });
        
        // 강조 텍스트 스타일링
        const strongElements = contentBody.querySelectorAll('strong');
        strongElements.forEach(strong => {
            strong.style.fontWeight = '600';
            strong.style.color = '#000';
        });
        
        // 이탤릭 텍스트 스타일링
        const emElements = contentBody.querySelectorAll('em');
        emElements.forEach(em => {
            em.style.fontStyle = 'italic';
            em.style.color = '#333';
        });
    }
    
    // 네비게이션 버튼 스타일링
    const navButtons = document.querySelectorAll('.nav-btn');
    navButtons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-1px)';
        });
        
        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
    
    console.log('공지사항 상세 페이지 JavaScript 로드 완료');
});

/**
 * 공지사항 공유하기 기능
 */
function shareNotice() {
    const title = document.querySelector('.notice-title');
    const url = window.location.href;
    
    if (navigator.share) {
        // Web Share API 지원 브라우저
        navigator.share({
            title: title ? title.textContent : '공지사항',
            url: url
        }).then(() => {
            console.log('공유 성공');
        }).catch((error) => {
            console.log('공유 실패:', error);
            fallbackShare(title, url);
        });
    } else {
        // Web Share API 미지원 브라우저
        fallbackShare(title, url);
    }
}

/**
 * 공유하기 폴백 함수
 */
function fallbackShare(title, url) {
    // 클립보드에 URL 복사
    if (navigator.clipboard) {
        navigator.clipboard.writeText(url).then(() => {
            alert('URL이 클립보드에 복사되었습니다.');
        }).catch(() => {
            // 클립보드 복사 실패 시 URL 표시
            prompt('공지사항 URL을 복사하세요:', url);
        });
    } else {
        // 클립보드 API 미지원 시 URL 표시
        prompt('공지사항 URL을 복사하세요:', url);
    }
}

/**
 * 페이지 인쇄 기능
 */
function printNotice() {
    window.print();
}

/**
 * 페이지 새로고침
 */
function refreshPage() {
    location.reload();
}

