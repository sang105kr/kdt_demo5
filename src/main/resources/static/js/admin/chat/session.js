/**
 * 관리자용 채팅 세션 JavaScript
 */

class AdminChatSession {
    constructor() {
        // URL에서 세션 ID 추출 (URL 파라미터 또는 경로에서)
        this.sessionId = new URLSearchParams(window.location.search).get('sessionId');
        if (!this.sessionId) {
            // URL 파라미터에 없으면 경로에서 추출
            const pathParts = window.location.pathname.split('/');
            this.sessionId = pathParts[pathParts.length - 1];
        }
        
        this.adminId = getCurrentUserId();
        this.adminName = getCurrentUserNickname() || '상담원';
        this.connected = false;
        this.stompClient = null;
        this.sessionData = null;
        this.unreadCount = 0;
        this.ownUnreadSeq = 0;
        this._isPageVisible = true;
        this._isWindowFocused = true;
        this._isNearBottom = true;
        this._readCheckTimer = null;
        this._lastReadNotifyAt = 0;
        this._isSessionEnded = false; // 세션 종료 플래그 추가
        
        this.bindEvents();
        this.initializeVisibilityMonitoring();
        
        if (this.sessionId) {
            this.loadSessionData();
            this.connectWebSocket();
        } else {
            this.showError('세션 ID가 없습니다.');
        }
    }
    
    /**
     * 초기화
     */
    init() {
        // URL에서 세션 ID 추출
        const pathParts = window.location.pathname.split('/');
        this.sessionId = pathParts[pathParts.length - 1];
        
        if (!this.sessionId) {
            this.showError('세션 ID가 없습니다.');
            return;
        }
        
        this.adminId = getCurrentUserId();
        this.adminName = getCurrentUserNickname() || '상담원';
        
        this.bindEvents();
        this.loadSessionData();
        this.connectWebSocket();
    }
    
    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');
        const endSessionBtn = document.getElementById('endSessionBtn');
        const charCount = document.getElementById('charCount');
        
        // 전송 버튼 클릭
        if (sendBtn) {
            sendBtn.addEventListener('click', () => {
                this.sendMessage();
            });
        }
        
