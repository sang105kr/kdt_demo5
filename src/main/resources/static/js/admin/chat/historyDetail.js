/**
 * 상담 히스토리 상세보기 JavaScript
 */
class ChatHistoryDetail {
    constructor() {
        this.sessionId = null;
        this.sessionData = null;
        
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
        await Promise.all([
            this.loadCategoryNames(),    // 카테고리 목록 로드
            this.loadExitReasons()       // 종료 사유 목록 로드
        ]);
        
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
        
        this.loadSessionData();
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
     * 카테고리 목록 조회 (서버에서 동적으로 로드)
     */
    async loadCategoryNames() {
        try {
            const response = await ajax.get('/api/faq/categories');
            if (response && response.code === '00' && response.data) {
                // 카테고리 코드를 키로, decode를 값으로 하는 객체 생성
                this.categoryNames = {};
                this.categoryData = {}; // codeId도 저장하기 위한 객체
                response.data.forEach(category => {
                    this.categoryNames[category.code] = category.decode;
                    this.categoryData[category.code] = category; // 전체 데이터 저장
                });
                console.log('카테고리 목록 로드 완료:', this.categoryNames);
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
     * 세션 데이터 로드
     */
    async loadSessionData() {
        try {
            console.log('=== 세션 데이터 로드 시작 ===');
            console.log('세션 ID:', this.sessionId);
            console.log('세션 ID 타입:', typeof this.sessionId);
            
            const url = `/api/admin/chat/sessions/${this.sessionId}`;
            console.log('요청 URL:', url);
            
            const response = await ajax.get(url);
            
            console.log('=== API 응답 ===');
            console.log('전체 응답:', response);
            console.log('응답 코드:', response?.code);
            console.log('응답 데이터:', response?.data);
            
            if (response && response.code === '00') {
                this.sessionData = response.data;
                console.log('세션 데이터 설정 완료:', this.sessionData);
                this.updateSessionInfo();
                this.loadMessageHistory();
            } else {
                console.error('API 응답이 성공이 아님:', response);
                throw new Error(`세션 정보를 불러올 수 없습니다. 응답: ${JSON.stringify(response)}`);
            }
        } catch (error) {
            console.error('=== 세션 데이터 로드 실패 ===');
            console.error('에러 객체:', error);
            console.error('에러 메시지:', error.message);
            console.error('에러 스택:', error.stack);
            this.showError(`세션 정보를 불러오는데 실패했습니다: ${error.message}`);
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
        
        // 헤더에 종료 사유 표시
        this.updateHeaderExitReason();
        
        // 사이드바 정보 업데이트 - 실제 데이터 사용
        document.getElementById('sidebarCustomerName').textContent = this.sessionData.memberName || '고객';
        document.getElementById('sidebarCustomerEmail').textContent = this.sessionData.memberEmail || '-';
        document.getElementById('sidebarCustomerPhone').textContent = this.sessionData.memberPhone || '-';
        document.getElementById('sidebarStartTime').textContent = this.formatDateTime(this.sessionData.startTime);
        document.getElementById('sidebarEndTime').textContent = this.formatDateTime(this.sessionData.endTime);
        document.getElementById('sidebarMessageCount').textContent = this.sessionData.messageCount || '0';
        document.getElementById('sidebarAdminName').textContent = this.sessionData.adminName || '상담원';
        document.getElementById('sidebarCategoryName').textContent = this.sessionData.categoryName || this.categoryNames['GENERAL'] || '일반 문의';
        
        // 종료 사유 정보 추가
        this.updateExitReasonInfo();
        
        console.log('사이드바 업데이트 완료');
    }
    
    /**
     * 헤더에 종료 사유 표시
     */
    updateHeaderExitReason() {
        const exitReasonId = this.sessionData.exitReasonId;
        const exitReasonText = this.getExitReasonText(exitReasonId);
        
        const exitReasonDisplay = document.getElementById('exitReasonDisplay');
        if (exitReasonDisplay) {
            exitReasonDisplay.innerHTML = exitReasonText;
        }
    }
    
    /**
     * 종료 사유 정보 업데이트
     */
    updateExitReasonInfo() {
        const exitReasonId = this.sessionData.exitReasonId;
        const exitReasonText = this.getExitReasonText(exitReasonId);
        
        // 종료 사유 정보를 사이드바에 추가
        const sidebarContent = document.querySelector('.sidebar-content');
        if (sidebarContent) {
            // 기존 종료 사유 섹션이 있으면 제거
            const existingExitReasonSection = sidebarContent.querySelector('.exit-reason-section');
            if (existingExitReasonSection) {
                existingExitReasonSection.remove();
            }
            
            // 새로운 종료 사유 섹션 추가
            const exitReasonSection = document.createElement('div');
            exitReasonSection.className = 'info-section exit-reason-section';
            exitReasonSection.innerHTML = `
                <h4>종료 정보</h4>
                <div class="info-item">
                    <span class="label">종료 사유:</span>
                    <span class="value">${exitReasonText}</span>
                </div>
            `;
            
            // 상담 정보 섹션 다음에 추가
            const consultationSection = sidebarContent.querySelector('.info-section:last-child');
            if (consultationSection) {
                consultationSection.parentNode.insertBefore(exitReasonSection, consultationSection.nextSibling);
            } else {
                sidebarContent.appendChild(exitReasonSection);
            }
        }
    }
    
    /**
     * 종료 사유 텍스트 생성
     */
    getExitReasonText(exitReasonId) {
        if (!exitReasonId) {
            return '<span class="exit-reason-badge unknown">알 수 없음</span>';
        }
        
        const exitReason = this.exitReasons[exitReasonId];
        if (exitReason) {
            return `<span class="exit-reason-badge">${exitReason}</span>`;
        } else {
            return '<span class="exit-reason-badge unknown">알 수 없음</span>';
        }
    }
    
    /**
     * 메시지 히스토리 로드
     */
    async loadMessageHistory() {
        try {
            const response = await ajax.get(`/api/admin/chat/sessions/${this.sessionId}/messages`);
            
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
        if (!messageArea) {
            console.error('messageArea 요소를 찾을 수 없습니다.');
            return;
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
        
        // 메시지 HTML 구성 - session.js와 동일한 구조
        if (messageClass === 'system') {
            messageDiv.innerHTML = `
                <div class="message-content">
                    <div class="message-text">${this.escapeHtml(message.content)}</div>
                </div>
            `;
        } else {
            messageDiv.innerHTML = `
                <div class="message-info">
                    <span class="message-name">${this.escapeHtml(message.senderName)}</span>
                    <span class="message-time">${timeString}</span>
                </div>
                <div class="message-content">${this.escapeHtml(message.content)}</div>
            `;
        }
        
        messageArea.appendChild(messageDiv);
    }
    
    /**
     * 메시지가 없을 때 표시
     */
    displayNoMessages() {
        const messageArea = document.getElementById('messageArea');
        if (!messageArea) {
            console.error('messageArea 요소를 찾을 수 없습니다.');
            return;
        }
        
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
        if (!messageArea) {
            console.error('messageArea 요소를 찾을 수 없습니다.');
            return;
        }
        
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
    }
    
    /**
     * 스크롤을 맨 아래로 이동
     */
    scrollToBottom() {
        const chatMessages = document.querySelector('.chat-messages');
        if (chatMessages) {
            chatMessages.scrollTop = chatMessages.scrollHeight;
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
        // 토스트 메시지 표시
        if (typeof showToast === 'function') {
            showToast(message, 'error');
        } else {
            console.error(message);
        }
        
        // 메시지 영역에도 오류 표시
        const messageArea = document.getElementById('messageArea');
        if (messageArea) {
            const errorDiv = document.createElement('div');
            errorDiv.className = 'message system error';
            errorDiv.innerHTML = `
                <div class="message-content">
                    <div class="message-text">
                        <i class="fas fa-exclamation-triangle"></i>
                        ${this.escapeHtml(message)}
                    </div>
                </div>
            `;
            messageArea.appendChild(errorDiv);
        }
    }
}

// 페이지 로드 시 히스토리 상세보기 초기화
document.addEventListener('DOMContentLoaded', () => {
    new ChatHistoryDetail();
});
