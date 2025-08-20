/**
 * 고객용 채팅 클래스 (WebSocket 기반)
 */
class CustomerChat {
    constructor() {
        this.stompClient = null;
        this.sessionId = null;
        this.connected = false;
        this.unreadCount = 0;
        this.ownUnreadSeq = 0; // 내가 보낸 메시지의 누적 읽음 시퀀스
        this._lastReadNotifyAt = 0;
        this._readCheckTimer = null;
        this._isPageVisible = true;
        this._isWindowFocused = true;
        this._isNearBottom = true;
        this._isSessionEnded = false; // 상담 종료 상태 추적
        
        // 새로 추가된 속성들
        this.disconnectReasons = []; // 이탈 사유 목록
        this.exitReasons = []; // 종료 사유 목록
        this._disconnectTimer = null; // 이탈 타이머
        this._graceUntil = null; // 유예 만료 시간
        
        // 카테고리 이름 매핑 (서버에서 동적으로 로드)
        this.categoryNames = {};
        this.categoryData = {}; // 카테고리 전체 데이터 (codeId 포함)
        
        // 페이지 가시성 및 포커스 상태 모니터링
        this.initializeVisibilityMonitoring();
        
        this.init();
    }

    init() {
        // 로그인 상태 확인
        if (!this.getCurrentUserId()) {
            this.showLoginRequired();
            return;
        }
        
        this.bindEvents();
        this.showCategorySelection();
    }

    /**
     * 로그인 필요 메시지 표시
     */
    showLoginRequired() {
        const categorySelection = document.getElementById('categorySelection');
        if (categorySelection) {
            categorySelection.innerHTML = `
                <div class="login-required">
                    <h2>로그인이 필요합니다</h2>
                    <p>1:1 상담을 이용하려면 로그인이 필요합니다.</p>
                    <div class="login-actions">
                        <a href="/login" class="btn btn--primary">로그인하기</a>
                        <a href="/members/join" class="btn btn--secondary">회원가입</a>
                    </div>
                </div>
            `;
        }
    }

