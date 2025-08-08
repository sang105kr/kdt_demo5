/**
 * 관리자 리뷰 목록 페이지 JavaScript
 * 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자 리뷰 목록 페이지 로드됨');
    
    // 전역 변수
    const reviewTable = document.querySelector('.review-table');
    const statusButtons = document.querySelectorAll('.status-btn');
    const statusModal = document.getElementById('statusModal');
    const statusForm = document.getElementById('statusForm');
    const statusReviewTitle = document.getElementById('statusReviewTitle');
    const filterForm = document.querySelector('.filter-form');
    
    // 초기화
    initAdminReviewList();
    
    /**
     * 관리자 리뷰 목록 초기화
     */
    function initAdminReviewList() {
        console.log('관리자 리뷰 목록 초기화 시작');
        
        // 상태 변경 버튼 이벤트 초기화
        initStatusButtons();
        
        // 필터 폼 이벤트 초기화
        initFilterForm();
        
        // 테이블 행 클릭 이벤트
        initTableRowClick();
        
        // 모달 이벤트 초기화
        initModalEvents();
        
        // 반응형 처리
        initResponsive();
        
        console.log('관리자 리뷰 목록 초기화 완료');
    }
    
    /**
     * 상태 변경 버튼 이벤트 초기화
     */
    function initStatusButtons() {
        statusButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                const reviewId = this.getAttribute('data-review-id');
                const reviewTitle = this.getAttribute('data-review-title');
                
                if (reviewId && reviewTitle) {
                    showStatusModal(reviewId, reviewTitle);
                }
            });
        });
    }
    
    /**
     * 상태 변경 모달 표시
     */
    function showStatusModal(reviewId, reviewTitle) {
        if (!statusModal || !statusReviewTitle || !statusForm) return;
        
        // 모달 내용 설정
        statusReviewTitle.textContent = reviewTitle;
        
        // 폼 액션 설정
        statusForm.action = `/admin/reviews/${reviewId}/status`;
        
        // 모달 표시
        statusModal.style.display = 'block';
        
        // 배경 클릭 시 모달 닫기
        statusModal.addEventListener('click', function(e) {
            if (e.target === statusModal) {
                closeStatusModal();
            }
        });
        
        // ESC 키로 모달 닫기
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeStatusModal();
            }
        });
        
        console.log('상태 변경 모달 표시:', reviewId, reviewTitle);
    }
    
    /**
     * 상태 변경 모달 닫기
     */
    function closeStatusModal() {
        if (statusModal) {
            statusModal.style.display = 'none';
        }
    }
    
    /**
     * 모달 이벤트 초기화
     */
    function initModalEvents() {
        // 모달 닫기 버튼
        const closeButton = statusModal?.querySelector('.close');
        if (closeButton) {
            closeButton.addEventListener('click', closeStatusModal);
        }
        
        // 모달 취소 버튼
        const cancelButton = statusModal?.querySelector('.btn-secondary');
        if (cancelButton) {
            cancelButton.addEventListener('click', closeStatusModal);
        }
        
        // 상태 변경 폼 제출 이벤트
        if (statusForm) {
            statusForm.addEventListener('submit', function(e) {
                e.preventDefault();
                
                const loadingElement = showLoading();
                
                // CSRF 토큰 추가 (필요시)
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
                if (csrfToken) {
                    const csrfInput = document.createElement('input');
                    csrfInput.type = 'hidden';
                    csrfInput.name = '_csrf';
                    csrfInput.value = csrfToken;
                    statusForm.appendChild(csrfInput);
                }
                
                // 폼 제출
                fetch(statusForm.action, {
                    method: 'POST',
                    body: new FormData(statusForm),
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                })
                .then(response => {
                    if (response.redirected) {
                        window.location.href = response.url;
                    } else {
                        return response.json();
                    }
                })
                .then(data => {
                    hideLoading(loadingElement);
                    closeStatusModal();
                    
                    if (data && data.success) {
                        showMessage('리뷰 상태가 변경되었습니다.', 'success');
                        // 페이지 새로고침
                        setTimeout(() => {
                            window.location.reload();
                        }, 1000);
                    } else if (data) {
                        showMessage(data.message || '상태 변경에 실패했습니다.', 'error');
                    }
                })
                .catch(error => {
                    hideLoading(loadingElement);
                    closeStatusModal();
                    showMessage('상태 변경 중 오류가 발생했습니다.', 'error');
                    console.error('상태 변경 오류:', error);
                });
            });
        }
    }
    
    /**
     * 필터 폼 이벤트 초기화
     */
    function initFilterForm() {
        if (!filterForm) return;
        
        // 검색어 입력 필드 이벤트
        const keywordInput = filterForm.querySelector('#keyword');
        if (keywordInput) {
            keywordInput.addEventListener('keyup', function(e) {
                if (e.key === 'Enter') {
                    filterForm.dispatchEvent(new Event('submit'));
                }
            });
        }
        
        // 날짜 필드 자동 설정
        const startDateInput = filterForm.querySelector('#startDate');
        const endDateInput = filterForm.querySelector('#endDate');
        
        if (startDateInput && endDateInput) {
            // 시작일이 변경되면 종료일 최소값 설정
            startDateInput.addEventListener('change', function() {
                if (this.value) {
                    endDateInput.min = this.value;
                }
            });
            
            // 종료일이 변경되면 시작일 최대값 설정
            endDateInput.addEventListener('change', function() {
                if (this.value) {
                    startDateInput.max = this.value;
                }
            });
        }
    }
    
    /**
     * 테이블 행 클릭 이벤트 초기화
     */
    function initTableRowClick() {
        if (!reviewTable) return;
        
        const rows = reviewTable.querySelectorAll('tbody tr');
        
        rows.forEach(row => {
            row.addEventListener('click', function(e) {
                // 액션 버튼 클릭 시는 무시
                if (e.target.closest('.action-buttons') || 
                    e.target.closest('button') || 
                    e.target.closest('a')) {
                    return;
                }
                
                const reviewId = this.getAttribute('data-review-id');
                if (reviewId) {
                    window.location.href = `/admin/reviews/${reviewId}`;
                }
            });
            
            // 호버 효과
            row.addEventListener('mouseenter', function() {
                if (!this.querySelector('.action-buttons')) {
                    this.style.cursor = 'pointer';
                }
            });
        });
    }
    
    /**
     * 반응형 처리
     */
    function initResponsive() {
        // 테이블 반응형 처리
        if (reviewTable) {
            const handleResize = () => {
                if (window.innerWidth <= 768) {
                    reviewTable.style.fontSize = '0.8rem';
                } else {
                    reviewTable.style.fontSize = '';
                }
            };
            
            window.addEventListener('resize', handleResize);
            handleResize(); // 초기 실행
        }
        
        // 필터 컨테이너 반응형 처리
        const filterContainer = document.querySelector('.filter-container');
        if (filterContainer) {
            const handleFilterResize = () => {
                if (window.innerWidth <= 1024) {
                    const filterRow = filterContainer.querySelector('.filter-row');
                    if (filterRow) {
                        filterRow.style.flexDirection = 'column';
                    }
                } else {
                    const filterRow = filterContainer.querySelector('.filter-row');
                    if (filterRow) {
                        filterRow.style.flexDirection = 'row';
                    }
                }
            };
            
            window.addEventListener('resize', handleFilterResize);
            handleFilterResize(); // 초기 실행
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
                <p>처리 중...</p>
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
        if (!reviewTable) return;
        
        const headers = reviewTable.querySelectorAll('th');
        
        headers.forEach((header, index) => {
            // 정렬 가능한 컬럼만 (번호, 신고수, 도움됨, 작성일)
            if (index === 0 || index === 4 || index === 5 || index === 6) {
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
        const tbody = reviewTable.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));
        
        // no-data 행 제외
        const dataRows = rows.filter(row => !row.querySelector('.no-data'));
        
        dataRows.sort((a, b) => {
            const aValue = a.cells[columnIndex].textContent.trim();
            const bValue = b.cells[columnIndex].textContent.trim();
            
            // 숫자 정렬
            if (columnIndex === 0 || columnIndex === 4 || columnIndex === 5) {
                return parseInt(aValue) - parseInt(bValue);
            }
            
            // 날짜 정렬
            if (columnIndex === 6) {
                return new Date(aValue) - new Date(bValue);
            }
            
            return aValue.localeCompare(bValue);
        });
        
        // 정렬된 행을 다시 추가
        dataRows.forEach(row => tbody.appendChild(row));
    }
    
    // 테이블 정렬 기능 초기화 (선택적)
    // initTableSort();
    
    // 전역 함수로 모달 닫기 함수 노출
    window.closeStatusModal = closeStatusModal;
    
    console.log('관리자 리뷰 목록 JavaScript 로드 완료');
}); 