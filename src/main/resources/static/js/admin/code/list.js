// 관리자 코드 목록 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 검색 폼 초기화
    initializeSearchForm();
    
    // 테이블 행 클릭 이벤트
    initializeTableEvents();
});

// 검색 폼 초기화
function initializeSearchForm() {
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        // 검색 버튼 클릭 시 폼 제출
        const searchBtn = searchForm.querySelector('button[type="submit"]');
        if (searchBtn) {
            searchBtn.addEventListener('click', function(e) {
                e.preventDefault();
                submitSearch();
            });
        }
    }
}

// 검색 제출
function submitSearch() {
    const gcode = document.getElementById('gcode').value;
    const searchText = document.getElementById('searchText').value.trim();
    
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

// 검색 초기화
function resetSearch() {
    window.location.href = '/admin/codes';
}

// 테이블 이벤트 초기화
function initializeTableEvents() {
    const tableRows = document.querySelectorAll('.data-table tbody tr');
    
    tableRows.forEach(row => {
        // 행 클릭 시 상세보기 (관리 버튼 클릭 제외)
        row.addEventListener('click', function(e) {
            // 관리 버튼 클릭 시에는 상세보기로 이동하지 않음
            if (e.target.closest('.action-cell') || e.target.tagName === 'BUTTON') {
                e.stopPropagation();
                return;
            }
            
            const codeId = this.getAttribute('data-code-id') || 
                          this.querySelector('td:first-child').textContent.trim();
            viewDetail(codeId);
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

// 코드 상세보기
function viewDetail(codeId) {
    window.location.href = `/admin/codes/${codeId}`;
}

// 코드 수정
function editCode(codeId) {
    window.location.href = `/admin/codes/${codeId}/edit`;
}

// 코드 삭제
async function deleteCode(codeId) {
    if (!confirm('정말로 이 코드를 삭제하시겠습니까?\n\n삭제된 코드는 복구할 수 없습니다.')) {
        return;
    }
    
    try {
        // 로딩 표시
        showLoading();
        
        // common.js의 ajax 객체 사용
        const data = await ajax.delete(`/api/admin/codes/${codeId}`);
        
        // 새로운 ApiResponse 형식 처리
        if (data.code === '00') { // SUCCESS
            showToast('코드가 성공적으로 삭제되었습니다.', 'success');
            // 현재 페이지 새로고침
            window.location.reload();
        } else {
            showToast(data.message || '코드 삭제 중 오류가 발생했습니다.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('네트워크 오류가 발생했습니다.', 'error');
    } finally {
        hideLoading();
    }
}

// 로딩 표시
function showLoading() {
    // 간단한 로딩 표시 (필요시 오버레이 추가 가능)
    const submitBtn = document.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = '처리 중...';
    }
}

// 로딩 숨김
function hideLoading() {
    const submitBtn = document.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = '검색';
    }
}

// 페이지 이동
function goToPage(page) {
    const currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set('page', page);
    window.location.href = currentUrl.toString();
}

// 그룹코드별 필터링
function filterByGcode(gcode) {
    const currentUrl = new URL(window.location.href);
    if (gcode) {
        currentUrl.searchParams.set('gcode', gcode);
    } else {
        currentUrl.searchParams.delete('gcode');
    }
    currentUrl.searchParams.delete('page'); // 페이지 초기화
    window.location.href = currentUrl.toString();
}

// 검색어 필터링
function filterBySearchText(searchText) {
    const currentUrl = new URL(window.location.href);
    if (searchText) {
        currentUrl.searchParams.set('searchText', searchText);
    } else {
        currentUrl.searchParams.delete('searchText');
    }
    currentUrl.searchParams.delete('page'); // 페이지 초기화
    window.location.href = currentUrl.toString();
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

// 테이블 정렬 (필요시 구현)
function sortTable(column) {
    // 테이블 정렬 기능 구현
    console.log('Sort by column:', column);
}

// 데이터 내보내기 (필요시 구현)
function exportData(format) {
    // CSV, Excel 등 데이터 내보내기 기능 구현
    console.log('Export data as:', format);
}

// 배치 작업 (필요시 구현)
function batchAction(action, selectedIds) {
    // 선택된 코드들에 대한 배치 작업 구현
    console.log('Batch action:', action, 'on IDs:', selectedIds);
} 