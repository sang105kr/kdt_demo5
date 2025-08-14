/**
 * 관리자용 채팅 세션 JavaScript
 */

class AdminChatSession {
    constructor() {
        this.sessionId = null;
        this.adminId = null;
        this.adminName = null;
        this.stompClient = null;
        this.connected = false;
        this.sessionData = null;
        
        this.init();
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
        
        // 페이지 언로드 시 연결 해제
        window.addEventListener('beforeunload', () => {
            this.disconnect();
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
        }, (error) => {
            console.error('WebSocket 연결 실패:', error);
            this.showError('실시간 연결에 실패했습니다. 페이지를 새로고침해주세요.');
        });
    }
    
    /**
     * 채팅 토픽 구독
     */
    subscribeToChat() {
        if (!this.sessionId) return;
        
        this.stompClient.subscribe(`/topic/chat/${this.sessionId}`, (message) => {
            const chatMessage = JSON.parse(message.body);
            this.displayMessage(chatMessage);
            this.scrollToBottom();
        });
    }
    
    /**
     * 채팅 세션 참가
     */
    joinChatSession() {
        if (!this.stompClient || !this.connected) return;
        
        const joinMessage = {
            sessionId: this.sessionId,
            senderId: this.adminId,
            senderType: 'A', // 관리자
            senderName: this.adminName,
            content: '상담원이 상담에 참가했습니다.',
            messageTypeId: 4 // 시스템 메시지
        };
        
        this.stompClient.send("/app/chat.addUser", {}, JSON.stringify(joinMessage));
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
        document.getElementById('sessionStatus').textContent = this.getStatusText(this.sessionData.statusId);
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
            
            if (response && response.code === '00') {
                const messages = response.data;
                messages.forEach(message => {
                    this.displayMessage(message);
                });
                
                this.scrollToBottom();
            }
        } catch (error) {
            console.error('메시지 히스토리 로드 실패:', error);
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
        const loadingElement = document.getElementById('loadingMessages');
        
        // 로딩 표시 제거
        if (loadingElement) {
            loadingElement.style.display = 'none';
        }
        
        const messageDiv = document.createElement('div');
        
        // 메시지 타입에 따른 클래스 설정
        let messageClass = '';
        if (message.senderType === 'M') {
            messageClass = 'customer';
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
        
        // 메시지 HTML 구성
        if (messageClass === 'system') {
            messageDiv.innerHTML = `
                <div class="message-content">
                    <div class="message-text">${this.escapeHtml(message.content)}</div>
                </div>
            `;
        } else {
            messageDiv.innerHTML = `
                <div class="message-content">
                    <div class="message-info">
                        <span class="sender-name">${this.escapeHtml(message.senderName)}</span>
                    </div>
                    <div class="message-text">${this.escapeHtml(message.content)}</div>
                    <div class="message-time">${timeString}</div>
                </div>
            `;
        }
        
        messageArea.appendChild(messageDiv);
        this.scrollToBottom();
    }
    
    /**
     * 상담 종료
     */
    async endSession() {
        if (!confirm('상담을 종료하시겠습니까?')) return;
        
        try {
            // 입력 필드 비활성화 (즉시)
            this.disableInput();
            
            // 상담 종료 메시지 전송
            if (this.stompClient && this.connected) {
                const endMessage = {
                    sessionId: this.sessionId,
                    senderId: this.adminId,
                    senderType: 'A',
                    senderName: this.adminName,
                    content: '상담이 종료되었습니다.',
                    messageTypeId: 4 // 시스템 메시지
                };
                
                this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(endMessage));
            }
            
            // API 호출로 세션 종료
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
            
            // 오류 발생 시 입력 필드 다시 활성화
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
            endSessionBtn.textContent = '상담 종료됨';
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
        switch (statusId) {
            case 1: return '대기 중';
            case 2: return '진행 중';
            case 3: return '완료';
            default: return '알 수 없음';
        }
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
            const leaveMessage = {
                sessionId: this.sessionId,
                senderId: this.adminId,
                senderType: 'A',
                senderName: this.adminName,
                content: '상담원이 상담을 종료했습니다.',
                messageTypeId: 4 // 시스템 메시지
            };
            
            this.stompClient.send("/app/chat.removeUser", {}, JSON.stringify(leaveMessage));
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
