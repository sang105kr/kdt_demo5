/**
 * 관리자 주문 목록 페이지 JavaScript
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    // CSSManager와 NotificationManager 사용
    const cssManager = window.CSSManager;
    const notify = window.notify;

    // 관리자 주문 목록 전용 스타일 생성
    cssManager.addStyle('admin-order-list', `
        .admin-order-list-page .order-list-container {
            opacity: 0;
            transform: translateY(20px);
            transition: all var(--transition-normal);
        }

        .admin-order-list-page .order-list-container.loaded {
            opacity: 1;
            transform: translateY(0);
        }

        .admin-order-list-page .filter-section {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-lg);
            margin-bottom: var(--space-lg);
        }

        .admin-order-list-page .filter-btn {
            padding: var(--space-sm) var(--space-md);
            border: 1px solid var(--color-border);
            background: var(--color-white);
            color: var(--color-text);
            border-radius: var(--radius-sm);
            margin-right: var(--space-xs);
            transition: all var(--transition-fast);
            cursor: pointer;
        }

        .admin-order-list-page .filter-btn:hover {
            background: var(--color-light-gray);
        }

        .admin-order-list-page .filter-btn.active {
            background: var(--color-primary);
            color: var(--color-white);
            border-color: var(--color-primary);
        }

        .admin-order-list-page .order-table {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            overflow: hidden;
            box-shadow: var(--shadow-sm);
        }

        .admin-order-list-page .order-table th {
            background: var(--color-light-gray);
            font-weight: 600;
            color: var(--color-text);
            padding: var(--space-md);
            text-align: left;
            border-bottom: 1px solid var(--color-border);
            cursor: pointer;
            user-select: none;
        }

        .admin-order-list-page .order-table th:hover {
            background: var(--color-gray-200);
        }

        .admin-order-list-page .order-table td {
            padding: var(--space-md);
            border-bottom: 1px solid var(--color-border);
            color: var(--color-text);
        }

        .admin-order-list-page .order-table tbody tr {
            transition: background var(--transition-fast);
        }

        .admin-order-list-page .order-table tbody tr:hover {
            background: var(--color-light-gray);
        }

        .admin-order-list-page .sort-icon {
            margin-left: var(--space-xs);
            font-size: var(--font-size-xs);
            color: var(--color-text-muted);
        }

        .admin-order-list-page .sort-asc .sort-icon {
            color: var(--color-primary);
        }

        .admin-order-list-page .sort-desc .sort-icon {
            color: var(--color-primary);
        }

        .admin-order-list-page .status-badge {
            padding: var(--space-xs) var(--space-sm);
            border-radius: var(--radius-sm);
            font-size: var(--font-size-xs);
            font-weight: 600;
            text-transform: uppercase;
            min-width: 80px;
            text-align: center;
        }

        .admin-order-list-page .status-badge.pending {
            background: var(--color-light-gray);
            color: var(--color-warning);
        }

        .admin-order-list-page .status-badge.confirmed {
            background: var(--color-light-gray);
            color: var(--color-info);
        }

        .admin-order-list-page .status-badge.shipped {
            background: var(--color-light-gray);
            color: var(--color-primary);
        }

        .admin-order-list-page .status-badge.delivered {
            background: var(--color-light-gray);
            color: var(--color-success);
        }

        .admin-order-list-page .status-badge.cancelled {
            background: var(--color-light-gray);
            color: var(--color-error);
        }

        .admin-order-list-page .message {
            padding: var(--space-md);
            border-radius: var(--radius-md);
            margin-bottom: var(--space-md);
            transition: opacity var(--transition-normal);
        }

        .admin-order-list-page .message.success {
            background: var(--color-light-gray);
            color: var(--color-success);
            border: 1px solid var(--color-success);
        }

        .admin-order-list-page .message.error {
            background: var(--color-light-gray);
            color: var(--color-error);
            border: 1px solid var(--color-error);
        }

        .admin-order-list-page .message.warning {
            background: var(--color-light-gray);
            color: var(--color-warning);
            border: 1px solid var(--color-warning);
        }

        .admin-order-list-page .message.info {
            background: var(--color-light-gray);
            color: var(--color-info);
            border: 1px solid var(--color-info);
        }

        .admin-order-list-page .action-buttons {
            display: flex;
            gap: var(--space-xs);
        }

        .admin-order-list-page .btn--view {
            background: var(--color-primary);
            color: var(--color-white);
        }

        .admin-order-list-page .btn--edit {
            background: var(--color-warning);
            color: var(--color-white);
        }

        .admin-order-list-page .btn--delete {
            background: var(--color-error);
            color: var(--color-white);
        }

        .admin-order-list-page .statistics-section {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: var(--space-md);
            margin-bottom: var(--space-lg);
        }

        .admin-order-list-page .stat-card {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-md);
            text-align: center;
        }

        .admin-order-list-page .stat-value {
            font-size: var(--font-size-xl);
            font-weight: 700;
            color: var(--color-primary);
            margin-bottom: var(--space-xs);
        }

        .admin-order-list-page .stat-label {
            font-size: var(--font-size-sm);
            color: var(--color-text-secondary);
        }
    `);

    /**
     * 초기화
     */
    function init() {
        autoHideMessages();
        initFilterButtons();
        initTableSorting();
        preserveFilterState();
        animatePageLoad();
    }

    /**
     * 메시지 자동 숨김
     */
    function autoHideMessages() {
        const messages = document.querySelectorAll('.message');
        messages.forEach(message => {
            setTimeout(() => {
                message.style.opacity = '0';
                setTimeout(() => {
                    message.style.display = 'none';
                }, 300);
            }, 3000);
        });
    }

    /**
     * 필터 버튼 초기화
     */
    function initFilterButtons() {
        const filterButtons = document.querySelectorAll('.filter-btn');
        
        filterButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                // 현재 활성화된 버튼의 활성화 상태 제거
                document.querySelectorAll('.filter-btn.active').forEach(btn => {
                    btn.classList.remove('active');
                });
                
                // 클릭된 버튼 활성화
                this.classList.add('active');
                
                notify.info('필터가 적용되었습니다.', '필터');
            });
        });
    }

    /**
     * 테이블 정렬 기능 초기화
     */
    function initTableSorting() {
        const tableHeaders = document.querySelectorAll('.order-table th');
        
        tableHeaders.forEach((header, index) => {
            // 정렬 가능한 컬럼만 처리 (주문번호, 주문일시, 총 금액)
            if (index === 0 || index === 1 || index === 3) {
                header.style.cursor = 'pointer';
                header.addEventListener('click', function() {
                    sortTable(index);
                });
                
                // 정렬 아이콘 추가
                const sortIcon = document.createElement('span');
                sortIcon.className = 'sort-icon';
                sortIcon.textContent = '↕';
                header.appendChild(sortIcon);
            }
        });
    }

    /**
     * 테이블 정렬
     */
    function sortTable(columnIndex) {
        const table = document.querySelector('.order-table table');
        const tbody = table.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));
        
        // 현재 정렬 방향 확인
        const header = table.querySelector(`th:nth-child(${columnIndex + 1})`);
        const isAscending = !header.classList.contains('sort-desc');
        
        // 정렬
        rows.sort((a, b) => {
            const aValue = a.cells[columnIndex].textContent.trim();
            const bValue = b.cells[columnIndex].textContent.trim();
            
            let comparison = 0;
            
            if (columnIndex === 1) {
                // 날짜 정렬
                const aDate = new Date(aValue);
                const bDate = new Date(bValue);
                comparison = aDate - bDate;
            } else if (columnIndex === 3) {
                // 금액 정렬 (숫자만 추출)
                const aNum = parseInt(aValue.replace(/[^\d]/g, ''));
                const bNum = parseInt(bValue.replace(/[^\d]/g, ''));
                comparison = aNum - bNum;
            } else {
                // 문자열 정렬
                comparison = aValue.localeCompare(bValue);
            }
            
            return isAscending ? comparison : -comparison;
        });
        
        // 정렬된 행들을 테이블에 다시 추가
        rows.forEach(row => tbody.appendChild(row));
        
        // 정렬 방향 표시 업데이트
        table.querySelectorAll('th').forEach(th => {
            th.classList.remove('sort-asc', 'sort-desc');
        });
        
        header.classList.add(isAscending ? 'sort-asc' : 'sort-desc');
        
        // 정렬 아이콘 업데이트
        const sortIcon = header.querySelector('.sort-icon');
        if (sortIcon) {
            sortIcon.textContent = isAscending ? '↑' : '↓';
        }
        
        notify.success('테이블이 정렬되었습니다.', '정렬 완료');
    }

    /**
     * 주문 상태별 통계 표시
     */
    function showOrderStatistics() {
        const statusBadges = document.querySelectorAll('.status-badge');
        const statistics = {
            pending: 0,
            confirmed: 0,
            shipped: 0,
            delivered: 0,
            cancelled: 0
        };
        
        statusBadges.forEach(badge => {
            const status = badge.className.split(' ').find(cls => cls !== 'status-badge');
            if (status && statistics.hasOwnProperty(status)) {
                statistics[status]++;
            }
        });
        
        console.log('주문 상태별 통계:', statistics);
        notify.info('주문 통계가 업데이트되었습니다.', '통계');
        
        // 통계를 화면에 표시하는 로직 추가 가능
        // 예: 차트 라이브러리 사용하여 시각화
    }

    /**
     * 페이지 새로고침 시 필터 상태 유지
     */
    function preserveFilterState() {
        const urlParams = new URLSearchParams(window.location.search);
        const orderStatus = urlParams.get('orderStatus');
        
        if (orderStatus) {
            const activeButton = document.querySelector(`[href*="orderStatus=${orderStatus}"]`);
            if (activeButton) {
                document.querySelectorAll('.filter-btn.active').forEach(btn => {
                    btn.classList.remove('active');
                });
                activeButton.classList.add('active');
            }
        }
    }

    /**
     * 페이지 로드 애니메이션
     */
    function animatePageLoad() {
        const container = document.querySelector('.order-list-container');
        if (container) {
            setTimeout(() => {
                container.classList.add('loaded');
            }, 100);
        }
    }

    // 전역 함수로 노출
    window.sortTable = sortTable;
    window.showOrderStatistics = showOrderStatistics;

    // 초기화 실행
    init();
}); 