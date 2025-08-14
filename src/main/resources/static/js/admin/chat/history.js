/**
 * 관리자 채팅 히스토리 JavaScript
 */

class AdminChatHistory {
    constructor() {
        this.currentPage = 1;
        this.pageSize = 10;
        this.totalPages = 1;
        this.totalCount = 0;
        this.filters = {
            dateFilter: 'today',
            statusFilter: 'all',
            searchQuery: ''
        };
        
        this.init();
    }
    
    /**
     * 초기화
     */
    init() {
        this.bindEvents();
        this.loadHistoryData();
    }
    
    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        const refreshBtn = document.getElementById('refreshBtn');
        const dateFilter = document.getElementById('dateFilter');
        const statusFilter = document.getElementById('statusFilter');
        const searchInput = document.getElementById('searchInput');
        const prevPageBtn = document.getElementById('prevPage');
        const nextPageBtn = document.getElementById('nextPage');
        
        // 새로고침 버튼
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                this.loadHistoryData();
            });
        }
        
        // 필터 변경 이벤트
        if (dateFilter) {
            dateFilter.addEventListener('change', (e) => {
                this.filters.dateFilter = e.target.value;
                this.currentPage = 1;
                this.loadHistoryData();
            });
        }
        
        if (statusFilter) {
            statusFilter.addEventListener('change', (e) => {
                this.filters.statusFilter = e.target.value;
                this.currentPage = 1;
                this.loadHistoryData();
            });
        }
        
        // 검색 입력
        if (searchInput) {
            let searchTimeout;
            searchInput.addEventListener('input', (e) => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    this.filters.searchQuery = e.target.value;
                    this.currentPage = 1;
                    this.loadHistoryData();
                }, 500);
            });
        }
        
        // 페이지네이션
        if (prevPageBtn) {
            prevPageBtn.addEventListener('click', () => {
                if (this.currentPage > 1) {
                    this.currentPage--;
                    this.loadHistoryData();
                }
            });
        }
        
        if (nextPageBtn) {
            nextPageBtn.addEventListener('click', () => {
                if (this.currentPage < this.totalPages) {
                    this.currentPage++;
                    this.loadHistoryData();
                }
            });
        }
    }
    
    /**
     * 히스토리 데이터 로드
     */
    async loadHistoryData() {
        try {
            this.showLoading();
            
            const params = new URLSearchParams({
                page: this.currentPage,
                size: this.pageSize,
                dateFilter: this.filters.dateFilter,
                statusFilter: this.filters.statusFilter,
                search: this.filters.searchQuery
            });
            
            const response = await ajax.get(`/api/chat/sessions/history?${params}`);
            
            if (response && response.code === '00') {
                this.updateHistoryTable(response.data);
                this.updatePagination(response.data);
            } else {
                this.showError('히스토리 데이터를 불러오는데 실패했습니다.');
                this.showEmptyState();
            }
        } catch (error) {
            console.error('히스토리 데이터 로드 실패:', error);
            this.showError('히스토리 데이터를 불러오는데 실패했습니다.');
            this.showEmptyState();
        }
    }
    
    /**
     * 히스토리 테이블 업데이트
     */
    updateHistoryTable(data) {
        const tbody = document.getElementById('historyTableBody');
        
        if (!tbody) {
            console.warn('히스토리 테이블 본문을 찾을 수 없습니다.');
            return;
        }
        
        if (!data.sessions || data.sessions.length === 0) {
            this.showEmptyState();
            return;
        }
        
        const rowsHtml = data.sessions.map(session => this.createHistoryRow(session)).join('');
        tbody.innerHTML = rowsHtml;
    }
    
    /**
     * 히스토리 행 생성
     */
    createHistoryRow(session) {
        const startTime = this.formatDateTime(session.startTime);
        const endTime = this.formatDateTime(session.endTime);
        const statusBadge = this.getStatusBadge(session.statusName);
        
        return `
            <tr>
                <td>${session.sessionId}</td>
                <td>${session.memberName || '고객'}</td>
                <td>${session.title || '상담 문의'}</td>
                <td>${session.categoryName || '일반 문의'}</td>
                <td>${session.adminName || '상담원'}</td>
                <td>${startTime}</td>
                <td>${endTime}</td>
                <td>${session.messageCount || 0}</td>
                <td>${statusBadge}</td>
                <td>
                    <button class="action-btn view" onclick="viewHistoryDetail('${session.sessionId}')">
                        <i class="fas fa-eye"></i>
                        상세보기
                    </button>
                </td>
            </tr>
        `;
    }
    
    /**
     * 상태 배지 생성
     */
    getStatusBadge(statusName) {
        const status = statusName?.toLowerCase() || 'completed';
        
        if (status.includes('완료') || status === 'completed') {
            return '<span class="status-badge completed">완료</span>';
        } else if (status.includes('취소') || status === 'cancelled') {
            return '<span class="status-badge cancelled">취소</span>';
        } else {
            return '<span class="status-badge completed">완료</span>';
        }
    }
    
    /**
     * 페이지네이션 업데이트
     */
    updatePagination(data) {
        this.totalCount = data.totalCount || 0;
        this.totalPages = data.totalPages || 1;
        
        const totalCountElement = document.getElementById('totalCount');
        const currentRangeElement = document.getElementById('currentRange');
        const currentPageElement = document.getElementById('currentPage');
        const totalPagesElement = document.getElementById('totalPages');
        const prevPageBtn = document.getElementById('prevPage');
        const nextPageBtn = document.getElementById('nextPage');
        
        if (totalCountElement) {
            totalCountElement.textContent = this.totalCount;
        }
        
        if (currentRangeElement) {
            const start = (this.currentPage - 1) * this.pageSize + 1;
            const end = Math.min(this.currentPage * this.pageSize, this.totalCount);
            currentRangeElement.textContent = `${start}-${end}`;
        }
        
        if (currentPageElement) {
            currentPageElement.textContent = this.currentPage;
        }
        
        if (totalPagesElement) {
            totalPagesElement.textContent = this.totalPages;
        }
        
        if (prevPageBtn) {
            prevPageBtn.disabled = this.currentPage <= 1;
        }
        
        if (nextPageBtn) {
            nextPageBtn.disabled = this.currentPage >= this.totalPages;
        }
    }
    
    /**
     * 로딩 상태 표시
     */
    showLoading() {
        const tbody = document.getElementById('historyTableBody');
        if (tbody) {
            tbody.innerHTML = `
                <tr class="loading-row">
                    <td colspan="10" class="loading-cell">
                        <i class="fas fa-spinner fa-spin"></i>
                        데이터를 불러오는 중...
                    </td>
                </tr>
            `;
        }
    }
    
    /**
     * 빈 상태 표시
     */
    showEmptyState() {
        const tbody = document.getElementById('historyTableBody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="10" class="loading-cell">
                        <i class="fas fa-inbox"></i>
                        완료된 상담이 없습니다.
                    </td>
                </tr>
            `;
        }
    }
    
    /**
     * 에러 메시지 표시
     */
    showError(message) {
        console.error(message);
        // 토스트 메시지나 알림 표시
        if (typeof showToast === 'function') {
            showToast(message, 'error');
        } else {
            alert(message);
        }
    }
    
    /**
     * 날짜/시간 포맷팅
     */
    formatDateTime(dateTimeString) {
        if (!dateTimeString) return '-';
        
        try {
            const date = new Date(dateTimeString);
            return date.toLocaleString('ko-KR', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch (error) {
            return dateTimeString;
        }
    }
}

/**
 * 세션 상세보기 함수 (전역 함수)
 */
function viewSessionDetail(sessionId) {
    // 새 창에서 세션 상세 정보 열기
    window.open(`/admin/chat/session/${sessionId}`, '_blank');
}

/**
 * 히스토리 상세보기 함수 (전역 함수)
 */
function viewHistoryDetail(sessionId) {
    // 히스토리 상세보기 페이지로 이동
    window.location.href = `/admin/chat/history/${sessionId}`;
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    new AdminChatHistory();
});
