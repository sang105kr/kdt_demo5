/**
 * 관리자 상품 상세 페이지 JavaScript
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    // CSSManager와 NotificationManager 사용
    const cssManager = window.CSSManager;
    const notify = window.notify;

    // 관리자 상품 상세 전용 스타일 생성
    cssManager.addStyle('admin-product-detail', `
        .admin-product-detail-page .product-detail-container {
            opacity: 0;
            transform: translateY(20px);
            transition: all var(--transition-normal);
        }

        .admin-product-detail-page .product-detail-container.loaded {
            opacity: 1;
            transform: translateY(0);
        }

        .admin-product-detail-page .file-thumbnail {
            cursor: pointer;
            transition: all var(--transition-fast);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            overflow: hidden;
        }

        .admin-product-detail-page .file-thumbnail:hover {
            transform: scale(1.05);
            box-shadow: var(--shadow-md);
        }

        .admin-product-detail-page .file-thumbnail img {
            width: 100%;
            height: auto;
            display: block;
        }

        .admin-product-detail-page .product-info-card {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-lg);
            margin-bottom: var(--space-lg);
        }

        .admin-product-detail-page .product-info-label {
            font-weight: 600;
            color: var(--color-text);
            margin-bottom: var(--space-xs);
        }

        .admin-product-detail-page .product-info-value {
            color: var(--color-text-secondary);
            margin-bottom: var(--space-md);
        }

        .admin-product-detail-page .action-buttons {
            display: flex;
            gap: var(--space-sm);
            margin-top: var(--space-lg);
        }

        .admin-product-detail-page .btn--edit {
            background: var(--color-primary);
            color: var(--color-white);
        }

        .admin-product-detail-page .btn--delete {
            background: var(--color-error);
            color: var(--color-white);
        }

        .admin-product-detail-page .btn--back {
            background: var(--color-secondary);
            color: var(--color-white);
        }

        .admin-product-detail-page .image-modal {
            background: rgba(0, 0, 0, 0.8);
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: var(--z-modal);
            align-items: center;
            justify-content: center;
        }

        .admin-product-detail-page .image-modal.show {
            display: flex;
        }

        .admin-product-detail-page .image-modal-content {
            max-width: 90%;
            max-height: 90%;
            position: relative;
        }

        .admin-product-detail-page .image-modal img {
            width: 100%;
            height: auto;
            border-radius: var(--radius-md);
        }

        .admin-product-detail-page .image-modal-close {
            position: absolute;
            top: -40px;
            right: 0;
            background: var(--color-white);
            border: none;
            border-radius: 50%;
            width: 30px;
            height: 30px;
            cursor: pointer;
            font-size: var(--font-size-lg);
            color: var(--color-text);
        }
    `);

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

        // 페이지 로드 애니메이션
        animatePageLoad();
    }

    /**
     * 삭제 모달 표시
     */
    function showDeleteModal() {
        const modal = document.getElementById('deleteModal');
        if (modal) {
            openModal(modal);
        }
    }

    /**
     * 삭제 모달 닫기
     */
    function closeDeleteModal() {
        const modal = document.getElementById('deleteModal');
        if (modal) {
            closeModal(modal);
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
            openModal(modal);
        }
    }

    /**
     * 이미지 모달 닫기
     */
    function closeImageModal() {
        const modal = document.getElementById('imageModal');
        if (modal) {
            closeModal(modal);
        }
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
        const iconMap = {
            'image': '🖼️',
            'pdf': '📄',
            'doc': '📝',
            'docx': '📝',
            'xls': '📊',
            'xlsx': '📊',
            'txt': '📄',
            'zip': '📦',
            'rar': '📦'
        };
        
        return iconMap[fileType.toLowerCase()] || '📄';
    }

    /**
     * 파일 다운로드
     */
    function downloadFile(fileUrl, fileName) {
        const link = document.createElement('a');
        link.href = fileUrl;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        notify.success('파일 다운로드가 시작되었습니다.', '다운로드');
    }

    /**
     * 파일 미리보기
     */
    function previewFile(fileUrl, fileType) {
        if (fileType.startsWith('image/')) {
            openImageModal(fileUrl, '파일 미리보기');
        } else {
            window.open(fileUrl, '_blank');
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
        window.location.href = '/admin/products';
    }

    /**
     * 수정 페이지로 이동
     */
    function goToEdit(productId) {
        window.location.href = `/admin/product/${productId}/edit`;
    }

    /**
     * 페이지 로드 애니메이션
     */
    function animatePageLoad() {
        const container = document.querySelector('.product-detail-container');
        if (container) {
            setTimeout(() => {
                container.classList.add('loaded');
            }, 100);
        }
    }

    /**
     * 확인 대화상자
     */
    function confirmAction(message, callback) {
        if (confirm(message)) {
            callback();
        }
    }

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
    window.confirmAction = confirmAction;

    // 초기화 실행
    initializePage();
}); 