    bindEvents() {
        // 카테고리 선택 이벤트
        document.querySelectorAll('.category-item').forEach(item => {
            item.addEventListener('click', (e) => {
                this.selectCategory(e.currentTarget.dataset.category);
            });
        });

        // 뒤로가기 버튼
        document.getElementById('backToCategory').addEventListener('click', () => {
            this.showCategorySelection();
        });

        // 메시지 입력 이벤트
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');

        messageInput.addEventListener('input', (e) => {
            this.updateCharCount(e.target.value);
            this.toggleSendButton(e.target.value.trim());
        });

        messageInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        sendBtn.addEventListener('click', () => {
            this.sendMessage();
        });

        // 상담 종료 버튼
        const endChatBtn = document.getElementById('endChatBtn');
        if (endChatBtn) {
            endChatBtn.addEventListener('click', () => {
                this.showExitReasonModal();
            });
        }

        // 페이지 이탈/숨김 시 이탈 처리
        const notifyInactive = () => {
            try {
                if (!this.sessionId) return;
                
                // 이탈 사유 중에서 PAGE_HIDE에 해당하는 ID 찾기
                const pageHideReason = this.disconnectReasons.find(reason => reason.code === 'PAGE_HIDE');
                if (pageHideReason) {
                    this.handleDisconnect(pageHideReason.codeId, 5); // 5분 유예
                }
            } catch (e) { 
                console.error('페이지 이탈 처리 실패:', e);
            }
        };

        window.addEventListener('pagehide', notifyInactive);
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'hidden') notifyInactive();
        });
        window.addEventListener('beforeunload', notifyInactive);
    }

    showCategorySelection() {
        // 이전 세션에서 예약된 화면 전환 타이머가 있으면 정리
        this.clearEndTimers();

        document.getElementById('categorySelection').style.display = 'block';
        document.getElementById('chatContainer').style.display = 'none';
        
        // 메시지 영역 초기화 (기존 메시지 모두 제거)
        const messageArea = document.getElementById('messageArea');
        messageArea.innerHTML = '';
        
        // 입력 필드 초기화
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');
        
        if (messageInput) {
            messageInput.value = '';
            messageInput.disabled = false;
            messageInput.placeholder = '메시지를 입력하세요... (Enter: 전송, Shift+Enter: 줄바꿈)';
        }
        
        if (sendBtn) {
            sendBtn.disabled = true;
        }
        
        this.updateCharCount('');
        
        // 기존 연결 해제
        if (this.stompClient) {
            this.disconnect();
        }
        
        // 선택된 카테고리 초기화
        this.selectedCategory = null;
        this.sessionId = null;

        // 재개 가능한 세션 안내 표시
        this.checkResumableSessions();
    }

    selectCategory(categoryCode) {
        this.selectedCategory = categoryCode;
        
        // 카테고리 선택 시각적 피드백
        document.querySelectorAll('.category-item').forEach(item => {
            item.classList.remove('selected');
        });
        document.querySelector(`[data-category="${categoryCode}"]`).classList.add('selected');
        
        // 잠시 후 채팅 화면으로 전환
        setTimeout(() => {
            this.startChat();
        }, 300);
    }

    async startChat() {
        try {
            // 상담 종료 플래그 리셋
            this._isSessionEnded = false;
            
            // 이전 세션에서 예약된 화면 전환 타이머가 있으면 정리
            this.clearEndTimers();

            // 채팅 화면 표시
            document.getElementById('categorySelection').style.display = 'none';
            document.getElementById('chatContainer').style.display = 'block';
            
            // 카테고리 정보 업데이트
            document.getElementById('selectedCategory').textContent = this.categoryNames[this.selectedCategory];
            
            // 메시지 영역 초기화 (기존 메시지 모두 제거)
            const messageArea = document.getElementById('messageArea');
            messageArea.innerHTML = '';
            messageArea.style.height = '400px';
            messageArea.style.overflowY = 'scroll';
            
            // 입력 필드 초기화 (연결 전에는 비활성화)
            const messageInput = document.getElementById('messageInput');
            const sendBtn = document.getElementById('sendBtn');
            
            messageInput.value = '';
            messageInput.disabled = false; // 입력 필드 활성화
            sendBtn.disabled = true; // 전송 버튼은 연결 후 활성화
            
            this.updateCharCount('');
            
            // 채팅 세션 생성
            await this.createChatSession();
            
            // 이탈 사유와 종료 사유 목록 로드
            await Promise.all([
                this.loadDisconnectReasons(),
                this.loadExitReasons(),
                this.loadCategoryNames()
            ]);
            
            // WebSocket 연결
            this.connectWebSocket();
            
            // 상담 종료 버튼 표시
            const endChatBtn = document.getElementById('endChatBtn');
            if (endChatBtn) {
                endChatBtn.style.display = 'flex';
            }
            
            // 재접속 처리
            try {
                await this.handleReconnect();
            } catch (e) { 
                console.error('재접속 처리 실패:', e);
            }
            
            // 미읽음 배지 초기화 및 DOM 추가
            this.unreadCount = 0;
            this.ownUnreadSeq = 0; // 누적 읽음 시퀀스도 초기화
            const statusEl = document.querySelector('.chat-status');
            if (statusEl && !document.getElementById('unreadCounter')) {
                const badge = document.createElement('span');
                badge.id = 'unreadCounter';
                badge.className = 'unread-badge';
                badge.style.marginLeft = '8px';
                statusEl.appendChild(badge);
            }
            this.renderUnreadBadge();
            
        } catch (error) {
            console.error('채팅 시작 실패:', error);
            alert('채팅을 시작할 수 없습니다. 다시 시도해주세요.');
            this.showCategorySelection();
        }
    }

    async checkResumableSessions() {
        try {
            const memberId = this.getCurrentUserId();
            if (!memberId) return;
            
            const res = await fetch(`/api/chat/sessions/resumable?memberId=${memberId}`);
            if (!res.ok) return;
            
            const data = await res.json();
            if (data && data.code === '00' && Array.isArray(data.data) && data.data.length > 0) {
                console.log('재개 가능한 세션 조회 결과:', data.data);
                
                // 실제로 재개 가능한 세션만 필터링 (종료되지 않은 세션)
                const resumableSession = data.data.find(session => {
                    console.log(`세션 ${session.sessionId} 상태:`, session.statusName);
                    return session.statusName !== '완료' && 
                           session.statusName !== '종료' && 
                           session.statusName !== 'COMPLETED' &&
                           session.statusName !== 'ENDED';
                });
                
                if (resumableSession) {
                    console.log('재개 가능한 세션 발견:', resumableSession);
                    this.renderResumeBanner(resumableSession);
                } else {
                    console.log('재개 가능한 세션이 없습니다.');
                    this.renderResumeBanner(null);
                }
            } else {
                console.log('재개 가능한 세션 데이터가 없습니다.');
                this.renderResumeBanner(null);
            }
        } catch (e) {
            console.error('재개 가능한 세션 조회 실패:', e);
            this.renderResumeBanner(null);
        }
    }

    renderResumeBanner(session) {
        const container = document.getElementById('categorySelection');
        if (!container) return;
        let banner = document.getElementById('resumeBanner');
        if (!session) {
            if (banner) banner.remove();
            return;
        }
        if (!banner) {
            banner = document.createElement('div');
            banner.id = 'resumeBanner';
            banner.style.marginBottom = '16px';
            banner.style.padding = '12px 16px';
            banner.style.border = '1px solid #e2e8f0';
            banner.style.borderRadius = '8px';
            banner.style.background = '#f8fafc';
            container.prepend(banner);
        }
        const title = session.title || '이전 상담';
        const categoryName = session.categoryName || '상담';
        banner.innerHTML = `
            <div style="display:flex; align-items:center; justify-content:space-between; gap:12px;">
                <div>
                    <div style="font-weight:600;">이어서 상담하기</div>
                    <div style="font-size:13px; color:#475569;">${this.escapeHtml(title)} · ${this.escapeHtml(categoryName)}</div>
                </div>
                <div>
                    <button id="resumeBtn" class="btn btn--primary">이어가기</button>
                </div>
            </div>
        `;
        const btn = banner.querySelector('#resumeBtn');
        if (btn) {
            btn.addEventListener('click', () => this.resumeChat(session));
        }
    }

    async resumeChat(session) {
        try {
            // 상담 종료 플래그 리셋
            this._isSessionEnded = false;
            
            // 세션 상태 체크 (종료된 세션은 이어갈 수 없음)
            if (session.statusName === '완료' || session.statusName === '종료' || 
                session.statusName === 'COMPLETED' || session.statusName === 'ENDED') {
                alert('이미 종료된 상담입니다. 새 상담을 시작해주세요.');
                this.renderResumeBanner(null);
                return;
            }
            
            // 화면 전환
            document.getElementById('categorySelection').style.display = 'none';
            document.getElementById('chatContainer').style.display = 'block';

            // 세션 설정
            this.sessionId = session.sessionId;
            this.selectedCategory = null;
            const categoryLabel = session.categoryName || '1:1 상담';
            document.getElementById('selectedCategory').textContent = categoryLabel;

            // 메시지 영역 초기화
            const messageArea = document.getElementById('messageArea');
            messageArea.innerHTML = '';
            messageArea.style.height = '400px';
            messageArea.style.overflowY = 'scroll';

            // 입력 필드 초기화 (연결 전에는 비활성화)
            const messageInput = document.getElementById('messageInput');
            const sendBtn = document.getElementById('sendBtn');
            
            messageInput.value = '';
            messageInput.disabled = false; // 입력 필드 활성화
            sendBtn.disabled = true; // 전송 버튼은 연결 후 활성화
            
            this.updateCharCount('');

            // WebSocket 연결 및 구독
            this.connectWebSocket();

            // 종료 버튼 표시
            const endChatBtn = document.getElementById('endChatBtn');
            if (endChatBtn) endChatBtn.style.display = 'flex';

            // 미읽음 배지 초기화
            this.unreadCount = 0;
            this.ownUnreadSeq = 0; // 누적 읽음 시퀀스도 초기화
            this.renderUnreadBadge();

            // presence ACTIVE (상태 변경 없이 last_seen 갱신)
            try {
                const payload = { side: 'MEMBER', state: 'ACTIVE', reason: 'RESUME' };
                await fetch(`/api/chat/sessions/${this.sessionId}/presence`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
            } catch (e) { /* noop */ }

            // 기존 메시지 로드
            try {
                const res = await fetch(`/api/chat/sessions/${this.sessionId}/messages`);
                if (res.ok) {
                    const payload = await res.json();
                    if (payload && payload.code === '00' && Array.isArray(payload.data)) {
                        payload.data.forEach(m => this.displayMessage(m));
                    }
                }
            } catch (e) { /* noop */ }
        } catch (error) {
            console.error('이전 상담 이어가기 실패:', error);
            alert('이전 상담을 이어갈 수 없습니다. 새 상담을 시작해주세요.');
            this.showCategorySelection();
        }
    }

    async createChatSession() {
        try {
            const categoryId = await this.getCategoryId(this.selectedCategory);
            const memberId = this.getCurrentUserId();
            
            if (!memberId) {
                throw new Error('로그인이 필요합니다. 로그인 후 다시 시도해주세요.');
            }
            
            const response = await fetch('/api/chat/session', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    memberId: memberId,
                    categoryId: categoryId,
                    title: `${this.categoryNames[this.selectedCategory]} - 1:1 상담`
                })
            });

            if (!response.ok) throw new Error('채팅 세션 생성 실패');

            const data = await response.json();
            if (data.code !== '00' || !data.sessionId) throw new Error(data.message || '채팅 세션 생성 실패');
            this.sessionId = data.sessionId;
            
            console.log('채팅 세션 생성됨:', this.sessionId);
            
        } catch (error) {
            console.error('채팅 세션 생성 오류:', error);
            throw error;
        }
    }

    async getCategoryId(categoryCode) {
        try {
            // 이미 로드된 카테고리 데이터가 있으면 사용
            if (this.categoryData && this.categoryData[categoryCode]) {
                return this.categoryData[categoryCode].codeId;
            } else {
                // 카테고리 목록이 로드되지 않은 경우 로드 후 조회
                await this.loadCategoryNames();
                if (this.categoryData && this.categoryData[categoryCode]) {
                    return this.categoryData[categoryCode].codeId;
                } else {
                    throw new Error(`카테고리를 찾을 수 없음: ${categoryCode}`);
                }
            }
        } catch (error) {
            console.error('FAQ 카테고리 조회 중 오류:', error);
            throw error;
        }
    }

    connectWebSocket() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('WebSocket 연결됨:', frame);
            this.connected = true; // 연결 상태 플래그 설정
            this.updateStatus('연결됨', 'connected');
            
            // 채팅 토픽 구독
            this.subscribeToChat();
            
            // 사용자 참가 메시지 전송
            this.joinChatSession();
            
            // 연결 후 입력 필드 상태 업데이트
            const messageInput = document.getElementById('messageInput');
            this.updateCharCount(messageInput.value);
            this.toggleSendButton(messageInput.value.trim());
            
        }, (error) => {
            console.error('WebSocket 연결 실패:', error);
            this.connected = false; // 연결 실패 시 플래그 해제
            this.updateStatus('연결 실패', 'disconnected');
            
            // 연결 실패 시 입력 필드 비활성화
            const sendBtn = document.getElementById('sendBtn');
            sendBtn.disabled = true;
        });
    }

    /**
     * 페이지 가시성 및 포커스 상태 모니터링 초기화
     */
    initializeVisibilityMonitoring() {
        // 페이지 가시성 변경 감지
        document.addEventListener('visibilitychange', () => {
            this._isPageVisible = document.visibilityState === 'visible';
            this.checkReadCondition();
        });

        // 윈도우 포커스 변경 감지
        window.addEventListener('focus', () => {
            this._isWindowFocused = true;
            this.checkReadCondition();
        });

        window.addEventListener('blur', () => {
            this._isWindowFocused = false;
        });

        // 스크롤 이벤트로 하단 근접 상태 감지
        const messageArea = document.getElementById('messageArea');
        if (messageArea) {
            messageArea.addEventListener('scroll', () => {
                this._isNearBottom = (messageArea.scrollHeight - messageArea.scrollTop - messageArea.clientHeight) < 8;
                this.checkReadCondition();
            });
        }
    }

    /**
     * 읽음 조건 체크 및 처리
     */
    checkReadCondition() {
        if (this._readCheckTimer) {
            clearTimeout(this._readCheckTimer);
        }

        // 카카오톡 방식: 조건 만족 시 약간의 지연 후 읽음 처리
        this._readCheckTimer = setTimeout(() => {
            if (this._isPageVisible && this._isWindowFocused && this._isNearBottom && this.unreadCount > 0) {
                this.markAllAsRead();
            }
        }, 500); // 0.5초 지연으로 안정성 확보
    }

    /**
     * 채팅 토픽 구독
     */
    subscribeToChat() {
        if (!this.sessionId) return;
        
        // 기존 구독이 남아있으면 정리
        if (this._subscription && typeof this._subscription.unsubscribe === 'function') {
            try { this._subscription.unsubscribe(); } catch (e) {}
            this._subscription = null;
        }

        this._subscription = this.stompClient.subscribe(`/topic/chat/${this.sessionId}`, (message) => {
            const chatMessage = JSON.parse(message.body);
            
            // 읽음 이벤트 처리
            if (chatMessage.content === 'READ_EVENT') {
                this.handleReadEvent(chatMessage);
                return;
            }

            // 상담 종료 후 불필요한 시스템 메시지 필터링
            if (this._isSessionEnded && 
                (chatMessage.content === '상담원이 잠시 이탈했습니다.' || 
                 chatMessage.content === '상담원이 다시 접속했습니다.')) {
                return; // 상담이 종료된 상태에서는 이탈/재접속 메시지 무시
            }

            this.displayMessage(chatMessage);

            // 미읽음 카운트 업데이트: 상대 메시지이고 즉시 읽음 조건을 만족하지 않으면 +1
            const isOther = chatMessage.senderId && chatMessage.senderId !== this.getCurrentUserId();
            if (isOther) {
                if (!(this._isPageVisible && this._isWindowFocused && this._isNearBottom)) {
                    this.unreadCount = (this.unreadCount || 0) + 1;
                    this.renderUnreadBadge();
                } else {
                    // 조건을 만족하면 즉시 읽음 처리
                    this.markAllAsRead();
                }
            }
        });
    }

    joinChatSession() {
        if (!this.sessionId || !this.stompClient) return;
        
        // 중복 시스템 메시지 방지를 위해 고객의 참가 시스템 메시지는 전송하지 않음
        // presence ACTIVE 전송과 서버측 상태 메시지로 충분히 인지 가능
    }

    /**
     * 메시지 표시
     */
    displayMessage(message) {
        const messageArea = document.getElementById('messageArea');
        const loadingElement = document.getElementById('loadingMessages');
        
        // 로딩 표시 제거
        if (loadingElement) {
            loadingElement.style.display = 'none';
        }
        
        const messageDiv = document.createElement('div');
        
        // 고객 화면과 동일한 클래스 체계로 통일: user | admin | system
        let messageClass = '';
        if (message.senderType === 'M') {
            messageClass = 'user';
        } else if (message.senderType === 'A') {
            messageClass = 'admin';
        } else if (message.senderType === 'S') {
            messageClass = 'system';
        }
        
        messageDiv.className = `message ${messageClass}`;
        
        // 시간 포맷팅
        const timestamp = new Date(message.timestamp);
        const timeString = timestamp.toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit'
        });
        
        // 메시지 HTML 구성 (고객 화면과 동일한 구조: info, content, meta)
        if (messageClass === 'system') {
            messageDiv.innerHTML = `
                <div class="message-content">
                    <div class="message-text">${this.escapeHtml(message.content)}</div>
                </div>
            `;
        } else {
            const isMyMessage = message.senderType === 'M';
            
            // 카카오톡 방식: 내 메시지에만 읽음 숫자 표시
            let readBadge = '';
            if (isMyMessage) {
                // 상대방이 읽지 않았으면 누적 숫자 표시
                if (message.isRead !== 'Y') {
                    // 첫 메시지부터 1부터 시작하도록 수정
                    this.ownUnreadSeq = Math.max(1, (this.ownUnreadSeq || 0) + 1);
                    readBadge = `<span class="read-badge unread">${this.ownUnreadSeq}</span>`;
                } else {
                    // 읽었으면 숫자 표시 안함
                    readBadge = '';
                }
            }
            
            messageDiv.innerHTML = `
                <div class="message-info">
                    <span class="message-name">${this.escapeHtml(message.senderName)}</span>
                    <span class="message-time">${timeString}</span>
                </div>
                <div class="message-content">${this.escapeHtml(message.content)}</div>
                <div class="message-meta">${readBadge}</div>
            `;
        }
        
        messageArea.appendChild(messageDiv);
        this.scrollToBottom();
        
        // 상담 종료 메시지 감지 (시스템 메시지)
        if (message.senderType === 'S' && message.content && 
            message.content.includes('상담이 종료되었습니다')) {
            console.log('상담 종료 메시지 감지. 카테고리 선택 화면으로 전환합니다.');
            this.handleSessionEnd();
        }
    }

    updateStatus(status, className) {
        const statusText = document.getElementById('statusText');
        const statusIndicator = document.getElementById('statusIndicator');
        
        statusText.textContent = status;
        statusIndicator.className = `status-indicator ${className}`;

        // 읽음 카운트 갱신 시도 (선택 영역에 표기하려면 엘리먼트가 있어야 함)
        try {
            const unreadCounter = document.getElementById('unreadCounter');
            if (unreadCounter) {
                // 서버에 unread 조회 API가 없으므로, read 이벤트 후에는 0으로 가정
                // 필요 시 별도 API 추가
                unreadCounter.textContent = '';
            }
        } catch (e) {}
    }

    updateCharCount(value) {
        const charCount = document.getElementById('charCount');
        charCount.textContent = `${value.length}/1000`;
    }

    toggleSendButton(value) {
        const sendBtn = document.getElementById('sendBtn');
        // WebSocket 연결 상태와 세션 존재 여부를 모두 체크
        const isConnected = this.stompClient && this.connected;
        const hasSession = this.sessionId;
        const hasContent = value && value.trim().length > 0;
        
        // 연결되어 있고 세션이 있고 내용이 있을 때만 활성화
        sendBtn.disabled = !isConnected || !hasSession || !hasContent;
    }

    /**
     * 상담 종료 처리
     */
    handleSessionEnd() {
        // 상담 종료 플래그 설정
        this._isSessionEnded = true;
        
        // 상태를 종료로 변경
        this.updateStatus('상담 종료', 'disconnected');
        
        // 입력 필드 비활성화
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');
        
        if (messageInput) {
            messageInput.disabled = true;
            messageInput.placeholder = '상담이 종료되었습니다.';
        }
        
        if (sendBtn) {
            sendBtn.disabled = true;
        }
        
        // WebSocket 연결 해제 (이때의 세션을 캡처)
        const endedSessionId = this.sessionId;
        this.disconnect();

        // 즉시 카테고리 선택 화면으로 전환
        this.showCategorySelection();
        
        // 재개 가능한 세션 재체크 (종료된 세션 제외)
        setTimeout(() => {
            this.checkResumableSessions();
        }, 200);
        
        // 관리자에 의한 종료 메시지 표시 (부드러운 알림)
        setTimeout(() => {
            // alert 대신 토스트 메시지나 상태 표시로 변경
            console.log('상담원이 상담을 종료했습니다. 카테고리 선택 화면으로 이동합니다.');
        }, 100);
    }

    /**
     * 고객이 상담 종료 요청
     */
    async endChat() {
        if (!confirm('상담을 종료하시겠습니까?\n종료하면 이 대화는 종료되며 이어서 대화할 수 없습니다.\n필요 시 새 상담을 시작할 수 있습니다.')) {
            return;
        }

        try {
            // 상담 종료 플래그 설정
            this._isSessionEnded = true;
            
            // 상태 업데이트
            this.updateStatus('상담 종료 중...', 'disconnected');
            
            // 입력 필드 비활성화
            const messageInput = document.getElementById('messageInput');
            const sendBtn = document.getElementById('sendBtn');
            const endChatBtn = document.getElementById('endChatBtn');
            
            if (messageInput) {
                messageInput.disabled = true;
                messageInput.placeholder = '상담이 종료되었습니다.';
            }
            
            if (sendBtn) {
                sendBtn.disabled = true;
            }
            
            if (endChatBtn) {
                endChatBtn.disabled = true;
                endChatBtn.textContent = '상담 종료';
            }

            // 상담 종료 메시지 전송
            if (this.stompClient && this.sessionId) {
                const endMessage = {
                    sessionId: this.sessionId,
                    senderId: this.getCurrentUserId(),
                    senderName: this.getCurrentUserName(),
                    senderType: 'M',
                    messageTypeId: 4, // 시스템 메시지
                    content: '고객이 상담을 종료했습니다.'
                };
                
                this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(endMessage));
            }

            // API 호출로 세션 종료
            const response = await fetch(`/api/chat/sessions/${this.sessionId}/end`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error('세션 종료 API 호출 실패');
            }

            const result = await response.json();
            if (result.code !== '00') {
                throw new Error(result.message || '세션 종료에 실패했습니다.');
            }

            // WebSocket 연결 해제 (이때의 세션을 캡처)
            const endedSessionId = this.sessionId;
            this.disconnect();

            // 즉시 카테고리 선택 화면으로 전환
            this.showCategorySelection();
            
            // 재개 가능한 세션 재체크 (종료된 세션 제외)
            setTimeout(() => {
                this.checkResumableSessions();
            }, 200);
            
            // 성공 메시지 표시
            setTimeout(() => {
                alert('상담이 성공적으로 종료되었습니다.\n새로운 상담이 필요하시면 카테고리를 선택해주세요.');
            }, 100);

        } catch (error) {
            console.error('상담 종료 실패:', error);
            alert('상담 종료에 실패했습니다. 다시 시도해주세요.');
            
            // 실패 시 입력 필드 다시 활성화
            const messageInput = document.getElementById('messageInput');
            const sendBtn = document.getElementById('sendBtn');
            const endChatBtn = document.getElementById('endChatBtn');
            
            if (messageInput) {
                messageInput.disabled = false;
                messageInput.placeholder = '메시지를 입력하세요... (Enter: 전송, Shift+Enter: 줄바꿈)';
            }
            
            if (sendBtn) {
                sendBtn.disabled = false;
            }
            
            if (endChatBtn) {
                endChatBtn.disabled = false;
                endChatBtn.textContent = '상담 종료';
            }
        }
    }

    disconnect() {
        // 구독 해제 우선 수행
        if (this._subscription && typeof this._subscription.unsubscribe === 'function') {
            try { this._subscription.unsubscribe(); } catch (e) {}
            this._subscription = null;
        }

        if (this.stompClient && this.sessionId) {
            // 퇴장 메시지 전송
            const leaveMessage = {
                sessionId: this.sessionId,
                senderId: this.getCurrentUserId(),
                senderName: this.getCurrentUserName(),
                senderType: 'M',
                messageTypeId: 4,
                content: '상담을 종료했습니다.'
            };
            
            try {
                this.stompClient.send('/app/chat.removeUser', {}, JSON.stringify(leaveMessage));
            } catch (e) {
                console.warn('퇴장 메시지 전송 실패:', e);
            }
            
            // WebSocket 연결 해제
            try {
                this.stompClient.disconnect();
            } catch (e) {
                console.warn('WebSocket 연결 해제 실패:', e);
            }
            
            this.stompClient = null;
        }
        
        // 연결 상태 플래그 해제
        this.connected = false;
        
        // 입력 필드 비활성화
        const sendBtn = document.getElementById('sendBtn');
        if (sendBtn) {
            sendBtn.disabled = true;
        }
    }

    clearEndTimers() {
        if (this._endTimerId) {
            clearTimeout(this._endTimerId);
            this._endTimerId = null;
        }
    }

    /**
     * 카카오톡 방식 읽음 처리
     */
    markAllAsRead() {
        if (!this.sessionId || this.unreadCount <= 0) return;
        
        try {
            // REST API로 읽음 처리
            fetch(`/api/chat/sessions/${this.sessionId}/read`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ receiverId: this.getCurrentUserId() })
            })
            .then(response => response.json())
            .then(data => {
                if (data && data.code === '00') {
                    // WebSocket으로 읽음 이벤트 전송
                    this.sendReadEvent();
                    
                    // UI 업데이트
                    this.unreadCount = 0;
                    this.renderUnreadBadge();
                    
                    console.log('읽음 처리 완료');
                }
            })
            .catch(error => {
                console.error('읽음 처리 실패:', error);
            });
        } catch (e) {
            console.error('읽음 처리 중 오류:', e);
        }
    }

    /**
     * 읽음 이벤트 전송
     */
    sendReadEvent() {
        try {
            const now = Date.now();
            if (this.stompClient && (!this._lastReadNotifyAt || (now - this._lastReadNotifyAt) > 1000)) {
                this.stompClient.send('/app/chat.read', {}, JSON.stringify({
                    sessionId: this.sessionId,
                    senderId: this.getCurrentUserId(),
                    senderType: 'M'
                }));
                this._lastReadNotifyAt = now;
            }
        } catch (e) {
            console.error('읽음 이벤트 전송 실패:', e);
        }
    }

    /**
     * 읽음 이벤트 처리 (카카오톡 방식)
     */
    handleReadEvent(message) {
        try {
            console.log('읽음 이벤트 수신:', message);
            
            // 상담원이 읽었을 때만 처리 (내가 보낸 메시지의 읽음 상태 업데이트)
            if (message.senderType === 'A') {
                console.log('상담원이 메시지를 읽었습니다. 읽음 배지 제거 시작...');
                
                // 내가 보낸 메시지들의 모든 읽음 배지 제거 (카카오톡 방식)
                const items = Array.from(document.querySelectorAll('#messageArea .message.user .read-badge.unread'));
                console.log('제거할 읽음 배지 개수:', items.length);
                
                items.forEach((el, index) => { 
                    console.log(`읽음 배지 ${index + 1} 제거:`, el.textContent);
                    el.classList.remove('unread'); 
                    el.textContent = ''; 
                });
                
                // 누적 시퀀스 리셋
                this.ownUnreadSeq = 0;
                
                console.log('상담원이 메시지를 읽었습니다. 모든 읽음 표시가 제거되었습니다.');
            } else {
                console.log('읽음 이벤트 발신자가 상담원이 아님:', message.senderType);
            }
        } catch (e) {
            console.error('읽음 이벤트 처리 실패:', e);
        }
    }

    /**
     * 메시지 전송
     */
    sendMessage() {
        const input = document.getElementById('messageInput');
        const content = input.value.trim();
        
        // 연결 상태와 세션 존재 여부 체크
        if (!content || !this.stompClient || !this.connected || !this.sessionId) {
            console.warn('메시지 전송 조건 불만족:', {
                content: !!content,
                stompClient: !!this.stompClient,
                connected: this.connected,
                sessionId: !!this.sessionId
            });
            return;
        }
        
        const message = {
            sessionId: this.sessionId,
            senderId: this.getCurrentUserId(),
            senderType: 'M', // 고객
            content: content,
            senderName: this.getCurrentUserName(),
            messageTypeId: 1 // 일반 메시지
        };
        
        try {
            // WebSocket으로 메시지 전송
            this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
            
            // 입력창 초기화
            input.value = '';
            this.updateCharCount('');
            this.toggleSendButton('');
            
            console.log('메시지 전송 완료:', content);
        } catch (error) {
            console.error('메시지 전송 실패:', error);
            alert('메시지 전송에 실패했습니다. 다시 시도해주세요.');
        }
    }

    /**
     * 스크롤을 맨 아래로
     */
    scrollToBottom() {
        const messageArea = document.getElementById('messageArea');
        if (messageArea) {
            messageArea.scrollTop = messageArea.scrollHeight;
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
     * 미읽음 배지 렌더링
     */
    renderUnreadBadge() {
        const badge = document.getElementById('unreadCounter');
        if (!badge) return;
        if (this.unreadCount > 0) {
            badge.textContent = `미읽음 ${this.unreadCount}`;
            badge.style.display = 'inline-flex';
        } else {
            badge.textContent = '';
            badge.style.display = 'none';
        }
    }

    /**
     * 현재 사용자 ID 가져오기
     */
    getCurrentUserId() {
        const userId = getCurrentUserId(); // 전역 함수 호출
        if (!userId) {
            console.warn('사용자 ID를 가져올 수 없습니다. 로그인이 필요합니다.');
            return null;
        }
        return userId;
    }

    /**
     * 현재 사용자 이름 가져오기
     */
    getCurrentUserName() {
        return getCurrentUserNickname() || '고객';
    }

    /**
     * 이탈 사유 코드 목록 조회
     */
    async loadDisconnectReasons() {
        try {
            const response = await fetch('/api/chat/disconnect-reasons');
            if (response.ok) {
                const result = await response.json();
                if (result.success) {
                    this.disconnectReasons = result.data;
                    console.log('이탈 사유 목록 로드 완료:', this.disconnectReasons);
                }
            }
        } catch (error) {
            console.error('이탈 사유 목록 조회 실패:', error);
        }
    }

    /**
     * 종료 사유 코드 목록 조회
     */
    async loadExitReasons() {
        try {
            const response = await fetch('/api/chat/exit-reasons');
            if (response.ok) {
                const result = await response.json();
                if (result.success) {
                    this.exitReasons = result.data;
                    console.log('종료 사유 목록 로드 완료:', this.exitReasons);
                }
            }
        } catch (error) {
            console.error('종료 사유 목록 조회 실패:', error);
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
     * 채팅 세션 이탈 처리
     */
    async handleDisconnect(disconnectReasonId, graceMinutes = 5) {
        if (!this.sessionId) {
            console.error('세션 ID가 없습니다.');
            return;
        }

        try {
            const response = await fetch(`/api/chat/sessions/${this.sessionId}/disconnect`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    disconnectReasonId: disconnectReasonId,
                    graceMinutes: graceMinutes
                })
            });

            if (response.ok) {
                console.log('채팅 세션 이탈 처리 완료');
                this._graceUntil = new Date(Date.now() + graceMinutes * 60 * 1000);
                this.startDisconnectTimer();
            } else {
                console.error('채팅 세션 이탈 처리 실패');
            }
        } catch (error) {
            console.error('채팅 세션 이탈 처리 중 오류:', error);
        }
    }

    /**
     * 채팅 세션 재접속 처리
     */
    async handleReconnect() {
        if (!this.sessionId) {
            console.error('세션 ID가 없습니다.');
            return;
        }

        try {
            const response = await fetch(`/api/chat/sessions/${this.sessionId}/reconnect`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                console.log('채팅 세션 재접속 처리 완료');
                this._graceUntil = null;
                this.stopDisconnectTimer();
                this.showReconnectMessage();
            } else {
                console.error('채팅 세션 재접속 처리 실패');
            }
        } catch (error) {
            console.error('채팅 세션 재접속 처리 중 오류:', error);
        }
    }

    /**
     * 채팅 세션 종료 (종료 사유 포함)
     */
    async endSessionWithReason(exitReasonId) {
        if (!this.sessionId) {
            console.error('세션 ID가 없습니다.');
            return;
        }

        try {
            const response = await fetch(`/api/chat/sessions/${this.sessionId}/end-with-reason`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    exitReasonId: exitReasonId,
                    endedBy: 'M' // 고객이 종료
                })
            });

            if (response.ok) {
                console.log('채팅 세션 종료 완료');
                this._isSessionEnded = true;
                this.showEndSessionMessage();
            } else {
                console.error('채팅 세션 종료 실패');
            }
        } catch (error) {
            console.error('채팅 세션 종료 중 오류:', error);
        }
    }

    /**
     * 마지막 접속 시간 업데이트
     */
    async updateLastSeen(side = 'MEMBER') {
        if (!this.sessionId) {
            return;
        }

        try {
            await fetch(`/api/chat/sessions/${this.sessionId}/last-seen`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    side: side
                })
            });
        } catch (error) {
            console.error('마지막 접속 시간 업데이트 실패:', error);
        }
    }

    /**
     * 이탈 타이머 시작
     */
    startDisconnectTimer() {
        if (this._disconnectTimer) {
            clearInterval(this._disconnectTimer);
        }

        this._disconnectTimer = setInterval(() => {
            if (this._graceUntil && new Date() > this._graceUntil) {
                this.stopDisconnectTimer();
                this.showGracePeriodExpiredMessage();
            }
        }, 1000);
    }

    /**
     * 이탈 타이머 정지
     */
    stopDisconnectTimer() {
        if (this._disconnectTimer) {
            clearInterval(this._disconnectTimer);
            this._disconnectTimer = null;
        }
    }

    /**
     * 재접속 메시지 표시
     */
    showReconnectMessage() {
        this.addSystemMessage('고객이 다시 접속했습니다.');
    }

    /**
     * 상담 종료 메시지 표시
     */
    showEndSessionMessage() {
        this.addSystemMessage('상담이 종료되었습니다.');
        this.disableChatInput();
    }

    /**
     * 유예 기간 만료 메시지 표시
     */
    showGracePeriodExpiredMessage() {
        this.addSystemMessage('유예 기간이 만료되었습니다. 상담이 종료됩니다.');
        this._isSessionEnded = true;
        this.disableChatInput();
    }

    /**
     * 채팅 입력 비활성화
     */
    disableChatInput() {
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');
        
        if (messageInput) {
            messageInput.disabled = true;
            messageInput.placeholder = '상담이 종료되었습니다.';
        }
        
        if (sendBtn) {
            sendBtn.disabled = true;
        }
    }

    /**
     * 시스템 메시지 추가
     */
    addSystemMessage(message) {
        const chatMessages = document.getElementById('chatMessages');
        if (!chatMessages) return;

        const messageDiv = document.createElement('div');
        messageDiv.className = 'message system-message';
        messageDiv.innerHTML = `
            <div class="message-content">
                <span class="system-text">${message}</span>
            </div>
        `;

        chatMessages.appendChild(messageDiv);
        this.scrollToBottom();
    }

    /**
     * 종료 사유 선택 모달 표시
     */
    showExitReasonModal() {
        if (this.exitReasons.length === 0) {
            console.error('종료 사유 목록이 없습니다.');
            return;
        }

        const modal = document.createElement('div');
        modal.className = 'modal';
        modal.innerHTML = `
            <div class="modal-content">
                <h3>상담 종료 사유를 선택해주세요</h3>
                <div class="reason-list">
                    ${this.exitReasons.map(reason => `
                        <div class="reason-item" data-reason-id="${reason.codeId}">
                            <input type="radio" name="exitReason" id="reason_${reason.codeId}" value="${reason.codeId}">
                            <label for="reason_${reason.codeId}">${reason.decode}</label>
                        </div>
                    `).join('')}
                </div>
                <div class="modal-actions">
                    <button class="btn btn--secondary" onclick="this.closest('.modal').remove()">취소</button>
                    <button class="btn btn--primary" onclick="customerChat.confirmExitSession()">종료</button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        // 이벤트 리스너 추가
        modal.querySelectorAll('.reason-item').forEach(item => {
            item.addEventListener('click', () => {
                const radio = item.querySelector('input[type="radio"]');
                radio.checked = true;
            });
        });
    }

    /**
     * 상담 종료 확인
     */
    confirmExitSession() {
        const selectedReason = document.querySelector('input[name="exitReason"]:checked');
        if (!selectedReason) {
            alert('종료 사유를 선택해주세요.');
            return;
        }

        const exitReasonId = parseInt(selectedReason.value);
        this.endSessionWithReason(exitReasonId);
        
        // 모달 닫기
        const modal = document.querySelector('.modal');
        if (modal) {
            modal.remove();
        }
    }
}

// 로그인 멤버 정보 전달
document.addEventListener('DOMContentLoaded', function() {
  // HTML 요소의 data 속성에서 세션 정보 가져오기
  const rootElement = document.getElementById('root');
  console.log('HTML root 요소:', rootElement);
  
  if (rootElement) {
    const isLoggedIn = rootElement.getAttribute('data-s-is-logged-in') === 'true';
    console.log('로그인 상태:', isLoggedIn);
    
    if (isLoggedIn) {
      const loginMember = {
        memberId: rootElement.getAttribute('data-s-member-id'),
        email: rootElement.getAttribute('data-s-email'),
        nickname: rootElement.getAttribute('data-s-nickname'),
        gubun: rootElement.getAttribute('data-s-gubun')
      };
      
      console.log('HTML data 속성에서 가져온 loginMember:', loginMember);
      
      // memberId가 숫자인지 확인
      if (loginMember.memberId && !isNaN(loginMember.memberId)) {
        loginMember.memberId = parseInt(loginMember.memberId);
        console.log('memberId를 숫자로 변환:', loginMember.memberId);
        
        try {
          const jsonString = JSON.stringify(loginMember);
          console.log('JSON 문자열:', jsonString);
          sessionStorage.setItem('loginMember', jsonString);
          console.log('sessionStorage 저장 완료');
        } catch (error) {
          console.error('sessionStorage 저장 실패:', error);
        }
      } else {
        console.error('memberId가 유효하지 않습니다:', loginMember.memberId);
        sessionStorage.removeItem('loginMember');
      }
    } else {
      console.log('로그인되지 않은 상태입니다.');
      sessionStorage.removeItem('loginMember');
    }
  } else {
    console.error('root 요소를 찾을 수 없습니다.');
    sessionStorage.removeItem('loginMember');
  }
});

// 카테고리 선택 후 채팅 시작
function startChat(category) {
  console.log('=== startChat 함수 시작 ===');
  console.log('호출된 카테고리:', category);
  
  // sessionStorage 전체 확인
  console.log('sessionStorage 전체 내용:');
  for (let i = 0; i < sessionStorage.length; i++) {
    const key = sessionStorage.key(i);
    const value = sessionStorage.getItem(key);
    console.log(`${key}:`, value);
  }
  
  // 로그인 체크
  const loginMemberJson = sessionStorage.getItem('loginMember');
  console.log('sessionStorage에서 가져온 loginMember JSON:', loginMemberJson);
  
  let loginMember = null;
  try {
    loginMember = JSON.parse(loginMemberJson || 'null');
    console.log('파싱된 loginMember 객체:', loginMember);
  } catch (error) {
    console.error('JSON 파싱 실패:', error);
    alert('로그인 정보를 불러올 수 없습니다. 페이지를 새로고침해주세요.');
    return;
  }
  
  console.log('로그인 체크 결과:', {
    loginMember: loginMember,
    memberId: loginMember?.memberId,
    hasMemberId: !!loginMember?.memberId
  });
  
  if (!loginMember || !loginMember.memberId) {
    console.log('로그인 체크 실패 - alert 표시');
    alert('로그인이 필요합니다. 로그인 후 다시 시도해주세요.');
    window.location.href = '/member/login/loginForm';
    return;
  }
  
  console.log('로그인 체크 성공, memberId:', loginMember.memberId);
  
  // 카테고리 정보 저장
  sessionStorage.setItem('selectedCategory', category);
  console.log('카테고리 저장 완료:', category);
  
  // iframe 채팅창 생성 및 표시
  console.log('iframe 채팅창 생성 시도');
  createChatIframe();
  console.log('=== startChat 함수 종료 ===');
}

// iframe 채팅창 생성 함수
function createChatIframe() {
  // 기존 모달이 있으면 제거
  const existingModal = document.getElementById('chatModal');
  if (existingModal) {
    existingModal.remove();
  }
  
  // iframe 컨테이너 생성 (드래그 가능한 non-blocking)
  const iframeContainer = document.createElement('div');
  iframeContainer.id = 'chatModal';
  iframeContainer.style.cssText = `
    position: fixed;
    bottom: 20px;
    right: 20px;
    width: 500px;
    height: 600px;
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
  iframe.src = '/chat/popup';
  iframe.style.cssText = `
    width: 100%;
    height: 100%;
    border: none;
    border-radius: 10px;
    background: white;
  `;
  
  // iframe 추가 (닫기 버튼 없이)
  iframeContainer.appendChild(iframe);
  document.body.appendChild(iframeContainer);
  
  // iframe에서 오는 메시지 수신 (종료사유 선택 후 닫기, 드래그 이벤트)
  window.addEventListener('message', function(event) {
    if (event.data && event.data.type === 'closeChat') {
      console.log('iframe에서 채팅 종료 요청 받음');
      closeChatIframe();
    } else if (event.data && event.data.type === 'chatDragStart') {
      console.log('iframe에서 드래그 시작 요청 받음');
      // 드래그 시작 시 iframe 내부 클릭 이벤트 방지
      iframe.style.pointerEvents = 'none';
         } else if (event.data && event.data.type === 'chatDragMove') {
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
    } else if (event.data && event.data.type === 'chatDragEnd') {
      console.log('iframe에서 드래그 종료 요청 받음');
      // 드래그 종료 시 iframe 내부 클릭 이벤트 복원
      iframe.style.pointerEvents = 'auto';
    }
  });
  
  // 드래그 핸들을 통해서만 드래그 가능하므로 기존 드래그 기능 제거
}

// iframe 닫기 함수
function closeChatIframe() {
  const modal = document.getElementById('chatModal');
  if (modal) {
    modal.remove();
  }
}

// 드래그 핸들을 통해서만 드래그 가능하므로 기존 드래그 기능 제거

// iframe 방식 사용으로 인해 불필요한 함수들 제거
