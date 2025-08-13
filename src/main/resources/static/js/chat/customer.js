/**
 * 고객용 채팅 클래스 (WebSocket 기반)
 */
class CustomerChat {
    constructor() {
        this.sessionId = null;
        this.memberId = null;
        this.memberName = null;
        this.isSessionActive = false;
        this.stompClient = null;
        this.connected = false;
        
        this.init();
    }
    
    /**
     * 초기화
     */
    init() {
        // 로그인 체크
        if (!isCurrentUserLoggedIn()) {
            this.showLoginRequired();
            return;
        }
        
        this.memberId = getCurrentUserId();
        this.memberName = getCurrentUserNickname() || '고객';
        
        this.bindEvents();
        this.createChatSession();
    }
    
    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');
        const charCount = document.getElementById('charCount');
        
        // 전송 버튼 클릭
        sendBtn.addEventListener('click', () => {
            this.sendMessage();
        });
        
        // Enter 키 처리
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
            
            // 전송 버튼 활성화/비활성화
            sendBtn.disabled = count === 0 || !this.isSessionActive;
        });
        
        // 페이지 언로드 시 연결 해제
        window.addEventListener('beforeunload', () => {
            this.disconnect();
        });
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
     * 채팅 세션 생성
     */
    async createChatSession() {
        try {
            this.showLoading('채팅 세션을 생성하고 있습니다...');
            
            const response = await ajax.post('/api/chat/sessions', {
                memberId: this.memberId,
                categoryId: 1, // 일반 문의
                title: '1:1 상담 문의'
            });
            
            if (response && response.code === '00') {
                this.sessionId = response.data;
                this.loadMessageHistory();
                this.connectWebSocket();
                this.updateStatus('active', '상담 대기 중...');
                this.isSessionActive = true;
                this.enableInput();
                this.hideLoading();
            } else {
                throw new Error(response?.message || '채팅 세션 생성에 실패했습니다.');
            }
        } catch (error) {
            console.error('채팅 세션 생성 실패:', error);
            this.showError(error.message || '채팅 세션 생성에 실패했습니다.');
        }
    }
    
    /**
     * 채팅 세션 참가
     */
    joinChatSession() {
        if (!this.stompClient || !this.connected) return;
        
        const joinMessage = {
            sessionId: this.sessionId,
            senderId: this.memberId,
            senderType: 'M',
            senderName: this.memberName,
            content: '채팅 세션에 참가했습니다.',
            messageTypeId: 4 // 시스템 메시지
        };
        
        this.stompClient.send("/app/chat.addUser", {}, JSON.stringify(joinMessage));
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
        
        if (!content || !this.isSessionActive || !this.connected) return;
        
        const message = {
            sessionId: this.sessionId,
            senderId: this.memberId,
            senderType: 'M', // 고객
            content: content,
            senderName: this.memberName,
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
     * 메시지 읽음 처리
     */
    async markMessagesAsRead() {
        try {
            await ajax.post(`/api/chat/sessions/${this.sessionId}/read`, {
                receiverId: this.memberId
            });
        } catch (error) {
            console.error('메시지 읽음 처리 실패:', error);
        }
    }
    
    /**
     * 상태 업데이트
     */
    updateStatus(status, text) {
        const statusText = document.getElementById('statusText');
        const statusIndicator = document.getElementById('statusIndicator');
        
        statusText.textContent = text;
        statusIndicator.className = `status-indicator ${status}`;
    }
    
    /**
     * 입력 활성화
     */
    enableInput() {
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');
        
        messageInput.disabled = false;
        messageInput.placeholder = '메시지를 입력하세요... (Enter: 전송, Shift+Enter: 줄바꿈)';
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
     * 로딩 표시
     */
    showLoading(message) {
        const messageArea = document.getElementById('messageArea');
        messageArea.innerHTML = `<div class="loading">${message}</div>`;
    }
    
    /**
     * 로딩 숨김
     */
    hideLoading() {
        const messageArea = document.getElementById('messageArea');
        const loading = messageArea.querySelector('.loading');
        if (loading) {
            loading.remove();
        }
    }
    
    /**
     * 에러 표시
     */
    showError(message) {
        showToast(message, 'error');
    }
    
    /**
     * 로그인 필요 메시지
     */
    showLoginRequired() {
        showModal({
            title: '로그인 필요',
            message: '1:1 상담을 이용하려면 로그인이 필요합니다.\n로그인 페이지로 이동하시겠습니까?',
            onConfirm: () => {
                const returnUrl = encodeURIComponent(window.location.pathname);
                window.location.href = `/member/login?returnUrl=${returnUrl}`;
            },
            onCancel: () => {
                window.history.back();
            }
        });
    }
    
    /**
     * 연결 해제
     */
    disconnect() {
        if (this.stompClient && this.connected) {
            const leaveMessage = {
                sessionId: this.sessionId,
                senderId: this.memberId,
                senderType: 'M',
                senderName: this.memberName,
                content: '채팅 세션을 종료했습니다.',
                messageTypeId: 4 // 시스템 메시지
            };
            
            this.stompClient.send("/app/chat.removeUser", {}, JSON.stringify(leaveMessage));
            this.stompClient.disconnect();
            this.connected = false;
        }
    }
}

// 페이지 로드 시 채팅 초기화
document.addEventListener('DOMContentLoaded', () => {
    new CustomerChat();
});
