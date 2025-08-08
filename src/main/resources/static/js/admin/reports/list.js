/**
 * 관리자 신고 목록 페이지 JavaScript
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    // CSSManager와 NotificationManager 사용
    const cssManager = window.CSSManager;
    const notify = window.notify;

    // 관리자 신고 목록 전용 스타일 생성
    cssManager.addStyle('admin-reports-list', `
        .admin-reports-list-page .reports-container {
            opacity: 0;
            transform: translateY(20px);
            transition: all var(--transition-normal);
        }

        .admin-reports-list-page .reports-container.loaded {
            opacity: 1;
            transform: translateY(0);
        }

        .admin-reports-list-page .status-badge {
            padding: var(--space-xs) var(--space-sm);
            border-radius: var(--radius-sm);
            font-size: var(--font-size-xs);
            font-weight: 600;
            text-transform: uppercase;
        }

        .admin-reports-list-page .status-pending {
            background: var(--color-light-gray);
            color: var(--color-text);
        }

        .admin-reports-list-page .status-processing {
            background: var(--color-light-gray);
            color: var(--color-warning);
        }

        .admin-reports-list-page .status-resolved {
            background: var(--color-light-gray);
            color: var(--color-success);
        }

        .admin-reports-list-page .status-rejected {
            background: var(--color-light-gray);
            color: var(--color-error);
        }

        .admin-reports-list-page .statistics-card {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-lg);
            text-align: center;
            transition: all var(--transition-fast);
        }

        .admin-reports-list-page .statistics-card:hover {
            box-shadow: var(--shadow-md);
            transform: translateY(-2px);
        }

        .admin-reports-list-page .statistics-number {
            font-size: var(--font-size-3xl);
            font-weight: 700;
            color: var(--color-primary);
            margin-bottom: var(--space-xs);
        }

        .admin-reports-list-page .statistics-label {
            font-size: var(--font-size-sm);
            color: var(--color-text-secondary);
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .admin-reports-list-page .table-hover {
            transition: background var(--transition-fast);
        }

        .admin-reports-list-page .table-hover:hover {
            background: var(--color-light-gray);
        }

        .admin-reports-list-page .bulk-actions {
            background: var(--color-light-gray);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-md);
            margin-bottom: var(--space-lg);
            display: none;
        }

        .admin-reports-list-page .bulk-actions.show {
            display: block;
        }
    `);

    // 전역 변수
    let selectedReports = new Set();
    let searchTimeout = null;

    /**
     * 초기화
     */
    function init() {
        initializeReportsPage();
        initializeFilters();
        initializeSearch();
        initializeTableInteractions();
        initializeStatisticsCards();
        initializeAutoActionButton();
        animatePageLoad();
    }

    /**
     * 페이지 초기화
     */
    function initializeReportsPage() {
        console.log('관리자 신고 목록 페이지 초기화');
        
        // 페이지 로드 애니메이션
        animatePageLoad();
        
        // 통계 카드 애니메이션
        animateStatisticsCards();
    }

    /**
     * 필터 초기화
     */
    function initializeFilters() {
        const filterSelects = document.querySelectorAll('.filter-select');
        
        filterSelects.forEach(select => {
            select.addEventListener('change', function() {
                applyFilters();
            });
        });
    }

    /**
     * 필터 적용
     */
    function applyFilters() {
        const form = document.querySelector('.filter-form');
        if (form) {
            form.submit();
        }
    }

    /**
     * 검색 초기화
     */
    function initializeSearch() {
        const searchInput = document.getElementById('searchInput');
        
        if (searchInput) {
            searchInput.addEventListener('input', function() {
                clearTimeout(searchTimeout);
                
                searchTimeout = setTimeout(() => {
                    const searchTerm = this.value.trim();
                    if (searchTerm.length >= 2 || searchTerm.length === 0) {
                        performSearch(searchTerm);
                    }
                }, 300);
            });
        }
    }

    /**
     * 검색 수행
     */
    function performSearch(searchTerm) {
        const currentUrl = new URL(window.location);
        
        if (searchTerm) {
            currentUrl.searchParams.set('search', searchTerm);
        } else {
            currentUrl.searchParams.delete('search');
        }
        
        window.location.href = currentUrl.toString();
    }

    /**
     * 테이블 상호작용 초기화
     */
    function initializeTableInteractions() {
        const tableRows = document.querySelectorAll('.reports-table tbody tr');
        
        tableRows.forEach(row => {
            row.classList.add('table-hover');
            
            // 상태 변경 버튼 이벤트
            const statusButtons = row.querySelectorAll('.btn-status');
            statusButtons.forEach(button => {
                button.addEventListener('click', function(e) {
                    e.preventDefault();
                    const reportId = this.dataset.reportId;
                    const status = this.dataset.status;
                    processReport(reportId, status);
                });
            });
        });
    }

    /**
     * 통계 카드 초기화
     */
    function initializeStatisticsCards() {
        const statisticsCards = document.querySelectorAll('.statistics-card');
        
        statisticsCards.forEach((card, index) => {
            setTimeout(() => {
                card.style.opacity = '0';
                card.style.transform = 'translateY(20px)';
                
                setTimeout(() => {
                    card.style.transition = 'all var(--transition-normal)';
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, 100);
            }, index * 100);
        });
    }

    /**
     * 자동 처리 버튼 초기화
     */
    function initializeAutoActionButton() {
        const autoActionButton = document.getElementById('autoActionButton');
        
        if (autoActionButton) {
            autoActionButton.addEventListener('click', function() {
                if (confirm('자동 처리를 실행하시겠습니까?')) {
                    executeAutoActions();
                }
            });
        }
    }

    /**
     * 자동 처리 실행
     */
    async function executeAutoActions() {
        try {
            const response = await ajax.post('/api/admin/reports/auto-process');
            
            if (response.success) {
                notify.success('자동 처리가 완료되었습니다.', '자동 처리 완료');
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                notify.error(response.message || '자동 처리에 실패했습니다.', '자동 처리 오류');
            }
        } catch (error) {
            console.error('Auto action error:', error);
            notify.error('자동 처리 중 오류가 발생했습니다.', '오류');
        }
    }

    /**
     * 신고 처리
     */
    async function processReport(reportId, status) {
        try {
            const response = await ajax.post(`/api/admin/reports/${reportId}/process`, {
                status: status
            });
            
            if (response.success) {
                notify.success(`신고가 ${getStatusText(status)} 상태로 변경되었습니다.`, '처리 완료');
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                notify.error(response.message || '신고 처리에 실패했습니다.', '처리 오류');
            }
        } catch (error) {
            console.error('Process report error:', error);
            notify.error('신고 처리 중 오류가 발생했습니다.', '오류');
        }
    }

    /**
     * 상태 텍스트 반환
     */
    function getStatusText(status) {
        const statusMap = {
            'PENDING': '대기중',
            'PROCESSING': '처리중',
            'RESOLVED': '해결됨',
            'REJECTED': '거부됨'
        };
        return statusMap[status] || status;
    }

    /**
     * 일괄 처리
     */
    function bulkProcessReports() {
        const selectedReports = document.querySelectorAll('input[name="selectedReports"]:checked');
        
        if (selectedReports.length === 0) {
            notify.warning('처리할 신고를 선택해주세요.', '선택 필요');
            return;
        }
        
        const status = prompt('변경할 상태를 입력하세요 (PENDING, PROCESSING, RESOLVED, REJECTED):');
        if (!status) return;
        
        if (!['PENDING', 'PROCESSING', 'RESOLVED', 'REJECTED'].includes(status.toUpperCase())) {
            notify.error('올바른 상태를 입력해주세요.', '입력 오류');
            return;
        }
        
        if (confirm(`${selectedReports.length}개의 신고를 ${getStatusText(status.toUpperCase())} 상태로 변경하시겠습니까?`)) {
            const reportIds = Array.from(selectedReports).map(checkbox => checkbox.value);
            bulkProcessReportsAPI(reportIds, status.toUpperCase());
        }
    }

    /**
     * 일괄 처리 API 호출
     */
    async function bulkProcessReportsAPI(reportIds, status) {
        try {
            const response = await ajax.post('/api/admin/reports/bulk-process', {
                reportIds: reportIds,
                status: status
            });
            
            if (response.success) {
                notify.success(`${reportIds.length}개의 신고가 ${getStatusText(status)} 상태로 변경되었습니다.`, '일괄 처리 완료');
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                notify.error(response.message || '일괄 처리에 실패했습니다.', '처리 오류');
            }
        } catch (error) {
            console.error('Bulk process error:', error);
            notify.error('일괄 처리 중 오류가 발생했습니다.', '오류');
        }
    }

    /**
     * 신고 내보내기
     */
    function exportReports() {
        const currentUrl = new URL(window.location);
        const params = currentUrl.searchParams;
        
        const exportUrl = `/admin/reports/export?${params.toString()}`;
        window.open(exportUrl, '_blank');
        
        notify.info('신고 내보내기가 시작되었습니다.', '내보내기');
    }

    /**
     * 페이지 로드 애니메이션
     */
    function animatePageLoad() {
        const container = document.querySelector('.reports-container');
        if (container) {
            setTimeout(() => {
                container.classList.add('loaded');
            }, 100);
        }
    }

    /**
     * 통계 카드 애니메이션
     */
    function animateStatisticsCards() {
        const cards = document.querySelectorAll('.statistics-card');
        
        cards.forEach((card, index) => {
            setTimeout(() => {
                cssManager.animate(card, 'slide-in', 500);
            }, index * 100);
        });
    }

    // 전역 함수로 노출
    window.bulkProcessReports = bulkProcessReports;
    window.exportReports = exportReports;
    window.processReport = processReport;

    // 초기화 실행
    init();
}); 