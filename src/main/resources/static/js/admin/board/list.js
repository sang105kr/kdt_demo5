/**
 * 관리자 게시판 목록 페이지 JavaScript
 * 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자 게시판 목록 페이지 로드됨');
    
    // 전역 변수
    const boardTable = document.querySelector('.board-table');
    const deleteButtons = document.querySelectorAll('.delete-btn');
    const deleteModal = document.getElementById('deleteModal');
    const deleteForm = document.getElementById('deleteForm');
    const deleteBoardTitle = document.getElementById('deleteBoardTitle');
    const filterButtons = document.querySelectorAll('.filter-container .btn');
    
    // 초기화
    initAdminBoardList();
    
    /**
     * 관리자 게시판 목록 초기화
     */
    function initAdminBoardList() {
        console.log('관리자 게시판 목록 초기화 시작');
        
        // 삭제 버튼 이벤트 초기화
        initDeleteButtons();
        
        // 필터 버튼 이벤트 초기화
        initFilterButtons();
        
        // 테이블 행 클릭 이벤트
        initTableRowClick();
        
        // 모달 이벤트 초기화
        initModalEvents();
        
        // 반응형 처리
        initResponsive();
        
        console.log('관리자 게시판 목록 초기화 완료');
    }
    
    /**
     * 삭제 버튼 이벤트 초기화
     */
    function initDeleteButtons() {
        deleteButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                const boardId = this.getAttribute('data-board-id');
                const boardTitle = this.getAttribute('data-board-title');
                
                if (boardId && boardTitle) {
                    showDeleteModal(boardId, boardTitle);
                }
            });
        });
    }
    
    /**
     * 삭제 모달 표시
     */
    function showDeleteModal(boardId, boardTitle) {
        if (!deleteModal || !deleteBoardTitle || !deleteForm) return;
        
        // 모달 내용 설정
        deleteBoardTitle.textContent = boardTitle;
        
        // 폼 액션 설정
        deleteForm.action = `/admin/board/${boardId}/delete`;
        
        // 모달 표시
        deleteModal.style.display = 'block';
        
        // 배경 클릭 시 모달 닫기
        deleteModal.addEventListener('click', function(e) {
            if (e.target === deleteModal) {
                closeDeleteModal();
            }
        });
        
        // ESC 키로 모달 닫기
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeDeleteModal();
            }
        });
        
        console.log('삭제 모달 표시:', boardId, boardTitle);
    }
    
    /**
     * 삭제 모달 닫기
     */
    function closeDeleteModal() {
        if (deleteModal) {
            deleteModal.style.display = 'none';
        }
    }
    
    /**
     * 모달 이벤트 초기화
     */
    function initModalEvents() {
        // 모달 닫기 버튼
        const closeButton = deleteModal?.querySelector('.close');
        if (closeButton) {
            closeButton.addEventListener('click', closeDeleteModal);
        }
        
        // 모달 취소 버튼
        const cancelButton = deleteModal?.querySelector('.btn-secondary');
        if (cancelButton) {
            cancelButton.addEventListener('click', closeDeleteModal);
        }
        
        // 삭제 폼 제출 이벤트
        if (deleteForm) {
            deleteForm.addEventListener('submit', function(e) {
                e.preventDefault();
                
                const loadingElement = showLoading();
                
                // CSRF 토큰 추가 (필요시)
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
                if (csrfToken) {
                    const csrfInput = document.createElement('input');
                    csrfInput.type = 'hidden';
                    csrfInput.name = '_csrf';
                    csrfInput.value = csrfToken;
                    deleteForm.appendChild(csrfInput);
                }
                
                // 폼 제출
                fetch(deleteForm.action, {
                    method: 'POST',
                    body: new FormData(deleteForm),
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                })
                .then(response => response.json())
                .then(data => {
                    hideLoading(loadingElement);
                    closeDeleteModal();
                    
                    if (data.success) {
                        showMessage('게시글이 삭제되었습니다.', 'success');
                        // 페이지 새로고침 또는 행 제거
                        setTimeout(() => {
                            window.location.reload();
                        }, 1000);
                    } else {
                        showMessage(data.message || '삭제에 실패했습니다.', 'error');
                    }
                })
                .catch(error => {
                    hideLoading(loadingElement);
                    closeDeleteModal();
                    showMessage('삭제 중 오류가 발생했습니다.', 'error');
                    console.error('삭제 오류:', error);
                });
            });
        }
    }
    
    /**
     * 필터 버튼 이벤트 초기화
     */
    function initFilterButtons() {
        filterButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                // 현재 활성 필터 표시
                filterButtons.forEach(btn => {
                    btn.classList.remove('btn-primary');
                    btn.classList.add('btn-secondary');
                });
                
                this.classList.remove('btn-secondary');
                this.classList.add('btn-primary');
                
                console.log('필터 변경:', this.href);
            });
        });
    }
    
    /**
     * 테이블 행 클릭 이벤트 초기화
     */
    function initTableRowClick() {
        if (!boardTable) return;
        
        const rows = boardTable.querySelectorAll('tbody tr');
        
        rows.forEach(row => {
            row.addEventListener('click', function(e) {
                // 액션 버튼 클릭 시는 무시
                if (e.target.closest('.action-buttons') || 
                    e.target.closest('button') || 
                    e.target.closest('a')) {
                    return;
                }
                
                const boardId = this.getAttribute('data-board-id');
                if (boardId) {
                    window.location.href = `/admin/board/${boardId}`;
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
        if (boardTable) {
            const handleResize = () => {
                if (window.innerWidth <= 768) {
                    boardTable.style.fontSize = '0.8rem';
                } else {
                    boardTable.style.fontSize = '';
                }
            };
            
            window.addEventListener('resize', handleResize);
            handleResize(); // 초기 실행
        }
        
        // 필터 컨테이너 반응형 처리
        const filterContainer = document.querySelector('.filter-container');
        if (filterContainer) {
            const handleFilterResize = () => {
                if (window.innerWidth <= 768) {
                    filterContainer.style.flexDirection = 'column';
                } else {
                    filterContainer.style.flexDirection = 'row';
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
        if (!boardTable) return;
        
        const headers = boardTable.querySelectorAll('th');
        
        headers.forEach((header, index) => {
            // 정렬 가능한 컬럼만 (번호, 조회수, 작성일)
            if (index === 0 || index === 5 || index === 6) {
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
        
        // no-data 행 제외
        const dataRows = rows.filter(row => !row.querySelector('.no-data'));
        
        dataRows.sort((a, b) => {
            const aValue = a.cells[columnIndex].textContent.trim();
            const bValue = b.cells[columnIndex].textContent.trim();
            
            // 숫자 정렬
            if (columnIndex === 0 || columnIndex === 5) {
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
    window.closeDeleteModal = closeDeleteModal;
    
    console.log('관리자 게시판 목록 JavaScript 로드 완료');
}); 