        // Enter 키 처리
        if (messageInput) {
            messageInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
            });
            
            // 문자 수 카운트
            messageInput.addEventListener('input', () => {
                const count = messageInput.value.length;
                charCount.textContent = `${count}/1000`;
                sendBtn.disabled = count === 0 || !this.connected;
            });
        }
        
        // 상담 종료 버튼
        if (endSessionBtn) {
            endSessionBtn.addEventListener('click', () => {
                this.endSession();
            });
        }
        
        // 페이지 이탈 시 presence INACTIVE 전송 (일시 이탈)
        const notifyInactive = () => {
            try {
                // 세션이 종료된 경우 presence 업데이트 하지 않음
                if (this._isSessionEnded) return;
                
                if (!this.sessionId || !this.adminId) return;
                const payload = { side: 'ADMIN', state: 'INACTIVE', reason: 'PAGE_HIDE', graceSeconds: 300 };
                const blob = new Blob([JSON.stringify(payload)], { type: 'application/json' });
                navigator.sendBeacon(`/api/chat/sessions/${this.sessionId}/presence`, blob);
            } catch (e) {}
        };
        // 중복 전송 방지 플래그
        this._presenceHiddenSent = false;
        this._presenceVisibleSentAt = 0;

        window.addEventListener('pagehide', () => {
            // 세션이 종료된 경우 presence 업데이트 하지 않음
            if (this._isSessionEnded) return;
            
            if (!this._presenceHiddenSent) {
                notifyInactive();
                this._presenceHiddenSent = true;
            }
        });
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'hidden') {
                if (!this._presenceHiddenSent) {
                    notifyInactive();
                    this._presenceHiddenSent = true;
                }
            } else if (document.visibilityState === 'visible') {
                // 복귀 시 ACTIVE presence 전송 (1.5초 쓰로틀)
                const now = Date.now();
                if (!this._presenceVisibleSentAt || (now - this._presenceVisibleSentAt) > 1500) {
                    try {
                        // 세션이 종료된 경우 presence 업데이트 하지 않음
                        if (this._isSessionEnded) return;
                        
                        if (!this.sessionId || !this.adminId) return;
                        const payload = { side: 'ADMIN', state: 'ACTIVE', reason: 'PAGE_SHOW' };
                        fetch(`/api/chat/sessions/${this.sessionId}/presence`, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(payload)
                        });
                        this._presenceVisibleSentAt = now;
                        this._presenceHiddenSent = false;
                    } catch (e) {}
                }
            }
        });
        window.addEventListener('beforeunload', () => {
            // 세션이 종료된 경우 presence 업데이트 하지 않음
            if (this._isSessionEnded) return;
            
            if (!this._presenceHiddenSent) {
                notifyInactive();
                this._presenceHiddenSent = true;
            }
        });
    }
    
    /**
     * 세션 데이터 로드
     */
    async loadSessionData() {
        try {
            console.log('세션 데이터 로드 시작:', this.sessionId);
            const response = await ajax.get(`/api/chat/sessions/${this.sessionId}`);
            
            console.log('API 응답:', response);
            
            if (response && response.code === '00') {
                this.sessionData = response.data;
                console.log('세션 데이터:', this.sessionData);
                this.updateSessionInfo();
                this.loadMessageHistory();
            } else {
                throw new Error('세션 정보를 불러올 수 없습니다.');
            }
        } catch (error) {
            console.error('세션 데이터 로드 실패:', error);
            
            // 오류 발생 시에도 로딩 표시 제거
            const loadingElement = document.getElementById('loadingMessages');
            if (loadingElement) {
                loadingElement.style.display = 'none';
            }
            
            // API 오류 시에도 기본 정보는 표시
            this.displayDefaultSessionInfo();
            this.showError('세션 정보를 불러오는데 실패했습니다. 기본 정보를 표시합니다.');
        }
    }
    
    /**
     * WebSocket 연결
     */
    connectWebSocket() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('WebSocket 연결 성공:', frame);
            this.connected = true;
            this.subscribeToChat();
            this.joinChatSession();
            this.enableInput();

            // presence ACTIVE로 복귀 알림
            try {
                // 세션이 종료된 경우 presence 업데이트 하지 않음
                if (!this._isSessionEnded) {
                    const payload = { side: 'ADMIN', state: 'ACTIVE', reason: 'PAGE_SHOW' };
                    fetch(`/api/chat/sessions/${this.sessionId}/presence`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    });
                }
            } catch (e) {}

            // 미읽음 배지 초기화
            this.unreadCount = 0;
            const headerMeta = document.querySelector('.session-meta');
            if (headerMeta && !document.getElementById('adminUnreadCounter')) {
                const badge = document.createElement('span');
                badge.id = 'adminUnreadCounter';
                badge.className = 'unread-badge';
                badge.style.marginLeft = '8px';
                headerMeta.appendChild(badge);
            }
            this.renderUnreadBadge();
        }, (error) => {
            console.error('WebSocket 연결 실패:', error);
            this.showError('실시간 연결에 실패했습니다. 페이지를 새로고침해주세요.');
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
        
        this.stompClient.subscribe(`/topic/chat/${this.sessionId}`, (message) => {
            const chatMessage = JSON.parse(message.body);
            
            // 읽음 이벤트 처리
            if (chatMessage.content === 'READ_EVENT') {
                this.handleReadEvent(chatMessage);
                return;
            }
            
            this.displayMessage(chatMessage);
            this.scrollToBottom();

            // 미읽음 카운트 업데이트: 고객 메시지이고 즉시 읽음 조건을 만족하지 않으면 +1
            if (chatMessage.senderType === 'M') {
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
    
    /**
     * 채팅 세션 참가
     */
    joinChatSession() {
        if (!this.stompClient || !this.connected) return;
        // 중복 시스템 메시지 방지를 위해 관리자의 참가 시스템 메시지는 전송하지 않음
        // presence ACTIVE 전송과 서버측 복귀 메시지로 충분히 인지 가능
    }
    
    /**
     * 세션 정보 업데이트
     */
    updateSessionInfo() {
        if (!this.sessionData) {
            console.log('세션 데이터가 없습니다.');
            // 임시 데이터로 기본 정보 표시
            this.displayDefaultSessionInfo();
            return;
        }
        
        console.log('세션 정보 업데이트 시작:', this.sessionData);
        
        // 헤더 정보 업데이트
        document.getElementById('sessionTitle').textContent = this.sessionData.title || '상담 세션';
        document.getElementById('customerName').textContent = this.sessionData.memberName || '고객';
        // 상태명은 서버에서 내려준 decode가 가장 정확함
        document.getElementById('sessionStatus').textContent = this.sessionData.statusName || this.getStatusText(this.sessionData.statusId);
        document.getElementById('sessionTime').textContent = this.formatTime(this.sessionData.startTime);
        
        // 사이드바 정보 업데이트 (API에서 데이터가 없으면 기본값 사용)
        const memberName = this.sessionData.memberName && this.sessionData.memberName !== '고객' ? this.sessionData.memberName : '테스트 고객';
        const memberEmail = this.sessionData.memberEmail || 'test@example.com';
        const memberPhone = this.sessionData.memberPhone || '010-1234-5678';
        
        document.getElementById('sidebarCustomerName').textContent = memberName;
        document.getElementById('sidebarCustomerEmail').textContent = memberEmail;
        document.getElementById('sidebarCustomerPhone').textContent = memberPhone;
        document.getElementById('sidebarStartTime').textContent = this.formatDateTime(this.sessionData.startTime);
        document.getElementById('sidebarMessageCount').textContent = this.sessionData.messageCount || '0';
        document.getElementById('sidebarAdminName').textContent = this.adminName;
        
        console.log('사이드바 업데이트 완료 - 고객명:', memberName, '이메일:', memberEmail, '전화:', memberPhone);
    }
    
    /**
     * 기본 세션 정보 표시 (API 오류 시 사용)
     */
    displayDefaultSessionInfo() {
        // 로딩 표시 제거
        const loadingElement = document.getElementById('loadingMessages');
        if (loadingElement) {
            loadingElement.style.display = 'none';
        }
        
        // 헤더 정보 업데이트
        document.getElementById('sessionTitle').textContent = '상담 세션';
        document.getElementById('customerName').textContent = '테스트 고객';
        document.getElementById('sessionStatus').textContent = '대기 중';
        document.getElementById('sessionTime').textContent = this.formatTime(new Date());
        
        // 사이드바 정보 업데이트
        document.getElementById('sidebarCustomerName').textContent = '테스트 고객';
        document.getElementById('sidebarCustomerEmail').textContent = 'test@example.com';
        document.getElementById('sidebarCustomerPhone').textContent = '010-1234-5678';
        document.getElementById('sidebarStartTime').textContent = this.formatDateTime(new Date());
        document.getElementById('sidebarMessageCount').textContent = '0';
        document.getElementById('sidebarAdminName').textContent = this.adminName;
        
        console.log('기본 세션 정보 표시 완료');
    }
    
    /**
     * 메시지 히스토리 로드
     */
    async loadMessageHistory() {
        try {
            const response = await ajax.get(`/api/chat/sessions/${this.sessionId}/messages`);
            
            // 로딩 표시 제거 (메시지가 있든 없든)
            const loadingElement = document.getElementById('loadingMessages');
            if (loadingElement) {
                loadingElement.style.display = 'none';
            }
            
            if (response && response.code === '00') {
                const messages = response.data;
                if (messages && messages.length > 0) {
                    messages.forEach(message => {
                        this.displayMessage(message);
                    });
                    this.scrollToBottom();
                } else {
                    console.log('메시지 히스토리가 없습니다.');
                }
            }
        } catch (error) {
            console.error('메시지 히스토리 로드 실패:', error);
            
            // 오류 발생 시에도 로딩 표시 제거
            const loadingElement = document.getElementById('loadingMessages');
            if (loadingElement) {
                loadingElement.style.display = 'none';
            }
        }
    }
    
    /**
     * 메시지 전송
     */
    sendMessage() {
        const input = document.getElementById('messageInput');
        const content = input.value.trim();
        
        if (!content || !this.connected) return;
        
        const message = {
            sessionId: this.sessionId,
            senderId: this.adminId,
            senderType: 'A', // 관리자
            content: content,
            senderName: this.adminName,
            messageTypeId: 1 // 일반 메시지
        };
        
        // WebSocket으로 메시지 전송
        this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
        
        // 입력창 초기화
        input.value = '';
        document.getElementById('charCount').textContent = '0/1000';
        document.getElementById('sendBtn').disabled = true;
    }
    
    /**
     * 메시지 표시
     */
    displayMessage(message) {
        const messageArea = document.getElementById('messageArea');
        
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
            const isMyMessage = message.senderType === 'A';
            
            // 카카오톡 방식: 내 메시지에만 읽음 숫자 표시
            let readBadge = '';
            if (isMyMessage) {
                // 상대방이 읽지 않았으면 누적 숫자 표시
                if (message.isRead !== 'Y') {
                    this.ownUnreadSeq = (this.ownUnreadSeq || 0) + 1;
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

        // 일시 이탈 / 복귀 시스템 메시지 감지
        if (message.senderType !== 'M' && message.content) {
            if (message.content.includes('잠시 이탈')) {
                const statusEl = document.getElementById('sessionStatus');
                if (statusEl) statusEl.textContent = '일시 이탈';
            } else if (message.content.includes('다시 접속')) {
                const statusEl = document.getElementById('sessionStatus');
                if (statusEl) statusEl.textContent = '진행 중';
            }
        }

        // 고객의 상담 종료 메시지 감지
        if (message.senderType === 'M' && message.content && 
            (message.content.includes('고객이 상담을 종료했습니다') || 
             message.content.includes('상담을 종료했습니다'))) {
            
            console.log('고객이 상담을 종료했습니다. 대시보드로 이동합니다.');
            
            // 입력 필드 비활성화
            this.disableInput();
            
            // 상태 업데이트
            const statusEl = document.getElementById('sessionStatus');
            if (statusEl) statusEl.textContent = '고객이 상담을 종료했습니다';
            
            // 3초 후 대시보드로 자동 이동
            setTimeout(() => {
                this.showSuccess('고객이 상담을 종료했습니다. 대시보드로 이동합니다.');
                
                // WebSocket 연결 해제
                if (this.stompClient) {
                    this.stompClient.disconnect();
                }
                
                // 대시보드로 이동
                window.location.href = '/admin/chat/dashboard';
            }, 3000);
        }
        
        // 백엔드 시스템 메시지로 전송되는 상담 종료 메시지 감지
        if (message.senderType === 'S' && message.content && 
            message.content.includes('상담이 종료되었습니다')) {
            
            console.log('시스템: 상담이 종료되었습니다. 대시보드로 이동합니다.');
            
            // 상담 종료 플래그 설정 (presence 업데이트 방지)
            this._isSessionEnded = true;
            
            // 입력 필드 비활성화
            this.disableInput();
            
            // 상태 업데이트
            const statusEl = document.getElementById('sessionStatus');
            if (statusEl) statusEl.textContent = '상담 종료';
            
            // 즉시 대시보드로 이동
            setTimeout(() => {
                this.showSuccess('상담이 종료되었습니다. 대시보드로 이동합니다.');
                
                // WebSocket 연결 해제
                if (this.stompClient) {
                    this.stompClient.disconnect();
                }
                
                // 대시보드로 이동
                window.location.href = '/admin/chat/dashboard';
            }, 1000);
        }
    }
    
    /**
     * 상담 종료
     */
    async endSession() {
        if (!confirm('상담을 종료하시겠습니까?')) return;
        
        try {
            // 세션 종료 플래그 설정 (presence 업데이트 방지)
            this._isSessionEnded = true;
            
            // 입력 필드 비활성화 (즉시)
            this.disableInput();
            
            // API 호출로 세션 종료 (백엔드에서 시스템 메시지도 함께 전송)
            const response = await ajax.post(`/api/chat/sessions/${this.sessionId}/end`);
            
            if (response && response.code === '00') {
                this.showSuccess('상담이 종료되었습니다.');
                
                // WebSocket 연결 해제 후 페이지 이동
                this.disconnect();
                
                // 연결 해제 완료 후 페이지 이동 (안전한 방법)
                setTimeout(() => {
                    try {
                        console.log('대시보드로 이동 시도...');
                        window.location.href = '/admin/chat/dashboard';
                    } catch (error) {
                        console.error('페이지 이동 실패:', error);
                        // 대체 방법으로 이동
                        window.location.replace('/admin/chat/dashboard');
                    }
                }, 1000); // 1초 대기
            } else {
                throw new Error(response?.message || '상담 종료에 실패했습니다.');
            }
        } catch (error) {
            console.error('상담 종료 실패:', error);
            this.showError(error.message || '상담 종료에 실패했습니다.');
            
            // 오류 발생 시 세션 종료 플래그 해제 및 입력 필드 다시 활성화
            this._isSessionEnded = false;
            this.enableInput();
        }
    }
    
    /**
     * 입력 활성화
     */
    enableInput() {
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');
        
        if (messageInput) {
            messageInput.disabled = false;
            messageInput.placeholder = '메시지를 입력하세요... (Enter: 전송, Shift+Enter: 줄바꿈)';
        }
        
        if (sendBtn) {
            sendBtn.disabled = false;
        }
    }
    
    /**
     * 입력 비활성화
     */
    disableInput() {
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');
        const endSessionBtn = document.getElementById('endSessionBtn');
        
        if (messageInput) {
            messageInput.disabled = true;
            messageInput.placeholder = '상담이 종료되었습니다.';
        }
        
        if (sendBtn) {
            sendBtn.disabled = true;
        }
        
        if (endSessionBtn) {
            endSessionBtn.disabled = true;
            endSessionBtn.textContent = '상담 종료';
        }
    }

    /**
     * 카카오톡 방식 읽음 처리
     */
    markAllAsRead() {
        if (!this.sessionId || this.unreadCount <= 0) return;
        
        try {
            // REST API로 읽음 처리
            ajax.post(`/api/chat/sessions/${this.sessionId}/read`, { receiverId: this.adminId })
                .then(response => {
                    if (response && response.code === '00') {
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
                    senderId: this.adminId,
                    senderType: 'A'
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
            // 고객이 읽었을 때만 처리 (내가 보낸 메시지의 읽음 상태 업데이트)
            if (message.senderType === 'M') {
                // 내가 보낸 메시지들의 모든 읽음 배지 제거 (카카오톡 방식)
                const items = Array.from(document.querySelectorAll('#messageArea .message.admin .read-badge.unread'));
                items.forEach(el => { 
                    el.classList.remove('unread'); 
                    el.textContent = ''; 
                });
                
                // 누적 시퀀스 리셋
                this.ownUnreadSeq = 0;
                
                console.log('고객이 메시지를 읽었습니다. 모든 읽음 표시가 제거되었습니다.');
            }
        } catch (e) {
            console.error('읽음 이벤트 처리 실패:', e);
        }
    }

    renderUnreadBadge() {
        const badge = document.getElementById('adminUnreadCounter');
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
     * 스크롤을 맨 아래로
     */
    scrollToBottom() {
        const messageArea = document.getElementById('messageArea');
        messageArea.scrollTop = messageArea.scrollHeight;
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
     * 상태 텍스트 반환
     */
    getStatusText(statusId) {
        // 하드코딩된 값 제거 - 서버에서 statusName을 받아서 사용
        return '알 수 없음';
    }
    
    /**
     * 시간 포맷팅
     */
    formatTime(dateTime) {
        if (!dateTime) return '-';
        return new Date(dateTime).toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }
    
    /**
     * 날짜시간 포맷팅
     */
    formatDateTime(dateTime) {
        if (!dateTime) return '-';
        return new Date(dateTime).toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
    
    /**
     * 연결 해제
     */
    disconnect() {
        if (this.stompClient && this.connected) {
            // 세션이 이미 endSession API로 완료 상태로 변경되었으므로 
            // 추가적인 removeUser 메시지 전송은 하지 않음
            this.stompClient.disconnect();
            this.connected = false;
        }
    }
    
    /**
     * 성공 메시지 표시
     */
    showSuccess(message) {
        showToast(message, 'success');
    }
    
    /**
     * 에러 메시지 표시
     */
    showError(message) {
        showToast(message, 'error');
    }
}

/**
 * 빠른 응답 전송
 */
function sendQuickReply(content) {
    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
        messageInput.value = content;
        messageInput.dispatchEvent(new Event('input'));
        document.getElementById('sendBtn').disabled = false;
    }
}

// 페이지 로드 시 채팅 세션 초기화
document.addEventListener('DOMContentLoaded', () => {
    new AdminChatSession();
});
