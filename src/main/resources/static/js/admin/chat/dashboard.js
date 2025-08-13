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
            // 실제 API 호출로 대체 필요
            const stats = {
                waiting: 3,
                active: 2,
                completed: 15,
                totalMessages: 127
            };
            
            this.updateStatistics(stats);
        } catch (error) {
            console.error('통계 데이터 로드 실패:', error);
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
                this.updateWaitingSessions([]);
            }
        } catch (error) {
            console.error('대기 중인 세션 로드 실패:', error);
            this.updateWaitingSessions([]);
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
            // 실제 API 호출로 대체 필요
            const completedSessions = [
                {
                    sessionId: 'CHAT_003',
                    memberName: '박고객',
                    title: '환불 문의',
                    endTime: '2024-01-15 13:45:00',
                    messageCount: 8
                }
            ];
            
            this.updateCompletedSessions(completedSessions);
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
        
        countElement.textContent = sessions.length;
        
        if (sessions.length === 0) {
            emptyState.style.display = 'block';
            return;
        }
        
        emptyState.style.display = 'none';
        
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
        
        countElement.textContent = sessions.length;
        
        if (sessions.length === 0) {
            emptyState.style.display = 'block';
            return;
        }
        
        emptyState.style.display = 'none';
        
        const sessionsHtml = sessions.map(session => this.createSessionItem(session, 'active')).join('');
        container.innerHTML = sessionsHtml;
    }
    
    /**
     * 완료된 세션 업데이트
     */
    updateCompletedSessions(sessions) {
        const container = document.getElementById('recentCompletedSessions');
        const emptyState = document.getElementById('noCompletedSessions');
        
        if (sessions.length === 0) {
            emptyState.style.display = 'block';
            return;
        }
        
        emptyState.style.display = 'none';
        
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
        
        return `
            <div class="session-item" onclick="openChatSession('${session.sessionId}')">
                <div class="session-header">
                    <div class="session-title">${this.escapeHtml(session.title)}</div>
                    <div class="session-time">${time}</div>
                </div>
                <div class="session-info">
                    <span class="session-customer">${this.escapeHtml(session.memberName)}</span>
                    <span>메시지 ${session.messageCount}개</span>
                </div>
                <div class="session-actions">
                    <a href="/admin/chat/session/${session.sessionId}" class="btn-sm btn-primary">
                        <i class="fas fa-comments"></i>
                        상담 시작
                    </a>
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
        
        return `
            <div class="session-item">
                <div class="session-header">
                    <div class="session-title">${this.escapeHtml(session.title)}</div>
                    <div class="session-time">${time}</div>
                </div>
                <div class="session-info">
                    <span class="session-customer">${this.escapeHtml(session.memberName)}</span>
                    <span>메시지 ${session.messageCount}개</span>
                </div>
                <div class="session-actions">
                    <a href="/admin/chat/session/${session.sessionId}" class="btn-sm btn-outline">
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
        showToast(message, 'error');
    }
}

/**
 * 채팅 세션 열기
 */
function openChatSession(sessionId) {
    window.open(`/admin/chat/session/${sessionId}`, '_blank');
}

// 페이지 로드 시 대시보드 초기화
document.addEventListener('DOMContentLoaded', () => {
    new AdminChatDashboard();
});
