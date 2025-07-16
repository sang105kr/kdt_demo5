/**
 * 상품 목록 페이지 JavaScript
 * 모달 기능 및 체크박스 관리
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('상품 목록 페이지 로드됨');
    
    initializeEventListeners();
    initializeCheckboxHandlers();
    initializeModalHandlers();
});

/**
 * 이벤트 리스너 초기화
 */
function initializeEventListeners() {
    // 개별 삭제 버튼
    const deleteButtons = document.querySelectorAll('.delete-btn');
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', handleIndividualDelete);
    });
    
    // 일괄 삭제 버튼
    const bulkDeleteBtn = document.getElementById('bulkDeleteBtn');
    if (bulkDeleteBtn) {
        bulkDeleteBtn.addEventListener('click', handleBulkDelete);
    }
}

/**
 * 모달 핸들러 초기화
 */
function initializeModalHandlers() {
    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', function(event) {
        const deleteModal = document.getElementById('deleteModal');
        const bulkDeleteModal = document.getElementById('bulkDeleteModal');
        
        if (event.target === deleteModal) {
            closeDeleteModal();
        }
        
        if (event.target === bulkDeleteModal) {
            closeBulkDeleteModal();
        }
    });
    
    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeDeleteModal();
            closeBulkDeleteModal();
        }
    });
}

/**
 * 체크박스 핸들러 초기화
 */
function initializeCheckboxHandlers() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const productCheckboxes = document.querySelectorAll('.product-checkbox');
    const bulkActions = document.querySelector('.bulk-actions');
    
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            const isChecked = this.checked;
            
            productCheckboxes.forEach(checkbox => {
                checkbox.checked = isChecked;
            });
            
            updateBulkActionsVisibility();
            updateSelectedCount();
        });
    }
    
    productCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateSelectAllCheckbox();
            updateBulkActionsVisibility();
            updateSelectedCount();
        });
    });
}

/**
 * 전체 선택 체크박스 상태 업데이트
 */
function updateSelectAllCheckbox() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const productCheckboxes = document.querySelectorAll('.product-checkbox');
    const checkedCheckboxes = document.querySelectorAll('.product-checkbox:checked');
    
    if (selectAllCheckbox) {
        if (checkedCheckboxes.length === 0) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
        } else if (checkedCheckboxes.length === productCheckboxes.length) {
            selectAllCheckbox.checked = true;
            selectAllCheckbox.indeterminate = false;
        } else {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = true;
        }
    }
}

/**
 * 일괄 삭제 버튼 표시/숨김 업데이트
 */
function updateBulkActionsVisibility() {
    const checkedCheckboxes = document.querySelectorAll('.product-checkbox:checked');
    const bulkActions = document.querySelector('.bulk-actions');
    
    if (bulkActions) {
        if (checkedCheckboxes.length > 0) {
            bulkActions.style.display = 'block';
        } else {
            bulkActions.style.display = 'none';
        }
    }
}

/**
 * 선택된 상품 개수 업데이트
 */
function updateSelectedCount() {
    const checkedCheckboxes = document.querySelectorAll('.product-checkbox:checked');
    const selectedCountElement = document.getElementById('selectedCount');
    
    if (selectedCountElement) {
        selectedCountElement.textContent = checkedCheckboxes.length;
    }
}

/**
 * 개별 삭제 처리
 */
function handleIndividualDelete(event) {
    const button = event.target.closest('.delete-btn');
    const productId = button.getAttribute('data-product-id');
    const productName = button.getAttribute('data-product-name');
    
    // 모달에 정보 설정
    const deleteProductName = document.getElementById('deleteProductName');
    const singleDeleteForm = document.getElementById('singleDeleteForm');
    
    if (deleteProductName && singleDeleteForm) {
        deleteProductName.textContent = productName;
        singleDeleteForm.action = `/admin/product/${productId}/delete`;
        
        // 모달 표시
        showDeleteModal();
    }
}

/**
 * 일괄 삭제 처리
 */
function handleBulkDelete() {
    const checkedCheckboxes = document.querySelectorAll('.product-checkbox:checked');
    
    if (checkedCheckboxes.length === 0) {
        showError('삭제할 상품을 선택해주세요.');
        return;
    }
    
    // 선택된 상품 정보 수집
    const selectedProducts = Array.from(checkedCheckboxes).map(checkbox => {
        const row = checkbox.closest('tr');
        return {
            id: checkbox.value,
            name: row.querySelector('.product-name-link').textContent
        };
    });
    
    // 모달에 정보 설정
    const bulkDeleteCount = document.getElementById('bulkDeleteCount');
    const selectedProductsList = document.getElementById('selectedProductsList');
    
    if (bulkDeleteCount && selectedProductsList) {
        bulkDeleteCount.textContent = selectedProducts.length;
        
        // 선택된 상품 목록 생성
        selectedProductsList.innerHTML = '';
        selectedProducts.forEach(product => {
            const productItem = document.createElement('div');
            productItem.className = 'selected-product-item';
            productItem.innerHTML = `
                <span class="product-id">${product.id}</span>
                <span class="product-name">${product.name}</span>
            `;
            selectedProductsList.appendChild(productItem);
        });
        
        // 모달 표시
        showBulkDeleteModal();
    }
}

