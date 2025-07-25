/**
 * 상품 상세 페이지 JavaScript
 * 모달 기능 및 이미지 확대 기능
 */

// DOM 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
});

/**
 * 페이지 초기화
 */
function initializePage() {
    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', function(event) {
        const deleteModal = document.getElementById('deleteModal');
        const imageModal = document.getElementById('imageModal');
        
        if (event.target === deleteModal) {
            closeDeleteModal();
        }
        
        if (event.target === imageModal) {
            closeImageModal();
        }
    });
    
    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeDeleteModal();
            closeImageModal();
        }
    });
    
    // 이미지 썸네일 클릭 이벤트
    const thumbnails = document.querySelectorAll('.file-thumbnail img');
    thumbnails.forEach(thumbnail => {
        thumbnail.addEventListener('click', function() {
            openImageModal(this.src, this.alt);
        });
    });
}

/**
 * 삭제 모달 표시
 */
function showDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden'; // 스크롤 방지
    }
}

/**
 * 삭제 모달 닫기
 */
function closeDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto'; // 스크롤 복원
    }
}

/**
 * 이미지 모달 열기
 */
function openImageModal(imageSrc, imageAlt) {
    const modal = document.getElementById('imageModal');
    const modalImage = document.getElementById('modalImage');
    const modalTitle = document.getElementById('imageModalTitle');
    
    if (modal && modalImage && modalTitle) {
        modalImage.src = imageSrc;
        modalImage.alt = imageAlt;
        modalTitle.textContent = imageAlt || '이미지 보기';
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden'; // 스크롤 방지
    }
}

/**
 * 이미지 모달 닫기
 */
function closeImageModal() {
    const modal = document.getElementById('imageModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto'; // 스크롤 복원
    }
}

/**
 * 파일 크기 포맷팅
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * 파일 타입 아이콘 반환
 */
function getFileTypeIcon(fileType) {
    const typeMap = {
        'application/pdf': '📄',
        'application/msword': '📝',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '📝',
        'text/plain': '📄',
        'image/jpeg': '🖼️',
        'image/png': '🖼️',
        'image/gif': '🖼️',
        'image/webp': '🖼️'
    };
    
    return typeMap[fileType] || '📄';
}

/**
 * 파일 다운로드
 */
function downloadFile(fileUrl, fileName) {
    const link = document.createElement('a');
    link.href = fileUrl;
    link.download = fileName;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * 파일 미리보기 (이미지가 아닌 경우)
 */
function previewFile(fileUrl, fileType) {
    if (fileType.startsWith('image/')) {
        openImageModal(fileUrl, '파일 미리보기');
    } else if (fileType === 'application/pdf') {
        // PDF는 새 창에서 열기
        window.open(fileUrl, '_blank');
    } else {
        // 기타 파일은 다운로드
        downloadFile(fileUrl, '파일');
    }
}

/**
 * 페이지 새로고침
 */
function refreshPage() {
    window.location.reload();
}

/**
 * 뒤로 가기
 */
function goBack() {
    window.history.back();
}

/**
 * 목록으로 이동
 */
function goToList() {
    window.location.href = '/admin/product';
}

/**
 * 수정 페이지로 이동
 */
function goToEdit(productId) {
    window.location.href = `/admin/product/${productId}/edit`;
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
 * 확인 대화상자
 */
function confirmAction(message, callback) {
    showModal({
        title: '확인',
        message: message,
        onConfirm: () => {
            callback();
        },
        onCancel: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
}

/**
 * 페이지 로드 애니메이션
 */
function animatePageLoad() {
    const container = document.querySelector('.product-detail-container');
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
window.addEventListener('load', animatePageLoad);

// 전역 함수로 노출
window.showDeleteModal = showDeleteModal;
window.closeDeleteModal = closeDeleteModal;
window.openImageModal = openImageModal;
window.closeImageModal = closeImageModal;
window.downloadFile = downloadFile;
window.previewFile = previewFile;
window.refreshPage = refreshPage;
window.goBack = goBack;
window.goToList = goToList;
window.goToEdit = goToEdit;
window.showSuccess = showSuccess;
window.showError = showError;
window.confirmAction = confirmAction;

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
`;
document.head.appendChild(style); 