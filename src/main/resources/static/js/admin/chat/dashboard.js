/**
 * 관리자 채팅 대시보드 JavaScript
 */

class AdminChatDashboard {
    constructor() {
        this.pollingInterval = null;
        this.refreshInterval = 5000; // 5초마다 새로고침 (백업용)
        this.activeStatusId = null; // 진행중 상태 ID
        this.completedStatusId = null; // 완료 상태 ID
        this.stompClient = null; // WebSocket 클라이언트
        this.isConnected = false; // WebSocket 연결 상태
        
        // 카테고리 이름 매핑 (서버에서 동적으로 로드)
        this.categoryNames = {};
        this.categoryData = {}; // 카테고리 전체 데이터 (codeId 포함)
        
        // 종료 사유 코드 매핑
        this.exitReasons = {};
        
        this.init();
    }
    
    /**
     * 초기화
     */
    async init() {
        await this.loadStatusIds();
        await Promise.all([
            this.loadCategoryNames(), // 카테고리 목록 로드
            this.loadExitReasons()    // 종료 사유 목록 로드
        ]);
        this.bindEvents();
        this.loadDashboardData();
        this.connectWebSocket(); // WebSocket 연결
        this.startPolling(); // 백업용 폴링
    }
    
    /**
     * WebSocket 연결
     */
    connectWebSocket() {
        try {
            console.log('관리자 대시보드 WebSocket 연결 시작...');
            
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);
            
            this.stompClient.connect({}, (frame) => {
                console.log('관리자 대시보드 WebSocket 연결 성공:', frame);
                this.isConnected = true;
                
                // 채팅 세션 상태 변경 구독
                this.stompClient.subscribe('/topic/chat/sessions', (message) => {
                    console.log('채팅 세션 상태 변경 수신:', message);
                    this.handleSessionUpdate(JSON.parse(message.body));
                });
                
                // 새로운 채팅 세션 생성 구독
                this.stompClient.subscribe('/topic/chat/new-session', (message) => {
                    console.log('새로운 채팅 세션 생성 수신:', message);
                    this.handleNewSession(JSON.parse(message.body));
                });
                
                // 채팅 메시지 구독 (통계 업데이트용)
                this.stompClient.subscribe('/topic/chat/messages', (message) => {
                    console.log('채팅 메시지 수신:', message);
                    this.handleMessageUpdate(JSON.parse(message.body));
                });
                
                // 일시 이탈/복귀 상태 변경 구독
                this.stompClient.subscribe('/topic/chat/presence', (message) => {
                    console.log('일시 이탈/복귀 상태 변경 수신:', message);
                    this.handlePresenceUpdate(JSON.parse(message.body));
                });
                
            }, (error) => {
                console.error('관리자 대시보드 WebSocket 연결 실패:', error);
                this.isConnected = false;
            });
            
        } catch (error) {
            console.error('WebSocket 연결 오류:', error);
            this.isConnected = false;
        }
    }
    
    /**
     * WebSocket 연결 해제
     */
    disconnectWebSocket() {
        if (this.stompClient && this.isConnected) {
            this.stompClient.disconnect();
            this.isConnected = false;
            console.log('관리자 대시보드 WebSocket 연결 해제');
        }
    }
    
    /**
     * 채팅 세션 상태 변경 처리
     */
    async handleSessionUpdate(sessionData) {
        console.log('세션 상태 변경 처리:', sessionData);
        
        const { sessionId, statusId, statusName, session, type } = sessionData;
        
        // 세션이 종료된 경우(SESSION_ENDED 이벤트 포함)
        if (type === 'SESSION_ENDED' || statusId === this.completedStatusId) {
            // 진행 중 목록에서 제거
            this.removeSessionFromList(sessionId, 'active');
            // 통계 업데이트
            this.updateSessionCount('active', -1);
            this.updateSessionCount('completed', 1);
            // 최근 완료 목록 갱신
            await this.loadRecentCompletedSessions();
            // 통계 새로고침
            this.loadStatistics();
            return;
        }

        // 세션이 대기 중에서 진행 중으로 변경된 경우
        if (statusId === this.activeStatusId) {
            // 대기 중인 세션 목록에서 해당 세션 제거
            this.removeSessionFromList(sessionId, 'waiting');
            
            // 진행 중인 세션 목록에 추가
            await this.addSessionToList(session, 'active');
            
            // 통계 업데이트
            this.updateSessionCount('waiting', -1);
            this.updateSessionCount('active', 1);
        }
        
        // 통계 새로고침
        this.loadStatistics();
    }
    
    /**
     * 새로운 채팅 세션 생성 처리
     */
    async handleNewSession(sessionData) {
        console.log('새로운 세션 생성 처리:', sessionData);
        
        // 대기 중인 세션 목록에 즉시 추가
        await this.addNewWaitingSession(sessionData);
        
        // 통계 업데이트
        this.updateWaitingCount(1);
    }
    
    /**
     * 채팅 메시지 업데이트 처리
     */
    handleMessageUpdate(messageData) {
        console.log('메시지 업데이트 처리:', messageData);
        
        // 통계 새로고침 (총 메시지 수 업데이트)
        this.loadStatistics();
    }
    
    /**
     * 일시 이탈/복귀 상태 변경 처리
     */
    async handlePresenceUpdate(presenceData) {
        console.log('일시 이탈/복귀 상태 변경 처리:', presenceData);
        
        const { sessionId, side, state, reason } = presenceData;
        
        // 해당 세션의 상태 표시 업데이트
        this.updateSessionPresenceStatus(sessionId, side, state, reason);
        
        // 진행 중인 세션 목록 새로고침 (상태 변경 반영)
        await this.loadActiveSessions();
    }
    
    /**
     * 세션의 일시 이탈 상태 업데이트
     */
    updateSessionPresenceStatus(sessionId, side, state, reason) {
        console.log(`세션 ${sessionId}의 일시 이탈 상태 업데이트: ${side} - ${state} (${reason})`);
        
        // 진행 중인 세션 목록에서 해당 세션 찾기
        const sessionElement = document.querySelector(`[data-session-id="${sessionId}"]`);
        if (sessionElement) {
            // 기존 presence 배지 제거
            const existingBadge = sessionElement.querySelector('.presence-badge');
            if (existingBadge) {
                existingBadge.remove();
            }
            
            // 새로운 상태에 따른 배지 추가
            if (state === 'INACTIVE') {
                const presenceBadge = document.createElement('span');
                presenceBadge.className = 'presence-badge inactive';
                presenceBadge.textContent = side === 'MEMBER' ? '고객 이탈' : '상담원 이탈';
                presenceBadge.title = `이탈 사유: ${reason}`;
                
                const sessionInfo = sessionElement.querySelector('.session-info');
                if (sessionInfo) {
                    sessionInfo.appendChild(presenceBadge);
                }
            }
        }
    }
    
    /**
     * 새로운 대기 세션을 목록에 추가
     */
    async addNewWaitingSession(sessionData) {
        const waitingList = document.getElementById('waitingSessionList');
        if (!waitingList) return;
        
        // 빈 상태 제거
        const emptyState = waitingList.querySelector('.empty-state');
        if (emptyState) {
            emptyState.remove();
        }
        
        // 새로운 세션 요소 생성
        const sessionElement = await this.createSessionElement(sessionData, 'waiting');
        waitingList.appendChild(sessionElement);
        
        // 카운트 업데이트
        this.updateSessionCount('waiting', 1);
    }
    
    /**
     * 대기 세션 카운트 업데이트
     */
    updateWaitingCount(increment) {
        const countElement = document.getElementById('waitingCount');
        if (countElement) {
            const currentCount = parseInt(countElement.textContent) || 0;
            countElement.textContent = currentCount + increment;
        }
    }
    
    /**
     * 세션 목록에서 특정 세션 제거
     */
    removeSessionFromList(sessionId, type) {
        const listElement = document.getElementById(type === 'waiting' ? 'waitingSessionList' : 'activeSessionList');
        if (!listElement) return;
        
        const sessionElement = listElement.querySelector(`[data-session-id="${sessionId}"]`);
        if (sessionElement) {
            sessionElement.remove();
            
            // 목록이 비어있으면 빈 상태 표시
            if (listElement.children.length === 0) {
                const emptyState = document.createElement('div');
                emptyState.className = 'empty-state';
                emptyState.id = `no${type.charAt(0).toUpperCase() + type.slice(1)}Sessions`;
                
                if (type === 'waiting') {
                    emptyState.innerHTML = `
                        <div class="empty-icon">
                            <i class="fas fa-clock"></i>
                        </div>
                        <h3>대기 중인 상담이 없습니다</h3>
                        <p>새로운 상담 요청을 기다리고 있습니다.</p>
                    `;
                } else {
                    emptyState.innerHTML = `
                        <div class="empty-icon">
                            <i class="fas fa-comments"></i>
                        </div>
                        <h3>진행 중인 상담이 없습니다</h3>
                        <p>현재 진행 중인 상담이 없습니다.</p>
                    `;
                }
                
                listElement.appendChild(emptyState);
            }
        }
    }
    
    /**
     * 세션 목록에 세션 추가
     */
    async addSessionToList(session, type) {
        const listElement = document.getElementById(type === 'waiting' ? 'waitingSessionList' : 'activeSessionList');
        if (!listElement) return;
        
        // 빈 상태 제거
        const emptyState = listElement.querySelector('.empty-state');
        if (emptyState) {
            emptyState.remove();
        }
        
        // 새로운 세션 요소 생성
        const sessionElement = await this.createSessionElement(session, type);
        listElement.appendChild(sessionElement);
    }
    
    /**
     * 세션 카운트 업데이트
     */
    updateSessionCount(type, increment) {
        const countElement = document.getElementById(type === 'waiting' ? 'waitingCount' : 
                                                   type === 'active' ? 'activeCount' : 'completedCount');
        if (countElement) {
            const currentCount = parseInt(countElement.textContent) || 0;
            countElement.textContent = Math.max(0, currentCount + increment);
        }
        
        // 세션 목록 헤더의 카운트도 업데이트
        const sessionCountElement = document.getElementById(type === 'waiting' ? 'waitingSessionCount' : 'activeSessionCount');
        if (sessionCountElement) {
            const currentSessionCount = parseInt(sessionCountElement.textContent) || 0;
            sessionCountElement.textContent = Math.max(0, currentSessionCount + increment);
        }
    }
    

    
    /**
     * 세션 요소 생성
     */
    async createSessionElement(sessionData, type) {
        const sessionElement = document.createElement('div');
        sessionElement.className = 'session-item';
        sessionElement.dataset.sessionId = sessionData.sessionId;
        
        const startTime = new Date(sessionData.startTime).toLocaleString('ko-KR');
        
        // 메시지 개수 동적 조회
        let messageCount = 0;
        try {
            const response = await fetch(`/api/admin/chat/sessions/${sessionData.sessionId}/message-count`);
            if (response.ok) {
                const result = await response.json();
                if (result.code === '00') {
                    messageCount = result.data.messageCount || 0;
                }
            }
        } catch (error) {
            console.error('메시지 개수 조회 실패:', error);
            messageCount = 0;
        }
        
        sessionElement.innerHTML = `
            <div class="session-info">
                <div class="session-header">
                    <h3 class="session-title">${sessionData.title || '상담 요청'}</h3>
                    <span class="session-time">${startTime}</span>
                </div>
                <div class="session-details">
                    <p class="member-name">${sessionData.memberName || '고객'}</p>
                    <p class="message-count">메시지: ${messageCount}개</p>
                </div>
            </div>
            <div class="session-actions">
                ${type === 'waiting' ? 
                    `<button class="start-chat-btn" data-session-id="${sessionData.sessionId}">
                        <i class="fas fa-comments"></i>
                        상담 시작
                    </button>` : 
                    `<button class="view-chat-btn" data-session-id="${sessionData.sessionId}">
                        <i class="fas fa-eye"></i>
                        상담 보기
                    </button>`
                }
            </div>
        `;
        
        return sessionElement;
    }
    
    /**
     * 상태 ID 로드
     */
    async loadStatusIds() {
        try {
            // 진행중 상태 ID 조회
            const response = await fetch('/api/admin/chat/status/active');
            if (response.ok) {
                const result = await response.json();
                if (result.code === '00') {
                    this.activeStatusId = result.data.statusId;
                    console.log('진행중 상태 ID 로드 완료:', this.activeStatusId);
                }
            }
            
            // 완료 상태 ID 조회
            const completedResponse = await fetch('/api/admin/chat/status/completed');
            if (completedResponse.ok) {
                const completedResult = await completedResponse.json();
                if (completedResult.code === '00') {
                    this.completedStatusId = completedResult.data.statusId;
                    console.log('완료 상태 ID 로드 완료:', this.completedStatusId);
                }
            }
        } catch (error) {
            console.error('상태 ID 로드 실패:', error);
            // 기본값 사용
            this.activeStatusId = 2;
            this.completedStatusId = 3;
        }
    }

    /**
     * 카테고리 목록 조회 (서버에서 동적으로 로드)
     */
    async loadCategoryNames() {
        try {
            const response = await fetch('/api/faq/categories');
            if (response.ok) {
                const result = await response.json();
                if (result.code === '00' && result.data) {
                    // 카테고리 코드를 키로, decode를 값으로 하는 객체 생성
                    this.categoryNames = {};
                    this.categoryData = {}; // codeId도 저장하기 위한 객체
                    result.data.forEach(category => {
                        this.categoryNames[category.code] = category.decode;
                        this.categoryData[category.code] = category; // 전체 데이터 저장
                    });
                    console.log('카테고리 목록 로드 완료:', this.categoryNames);
                }
            }
        } catch (error) {
            console.error('카테고리 목록 조회 실패:', error);
            // 실패 시 기본값 설정
            this.categoryNames = {
                'GENERAL': '일반 문의',
                'ORDER': '주문/결제',
                'DELIVERY': '배송',
                'RETURN': '반품/교환',
                'ACCOUNT': '회원/계정',
                'TECHNICAL': '기술지원'
            };
        }
    }
    
    /**
     * 종료 사유 목록 조회
     */
    async loadExitReasons() {
        try {
            const response = await ajax.get('/api/admin/chat/exit-reasons');
            if (response && response.code === '00' && response.data) {
                this.exitReasons = {};
                response.data.forEach(reason => {
                    this.exitReasons[reason.codeId] = reason.decode;
                });
                console.log('종료 사유 목록 로드 완료:', this.exitReasons);
            }
        } catch (error) {
            console.error('종료 사유 목록 조회 실패:', error);
        }
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
            
            // 상담 보기 버튼 이벤트 위임 추가
            if (e.target.closest('.view-chat-btn')) {
                const sessionId = e.target.closest('.view-chat-btn').dataset.sessionId;
                this.openChatView(sessionId);
            }
        });
        
        // 페이지 언로드 시 폴링 중지
        window.addEventListener('beforeunload', () => {
            this.stopPolling();
            this.disconnectWebSocket();
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
            const response = await ajax.get('/api/admin/chat/statistics');
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
            const response = await ajax.get('/api/admin/chat/sessions/waiting');
            
            if (response && response.code === '00') {
                await this.updateWaitingSessions(response.data);
            } else {
                console.log('대기 세션 API 호출 실패, 테스트 데이터 사용');
                // 테스트 데이터 사용
                const testWaitingSessions = [
                    {
                        sessionId: 'CHAT_WAIT_001',
                        memberName: '김고객',
                        title: `${this.categoryNames['GENERAL'] || '일반 문의'} - 1:1 상담`,
                        startTime: '2024-01-15 14:30:00',
                        messageCount: 1
                    },
                    {
                        sessionId: 'CHAT_WAIT_002',
                        memberName: '이고객',
                        title: `${this.categoryNames['DELIVERY'] || '배송'} - 1:1 상담`,
                        startTime: '2024-01-15 15:00:00',
                        messageCount: 2
                    }
                ];
                await this.updateWaitingSessions(testWaitingSessions);
            }
        } catch (error) {
            console.error('대기 중인 세션 로드 실패:', error);
            // 테스트 데이터 사용
            const testWaitingSessions = [
                {
                    sessionId: 'CHAT_WAIT_001',
                    memberName: '김고객',
                    title: `${this.categoryNames['GENERAL'] || '일반 문의'} - 1:1 상담`,
                    startTime: '2024-01-15 14:30:00',
                    messageCount: 1
                },
                {
                    sessionId: 'CHAT_WAIT_002',
                    memberName: '이고객',
                    title: `${this.categoryNames['DELIVERY'] || '배송'} - 1:1 상담`,
                    startTime: '2024-01-15 15:00:00',
                    messageCount: 2
                }
            ];
            await this.updateWaitingSessions(testWaitingSessions);
        }
    }
    
    /**
     * 진행 중인 세션 로드
     */
    async loadActiveSessions() {
        try {
            console.log('=== 진행중인 세션 로드 시작 ===');
            const response = await ajax.get('/api/admin/chat/sessions/active');
            console.log('진행중인 세션 API 응답:', response);
            
            if (response && response.code === '00') {
                console.log('진행중인 세션 데이터:', response.data);
                console.log('진행중인 세션 개수:', response.data.length);
                await this.updateActiveSessions(response.data);
            } else {
                console.log('진행중인 세션 API 호출 실패, 빈 배열 사용');
                console.log('응답 코드:', response?.code);
                console.log('응답 메시지:', response?.message);
                await this.updateActiveSessions([]);
            }
        } catch (error) {
            console.error('진행 중인 세션 로드 실패:', error);
            console.error('에러 스택:', error.stack);
            await this.updateActiveSessions([]);
        }
    }
    
    /**
     * 최근 완료된 세션 로드
     */
    async loadRecentCompletedSessions() {
        try {
            console.log('=== 완료된 세션 로드 시작 ===');
            const response = await ajax.get('/api/admin/chat/sessions/completed');
            console.log('완료된 세션 API 응답:', response);
            
            if (response && response.code === '00') {
                console.log('완료된 세션 데이터:', response.data);
                console.log('완료된 세션 개수:', response.data.length);
                await this.updateCompletedSessions(response.data);
            } else {
                console.log('완료된 세션 API 호출 실패, 빈 배열 사용');
                console.log('응답 코드:', response?.code);
                console.log('응답 메시지:', response?.message);
                await this.updateCompletedSessions([]);
            }
        } catch (error) {
            console.error('완료된 세션 로드 실패:', error);
            console.error('에러 스택:', error.stack);
            await this.updateCompletedSessions([]);
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
    async updateWaitingSessions(sessions) {
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
        
        const sessionHtmlPromises = sessions.map(session => this.createSessionItem(session, 'waiting'));
        const sessionsHtml = await Promise.all(sessionHtmlPromises);
        container.innerHTML = sessionsHtml.join('');
    }
    
    /**
     * 진행 중인 세션 업데이트
     */
    async updateActiveSessions(sessions) {
        console.log('=== 진행중인 세션 업데이트 시작 ===');
        console.log('세션 데이터:', sessions);
        
        const container = document.getElementById('activeSessionList');
        const countElement = document.getElementById('activeSessionCount');
        const emptyState = document.getElementById('noActiveSessions');
        
        console.log('컨테이너 요소:', container);
        console.log('카운트 요소:', countElement);
        console.log('빈 상태 요소:', emptyState);
        
        if (!container || !countElement) {
            console.warn('진행 중인 세션 컨테이너를 찾을 수 없습니다.');
            return;
        }
        
        countElement.textContent = sessions.length;
        console.log('카운트 업데이트 완료:', sessions.length);
        
        if (sessions.length === 0) {
            console.log('진행중인 세션이 없음, 빈 상태 표시');
            if (emptyState) {
                emptyState.style.display = 'block';
            }
            return;
        }
        
        console.log('진행중인 세션 HTML 생성 시작');
        if (emptyState) {
            emptyState.style.display = 'none';
        }
        
        const sessionHtmlPromises = sessions.map(session => this.createSessionItem(session, 'active'));
        const sessionsHtml = await Promise.all(sessionHtmlPromises);
        console.log('생성된 HTML:', sessionsHtml);
        container.innerHTML = sessionsHtml.join('');
        console.log('진행중인 세션 업데이트 완료');
    }
    
    /**
     * 완료된 세션 업데이트
     */
    async updateCompletedSessions(sessions) {
        console.log('=== 완료된 세션 업데이트 시작 ===');
        console.log('세션 데이터:', sessions);
        
        const container = document.getElementById('recentCompletedSessions');
        const emptyState = document.getElementById('noCompletedSessions');
        
        console.log('컨테이너 요소:', container);
        console.log('빈 상태 요소:', emptyState);
        
        if (!container) {
            console.warn('완료된 세션 컨테이너를 찾을 수 없습니다.');
            return;
        }
        
        if (sessions.length === 0) {
            console.log('완료된 세션이 없음, 빈 상태 표시');
            if (emptyState) {
                emptyState.style.display = 'block';
            }
            return;
        }
        
        console.log('완료된 세션 HTML 생성 시작');
        if (emptyState) {
            emptyState.style.display = 'none';
        }
        
        const sessionHtmlPromises = sessions.map(session => this.createCompletedSessionItem(session));
        const sessionsHtml = await Promise.all(sessionHtmlPromises);
        console.log('생성된 HTML:', sessionsHtml);
        container.innerHTML = sessionsHtml.join('');
        console.log('완료된 세션 업데이트 완료');
    }
    
    /**
     * 세션 아이템 HTML 생성
     */
    async createSessionItem(session, type) {
        const time = new Date(session.startTime).toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit'
        });
        
        // 메시지 개수 동적 조회
        let messageCount = 0;
        try {
            const response = await fetch(`/api/admin/chat/sessions/${session.sessionId}/message-count`);
            if (response.ok) {
                const result = await response.json();
                if (result.code === '00') {
                    messageCount = result.data.messageCount || 0;
                }
            }
        } catch (error) {
            console.error('메시지 개수 조회 실패:', error);
            messageCount = session.messageCount || 0; // 폴백으로 기존 값 사용
        }
        
        // 서버에서 받은 statusName만 사용 (하드코딩된 값 제거)
        const statusText = session.statusName || '알 수 없음';
        const statusBadge = statusText ? 
            `<span class="status-badge ${type}">${statusText}</span>` : '';
        
        // 일시 이탈 상태 표시 (서버 데이터에서 확인)
        const presenceBadge = this.getPresenceBadge(session);
        
        // 진행중인 세션과 대기중인 세션의 버튼을 다르게 표시
        const actionButton = type === 'active' ? 
            `<button class="btn-sm btn-primary view-chat-btn" data-session-id="${session.sessionId}">
                <i class="fas fa-eye"></i>
                상담 보기
            </button>` :
            `<button class="btn-sm btn-primary start-chat-btn" data-session-id="${session.sessionId}">
                <i class="fas fa-comments"></i>
                상담 시작
            </button>`;
        
        return `
            <div class="session-item" data-session-id="${session.sessionId}">
                <div class="session-header">
                    <div class="session-title">${this.escapeHtml(session.title)}</div>
                    <div class="session-time">${time}</div>
                </div>
                <div class="session-info">
                    <span class="session-customer">${this.escapeHtml(session.memberName)}</span>
                    <span>메시지 ${messageCount}개</span>
                    ${statusBadge}
                    ${presenceBadge}
                </div>
                <div class="session-actions">
                    ${actionButton}
                </div>
            </div>
        `;
    }
    
    /**
     * 완료된 세션 아이템 HTML 생성
     */
    async createCompletedSessionItem(session) {
        const time = new Date(session.endTime).toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit'
        });
        
        // 메시지 개수 동적 조회
        let messageCount = 0;
        try {
            const response = await fetch(`/api/admin/chat/sessions/${session.sessionId}/message-count`);
            if (response.ok) {
                const result = await response.json();
                if (result.code === '00') {
                    messageCount = result.data.messageCount || 0;
                }
            }
        } catch (error) {
            console.error('메시지 개수 조회 실패:', error);
            messageCount = session.messageCount || 0; // 폴백으로 기존 값 사용
        }
        
        const statusText = session.statusName || (session.statusId === 3 ? '완료' : '');
        const statusBadge = statusText ? 
            `<span class="status-badge completed">${statusText}</span>` : '';
        
        // 종료 사유 표시
        const exitReasonText = this.getExitReasonText(session.exitReasonId);
        const exitReasonBadge = exitReasonText ? 
            `<span class="exit-reason-badge">${exitReasonText}</span>` : '';
        
        return `
            <div class="session-item">
                <div class="session-header">
                    <div class="session-title">${this.escapeHtml(session.title)}</div>
                    <div class="session-time">${time}</div>
                </div>
                <div class="session-info">
                    <span class="session-customer">${this.escapeHtml(session.memberName)}</span>
                    <span>메시지 ${messageCount}개</span>
                    ${statusBadge}
                    ${exitReasonBadge}
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
     * 일시 이탈 상태 배지 생성
     */
    getPresenceBadge(session) {
        // 서버에서 받은 데이터를 기반으로 일시 이탈 상태 확인
        if (session.disconnectReason) {
            const side = session.memberLastSeen && session.adminLastSeen ? 
                (session.memberLastSeen > session.adminLastSeen ? 'MEMBER' : 'ADMIN') :
                (session.memberLastSeen ? 'MEMBER' : 'ADMIN');
            
            const badgeText = side === 'MEMBER' ? '고객 이탈' : '상담원 이탈';
            const badgeTitle = `이탈 사유: ${session.disconnectReason}`;
            
            return `<span class="presence-badge inactive" title="${badgeTitle}">${badgeText}</span>`;
        }
        return '';
    }
    
    /**
     * 종료 사유 텍스트 생성
     */
    getExitReasonText(exitReasonId) {
        if (!exitReasonId) {
            return null;
        }
        
        const exitReason = this.exitReasons[exitReasonId];
        return exitReason || null;
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
     * 채팅 세션 열기 (iframe 모달)
     */
    async openChatSession(sessionId) {
        try {
            console.log('상담 시작 시도:', sessionId);
            
            // 세션 상태를 "진행중"으로 업데이트 (백엔드에서 ACTIVE 상태로 처리)
            const response = await fetch(`/api/admin/chat/sessions/${sessionId}/status`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    status: 'ACTIVE' // 하드코딩된 statusId 대신 상태명 사용
                })
            });

            console.log('세션 상태 업데이트 응답:', response.status, response.statusText);

            if (!response.ok) {
                throw new Error(`세션 상태 업데이트 실패: ${response.status} ${response.statusText}`);
            }

            const result = await response.json();
            console.log('세션 상태 업데이트 결과:', result);
            
            if (result.code !== '00') {
                throw new Error(result.message || '세션 상태 업데이트에 실패했습니다.');
            }

            console.log('세션 상태가 진행중으로 업데이트되었습니다:', sessionId);
            
            // iframe 모달로 관리자 채팅 세션 페이지 열기
            this.createAdminChatIframe(sessionId);
            
        } catch (error) {
            console.error('상담 시작 실패:', error);
            alert('상담을 시작할 수 없습니다. 다시 시도해주세요. 오류: ' + error.message);
        }
    }

    /**
     * 상담 보기 (모달창으로 열기)
     */
    async openChatView(sessionId) {
        try {
            console.log('상담 보기 시도:', sessionId);
            
            // iframe 모달로 관리자 채팅 세션 페이지 열기 (상담 시작 없이)
            this.createAdminChatIframe(sessionId);
            
        } catch (error) {
            console.error('상담 보기 실패:', error);
            alert('상담을 볼 수 없습니다. 다시 시도해주세요. 오류: ' + error.message);
        }
    }

    /**
     * 관리자 채팅 iframe 모달 생성
     */
    createAdminChatIframe(sessionId) {
        // 기존 모달이 있으면 제거
        const existingModal = document.getElementById('adminChatModal');
        if (existingModal) {
            existingModal.remove();
        }
        
        // iframe 컨테이너 생성 (드래그 가능한 non-blocking)
        const iframeContainer = document.createElement('div');
        iframeContainer.id = 'adminChatModal';
        iframeContainer.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 900px;
            height: 700px;
            z-index: 9999;
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
            overflow: hidden;
            cursor: move;
            user-select: none;
            background: white;
            border: 1px solid #ddd;
        `;
        
        // iframe 생성
        const iframe = document.createElement('iframe');
        iframe.src = `/admin/chat/session/${sessionId}`;
        iframe.style.cssText = `
            width: 100%;
            height: 100%;
            border: none;
            border-radius: 10px;
            background: white;
        `;
        
        // 모달에 iframe 추가
        iframeContainer.appendChild(iframe);
        document.body.appendChild(iframeContainer);
        
        // 드래그 핸들을 통해서만 드래그 가능하므로 기존 드래그 기능 제거
        
        // iframe에서 오는 메시지 수신 (종료사유 선택 후 닫기, 드래그 이벤트)
        window.addEventListener('message', (event) => {
            if (event.data && event.data.type === 'closeAdminChat') {
                console.log('iframe에서 관리자 채팅 종료 요청 받음');
                this.closeAdminChatIframe();
            } else if (event.data && event.data.type === 'adminChatDragStart') {
                console.log('iframe에서 관리자 채팅 드래그 시작 요청 받음');
                // 드래그 시작 시 iframe 내부 클릭 이벤트 방지
                iframe.style.pointerEvents = 'none';
            } else if (event.data && event.data.type === 'adminChatDragMove') {
                // iframe 컨테이너를 드래그로 이동
                const deltaX = event.data.deltaX;
                const deltaY = event.data.deltaY;
                
                const currentTransform = iframeContainer.style.transform;
                const match = currentTransform.match(/translate3d\(([^,]+),\s*([^,]+)/);
                
                let currentX = 0;
                let currentY = 0;
                
                if (match) {
                    currentX = parseInt(match[1]);
                    currentY = parseInt(match[2]);
                }
                
                const newX = currentX + deltaX;
                const newY = currentY + deltaY;
                
                // 부드러운 이동을 위해 transform 사용
                iframeContainer.style.transform = `translate3d(${newX}px, ${newY}px, 0)`;
            } else if (event.data && event.data.type === 'adminChatDragEnd') {
                console.log('iframe에서 관리자 채팅 드래그 종료 요청 받음');
                // 드래그 종료 시 iframe 내부 클릭 이벤트 복원
                iframe.style.pointerEvents = 'auto';
            }
        });
        
        // 고객 채팅 iframe 메시지 리스너도 추가
        window.addEventListener('message', (event) => {
            if (event.data && event.data.type === 'closeChat') {
                console.log('iframe에서 고객 채팅 종료 요청 받음');
                // 고객 채팅창 닫기 (있다면)
                const customerChatModal = document.getElementById('customerChatModal');
                if (customerChatModal) {
                    customerChatModal.remove();
                }
            }
        });
    }
    
    /**
     * 관리자 채팅 iframe 닫기
     */
    closeAdminChatIframe() {
        const modal = document.getElementById('adminChatModal');
        if (modal) {
            modal.remove();
            console.log('관리자 채팅 iframe 모달이 닫혔습니다.');
        }
    }

    /**
     * 모달창 드래그 기능
     */
    makeDraggable(modal, handle) {
        let isDragging = false;
        let currentX;
        let currentY;
        let initialX;
        let initialY;
        let xOffset = 0;
        let yOffset = 0;

        handle.addEventListener('mousedown', (e) => {
            initialX = e.clientX - xOffset;
            initialY = e.clientY - yOffset;
            
            if (e.target === handle || handle.contains(e.target)) {
                isDragging = true;
            }
        });

        document.addEventListener('mousemove', (e) => {
            if (isDragging) {
                e.preventDefault();
                currentX = e.clientX - initialX;
                currentY = e.clientY - initialY;
                xOffset = currentX;
                yOffset = currentY;

                modal.style.transform = `translate(${currentX}px, ${currentY}px)`;
            }
        });

        document.addEventListener('mouseup', () => {
            isDragging = false;
        });
    }
}

// 페이지 로드 시 대시보드 초기화
document.addEventListener('DOMContentLoaded', () => {
    new AdminChatDashboard();
});
