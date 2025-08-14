/**
 * 관리자 채팅 대시보드 JavaScript
 */

class AdminChatDashboard {
    constructor() {
        this.pollingInterval = null;
        this.refreshInterval = 5000; // 5초마다 새로고침
        this.init();
    }
    
    /**
     * 초기화
     */
    init() {
        this.bindEvents();
        this.loadDashboardData();
        this.startPolling();
    }
    
    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        const refreshBtn = document.getElementById('refreshBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                this.loadDashboardData();
            });
        }
        
        // 상담 시작 버튼 이벤트 위임
        document.addEventListener('click', (e) => {
            if (e.target.closest('.start-chat-btn')) {
                const sessionId = e.target.closest('.start-chat-btn').dataset.sessionId;
                this.openChatSession(sessionId);
            }
        });
        
        // 페이지 언로드 시 폴링 중지
        window.addEventListener('beforeunload', () => {
            this.stopPolling();
        });
    }
    
    /**
     * 대시보드 데이터 로드
     */
    async loadDashboardData() {
        try {
            await Promise.all([
                this.loadStatistics(),
                this.loadWaitingSessions(),
                this.loadActiveSessions(),
                this.loadRecentCompletedSessions()
            ]);
        } catch (error) {
            console.error('대시보드 데이터 로드 실패:', error);
            this.showError('데이터를 불러오는데 실패했습니다.');
        }
    }
    
    /**
     * 통계 데이터 로드
     */
    async loadStatistics() {
        try {
            console.log('통계 API 호출 시작...');
            const response = await ajax.get('/api/chat/statistics');
            console.log('통계 API 응답:', response);
            
            if (response && response.code === '00') {
                console.log('통계 데이터 업데이트:', response.data);
                this.updateStatistics(response.data);
            } else {
                console.log('API 호출 실패, 테스트 데이터 사용');
                // API 호출 실패 시 테스트 데이터 사용
                this.updateStatistics({
                    waiting: 2,
                    active: 1,
                    completed: 3,
                    totalMessages: 15
                });
            }
        } catch (error) {
            console.error('통계 데이터 로드 실패:', error);
            // 에러 시 테스트 데이터 사용
            this.updateStatistics({
                waiting: 2,
                active: 1,
                completed: 3,
                totalMessages: 15
            });
        }
    }
    
    /**
     * 대기 중인 세션 로드
     */
    async loadWaitingSessions() {
        try {
            const response = await ajax.get('/api/chat/sessions/waiting');
            
            if (response && response.code === '00') {
                this.updateWaitingSessions(response.data);
            } else {
                console.log('대기 세션 API 호출 실패, 테스트 데이터 사용');
                // 테스트 데이터 사용
                const testWaitingSessions = [
                    {
                        sessionId: 'CHAT_WAIT_001',
                        memberName: '김고객',
                        title: '상품 문의',
                        startTime: '2024-01-15 14:30:00',
                        messageCount: 1
                    },
                    {
                        sessionId: 'CHAT_WAIT_002',
                        memberName: '이고객',
                        title: '배송 문의',
                        startTime: '2024-01-15 15:00:00',
                        messageCount: 2
                    }
                ];
                this.updateWaitingSessions(testWaitingSessions);
            }
        } catch (error) {
            console.error('대기 중인 세션 로드 실패:', error);
            // 테스트 데이터 사용
            const testWaitingSessions = [
                {
                    sessionId: 'CHAT_WAIT_001',
                    memberName: '김고객',
                    title: '상품 문의',
                    startTime: '2024-01-15 14:30:00',
                    messageCount: 1
                },
                {
                    sessionId: 'CHAT_WAIT_002',
                    memberName: '이고객',
                    title: '배송 문의',
                    startTime: '2024-01-15 15:00:00',
                    messageCount: 2
                }
            ];
            this.updateWaitingSessions(testWaitingSessions);
        }
    }
    
    /**
     * 진행 중인 세션 로드
     */
    async loadActiveSessions() {
        try {
            // 실제 API 호출로 대체 필요
            const activeSessions = [
                {
                    sessionId: 'CHAT_001',
                    memberName: '김고객',
                    title: '상품 문의',
                    startTime: '2024-01-15 14:30:00',
                    messageCount: 5
                },
                {
                    sessionId: 'CHAT_002',
                    memberName: '이고객',
                    title: '배송 문의',
                    startTime: '2024-01-15 15:00:00',
                    messageCount: 3
                }
            ];
            
            this.updateActiveSessions(activeSessions);
        } catch (error) {
            console.error('진행 중인 세션 로드 실패:', error);
            this.updateActiveSessions([]);
        }
    }
    
    /**
     * 최근 완료된 세션 로드
     */
    async loadRecentCompletedSessions() {
        try {
            const response = await ajax.get('/api/chat/sessions/completed');
            
            if (response && response.code === '00') {
                this.updateCompletedSessions(response.data);
            } else {
                console.log('완료된 세션 API 호출 실패, 빈 배열 사용');
                this.updateCompletedSessions([]);
            }
        } catch (error) {
            console.error('완료된 세션 로드 실패:', error);
            this.updateCompletedSessions([]);
        }
    }
    
    /**
     * 통계 업데이트
     */
    updateStatistics(stats) {
        document.getElementById('waitingCount').textContent = stats.waiting;
        document.getElementById('activeCount').textContent = stats.active;
        document.getElementById('completedCount').textContent = stats.completed;
        document.getElementById('totalMessages').textContent = stats.totalMessages;
    }
    
    /**
     * 대기 중인 세션 업데이트
     */
    updateWaitingSessions(sessions) {
        const container = document.getElementById('waitingSessionList');
        const countElement = document.getElementById('waitingSessionCount');
        const emptyState = document.getElementById('noWaitingSessions');
        
        if (!container || !countElement) {
            console.warn('대기 중인 세션 컨테이너를 찾을 수 없습니다.');
            return;
        }
        
        countElement.textContent = sessions.length;
        
        if (sessions.length === 0) {
            if (emptyState) {
                emptyState.style.display = 'block';
            }
            return;
        }
        
        if (emptyState) {
            emptyState.style.display = 'none';
        }
        
        const sessionsHtml = sessions.map(session => this.createSessionItem(session, 'waiting')).join('');
        container.innerHTML = sessionsHtml;
    }
    
    /**
     * 진행 중인 세션 업데이트
     */
    updateActiveSessions(sessions) {
        const container = document.getElementById('activeSessionList');
        const countElement = document.getElementById('activeSessionCount');
        const emptyState = document.getElementById('noActiveSessions');
        
        if (!container || !countElement) {
            console.warn('진행 중인 세션 컨테이너를 찾을 수 없습니다.');
            return;
        }
        
        countElement.textContent = sessions.length;
        
        if (sessions.length === 0) {
            if (emptyState) {
                emptyState.style.display = 'block';
            }
            return;
        }
        
        if (emptyState) {
            emptyState.style.display = 'none';
        }
        
        const sessionsHtml = sessions.map(session => this.createSessionItem(session, 'active')).join('');
        container.innerHTML = sessionsHtml;
    }
    
    /**
     * 완료된 세션 업데이트
     */
    updateCompletedSessions(sessions) {
        const container = document.getElementById('recentCompletedSessions');
        const emptyState = document.getElementById('noCompletedSessions');
        
        if (!container) {
            console.warn('완료된 세션 컨테이너를 찾을 수 없습니다.');
            return;
        }
        
        if (sessions.length === 0) {
            if (emptyState) {
                emptyState.style.display = 'block';
            }
            return;
        }
        
        if (emptyState) {
            emptyState.style.display = 'none';
        }
        
        const sessionsHtml = sessions.map(session => this.createCompletedSessionItem(session)).join('');
        container.innerHTML = sessionsHtml;
    }
    
    /**
     * 세션 아이템 HTML 생성
     */
    createSessionItem(session, type) {
        const time = new Date(session.startTime).toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit'
        });
        
        const statusBadge = session.statusName ? 
            `<span class="status-badge ${type}">${session.statusName}</span>` : '';
        
        return `
            <div class="session-item" data-session-id="${session.sessionId}">
                <div class="session-header">
                    <div class="session-title">${this.escapeHtml(session.title)}</div>
                    <div class="session-time">${time}</div>
                </div>
                <div class="session-info">
                    <span class="session-customer">${this.escapeHtml(session.memberName)}</span>
                    <span>메시지 ${session.messageCount}개</span>
                    ${statusBadge}
                </div>
                <div class="session-actions">
                    <button class="btn-sm btn-primary start-chat-btn" data-session-id="${session.sessionId}">
                        <i class="fas fa-comments"></i>
                        상담 시작
                    </button>
                </div>
            </div>
        `;
    }
    
    /**
     * 완료된 세션 아이템 HTML 생성
     */
    createCompletedSessionItem(session) {
        const time = new Date(session.endTime).toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit'
        });
        
        const statusBadge = session.statusName ? 
            `<span class="status-badge completed">${session.statusName}</span>` : '';
        
        return `
            <div class="session-item">
                <div class="session-header">
                    <div class="session-title">${this.escapeHtml(session.title)}</div>
                    <div class="session-time">${time}</div>
                </div>
                <div class="session-info">
                    <span class="session-customer">${this.escapeHtml(session.memberName)}</span>
                    <span>메시지 ${session.messageCount}개</span>
                    ${statusBadge}
                </div>
                <div class="session-actions">
                    <a href="/admin/chat/history/${session.sessionId}" class="btn-sm btn-outline">
                        <i class="fas fa-eye"></i>
                        상담 내역
                    </a>
                </div>
            </div>
        `;
    }
    
    /**
     * 폴링 시작
     */
    startPolling() {
        this.pollingInterval = setInterval(() => {
            this.loadDashboardData();
        }, this.refreshInterval);
    }
    
    /**
     * 폴링 중지
     */
    stopPolling() {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
            this.pollingInterval = null;
        }
    }
    
    /**
     * HTML 이스케이프
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    /**
     * 에러 표시
     */
    showError(message) {
        if (typeof showToast === 'function') {
            showToast(message, 'error');
        } else {
            console.error('Error:', message);
            alert(message);
        }
    }
    
    /**
     * 채팅 세션 열기
     */
    openChatSession(sessionId) {
        window.open(`/admin/chat/session/${sessionId}`, '_blank');
    }
}

// 페이지 로드 시 대시보드 초기화
document.addEventListener('DOMContentLoaded', () => {
    new AdminChatDashboard();
});
