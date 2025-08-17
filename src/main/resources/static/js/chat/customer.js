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
        
        // 카테고리 이름 매핑
        this.categoryNames = {
            'GENERAL': '일반 문의',
            'ORDER': '주문/결제',
            'DELIVERY': '배송',
            'RETURN': '반품/교환',
            'ACCOUNT': '회원/계정',
            'TECHNICAL': '기술지원'
        };
        
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
                this.endChat();
            });
        }

        // 페이지 이탈/숨김 시 presence를 서버에 알림 (sendBeacon)
        const notifyInactive = () => {
            try {
                if (!this.sessionId) return;
                const payload = {
                    side: 'MEMBER',
                    state: 'INACTIVE',
                    reason: 'PAGE_HIDE',
                    graceSeconds: 300
                };
                const blob = new Blob([JSON.stringify(payload)], { type: 'application/json' });
                navigator.sendBeacon(`/api/chat/sessions/${this.sessionId}/presence`, blob);
            } catch (e) { /* noop */ }
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
            
            // WebSocket 연결
            this.connectWebSocket();
            
            // 상담 종료 버튼 표시
            const endChatBtn = document.getElementById('endChatBtn');
            if (endChatBtn) {
                endChatBtn.style.display = 'flex';
            }
            
            // 재접속 presence 알림(상태 변경 없이 last_seen 갱신)
            try {
                const payload = { side: 'MEMBER', state: 'ACTIVE', reason: 'PAGE_SHOW' };
                await fetch(`/api/chat/sessions/${this.sessionId}/presence`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
            } catch (e) { /* noop */ }
            
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
            // 서버에서 FAQ 카테고리 목록 조회
            const response = await fetch('/api/faq/categories');
            if (!response.ok) throw new Error('FAQ 카테고리 조회 실패');
            
            const result = await response.json();
            if (result.code !== '00') throw new Error(result.message || 'FAQ 카테고리 조회 실패');
            
            const categories = result.data;
            const category = categories.find(cat => cat.code === categoryCode);
            
            if (!category) throw new Error(`카테고리를 찾을 수 없음: ${categoryCode}`);
            return category.codeId;
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
}

// 페이지 로드 시 채팅 초기화
document.addEventListener('DOMContentLoaded', () => {
    // 메시지 영역 초기화 (페이지 로드 시)
    const messageArea = document.getElementById('messageArea');
    if (messageArea) {
        messageArea.innerHTML = '';
    }
    
    new CustomerChat();
});
