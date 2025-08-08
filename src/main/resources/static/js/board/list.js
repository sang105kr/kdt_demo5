/**
 * 게시판 목록 페이지 JavaScript
 * 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('게시판 목록 페이지 로드됨');
    
    // 전역 변수
    const boardTable = document.querySelector('.board-table');
    const searchForm = document.querySelector('.search-form');
    const searchInput = document.querySelector('.search-input');
    const clearSearchBtn = document.querySelector('.clear-search');
    const pagination = document.querySelector('.pagination');
    
    // 초기화
    initBoardList();
    
    /**
     * 게시판 목록 초기화
     */
    function initBoardList() {
        console.log('게시판 목록 초기화 시작');
        
        // 검색 기능 초기화
        initSearch();
        
        // 테이블 행 클릭 이벤트
        initTableRowClick();
        
        // 페이지네이션 초기화
        initPagination();
        
        // 반응형 처리
        initResponsive();
        
        console.log('게시판 목록 초기화 완료');
    }
    
    /**
     * 검색 기능 초기화
     */
    function initSearch() {
        if (!searchForm) return;
        
        // 검색 폼 제출 이벤트
        searchForm.addEventListener('submit', function(e) {
            const searchValue = searchInput.value.trim();
            
            if (searchValue === '') {
                e.preventDefault();
                showMessage('검색어를 입력해주세요.', 'warning');
                searchInput.focus();
                return;
            }
            
            console.log('검색 실행:', searchValue);
        });
        
        // 검색 입력 필드 이벤트
        if (searchInput) {
            searchInput.addEventListener('keyup', function(e) {
                if (e.key === 'Enter') {
                    searchForm.dispatchEvent(new Event('submit'));
                }
            });
            
            // 검색어 자동 완성 (선택적)
            searchInput.addEventListener('input', function() {
                const value = this.value.trim();
                if (value.length > 2) {
                    // 검색어 자동 완성 로직 (필요시 구현)
                }
            });
        }
        
        // 검색 초기화 버튼
        if (clearSearchBtn) {
            clearSearchBtn.addEventListener('click', function(e) {
                e.preventDefault();
                clearSearch();
            });
        }
    }
    
    /**
     * 검색 초기화
     */
    function clearSearch() {
        if (searchInput) {
            searchInput.value = '';
        }
        
        // 현재 페이지에서 검색 파라미터 제거하고 이동
        const currentUrl = new URL(window.location);
        currentUrl.searchParams.delete('search');
        currentUrl.searchParams.set('pageNo', '1');
        
        window.location.href = currentUrl.toString();
    }
    
    /**
     * 테이블 행 클릭 이벤트 초기화
     */
    function initTableRowClick() {
        if (!boardTable) return;
        
        const rows = boardTable.querySelectorAll('tbody tr');
        
        rows.forEach(row => {
            row.addEventListener('click', function(e) {
                // 링크나 버튼 클릭 시는 무시
                if (e.target.tagName === 'A' || e.target.tagName === 'BUTTON' || 
                    e.target.closest('a') || e.target.closest('button')) {
                    return;
                }
                
                const boardId = this.getAttribute('data-board-id');
                if (boardId) {
                    window.location.href = `/board/${boardId}`;
                }
            });
            
            // 호버 효과
            row.addEventListener('mouseenter', function() {
                this.style.cursor = 'pointer';
            });
        });
    }
    
    /**
     * 페이지네이션 초기화
     */
    function initPagination() {
        if (!pagination) return;
        
        const paginationBtns = pagination.querySelectorAll('.pagination-btn');
        
        paginationBtns.forEach(btn => {
            btn.addEventListener('click', function(e) {
                // 현재 페이지 버튼 클릭 시 무시
                if (this.classList.contains('btn--primary')) {
                    e.preventDefault();
                    return;
                }
                
                console.log('페이지 이동:', this.href);
            });
        });
    }
    
    /**
     * 반응형 처리
     */
    function initResponsive() {
        // 테이블 반응형 처리
        if (boardTable) {
            const handleResize = () => {
                if (window.innerWidth <= 768) {
                    boardTable.style.fontSize = '0.85rem';
                } else {
                    boardTable.style.fontSize = '';
                }
            };
            
            window.addEventListener('resize', handleResize);
            handleResize(); // 초기 실행
        }
        
        // 검색 폼 반응형 처리
        if (searchForm) {
            const handleSearchResize = () => {
                if (window.innerWidth <= 768) {
                    searchForm.style.flexDirection = 'column';
                } else {
                    searchForm.style.flexDirection = 'row';
                }
            };
            
            window.addEventListener('resize', handleSearchResize);
            handleSearchResize(); // 초기 실행
        }
    }
    
    /**
     * 메시지 표시
     */
    function showMessage(message, type = 'info') {
        // 기존 메시지 제거
        const existingMessage = document.querySelector('.message-toast');
        if (existingMessage) {
            existingMessage.remove();
        }
        
        // 새 메시지 생성
        const messageElement = document.createElement('div');
        messageElement.className = `message-toast message-${type}`;
        messageElement.textContent = message;
        
        // 스타일 적용
        messageElement.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            z-index: 1000;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            transform: translateX(100%);
            transition: transform 0.3s ease;
        `;
        
        // 타입별 색상
        const colors = {
            'info': '#007bff',
            'success': '#28a745',
            'warning': '#ffc107',
            'error': '#dc3545'
        };
        
        messageElement.style.backgroundColor = colors[type] || colors.info;
        
        // DOM에 추가
        document.body.appendChild(messageElement);
        
        // 애니메이션
        setTimeout(() => {
            messageElement.style.transform = 'translateX(0)';
        }, 100);
        
        // 자동 제거
        setTimeout(() => {
            messageElement.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (messageElement.parentNode) {
                    messageElement.remove();
                }
            }, 300);
        }, 3000);
    }
    
    /**
     * 로딩 상태 표시
     */
    function showLoading() {
        const loadingElement = document.createElement('div');
        loadingElement.className = 'loading-overlay';
        loadingElement.innerHTML = `
            <div class="loading-spinner">
                <div class="spinner"></div>
                <p>로딩 중...</p>
            </div>
        `;
        
        loadingElement.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
        `;
        
        document.body.appendChild(loadingElement);
        
        return loadingElement;
    }
    
    /**
     * 로딩 상태 제거
     */
    function hideLoading(loadingElement) {
        if (loadingElement && loadingElement.parentNode) {
            loadingElement.remove();
        }
    }
    
    /**
     * 테이블 정렬 기능 (선택적)
     */
    function initTableSort() {
        if (!boardTable) return;
        
        const headers = boardTable.querySelectorAll('th');
        
        headers.forEach((header, index) => {
            // 정렬 가능한 컬럼만
            if (index === 0 || index === 4 || index === 5) { // 번호, 조회수, 작성일
                header.style.cursor = 'pointer';
                header.addEventListener('click', function() {
                    sortTable(index);
                });
            }
        });
    }
    
    /**
     * 테이블 정렬
     */
    function sortTable(columnIndex) {
        const tbody = boardTable.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));
        
        rows.sort((a, b) => {
            const aValue = a.cells[columnIndex].textContent.trim();
            const bValue = b.cells[columnIndex].textContent.trim();
            
            // 숫자 정렬
            if (columnIndex === 0 || columnIndex === 4) {
                return parseInt(aValue) - parseInt(bValue);
            }
            
            // 날짜 정렬
            if (columnIndex === 5) {
                return new Date(aValue) - new Date(bValue);
            }
            
            return aValue.localeCompare(bValue);
        });
        
        // 정렬된 행을 다시 추가
        rows.forEach(row => tbody.appendChild(row));
    }
    
    // 테이블 정렬 기능 초기화 (선택적)
    // initTableSort();
    
    console.log('게시판 목록 JavaScript 로드 완료');
});
