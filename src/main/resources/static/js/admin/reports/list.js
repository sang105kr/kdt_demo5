/**
 * Admin Reports List Page JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeReportsPage();
});

function initializeReportsPage() {
    // Initialize filters
    initializeFilters();
    
    // Initialize auto action button
    initializeAutoActionButton();
    
    // Initialize table interactions
    initializeTableInteractions();
    
    // Initialize statistics cards
    initializeStatisticsCards();
}

/**
 * Initialize filter functionality
 */
function initializeFilters() {
    const statusFilter = document.getElementById('statusFilter');
    const targetTypeFilter = document.getElementById('targetTypeFilter');
    
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            applyFilters();
        });
    }
    
    if (targetTypeFilter) {
        targetTypeFilter.addEventListener('change', function() {
            applyFilters();
        });
    }
}

/**
 * Apply filters and reload page
 */
function applyFilters() {
    const statusFilter = document.getElementById('statusFilter');
    const targetTypeFilter = document.getElementById('targetTypeFilter');
    
    const status = statusFilter ? statusFilter.value : 'PENDING';
    const targetType = targetTypeFilter ? targetTypeFilter.value : '';
    
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (targetType) params.append('targetType', targetType);
    
    const url = `/admin/reports?${params.toString()}`;
    window.location.href = url;
}

/**
 * Initialize auto action button
 */
function initializeAutoActionButton() {
    const autoActionBtn = document.getElementById('autoActionBtn');
    
    if (autoActionBtn) {
        autoActionBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            if (confirm('자동 조치를 실행하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
                executeAutoActions();
            }
        });
    }
}

/**
 * Execute auto actions
 */
function executeAutoActions() {
    const autoActionBtn = document.getElementById('autoActionBtn');
    const originalText = autoActionBtn.textContent;
    
    // Show loading state
    autoActionBtn.disabled = true;
    autoActionBtn.innerHTML = '<span class="loading-spinner"></span> 실행 중...';
    
    // Create form and submit
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/admin/reports/auto-actions';
    
    document.body.appendChild(form);
    form.submit();
}

/**
 * Initialize table interactions
 */
function initializeTableInteractions() {
    const tableRows = document.querySelectorAll('.reports-table tbody tr');
    
    tableRows.forEach(row => {
        row.addEventListener('click', function(e) {
            // Don't trigger if clicking on action buttons
            if (e.target.closest('.action-buttons')) {
                return;
            }
            
            const reportId = this.dataset.reportId;
            if (reportId) {
                window.location.href = `/admin/reports/${reportId}`;
            }
        });
        
        // Add hover effect
        row.style.cursor = 'pointer';
    });
}

/**
 * Initialize statistics cards
 */
function initializeStatisticsCards() {
    const statCards = document.querySelectorAll('.stat-card');
    
    statCards.forEach(card => {
        card.addEventListener('click', function() {
            const status = this.dataset.status;
            if (status) {
                const params = new URLSearchParams();
                params.append('status', status);
                window.location.href = `/admin/reports?${params.toString()}`;
            }
        });
        
        // Add hover effect
        card.style.cursor = 'pointer';
    });
}

/**
 * Process report status
 */
function processReport(reportId, status) {
    if (!confirm(`신고를 ${getStatusText(status)} 상태로 변경하시겠습니까?`)) {
        return;
    }
    
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/admin/reports/${reportId}/process`;
    
    const statusInput = document.createElement('input');
    statusInput.type = 'hidden';
    statusInput.name = 'status';
    statusInput.value = status;
    
    form.appendChild(statusInput);
    document.body.appendChild(form);
    form.submit();
}

/**
 * Get status text in Korean
 */
function getStatusText(status) {
    const statusMap = {
        'PENDING': '대기',
        'PROCESSING': '처리중',
        'RESOLVED': '해결됨',
        'REJECTED': '거부됨'
    };
    
    return statusMap[status] || status;
}

/**
 * Show success message
 */
function showSuccessMessage(message) {
    showMessage(message, 'success');
}

/**
 * Show error message
 */
function showErrorMessage(message) {
    showMessage(message, 'error');
}

/**
 * Show message with type
 */
function showMessage(message, type) {
    // Remove existing messages
    const existingMessages = document.querySelectorAll('.message');
    existingMessages.forEach(msg => msg.remove());
    
    // Create message element
    const messageDiv = document.createElement('div');
    messageDiv.className = `message message-${type}`;
    messageDiv.textContent = message;
    
    // Add styles
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 8px;
        color: white;
        font-weight: 600;
        z-index: 1000;
        animation: slideIn 0.3s ease;
        ${type === 'success' ? 'background: #10b981;' : 'background: #ef4444;'}
    `;
    
    document.body.appendChild(messageDiv);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        messageDiv.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => messageDiv.remove(), 300);
    }, 5000);
}

/**
 * Add CSS animations
 */
function addMessageAnimations() {
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        
        @keyframes slideOut {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);
}

/**
 * Initialize search functionality
 */
function initializeSearch() {
    const searchInput = document.getElementById('searchInput');
    
    if (searchInput) {
        let searchTimeout;
        
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
 * Perform search
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
 * Export reports to CSV
 */
function exportReports() {
    const currentUrl = new URL(window.location);
    const params = currentUrl.searchParams;
    
    const exportUrl = `/admin/reports/export?${params.toString()}`;
    window.open(exportUrl, '_blank');
}

/**
 * Bulk process reports
 */
function bulkProcessReports() {
    const selectedReports = document.querySelectorAll('input[name="selectedReports"]:checked');
    
    if (selectedReports.length === 0) {
        alert('처리할 신고를 선택해주세요.');
        return;
    }
    
    const status = prompt('변경할 상태를 입력하세요 (PENDING, PROCESSING, RESOLVED, REJECTED):');
    if (!status) return;
    
    if (!['PENDING', 'PROCESSING', 'RESOLVED', 'REJECTED'].includes(status.toUpperCase())) {
        alert('올바른 상태를 입력해주세요.');
        return;
    }
    
    if (confirm(`${selectedReports.length}개의 신고를 ${getStatusText(status.toUpperCase())} 상태로 변경하시겠습니까?`)) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/admin/reports/bulk-process';
        
        const statusInput = document.createElement('input');
        statusInput.type = 'hidden';
        statusInput.name = 'status';
        statusInput.value = status.toUpperCase();
        
        form.appendChild(statusInput);
        
        selectedReports.forEach(checkbox => {
            const reportIdInput = document.createElement('input');
            reportIdInput.type = 'hidden';
            reportIdInput.name = 'reportIds';
            reportIdInput.value = checkbox.value;
            form.appendChild(reportIdInput);
        });
        
        document.body.appendChild(form);
        form.submit();
    }
}

// Initialize message animations
addMessageAnimations();

// Check for flash messages on page load
document.addEventListener('DOMContentLoaded', function() {
    const successMessage = document.querySelector('[data-success-message]');
    const errorMessage = document.querySelector('[data-error-message]');
    
    if (successMessage) {
        showSuccessMessage(successMessage.dataset.successMessage);
    }
    
    if (errorMessage) {
        showErrorMessage(errorMessage.dataset.errorMessage);
    }
}); 