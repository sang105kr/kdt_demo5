/**
 * 고객용 채팅 클래스 (WebSocket 기반)
 */
class CustomerChat {
    constructor() {
        this.stompClient = null;
        this.sessionId = null;
        this.selectedCategory = null;
        this.categoryNames = {
            'GENERAL': '일반 문의',
            'ORDER': '주문/결제',
            'DELIVERY': '배송',
            'RETURN': '반품/교환',
            'ACCOUNT': '회원/계정',
            'TECHNICAL': '기술지원'
        };
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.showCategorySelection();
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

        // 페이지 언로드 시 연결 해제
        window.addEventListener('beforeunload', () => {
            this.disconnect();
        });
    }

    showCategorySelection() {
        document.getElementById('categorySelection').style.display = 'block';
        document.getElementById('chatContainer').style.display = 'none';
        
        // 메시지 영역 초기화 (기존 메시지 모두 제거)
        const messageArea = document.getElementById('messageArea');
        messageArea.innerHTML = '';
        
        // 기존 연결 해제
        if (this.stompClient) {
            this.disconnect();
        }
        
        // 선택된 카테고리 초기화
        this.selectedCategory = null;
        this.sessionId = null;
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
            
            // 입력 필드 초기화
            const messageInput = document.getElementById('messageInput');
            messageInput.value = '';
            this.updateCharCount('');
            this.toggleSendButton('');
            
            // 채팅 세션 생성
            await this.createChatSession();
            
            // WebSocket 연결
            this.connectWebSocket();
            
            // 상담 종료 버튼 표시
            const endChatBtn = document.getElementById('endChatBtn');
            if (endChatBtn) {
                endChatBtn.style.display = 'flex';
            }
            
        } catch (error) {
            console.error('채팅 시작 실패:', error);
            alert('채팅을 시작할 수 없습니다. 다시 시도해주세요.');
            this.showCategorySelection();
        }
    }

    async createChatSession() {
        try {
            const response = await fetch('/api/chat/session', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    categoryId: this.getCategoryId(this.selectedCategory),
                    title: `${this.categoryNames[this.selectedCategory]} - 1:1 상담`
                })
            });

            if (!response.ok) {
                throw new Error('채팅 세션 생성 실패');
            }

            const data = await response.json();
            this.sessionId = data.sessionId;
            
            console.log('채팅 세션 생성됨:', this.sessionId);
            
        } catch (error) {
            console.error('채팅 세션 생성 오류:', error);
            throw error;
        }
    }

    getCategoryId(categoryCode) {
        // FAQ_CATEGORY 코드 ID 매핑
        const categoryMap = {
            'GENERAL': 1,
            'ORDER': 2,
            'DELIVERY': 3,
            'RETURN': 4,
            'ACCOUNT': 5,
            'TECHNICAL': 6
        };
        return categoryMap[categoryCode] || 1;
    }

    connectWebSocket() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('WebSocket 연결됨:', frame);
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
            this.updateStatus('연결 실패', 'disconnected');
            
            // 연결 실패 시 입력 필드 비활성화
            const sendBtn = document.getElementById('sendBtn');
            sendBtn.disabled = true;
        });
    }

    subscribeToChat() {
        if (!this.sessionId) return;
        
        this.stompClient.subscribe(`/topic/chat/${this.sessionId}`, (message) => {
            const chatMessage = JSON.parse(message.body);
            this.displayMessage(chatMessage);
        });
    }

    joinChatSession() {
        if (!this.sessionId || !this.stompClient) return;
        
        const joinMessage = {
            sessionId: this.sessionId,
            senderId: this.getCurrentUserId(),
            senderName: this.getCurrentUserName(),
            senderType: 'M',
            messageTypeId: 4, // 시스템 메시지
            content: '상담에 참가했습니다.'
        };
        
        this.stompClient.send('/app/chat.addUser', {}, JSON.stringify(joinMessage));
    }

    sendMessage() {
        const messageInput = document.getElementById('messageInput');
        const content = messageInput.value.trim();
        
        if (!content || !this.sessionId || !this.stompClient) return;
        
        const message = {
            sessionId: this.sessionId,
            senderId: this.getCurrentUserId(),
            senderName: this.getCurrentUserName(),
            senderType: 'M',
            messageTypeId: 1, // 일반 메시지
            content: content
        };
        
        this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
        messageInput.value = '';
        this.updateCharCount('');
        this.toggleSendButton('');
    }

    displayMessage(message) {
        const messageArea = document.getElementById('messageArea');
        const messageDiv = document.createElement('div');
        
        const messageClass = message.senderType === 'M' ? 'user' : 
                           message.senderType === 'A' ? 'admin' : 'system';
        
        const time = message.timestamp ? new Date(message.timestamp).toLocaleTimeString() : 
                    new Date().toLocaleTimeString();
        
        messageDiv.className = `message ${messageClass}`;
        messageDiv.innerHTML = `
            <div class="message-info">
                <span class="message-name">${message.senderName}</span>
                <span class="message-time">${time}</span>
            </div>
            <div class="message-content">${this.escapeHtml(message.content)}</div>
        `;
        
        messageArea.appendChild(messageDiv);
        messageArea.scrollTop = messageArea.scrollHeight;
        
        // 상담 종료 메시지 감지 및 처리
        if (message.content && (
            message.content.includes('상담이 종료되었습니다') || 
            message.content.includes('관리자1님이 상담을 종료했습니다')
        )) {
            this.handleSessionEnd();
        }
        
        // 메시지 표시 후 입력 필드 상태 업데이트
        const messageInput = document.getElementById('messageInput');
        this.updateCharCount(messageInput.value);
        this.toggleSendButton(messageInput.value.trim());
    }

    updateStatus(status, className) {
        const statusText = document.getElementById('statusText');
        const statusIndicator = document.getElementById('statusIndicator');
        
        statusText.textContent = status;
        statusIndicator.className = `status-indicator ${className}`;
    }

    updateCharCount(value) {
        const charCount = document.getElementById('charCount');
        charCount.textContent = `${value.length}/1000`;
    }

    toggleSendButton(value) {
        const sendBtn = document.getElementById('sendBtn');
        // 세션이 있고 내용이 있을 때만 활성화
        sendBtn.disabled = !value || !this.sessionId || !this.stompClient;
    }

    /**
     * 상담 종료 처리
     */
    handleSessionEnd() {
        // 상태를 종료로 변경
        this.updateStatus('상담 종료됨', 'disconnected');
        
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
        
        // WebSocket 연결 해제
        this.disconnect();
        
        // 3초 후 카테고리 선택 화면으로 돌아가기
        setTimeout(() => {
            this.showCategorySelection();
        }, 3000);
    }

    /**
     * 고객이 상담 종료 요청
     */
    async endChat() {
        if (!confirm('상담을 종료하시겠습니까?\n종료 후에는 다시 상담을 시작할 수 있습니다.')) {
            return;
        }

        try {
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
                endChatBtn.textContent = '상담 종료됨';
            }

            // WebSocket 연결 해제
            this.disconnect();
            
            // 2초 후 카테고리 선택 화면으로 이동
            setTimeout(() => {
                this.showCategorySelection();
            }, 2000);

        } catch (error) {
            console.error('상담 종료 실패:', error);
            alert('상담 종료에 실패했습니다. 다시 시도해주세요.');
        }
    }

    disconnect() {
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
            
            this.stompClient.send('/app/chat.removeUser', {}, JSON.stringify(leaveMessage));
            
            // WebSocket 연결 해제
            this.stompClient.disconnect();
        }
    }

    getCurrentUserId() {
        // 세션에서 사용자 ID 가져오기 (실제 구현에서는 서버에서 제공)
        return 1; // 임시 값
    }

    getCurrentUserName() {
        // 세션에서 사용자 이름 가져오기 (실제 구현에서는 서버에서 제공)
        return '테스터1'; // 임시 값
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
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
