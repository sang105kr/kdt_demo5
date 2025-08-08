/**
 * 관리자 신고 상세 페이지 JavaScript
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    // CSSManager와 NotificationManager 사용
    const cssManager = window.CSSManager;
    const notify = window.notify;

    // 관리자 신고 상세 전용 스타일 생성
    cssManager.addStyle('admin-report-detail', `
        .admin-report-detail-page .report-detail-container {
            opacity: 0;
            transform: translateY(20px);
            transition: all var(--transition-normal);
        }

        .admin-report-detail-page .report-detail-container.loaded {
            opacity: 1;
            transform: translateY(0);
        }

        .admin-report-detail-page .report-status {
            padding: var(--space-xs) var(--space-sm);
            border-radius: var(--radius-sm);
            font-size: var(--font-size-xs);
            font-weight: 600;
            text-transform: uppercase;
        }

        .admin-report-detail-page .status-pending {
            background: var(--color-light-gray);
            color: var(--color-text);
        }

        .admin-report-detail-page .status-processing {
            background: var(--color-light-gray);
            color: var(--color-warning);
        }

        .admin-report-detail-page .status-resolved {
            background: var(--color-light-gray);
            color: var(--color-success);
        }

        .admin-report-detail-page .status-rejected {
            background: var(--color-light-gray);
            color: var(--color-error);
        }

        .admin-report-detail-page .report-info-card {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-lg);
            margin-bottom: var(--space-lg);
        }

        .admin-report-detail-page .report-info-label {
            font-weight: 600;
            color: var(--color-text);
            margin-bottom: var(--space-xs);
        }

        .admin-report-detail-page .report-info-value {
            color: var(--color-text-secondary);
            margin-bottom: var(--space-md);
        }

        .admin-report-detail-page .related-report-item {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-md);
            margin-bottom: var(--space-sm);
            transition: all var(--transition-fast);
            cursor: pointer;
        }

        .admin-report-detail-page .related-report-item:hover {
            background: var(--color-light-gray);
            transform: translateY(-2px);
            box-shadow: var(--shadow-sm);
        }

        .admin-report-detail-page .process-modal {
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

        .admin-report-detail-page .process-modal.show {
            display: flex;
        }

        .admin-report-detail-page .process-modal-content {
            background: var(--color-white);
            border-radius: var(--radius-lg);
            padding: var(--space-xl);
            max-width: 500px;
            width: 90%;
            max-height: 90vh;
            overflow-y: auto;
        }

        .admin-report-detail-page .process-form-group {
            margin-bottom: var(--space-md);
        }

        .admin-report-detail-page .process-form-label {
            display: block;
            font-weight: 600;
            color: var(--color-text);
            margin-bottom: var(--space-xs);
        }

        .admin-report-detail-page .process-form-input,
        .admin-report-detail-page .process-form-textarea,
        .admin-report-detail-page .process-form-select {
            width: 100%;
            padding: var(--space-sm);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            font-size: var(--font-size-base);
            transition: border-color var(--transition-fast);
        }

        .admin-report-detail-page .process-form-input:focus,
        .admin-report-detail-page .process-form-textarea:focus,
        .admin-report-detail-page .process-form-select:focus {
            outline: none;
            border-color: var(--color-primary);
            box-shadow: 0 0 0 2px rgba(34, 40, 49, 0.1);
        }

        .admin-report-detail-page .process-form-textarea {
            min-height: 120px;
            resize: vertical;
        }

        .admin-report-detail-page .process-form-actions {
            display: flex;
            gap: var(--space-sm);
            justify-content: flex-end;
            margin-top: var(--space-lg);
        }
    `);

    /**
     * 신고 상세 페이지 초기화
     */
    function initializeReportDetail() {
        // 모달 기능 초기화
        initializeModal();
        
        // 폼 처리 초기화
        initializeFormHandling();
        
        // 상태 배지 초기화
        initializeStatusBadges();
        
        // 관련 신고 상호작용 초기화
        initializeRelatedReports();
        
        // 페이지 로드 애니메이션
        animatePageLoad();
    }

    /**
     * 모달 기능 초기화
     */
    function initializeModal() {
        const modal = document.getElementById('processModal');
        const closeBtn = modal?.querySelector('.close');
        
        if (closeBtn) {
            closeBtn.addEventListener('click', closeProcessModal);
        }
        
        if (modal) {
            // 모달 외부 클릭 시 닫기
            modal.addEventListener('click', function(event) {
                if (event.target === modal) {
                    closeProcessModal();
                }
            });
            
            // ESC 키로 모달 닫기
            document.addEventListener('keydown', function(event) {
                if (event.key === 'Escape' && modal.style.display === 'flex') {
                    closeProcessModal();
                }
            });
        }
    }

    /**
     * 폼 처리 초기화
     */
    function initializeFormHandling() {
        const processForm = document.getElementById('processForm');
        
        if (processForm) {
            processForm.addEventListener('submit', handleProcessSubmit);
        }
    }

    /**
     * 상태 배지 초기화
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
     * 관련 신고 상호작용 초기화
     */
    function initializeRelatedReports() {
        const relatedReportItems = document.querySelectorAll('.related-report-item');
        
        relatedReportItems.forEach(item => {
            item.addEventListener('click', function(event) {
                // "상세보기" 버튼 클릭 시 네비게이션 방지
                if (event.target.tagName === 'A' || event.target.closest('a')) {
                    return;
                }
                
                // 관련 신고 상세 페이지로 이동
                const reportId = this.querySelector('.related-report-id')?.textContent.replace('#', '');
                if (reportId) {
                    window.location.href = `/admin/reports/${reportId}`;
                }
            });
        });
    }

    /**
     * 처리 모달 열기
     */
    function openProcessModal() {
        const modal = document.getElementById('processModal');
        if (modal) {
            openModal(modal);
        }
    }

    /**
     * 처리 모달 닫기
     */
    function closeProcessModal() {
        const modal = document.getElementById('processModal');
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
     * 처리 폼 제출 처리
     */
    async function handleProcessSubmit(event) {
        event.preventDefault();
        
        const form = event.target;
        const formData = new FormData(form);
        
        if (!validateProcessForm(formData)) {
            return;
        }
        
        try {
            const response = await ajax.post('/api/admin/reports/process', {
                reportId: formData.get('reportId'),
                status: formData.get('status'),
                comment: formData.get('comment')
            });
            
            if (response.success) {
                notify.success('신고가 성공적으로 처리되었습니다.', '처리 완료');
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                notify.error(response.message || '신고 처리에 실패했습니다.', '처리 오류');
            }
        } catch (error) {
            console.error('Process report error:', error);
            notify.error('신고 처리 중 오류가 발생했습니다.', '오류');
        }
    }

    /**
     * 처리 폼 유효성 검사
     */
    function validateProcessForm(formData) {
        const status = formData.get('status');
        const comment = formData.get('comment');
        
        if (!status) {
            notify.error('상태를 선택해주세요.', '입력 오류');
            return false;
        }
        
        if (!comment || comment.trim().length < 10) {
            notify.error('처리 내용을 10자 이상 입력해주세요.', '입력 오류');
            return false;
        }
        
        return true;
    }

    /**
     * 날짜 포맷팅
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
     * 텍스트 자르기
     */
    function truncateText(text, maxLength = 100) {
        if (!text) return '';
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    }

    /**
     * HTML 이스케이프
     */
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * 페이지 로드 애니메이션
     */
    function animatePageLoad() {
        const container = document.querySelector('.report-detail-container');
        if (container) {
            setTimeout(() => {
                container.classList.add('loaded');
            }, 100);
        }
    }

    // 전역 함수로 노출
    window.openProcessModal = openProcessModal;
    window.closeProcessModal = closeProcessModal;
    window.formatDate = formatDate;
    window.truncateText = truncateText;
    window.escapeHtml = escapeHtml;

    // 초기화 실행
    initializeReportDetail();
}); 