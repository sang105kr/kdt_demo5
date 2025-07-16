/**
 * 관리자 주문 목록 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자 주문 목록 페이지 로드됨');
    
    // 메시지 자동 숨김
    autoHideMessages();
    
    // 필터 버튼 활성화 상태 관리
    initFilterButtons();
    
    // 테이블 정렬 기능 (선택적)
    initTableSorting();
});

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
            sortIcon.style.marginLeft = '5px';
            sortIcon.style.fontSize = '12px';
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
}

/**
 * 주문 상태별 통계 표시 (선택적 기능)
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

// 페이지 로드 시 필터 상태 복원
document.addEventListener('DOMContentLoaded', preserveFilterState); 