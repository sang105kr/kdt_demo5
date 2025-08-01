/**
 * Admin Report Detail Page JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeReportDetail();
});

/**
 * Initialize report detail page functionality
 */
function initializeReportDetail() {
    // Initialize modal functionality
    initializeModal();
    
    // Initialize form handling
    initializeFormHandling();
    
    // Initialize status badges
    initializeStatusBadges();
    
    // Initialize related reports interactions
    initializeRelatedReports();
}

/**
 * Initialize modal functionality
 */
function initializeModal() {
    const modal = document.getElementById('processModal');
    const closeBtn = modal.querySelector('.close');
    
    // Close modal when clicking on X
    closeBtn.addEventListener('click', closeProcessModal);
    
    // Close modal when clicking outside
    modal.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeProcessModal();
        }
    });
    
    // Close modal with Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && modal.style.display === 'block') {
            closeProcessModal();
        }
    });
}

/**
 * Initialize form handling
 */
function initializeFormHandling() {
    const processForm = document.getElementById('processForm');
    
    if (processForm) {
        processForm.addEventListener('submit', handleProcessSubmit);
    }
}

/**
 * Initialize status badges with appropriate styling
 */
function initializeStatusBadges() {
    const statusElements = document.querySelectorAll('.report-status, .related-report-status');
    
    statusElements.forEach(element => {
        const status = element.textContent.toLowerCase();
        element.className = element.className.replace(/status-\w+/g, '');
        element.classList.add(`status-${status}`);
    });
}

/**
 * Initialize related reports interactions
 */
function initializeRelatedReports() {
    const relatedReportItems = document.querySelectorAll('.related-report-item');
    
    relatedReportItems.forEach(item => {
        item.addEventListener('click', function(event) {
            // Don't navigate if clicking on the "상세보기" button
            if (event.target.tagName === 'A' || event.target.closest('a')) {
                return;
            }
            
            // Navigate to the related report detail
            const reportId = this.querySelector('.related-report-id').textContent.replace('#', '');
            window.location.href = `/admin/reports/${reportId}`;
        });
        
        // Add hover effect
        item.style.cursor = 'pointer';
    });
}

/**
 * Open the process modal
 */
function openProcessModal() {
    const modal = document.getElementById('processModal');
    modal.style.display = 'block';
    
    // Focus on the first form element
    const firstInput = modal.querySelector('select, textarea, input');
    if (firstInput) {
        firstInput.focus();
    }
    
    // Prevent body scroll
    document.body.style.overflow = 'hidden';
}

/**
 * Close the process modal
 */
function closeProcessModal() {
    const modal = document.getElementById('processModal');
    modal.style.display = 'none';
    
    // Restore body scroll
    document.body.style.overflow = 'auto';
    
    // Reset form
    const form = document.getElementById('processForm');
    if (form) {
        form.reset();
    }
}

/**
 * Handle process form submission
 */
function handleProcessSubmit(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    // Validate form
    if (!validateProcessForm(formData)) {
        return;
    }
    
    // Show loading state
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = '처리 중...';
    submitBtn.disabled = true;
    
    // Submit form using ajax
    try {
        const data = await ajax.post(form.action, formData);
        
        if (data && data.code === '00') {
            showSuccessMessage('신고가 성공적으로 처리되었습니다.');
            
            // Close modal
            closeProcessModal();
            
            // Reload page after a short delay to show updated status
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showErrorMessage(data?.message || '신고 처리 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('Error:', error);
        showErrorMessage('신고 처리 중 오류가 발생했습니다.');
    } finally {
        // Restore button state
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
}

/**
 * Validate process form
 */
function validateProcessForm(formData) {
    const status = formData.get('status');
    const adminNotes = formData.get('adminNotes');
    
    if (!status) {
        showErrorMessage('처리 상태를 선택해주세요.');
        return false;
    }
    
    if (!adminNotes || adminNotes.trim() === '') {
        showErrorMessage('관리자 메모를 입력해주세요.');
        return false;
    }
    
    if (adminNotes.length > 1000) {
        showErrorMessage('관리자 메모는 1000자 이내로 입력해주세요.');
        return false;
    }
    
    return true;
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
 * Show message with appropriate styling
 */
function showMessage(message, type) {
    // Remove existing message
    const existingMessage = document.querySelector('.message-toast');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    // Create message element
    const messageElement = document.createElement('div');
    messageElement.className = `message-toast message-${type}`;
    messageElement.innerHTML = `
        <div class="message-content">
            <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
            <span>${message}</span>
        </div>
    `;
    
    // Add styles
    messageElement.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        padding: 1rem 1.5rem;
        border-radius: 0.5rem;
        color: white;
        font-weight: 500;
        box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
        animation: slideIn 0.3s ease-out;
        max-width: 400px;
    `;
    
    if (type === 'success') {
        messageElement.style.backgroundColor = '#10b981';
    } else {
        messageElement.style.backgroundColor = '#ef4444';
    }
    
    // Add to page
    document.body.appendChild(messageElement);
    
    // Remove after 5 seconds
    setTimeout(() => {
        if (messageElement.parentNode) {
            messageElement.style.animation = 'slideOut 0.3s ease-in';
            setTimeout(() => {
                if (messageElement.parentNode) {
                    messageElement.remove();
                }
            }, 300);
        }
    }, 5000);
}

/**
 * Add CSS animations for messages
 */
function addMessageStyles() {
    if (!document.getElementById('message-styles')) {
        const style = document.createElement('style');
        style.id = 'message-styles';
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
            
            .message-content {
                display: flex;
                align-items: center;
                gap: 0.5rem;
            }
            
            .message-content i {
                font-size: 1.125rem;
            }
        `;
        document.head.appendChild(style);
    }
}

// Add message styles when script loads
addMessageStyles();

/**
 * Utility function to format date
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Utility function to truncate text
 */
function truncateText(text, maxLength = 100) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

/**
 * Utility function to escape HTML
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
} 