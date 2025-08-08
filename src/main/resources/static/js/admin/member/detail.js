/**
 * 관리자 회원 상세 페이지 JavaScript
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    // CSSManager와 NotificationManager 사용
    const cssManager = window.CSSManager;
    const notify = window.notify;

    // 관리자 회원 상세 전용 스타일 생성
    cssManager.addStyle('admin-member-detail', `
        .admin-member-detail-page .member-detail-container {
            opacity: 0;
            transform: translateY(20px);
            transition: all var(--transition-normal);
        }

        .admin-member-detail-page .member-detail-container.loaded {
            opacity: 1;
            transform: translateY(0);
        }

        .admin-member-detail-page .member-info-card {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-lg);
            margin-bottom: var(--space-lg);
        }

        .admin-member-detail-page .info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: var(--space-sm) 0;
            border-bottom: 1px solid var(--color-border);
        }

        .admin-member-detail-page .info-item:last-child {
            border-bottom: none;
        }

        .admin-member-detail-page .info-label {
            font-weight: 600;
            color: var(--color-text);
            min-width: 120px;
        }

        .admin-member-detail-page .info-value {
            color: var(--color-text-secondary);
            flex: 1;
            text-align: right;
        }

        .admin-member-detail-page .status-badge {
            padding: var(--space-xs) var(--space-sm);
            border-radius: var(--radius-sm);
            font-size: var(--font-size-xs);
            font-weight: 600;
            text-transform: uppercase;
            min-width: 80px;
            text-align: center;
        }

        .admin-member-detail-page .status-badge.active {
            background: var(--color-light-gray);
            color: var(--color-success);
        }

        .admin-member-detail-page .status-badge.inactive {
            background: var(--color-light-gray);
            color: var(--color-text-muted);
        }

        .admin-member-detail-page .status-badge.suspended {
            background: var(--color-light-gray);
            color: var(--color-warning);
        }

        .admin-member-detail-page .status-badge.deleted {
            background: var(--color-light-gray);
            color: var(--color-error);
        }

        .admin-member-detail-page .status-form {
            background: var(--color-light-gray);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-md);
            margin-bottom: var(--space-lg);
        }

        .admin-member-detail-page .status-preview {
            margin-top: var(--space-sm);
            font-size: var(--font-size-sm);
            color: var(--color-text-muted);
        }

        .admin-member-detail-page .status-preview.changed {
            color: var(--color-text);
        }

        .admin-member-detail-page .action-buttons {
            display: flex;
            gap: var(--space-sm);
            margin-top: var(--space-lg);
        }

        .admin-member-detail-page .btn--edit {
            background: var(--color-primary);
            color: var(--color-white);
        }

        .admin-member-detail-page .btn--list {
            background: var(--color-secondary);
            color: var(--color-white);
        }

        .admin-member-detail-page .btn--delete {
            background: var(--color-error);
            color: var(--color-white);
        }

        .admin-member-detail-page .member-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: var(--space-md);
            margin-bottom: var(--space-lg);
        }

        .admin-member-detail-page .stat-card {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-md);
            text-align: center;
        }

        .admin-member-detail-page .stat-value {
            font-size: var(--font-size-xl);
            font-weight: 700;
            color: var(--color-primary);
            margin-bottom: var(--space-xs);
        }

        .admin-member-detail-page .stat-label {
            font-size: var(--font-size-sm);
            color: var(--color-text-secondary);
        }
    `);

    /**
     * 초기화
     */
    function init() {
        initializeFormEvents();
        animatePageLoad();
        updateStatusBadgeColors();
    }

    /**
     * 폼 이벤트 초기화
     */
    function initializeFormEvents() {
        // 상태 변경 폼 처리
        const statusForm = document.querySelector('.status-form');
        if (statusForm) {
            statusForm.addEventListener('submit', function(e) {
                if (!confirmStatusChange()) {
                    e.preventDefault();
                    return false;
                }
            });
        }
        
        // 회원 탈퇴 폼 처리
        const deleteForm = document.querySelector('.delete-form');
        if (deleteForm) {
            deleteForm.addEventListener('submit', function(e) {
                if (!confirmDelete()) {
                    e.preventDefault();
                    return false;
                }
            });
        }
        
        // 상태 변경 셀렉트 이벤트
        const statusSelect = document.querySelector('.status-form select[name="status"]');
        if (statusSelect) {
            statusSelect.addEventListener('change', function() {
                updateStatusPreview(this.value);
            });
            
            // 페이지 로드 시 초기 상태 미리보기
            updateStatusPreview(statusSelect.value);
        }
    }

    /**
     * 상태 변경 확인
     */
    function confirmStatusChange() {
        const statusSelect = document.querySelector('.status-form select[name="status"]');
        const selectedOption = statusSelect.options[statusSelect.selectedIndex];
        const statusName = selectedOption.textContent;
        
        if (confirm(`회원 상태를 "${statusName}"로 변경하시겠습니까?`)) {
            notify.info('회원 상태를 변경하고 있습니다...', '상태 변경');
            return true;
        }
        return false;
    }

    /**
     * 회원 탈퇴 확인
     */
    function confirmDelete() {
        if (confirm('정말로 이 회원을 탈퇴(삭제)하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.')) {
            notify.warning('회원 탈퇴를 진행합니다...', '탈퇴 진행');
            return true;
        }
        return false;
    }

    /**
     * 상태 미리보기 업데이트
     */
    function updateStatusPreview(statusValue) {
        const statusSelect = document.querySelector('.status-form select[name="status"]');
        const selectedOption = statusSelect.options[statusSelect.selectedIndex];
        const statusName = selectedOption.textContent;
        
        // 상태 미리보기 요소 찾기 또는 생성
        let previewElement = document.querySelector('.status-preview');
        if (!previewElement) {
            previewElement = document.createElement('div');
            previewElement.className = 'status-preview';
            
            const statusForm = document.querySelector('.status-form');
            statusForm.appendChild(previewElement);
        }
        
        // 현재 상태와 비교
        const currentStatusElement = document.querySelector('.status-badge');
        const currentStatus = currentStatusElement ? currentStatusElement.textContent.trim() : '';
        
        if (statusName === currentStatus) {
            previewElement.textContent = '현재 상태와 동일합니다.';
            previewElement.classList.remove('changed');
        } else {
            previewElement.textContent = `상태를 "${statusName}"로 변경합니다.`;
            previewElement.classList.add('changed');
        }
    }

    /**
     * 회원 정보 수정 페이지로 이동
     */
    function goToEditPage(memberId) {
        if (confirm('회원 정보를 수정하시겠습니까?')) {
            notify.info('회원 정보 수정 페이지로 이동합니다...', '이동');
            window.location.href = `/admin/members/${memberId}/edit`;
        }
    }

    /**
     * 회원 목록으로 돌아가기
     */
    function goToList() {
        if (confirm('회원 목록으로 돌아가시겠습니까?')) {
            notify.info('회원 목록으로 이동합니다...', '이동');
            window.location.href = '/admin/members';
        }
    }

    /**
     * 상태 배지 색상 업데이트
     */
    function updateStatusBadgeColors() {
        const statusBadges = document.querySelectorAll('.status-badge');
        statusBadges.forEach(badge => {
            const status = badge.textContent.trim();
            badge.className = 'status-badge ' + status.toLowerCase();
        });
    }

    /**
     * 페이지 로드 애니메이션
     */
    function animatePageLoad() {
        const container = document.querySelector('.member-detail-container');
        if (container) {
            setTimeout(() => {
                container.classList.add('loaded');
            }, 100);
        }
    }

    // 전역 함수로 노출
    window.confirmStatusChange = confirmStatusChange;
    window.confirmDelete = confirmDelete;
    window.goToEditPage = goToEditPage;
    window.goToList = goToList;

    // 초기화 실행
    init();
}); 