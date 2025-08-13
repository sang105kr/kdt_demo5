/**
 * 공지사항 목록 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    
    // 필터 변경 시 자동 폼 제출
    const categoryFilter = document.getElementById('categoryFilter');
    const sortFilter = document.getElementById('sortFilter');
    const searchForm = document.getElementById('searchForm');
    
    if (categoryFilter) {
        categoryFilter.addEventListener('change', function() {
            // 검색 폼에 카테고리 값 추가
            const categoryInput = searchForm.querySelector('input[name="categoryId"]');
            if (!categoryInput) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'categoryId';
                input.value = this.value;
                searchForm.appendChild(input);
            } else {
                categoryInput.value = this.value;
            }
            
            // 페이지를 1로 리셋
            const pageInput = searchForm.querySelector('input[name="page"]');
            if (!pageInput) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'page';
                input.value = '1';
                searchForm.appendChild(input);
            } else {
                pageInput.value = '1';
            }
            
            searchForm.submit();
        });
    }
    
    if (sortFilter) {
        sortFilter.addEventListener('change', function() {
            // 검색 폼에 정렬 값 추가
            const sortInput = searchForm.querySelector('input[name="sortBy"]');
            if (!sortInput) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'sortBy';
                input.value = this.value;
                searchForm.appendChild(input);
            } else {
                sortInput.value = this.value;
            }
            
            // 페이지를 1로 리셋
            const pageInput = searchForm.querySelector('input[name="page"]');
            if (!pageInput) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'page';
                input.value = '1';
                searchForm.appendChild(input);
            } else {
                pageInput.value = '1';
            }
            
            searchForm.submit();
        });
    }
    
    // 검색어 입력란 포커스 시 전체 선택
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('focus', function() {
            this.select();
        });
    }
    
    // 검색 폼 제출 시 빈 검색어 처리
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            const searchKeyword = this.querySelector('input[name="searchKeyword"]');
            if (searchKeyword && searchKeyword.value.trim() === '') {
                searchKeyword.value = '';
            }
        });
    }
    
    // 중요/고정 공지사항 행 스타일링
    const importantRows = document.querySelectorAll('tr.important');
    const fixedRows = document.querySelectorAll('tr.fixed');
    
    importantRows.forEach(row => {
        row.style.fontWeight = 'bold';
    });
    
    fixedRows.forEach(row => {
        row.style.backgroundColor = '#f8f9fa';
    });
    
    // 테이블 행 호버 효과
    const tableRows = document.querySelectorAll('.board-table tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.backgroundColor = '#f8f9fa';
        });
        
        row.addEventListener('mouseleave', function() {
            if (!this.classList.contains('fixed')) {
                this.style.backgroundColor = '';
            }
        });
    });
    
    // 페이지네이션 버튼 스타일링
    const paginationButtons = document.querySelectorAll('.pagination-btn');
    paginationButtons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            if (!this.classList.contains('active')) {
                this.style.backgroundColor = '#f8f9fa';
            }
        });
        
        button.addEventListener('mouseleave', function() {
            if (!this.classList.contains('active')) {
                this.style.backgroundColor = '';
            }
        });
    });
    
    console.log('공지사항 목록 페이지 JavaScript 로드 완료');
});
