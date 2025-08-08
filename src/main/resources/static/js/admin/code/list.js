/**
 * 관리자 코드 목록 페이지 JavaScript
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    // CSSManager와 NotificationManager 사용
    const cssManager = window.CSSManager;
    const notify = window.notify;

    // 관리자 코드 목록 전용 스타일 생성
    cssManager.addStyle('admin-code-list', `
        .admin-code-list-page .code-list-container {
            opacity: 0;
            transform: translateY(20px);
            transition: all var(--transition-normal);
        }

        .admin-code-list-page .code-list-container.loaded {
            opacity: 1;
            transform: translateY(0);
        }

        .admin-code-list-page .search-form {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-lg);
            margin-bottom: var(--space-lg);
        }

        .admin-code-list-page .search-form-group {
            display: flex;
            gap: var(--space-md);
            align-items: end;
        }

        .admin-code-list-page .form-group {
            flex: 1;
        }

        .admin-code-list-page .form-label {
            display: block;
            font-weight: 600;
            color: var(--color-text);
            margin-bottom: var(--space-xs);
        }

        .admin-code-list-page .form-input,
        .admin-code-list-page .form-select {
            width: 100%;
            padding: var(--space-sm);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            font-size: var(--font-size-base);
            transition: border-color var(--transition-fast);
        }

        .admin-code-list-page .form-input:focus,
        .admin-code-list-page .form-select:focus {
            outline: none;
            border-color: var(--color-primary);
            box-shadow: 0 0 0 2px rgba(34, 40, 49, 0.1);
        }

        .admin-code-list-page .data-table {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            overflow: hidden;
            box-shadow: var(--shadow-sm);
        }

        .admin-code-list-page .data-table th {
            background: var(--color-light-gray);
            font-weight: 600;
            color: var(--color-text);
            padding: var(--space-md);
            text-align: left;
            border-bottom: 1px solid var(--color-border);
        }

        .admin-code-list-page .data-table td {
            padding: var(--space-md);
            border-bottom: 1px solid var(--color-border);
            color: var(--color-text);
        }

        .admin-code-list-page .data-table tbody tr {
            transition: background var(--transition-fast);
            cursor: pointer;
        }

        .admin-code-list-page .data-table tbody tr:hover {
            background: var(--color-light-gray);
        }

        .admin-code-list-page .action-cell {
            text-align: center;
        }

        .admin-code-list-page .btn--edit {
            background: var(--color-primary);
            color: var(--color-white);
            margin-right: var(--space-xs);
        }

        .admin-code-list-page .btn--delete {
            background: var(--color-error);
            color: var(--color-white);
        }

        .admin-code-list-page .pagination {
            display: flex;
            justify-content: center;
            gap: var(--space-xs);
            margin-top: var(--space-lg);
        }

        .admin-code-list-page .pagination .btn {
            padding: var(--space-sm) var(--space-md);
            border: 1px solid var(--color-border);
            background: var(--color-white);
            color: var(--color-text);
            border-radius: var(--radius-sm);
            transition: all var(--transition-fast);
        }

        .admin-code-list-page .pagination .btn:hover {
            background: var(--color-light-gray);
        }

        .admin-code-list-page .pagination .btn.active {
            background: var(--color-primary);
            color: var(--color-white);
            border-color: var(--color-primary);
        }

        .admin-code-list-page .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: var(--z-modal);
        }

        .admin-code-list-page .loading-spinner {
            width: 40px;
            height: 40px;
            border: 4px solid var(--color-border);
            border-top: 4px solid var(--color-primary);
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    `);

    /**
     * 초기화
     */
    function init() {
        initializeSearchForm();
        initializeTableEvents();
        animatePageLoad();
    }

    /**
     * 검색 폼 초기화
     */
    function initializeSearchForm() {
        const searchForm = document.querySelector('.search-form');
        if (searchForm) {
            const searchBtn = searchForm.querySelector('button[type="submit"]');
            if (searchBtn) {
                searchBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    submitSearch();
                });
            }
        }
    }

    /**
     * 검색 제출
     */
    function submitSearch() {
        const gcode = document.getElementById('gcode')?.value;
        const searchText = document.getElementById('searchText')?.value.trim();
        
        let url = '/admin/codes?';
        const params = new URLSearchParams();
        
        if (gcode) {
            params.append('gcode', gcode);
        }
        
        if (searchText) {
            params.append('searchText', searchText);
        }
        
        window.location.href = url + params.toString();
    }

    /**
     * 검색 초기화
     */
    function resetSearch() {
        window.location.href = '/admin/codes';
    }

    /**
     * 테이블 이벤트 초기화
     */
    function initializeTableEvents() {
        const tableRows = document.querySelectorAll('.data-table tbody tr');
        
        tableRows.forEach(row => {
            // 행 클릭 시 상세보기 (관리 버튼 클릭 제외)
            row.addEventListener('click', function(e) {
                if (e.target.closest('.action-cell') || e.target.tagName === 'BUTTON') {
                    e.stopPropagation();
                    return;
                }
                
                const codeId = this.getAttribute('data-code-id') || 
                              this.querySelector('td:first-child')?.textContent.trim();
                if (codeId) {
                    viewDetail(codeId);
                }
            });
            
            // 행에 코드 ID 설정
            const firstCell = row.querySelector('td:first-child');
            if (firstCell) {
                row.setAttribute('data-code-id', firstCell.textContent.trim());
            }
        });
        
        // 버튼 클릭 이벤트 별도 처리
        const actionButtons = document.querySelectorAll('.action-cell button');
        actionButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.stopPropagation();
            });
        });
    }

    /**
     * 코드 상세보기
     */
    function viewDetail(codeId) {
        window.location.href = `/admin/codes/${codeId}`;
    }

    /**
     * 코드 수정
     */
    function editCode(codeId) {
        window.location.href = `/admin/codes/${codeId}/edit`;
    }

    /**
     * 코드 삭제
     */
    async function deleteCode(codeId) {
        if (!confirm('정말로 이 코드를 삭제하시겠습니까?\n\n삭제된 코드는 복구할 수 없습니다.')) {
            return;
        }
        
        showLoading();
        
        try {
            const data = await ajax.delete(`/api/admin/codes/${codeId}`);
            
            if (data.code === '00') {
                notify.success('코드가 성공적으로 삭제되었습니다.', '삭제 완료');
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                notify.error(data.message || '코드 삭제 중 오류가 발생했습니다.', '삭제 오류');
            }
        } catch (error) {
            console.error('Error:', error);
            notify.error('네트워크 오류가 발생했습니다.', '오류');
        } finally {
            hideLoading();
        }
    }

    /**
     * 로딩 표시
     */
    function showLoading() {
        const submitBtn = document.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = '처리 중...';
        }
    }

    /**
     * 로딩 숨김
     */
    function hideLoading() {
        const submitBtn = document.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = '검색';
        }
    }

    /**
     * 페이지 이동
     */
    function goToPage(page) {
        const currentUrl = new URL(window.location.href);
        currentUrl.searchParams.set('page', page);
        window.location.href = currentUrl.toString();
    }

    /**
     * 그룹코드별 필터링
     */
    function filterByGcode(gcode) {
        const currentUrl = new URL(window.location.href);
        if (gcode) {
            currentUrl.searchParams.set('gcode', gcode);
        } else {
            currentUrl.searchParams.delete('gcode');
        }
        currentUrl.searchParams.delete('page');
        window.location.href = currentUrl.toString();
    }

    /**
     * 검색어 필터링
     */
    function filterBySearchText(searchText) {
        const currentUrl = new URL(window.location.href);
        if (searchText) {
            currentUrl.searchParams.set('searchText', searchText);
        } else {
            currentUrl.searchParams.delete('searchText');
        }
        currentUrl.searchParams.delete('page');
        window.location.href = currentUrl.toString();
    }

    /**
     * 테이블 정렬
     */
    function sortTable(column) {
        console.log('Sort by column:', column);
        // 테이블 정렬 기능 구현
    }

    /**
     * 데이터 내보내기
     */
    function exportData(format) {
        console.log('Export data as:', format);
        notify.info(`${format.toUpperCase()} 형식으로 내보내기가 시작되었습니다.`, '내보내기');
    }

    /**
     * 배치 작업
     */
    function batchAction(action, selectedIds) {
        console.log('Batch action:', action, 'on IDs:', selectedIds);
        notify.info(`${action} 작업이 시작되었습니다.`, '배치 작업');
    }

    /**
     * 페이지 로드 애니메이션
     */
    function animatePageLoad() {
        const container = document.querySelector('.code-list-container');
        if (container) {
            setTimeout(() => {
                container.classList.add('loaded');
            }, 100);
        }
    }

    // 키보드 이벤트 처리
    document.addEventListener('keydown', function(e) {
        // Enter 키로 검색
        if (e.key === 'Enter' && e.target.matches('.search-form input')) {
            e.preventDefault();
            submitSearch();
        }
        
        // ESC 키로 검색 초기화
        if (e.key === 'Escape' && e.target.matches('.search-form input')) {
            e.target.value = '';
            submitSearch();
        }
    });

    // 전역 함수로 노출
    window.viewDetail = viewDetail;
    window.editCode = editCode;
    window.deleteCode = deleteCode;
    window.goToPage = goToPage;
    window.filterByGcode = filterByGcode;
    window.filterBySearchText = filterBySearchText;
    window.sortTable = sortTable;
    window.exportData = exportData;
    window.batchAction = batchAction;
    window.resetSearch = resetSearch;

    // 초기화 실행
    init();
}); 