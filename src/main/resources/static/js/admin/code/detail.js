/**
 * 관리자 코드 상세 페이지 JavaScript
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    // CSSManager와 NotificationManager 사용
    const cssManager = window.CSSManager;
    const notify = window.notify;

    // 관리자 코드 상세 전용 스타일 생성
    cssManager.addStyle('admin-code-detail', `
        .admin-code-detail-page .code-detail-container {
            opacity: 0;
            transform: translateY(20px);
            transition: all var(--transition-normal);
        }

        .admin-code-detail-page .code-detail-container.loaded {
            opacity: 1;
            transform: translateY(0);
        }

        .admin-code-detail-page .code-info-card {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-lg);
            margin-bottom: var(--space-lg);
        }

        .admin-code-detail-page .info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: var(--space-sm) 0;
            border-bottom: 1px solid var(--color-border);
        }

        .admin-code-detail-page .info-item:last-child {
            border-bottom: none;
        }

        .admin-code-detail-page .info-label {
            font-weight: 600;
            color: var(--color-text);
            min-width: 120px;
        }

        .admin-code-detail-page .info-value {
            color: var(--color-text-secondary);
            flex: 1;
            text-align: right;
        }

        .admin-code-detail-page .related-codes-section {
            margin-top: var(--space-xl);
        }

        .admin-code-detail-page .related-code-item {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-md);
            margin-bottom: var(--space-sm);
            display: flex;
            justify-content: space-between;
            align-items: center;
            transition: all var(--transition-fast);
        }

        .admin-code-detail-page .related-code-item:hover {
            background: var(--color-light-gray);
            transform: translateY(-2px);
            box-shadow: var(--shadow-sm);
        }

        .admin-code-detail-page .code-value {
            font-weight: 600;
            color: var(--color-primary);
            min-width: 80px;
        }

        .admin-code-detail-page .code-name {
            flex: 1;
            margin: 0 var(--space-md);
            color: var(--color-text);
        }

        .admin-code-detail-page .code-status {
            padding: var(--space-xs) var(--space-sm);
            border-radius: var(--radius-sm);
            font-size: var(--font-size-xs);
            font-weight: 600;
            text-transform: uppercase;
            min-width: 60px;
            text-align: center;
        }

        .admin-code-detail-page .code-status.active {
            background: var(--color-light-gray);
            color: var(--color-success);
        }

        .admin-code-detail-page .code-status.inactive {
            background: var(--color-light-gray);
            color: var(--color-text-muted);
        }

        .admin-code-detail-page .code-actions {
            display: flex;
            gap: var(--space-xs);
            margin-left: var(--space-md);
        }

        .admin-code-detail-page .btn--detail {
            background: var(--color-primary);
            color: var(--color-white);
        }

        .admin-code-detail-page .btn--edit {
            background: var(--color-warning);
            color: var(--color-white);
        }

        .admin-code-detail-page .btn--delete {
            background: var(--color-error);
            color: var(--color-white);
        }

        .admin-code-detail-page .action-buttons {
            display: flex;
            gap: var(--space-sm);
            margin-top: var(--space-lg);
        }

        .admin-code-detail-page .btn--back {
            background: var(--color-secondary);
            color: var(--color-white);
        }

        .admin-code-detail-page .btn--list {
            background: var(--color-primary);
            color: var(--color-white);
        }

        .admin-code-detail-page .btn--refresh {
            background: var(--color-info);
            color: var(--color-white);
        }
    `);

    /**
     * 초기화
     */
    function init() {
        loadRelatedCodes();
        animatePageLoad();
    }

    /**
     * 관련 코드 목록 로드
     */
    async function loadRelatedCodes() {
        const gcodeElement = document.querySelector('.info-item span');
        if (!gcodeElement) return;

        const gcode = gcodeElement.textContent.trim();
        const relatedCodesList = document.getElementById('relatedCodesList');
        
        if (!relatedCodesList) return;
        
        try {
            const data = await ajax.get(`/api/admin/codes/gcode/${gcode}`);
            
            if (data.code === '00') {
                const codes = data.data;
                if (codes && codes.length > 0) {
                    const html = codes.map(code => `
                        <div class="related-code-item">
                            <span class="code-value">${code.code}</span>
                            <span class="code-name">${code.decode}</span>
                            <span class="code-status ${code.useYn === 'Y' ? 'active' : 'inactive'}">
                                ${code.useYn === 'Y' ? '사용' : '미사용'}
                            </span>
                            <div class="code-actions">
                                <button type="button" class="btn btn--detail" onclick="viewDetail(${code.codeId})">상세</button>
                                <button type="button" class="btn btn--edit" onclick="editCode(${code.codeId})">수정</button>
                            </div>
                        </div>
                    `).join('');
                    relatedCodesList.innerHTML = html;
                } else {
                    relatedCodesList.innerHTML = '<p>같은 그룹의 다른 코드가 없습니다.</p>';
                }
            } else {
                relatedCodesList.innerHTML = '<p>관련 코드 목록을 불러오는데 실패했습니다.</p>';
                notify.error('관련 코드 목록을 불러오는데 실패했습니다.', '로드 오류');
            }
        } catch (error) {
            console.error('Error:', error);
            relatedCodesList.innerHTML = '<p>네트워크 오류가 발생했습니다.</p>';
            notify.error('네트워크 오류가 발생했습니다.', '오류');
        }
    }

    /**
     * 코드 상세보기
     */
    function viewDetail(codeId) {
        window.location.href = `/admin/codes/${codeId}`;
    }

    /**
     * 코드 수정
     */
    function editCode(codeId) {
        window.location.href = `/admin/codes/${codeId}/edit`;
    }

    /**
     * 코드 삭제
     */
    async function deleteCode(codeId) {
        if (!confirm('정말로 이 코드를 삭제하시겠습니까?\n\n삭제된 코드는 복구할 수 없습니다.')) {
            return;
        }
        
        try {
            const data = await ajax.delete(`/api/admin/codes/${codeId}`);
            
            if (data.code === '00') {
                notify.success('코드가 성공적으로 삭제되었습니다.', '삭제 완료');
                setTimeout(() => {
                    window.location.href = '/admin/codes';
                }, 1000);
            } else {
                notify.error(data.message || '코드 삭제 중 오류가 발생했습니다.', '삭제 오류');
            }
        } catch (error) {
            console.error('Error:', error);
            notify.error('네트워크 오류가 발생했습니다.', '오류');
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
        window.location.href = '/admin/codes';
    }

    /**
     * 페이지 로드 애니메이션
     */
    function animatePageLoad() {
        const container = document.querySelector('.code-detail-container');
        if (container) {
            setTimeout(() => {
                container.classList.add('loaded');
            }, 100);
        }
    }

    // 전역 함수로 노출
    window.viewDetail = viewDetail;
    window.editCode = editCode;
    window.deleteCode = deleteCode;
    window.refreshPage = refreshPage;
    window.goBack = goBack;
    window.goToList = goToList;

    // 초기화 실행
    init();
}); 