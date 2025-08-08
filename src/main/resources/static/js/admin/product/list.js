/**
 * 관리자 상품 목록 페이지 JavaScript
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    // CSSManager와 NotificationManager 사용
    const cssManager = window.CSSManager;
    const notify = window.notify;

    // 관리자 상품 목록 전용 스타일 생성
    cssManager.addStyle('admin-product-list', `
        .admin-product-list-page .product-list-container {
            opacity: 0;
            transform: translateY(20px);
            transition: all var(--transition-normal);
        }

        .admin-product-list-page .product-list-container.loaded {
            opacity: 1;
            transform: translateY(0);
        }

        .admin-product-list-page .selected-products-list {
            max-height: 200px;
            overflow-y: auto;
            margin-top: var(--space-md);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-sm);
            background: var(--color-light-gray);
        }

        .admin-product-list-page .selected-product-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: var(--space-sm);
            border-bottom: 1px solid var(--color-border);
        }

        .admin-product-list-page .selected-product-item:last-child {
            border-bottom: none;
        }

        .admin-product-list-page .product-id {
            font-weight: 600;
            color: var(--color-text-muted);
            min-width: 60px;
        }

        .admin-product-list-page .product-name {
            flex: 1;
            margin-left: var(--space-sm);
            color: var(--color-text);
        }

        .admin-product-list-page .table-hover {
            transition: background var(--transition-fast);
        }

        .admin-product-list-page .table-hover:hover {
            background: var(--color-light-gray);
        }

        .admin-product-list-page .bulk-actions {
            background: var(--color-light-gray);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-md);
            margin-bottom: var(--space-lg);
            display: none;
        }

        .admin-product-list-page .bulk-actions.show {
            display: block;
        }

        .admin-product-list-page .selected-count {
            color: var(--color-primary);
            font-weight: 600;
        }
    `);

    // 전역 변수
    let selectedProducts = new Set();
    let isSelectAllChecked = false;

    // DOM 요소들
    const selectAllCheckbox = document.getElementById('selectAll');
    const productCheckboxes = document.querySelectorAll('.product-checkbox');
    const bulkActionsContainer = document.querySelector('.bulk-actions');
    const selectedCountElement = document.querySelector('.selected-count');

    /**
     * 초기화
     */
    function init() {
        initializeEventListeners();
        initializeModalHandlers();
        initializeCheckboxHandlers();
        initializeTableHoverEffects();
        animatePageLoad();
    }

    /**
     * 이벤트 리스너 초기화
     */
    function initializeEventListeners() {
        // 검색 폼 제출
        const searchForm = document.querySelector('.search-form');
        if (searchForm) {
            searchForm.addEventListener('submit', function(e) {
                e.preventDefault();
                performSearch();
            });
        }

        // 필터 변경
        const filterSelects = document.querySelectorAll('.filter-select');
        filterSelects.forEach(select => {
            select.addEventListener('change', function() {
                applyFilters();
            });
        });

        // 정렬 변경
        const sortSelect = document.querySelector('.sort-select');
        if (sortSelect) {
            sortSelect.addEventListener('change', function() {
                applyFilters();
            });
        }
    }

    /**
     * 모달 핸들러 초기화
     */
    function initializeModalHandlers() {
        // 개별 삭제 모달
        const deleteButtons = document.querySelectorAll('.btn-delete');
        deleteButtons.forEach(button => {
            button.addEventListener('click', handleIndividualDelete);
        });

        // 일괄 삭제 버튼
        const bulkDeleteButton = document.querySelector('.btn-bulk-delete');
        if (bulkDeleteButton) {
            bulkDeleteButton.addEventListener('click', handleBulkDelete);
        }

        // 모달 닫기 버튼들
        const closeButtons = document.querySelectorAll('.modal-close, .btn-cancel');
        closeButtons.forEach(button => {
            button.addEventListener('click', function() {
                const modal = this.closest('.modal');
                if (modal) {
                    closeModal(modal);
                }
            });
        });
    }

    /**
     * 체크박스 핸들러 초기화
     */
    function initializeCheckboxHandlers() {
        // 전체 선택 체크박스
        if (selectAllCheckbox) {
            selectAllCheckbox.addEventListener('change', function() {
                isSelectAllChecked = this.checked;
                productCheckboxes.forEach(checkbox => {
                    checkbox.checked = isSelectAllChecked;
                    if (isSelectAllChecked) {
                        selectedProducts.add(checkbox.value);
                    } else {
                        selectedProducts.delete(checkbox.value);
                    }
                });
                updateBulkActionsVisibility();
                updateSelectedCount();
            });
        }

        // 개별 체크박스들
        productCheckboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                if (this.checked) {
                    selectedProducts.add(this.value);
                } else {
                    selectedProducts.delete(this.value);
                }
                updateSelectAllCheckbox();
                updateBulkActionsVisibility();
                updateSelectedCount();
            });
        });
    }

    /**
     * 전체 선택 체크박스 업데이트
     */
    function updateSelectAllCheckbox() {
        if (!selectAllCheckbox) return;

        const checkedCount = selectedProducts.size;
        const totalCount = productCheckboxes.length;

        if (checkedCount === 0) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
        } else if (checkedCount === totalCount) {
            selectAllCheckbox.checked = true;
            selectAllCheckbox.indeterminate = false;
        } else {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = true;
        }
    }

    /**
     * 일괄 작업 버튼 표시/숨김
     */
    function updateBulkActionsVisibility() {
        if (!bulkActionsContainer) return;

        if (selectedProducts.size > 0) {
            bulkActionsContainer.classList.add('show');
            cssManager.animate(bulkActionsContainer, 'slide-in', 300);
        } else {
            bulkActionsContainer.classList.remove('show');
        }
    }

    /**
     * 선택된 상품 개수 업데이트
     */
    function updateSelectedCount() {
        if (selectedCountElement) {
            selectedCountElement.textContent = selectedProducts.size;
        }
    }

    /**
     * 개별 삭제 처리
     */
    function handleIndividualDelete(event) {
        event.preventDefault();
        const productId = event.target.dataset.productId;
        const productName = event.target.dataset.productName;

        if (confirm(`"${productName}" 상품을 삭제하시겠습니까?`)) {
            deleteProduct(productId);
        }
    }

    /**
     * 일괄 삭제 처리
     */
    function handleBulkDelete() {
        if (selectedProducts.size === 0) {
            notify.warning('삭제할 상품을 선택해주세요.', '선택 필요');
            return;
        }

        const productList = Array.from(selectedProducts).map(id => {
            const checkbox = document.querySelector(`input[value="${id}"]`);
            const productName = checkbox?.dataset.productName || `상품 ${id}`;
            return { id, name: productName };
        });

        showBulkDeleteModal(productList);
    }

    /**
     * 상품 삭제 API 호출
     */
    async function deleteProduct(productId) {
        try {
            const response = await ajax.delete(`/api/admin/products/${productId}`);
            
            if (response.success) {
                notify.success('상품이 삭제되었습니다.', '삭제 완료');
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                notify.error(response.message || '삭제에 실패했습니다.', '삭제 오류');
            }
        } catch (error) {
            console.error('Delete product error:', error);
            notify.error('삭제 중 오류가 발생했습니다.', '오류');
        }
    }

    /**
     * 일괄 삭제 확인
     */
    async function confirmBulkDelete() {
        if (selectedProducts.size === 0) {
            notify.warning('삭제할 상품을 선택해주세요.', '선택 필요');
            return;
        }

        try {
            const response = await ajax.post('/api/admin/products/bulk-delete', {
                productIds: Array.from(selectedProducts)
            });

            if (response.success) {
                notify.success(`${selectedProducts.size}개의 상품이 삭제되었습니다.`, '일괄 삭제 완료');
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                notify.error(response.message || '일괄 삭제에 실패했습니다.', '삭제 오류');
            }
        } catch (error) {
            console.error('Bulk delete error:', error);
            notify.error('일괄 삭제 중 오류가 발생했습니다.', '오류');
        }
    }

    /**
     * 일괄 삭제 모달 표시
     */
    function showBulkDeleteModal(productList) {
        const modal = document.getElementById('bulkDeleteModal');
        if (!modal) return;

        const productListContainer = modal.querySelector('.selected-products-list');
        if (productListContainer) {
            productListContainer.innerHTML = productList.map(product => `
                <div class="selected-product-item">
                    <span class="product-id">${product.id}</span>
                    <span class="product-name">${product.name}</span>
                </div>
            `).join('');
        }

        openModal(modal);
    }

    /**
     * 모달 열기
     */
    function openModal(modal) {
        modal.style.display = 'flex';
        cssManager.animate(modal, 'fade-in', 300);
    }

    /**
     * 모달 닫기
     */
    function closeModal(modal) {
        cssManager.animate(modal, 'fade-in', 300);
        setTimeout(() => {
            modal.style.display = 'none';
        }, 300);
    }

    /**
     * 테이블 호버 효과 초기화
     */
    function initializeTableHoverEffects() {
        const tableRows = document.querySelectorAll('.product-table tbody tr');
        tableRows.forEach(row => {
            row.classList.add('table-hover');
        });
    }

    /**
     * 페이지 로드 애니메이션
     */
    function animatePageLoad() {
        const container = document.querySelector('.product-list-container');
        if (container) {
            setTimeout(() => {
                container.classList.add('loaded');
            }, 100);
        }
    }

    /**
     * 검색 수행
     */
    async function performSearch() {
        const searchForm = document.querySelector('.search-form');
        if (!searchForm) return;

        const formData = new FormData(searchForm);
        const searchParams = new URLSearchParams();

        for (const [key, value] of formData.entries()) {
            if (value.trim()) {
                searchParams.append(key, value);
            }
        }

        try {
            const url = `/admin/products?${searchParams.toString()}`;
            window.location.href = url;
        } catch (error) {
            console.error('Search error:', error);
            notify.error('검색 중 오류가 발생했습니다.', '검색 오류');
        }
    }

    /**
     * 필터 적용
     */
    function applyFilters() {
        performSearch();
    }

    // 초기화 실행
    init();
}); 