/**
 * 단일 삭제 모달 표시
 */
function showDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden'; // 스크롤 방지
    }
}

/**
 * 단일 삭제 모달 닫기
 */
function closeDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto'; // 스크롤 복원
    }
}

/**
 * 다중 삭제 모달 표시
 */
function showBulkDeleteModal() {
    const modal = document.getElementById('bulkDeleteModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden'; // 스크롤 방지
    }
}

/**
 * 다중 삭제 모달 닫기
 */
function closeBulkDeleteModal() {
    const modal = document.getElementById('bulkDeleteModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto'; // 스크롤 복원
    }
}

/**
 * 다중 삭제 확인 처리
 */
function confirmBulkDelete() {
    const deleteForm = document.getElementById('deleteForm');
    if (deleteForm) {
        deleteForm.submit();
    }
}

/**
 * 테이블 행 호버 효과
 */
function initializeTableHoverEffects() {
    const tableRows = document.querySelectorAll('.product-table tbody tr');
    
    tableRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.backgroundColor = '#f8f9fa';
        });
        
        row.addEventListener('mouseleave', function() {
            this.style.backgroundColor = '';
        });
    });
}

/**
 * 성공 메시지 표시
 */
function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'alert alert-success';
    successDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #d4edda;
        color: #155724;
        border: 1px solid #c3e6cb;
        border-radius: 6px;
        padding: 15px 20px;
        z-index: 1001;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        animation: slideInRight 0.3s ease;
    `;
    successDiv.textContent = message;
    
    document.body.appendChild(successDiv);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        if (successDiv.parentNode) {
            successDiv.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => {
                if (successDiv.parentNode) {
                    successDiv.remove();
                }
            }, 300);
        }
    }, 3000);
}

/**
 * 오류 메시지 표시
 */
function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-danger';
    errorDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #f8d7da;
        color: #721c24;
        border: 1px solid #f5c6cb;
        border-radius: 6px;
        padding: 15px 20px;
        z-index: 1001;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        animation: slideInRight 0.3s ease;
    `;
    errorDiv.textContent = message;
    
    document.body.appendChild(errorDiv);
    
    // 5초 후 자동 제거
    setTimeout(() => {
        if (errorDiv.parentNode) {
            errorDiv.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => {
                if (errorDiv.parentNode) {
                    errorDiv.remove();
                }
            }, 300);
        }
    }, 5000);
}

/**
 * 페이지 로드 시 애니메이션
 */
function animatePageLoad() {
    const container = document.querySelector('.product-list-container');
    if (container) {
        container.style.opacity = '0';
        container.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            container.style.transition = 'all 0.5s ease';
            container.style.opacity = '1';
            container.style.transform = 'translateY(0)';
        }, 100);
    }
}

// 페이지 로드 시 애니메이션 실행
window.addEventListener('load', function() {
    animatePageLoad();
    initializeTableHoverEffects();
});

// 전역 함수로 노출
window.closeDeleteModal = closeDeleteModal;
window.closeBulkDeleteModal = closeBulkDeleteModal;
window.confirmBulkDelete = confirmBulkDelete;

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            opacity: 0;
            transform: translateX(100%);
        }
        to {
            opacity: 1;
            transform: translateX(0);
        }
    }
    
    @keyframes slideOutRight {
        from {
            opacity: 1;
            transform: translateX(0);
        }
        to {
            opacity: 0;
            transform: translateX(100%);
        }
    }
    
    .selected-products-list {
        max-height: 200px;
        overflow-y: auto;
        margin-top: 15px;
        border: 1px solid #e9ecef;
        border-radius: 6px;
        padding: 10px;
        background: #f8f9fa;
    }
    
    .selected-product-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px;
        border-bottom: 1px solid #e9ecef;
    }
    
    .selected-product-item:last-child {
        border-bottom: none;
    }
    
    .product-id {
        font-weight: bold;
        color: #666;
        min-width: 60px;
    }
    
    .product-name {
        flex: 1;
        margin-left: 10px;
        color: #333;
    }
`;
document.head.appendChild(style); 