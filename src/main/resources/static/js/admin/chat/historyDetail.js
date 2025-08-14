/**
 * 상담 히스토리 상세보기 JavaScript
 */
class ChatHistoryDetail {
    constructor() {
        this.sessionId = null;
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
        
        console.log('URL 경로:', window.location.pathname);
        console.log('추출된 세션 ID:', this.sessionId);
        
        if (!this.sessionId) {
            this.showError('세션 ID가 없습니다.');
            this.displayErrorState();
            return;
        }
        
        // 세션 ID가 유효한지 확인
        if (this.sessionId === 'history' || this.sessionId.length < 10) {
            this.showError('유효하지 않은 세션 ID입니다.');
            this.displayErrorState();
            return;
        }
        
        this.loadSessionData();
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
            this.showError('세션 정보를 불러오는데 실패했습니다.');
            this.displayErrorState();
        }
    }
    
    /**
     * 세션 정보 업데이트
     */
    updateSessionInfo() {
        if (!this.sessionData) {
            console.log('세션 데이터가 없습니다.');
            return;
        }
        
        console.log('세션 정보 업데이트 시작:', this.sessionData);
        
        // 헤더 정보 업데이트
        document.getElementById('sessionTitle').textContent = this.sessionData.title || '상담 히스토리';
        document.getElementById('customerName').textContent = this.sessionData.memberName || '고객';
        document.getElementById('sessionTime').textContent = this.formatDateTime(this.sessionData.startTime);
        
        // 사이드바 정보 업데이트
        const memberName = this.sessionData.memberName && this.sessionData.memberName !== '고객' ? this.sessionData.memberName : '테스트 고객';
        const memberEmail = this.sessionData.memberEmail || 'test@example.com';
        const memberPhone = this.sessionData.memberPhone || '010-1234-5678';
        
        document.getElementById('sidebarCustomerName').textContent = memberName;
        document.getElementById('sidebarCustomerEmail').textContent = memberEmail;
        document.getElementById('sidebarCustomerPhone').textContent = memberPhone;
        document.getElementById('sidebarStartTime').textContent = this.formatDateTime(this.sessionData.startTime);
        document.getElementById('sidebarEndTime').textContent = this.formatDateTime(this.sessionData.endTime);
        document.getElementById('sidebarMessageCount').textContent = this.sessionData.messageCount || '0';
        document.getElementById('sidebarAdminName').textContent = this.sessionData.adminName || '상담원';
        document.getElementById('sidebarCategoryName').textContent = this.sessionData.categoryName || '일반 문의';
        
        console.log('사이드바 업데이트 완료');
    }
    
    /**
     * 메시지 히스토리 로드
     */
    async loadMessageHistory() {
        try {
            const response = await ajax.get(`/api/chat/sessions/${this.sessionId}/messages`);
            
            if (response && response.code === '00') {
                const messages = response.data;
                console.log('메시지 히스토리:', messages);
                
                // 로딩 표시 제거
                const loadingElement = document.getElementById('loadingMessages');
                if (loadingElement) {
                    loadingElement.style.display = 'none';
                }
                
                // 메시지가 없으면 안내 메시지 표시
                if (messages.length === 0) {
                    this.displayNoMessages();
                } else {
                    messages.forEach(message => {
                        this.displayMessage(message);
                    });
                }
                
                this.scrollToBottom();
            } else {
                throw new Error('메시지 히스토리를 불러올 수 없습니다.');
            }
        } catch (error) {
            console.error('메시지 히스토리 로드 실패:', error);
            this.showError('메시지 히스토리를 불러오는데 실패했습니다.');
        }
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
                        <span class="message-name">${this.escapeHtml(message.senderName)}</span>
                        <span class="message-time">${timeString}</span>
                    </div>
                    <div class="message-text">${this.escapeHtml(message.content)}</div>
                </div>
            `;
        }
        
        messageArea.appendChild(messageDiv);
    }
    
    /**
     * 메시지가 없을 때 표시
     */
    displayNoMessages() {
        const messageArea = document.getElementById('messageArea');
        const noMessageDiv = document.createElement('div');
        noMessageDiv.className = 'message system';
        noMessageDiv.innerHTML = `
            <div class="message-content">
                <div class="message-text">이 상담에는 메시지가 없습니다.</div>
            </div>
        `;
        messageArea.appendChild(noMessageDiv);
    }
    
    /**
     * 오류 상태 표시
     */
    displayErrorState() {
        // 로딩 표시 제거
        const loadingElement = document.getElementById('loadingMessages');
        if (loadingElement) {
            loadingElement.style.display = 'none';
        }
        
        const messageArea = document.getElementById('messageArea');
        const errorDiv = document.createElement('div');
        errorDiv.className = 'message system error';
        errorDiv.innerHTML = `
            <div class="message-content">
                <div class="message-text">
                    <i class="fas fa-exclamation-triangle"></i>
                    세션 정보를 불러올 수 없습니다.<br>
                    <small>세션 ID: ${this.sessionId}</small>
                </div>
            </div>
        `;
        messageArea.appendChild(errorDiv);
        
        // 기본 세션 정보 표시
        this.displayDefaultSessionInfo();
    }
    
    /**
     * 기본 세션 정보 표시 (오류 시)
     */
    displayDefaultSessionInfo() {
        // 헤더 정보 업데이트
        document.getElementById('sessionTitle').textContent = '상담 히스토리 (오류)';
        document.getElementById('customerName').textContent = '알 수 없음';
        document.getElementById('sessionTime').textContent = '-';
        
        // 사이드바 정보 업데이트
        document.getElementById('sidebarCustomerName').textContent = '알 수 없음';
        document.getElementById('sidebarCustomerEmail').textContent = '-';
        document.getElementById('sidebarCustomerPhone').textContent = '-';
        document.getElementById('sidebarStartTime').textContent = '-';
        document.getElementById('sidebarEndTime').textContent = '-';
        document.getElementById('sidebarMessageCount').textContent = '0';
        document.getElementById('sidebarAdminName').textContent = '-';
        document.getElementById('sidebarCategoryName').textContent = '-';
        
        // 테스트용 더미 메시지 표시 (개발 환경에서만)
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            this.displayDummyMessages();
        }
    }
    
    /**
     * 테스트용 더미 메시지 표시
     */
    displayDummyMessages() {
        const messageArea = document.getElementById('messageArea');
        
        // 기존 오류 메시지 제거
        const errorMessage = messageArea.querySelector('.message.system.error');
        if (errorMessage) {
            errorMessage.remove();
        }
        
        const dummyMessages = [
            {
                senderType: 'M',
                senderName: '테스트 고객',
                content: '안녕하세요. 상담 문의드립니다.',
                timestamp: new Date(Date.now() - 3600000) // 1시간 전
            },
            {
                senderType: 'A',
                senderName: '상담원',
                content: '안녕하세요. 무엇을 도와드릴까요?',
                timestamp: new Date(Date.now() - 3500000) // 58분 전
            },
            {
                senderType: 'M',
                senderName: '테스트 고객',
                content: '주문한 상품이 아직 배송되지 않았습니다.',
                timestamp: new Date(Date.now() - 3400000) // 56분 전
            },
            {
                senderType: 'A',
                senderName: '상담원',
                content: '주문번호를 알려주시면 확인해드리겠습니다.',
                timestamp: new Date(Date.now() - 3300000) // 55분 전
            },
            {
                senderType: 'S',
                content: '상담이 종료되었습니다. 감사합니다.',
                timestamp: new Date(Date.now() - 3200000) // 53분 전
            }
        ];
        
        dummyMessages.forEach(message => {
            this.displayMessage(message);
        });
        
        this.scrollToBottom();
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
     * 에러 메시지 표시
     */
    showError(message) {
        showToast(message, 'error');
    }
}

// 페이지 로드 시 히스토리 상세보기 초기화
document.addEventListener('DOMContentLoaded', () => {
    new ChatHistoryDetail();
});
