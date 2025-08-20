/**
 * 관리자 채팅 세션 JavaScript
 */

class AdminChatSession {
    constructor() {
    this.currentSession = null;
    this.stompClient = null;
    this.isConnected = false;
    this.messages = []; // 메시지 배열 추가
    this.unreadCount = 0; // 읽지 않은 메시지 카운트
    this.lastReadCount = 0; // 마지막으로 읽힌 메시지 개수
    this.lastAdminReadCount = 0; // 마지막으로 읽힌 고객 메시지 개수
    this.isWindowActive = true; // 창이 활성화되어 있는지 여부
    this.readTimeout = null; // 읽음 처리 타이머
    this.readDelayTimeout = null; // 읽음 처리 지연 타이머
    this.scrollTimeout = null; // 스크롤 디바운싱 타이머
    this.lastReadCheck = 0; // 마지막 읽음 체크 시간
    this.presenceHiddenSent = false; // 일시 이탈 알림 전송 여부
    this.presenceVisibleSentAt = 0; // 복귀 알림 마지막 전송 시간
    this.chatSubscription = null; // WebSocket 구독 객체
    this.historyLoaded = false; // 히스토리 로드 완료 여부
    this.messageTypes = {}; // 메시지 타입 코드 저장 (code -> codeId 매핑)
    
    // 카테고리 이름 매핑 (서버에서 동적으로 로드)
    this.categoryNames = {};
    this.categoryData = {}; // 카테고리 전체 데이터 (codeId 포함)
    
    // 관리자 정보 로드
    this.adminId = this.loadAdminInfo();
    // URL에서 sessionId 파라미터 확인
    this.sessionId = this.getSessionIdFromUrl();
    this.init();
  }
  
  loadAdminInfo() {
    const adminId = document.documentElement.getAttribute('data-admin-id');
    if (!adminId) {
      console.error('관리자 ID를 찾을 수 없습니다.');
      return null;
    }
    console.log('관리자 ID 로드:', adminId);
    return parseInt(adminId);
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
  
  getSessionIdFromUrl() {
    const pathSegments = window.location.pathname.split('/');
    const sessionIdIndex = pathSegments.indexOf('session') + 1;
    const sessionId = pathSegments[sessionIdIndex] && pathSegments[sessionIdIndex] !== 'new' 
      ? pathSegments[sessionIdIndex] 
      : null;
    
    console.log('URL에서 추출한 sessionId:', sessionId);
    
    // sessionId가 숫자인지 확인 (Oracle Sequence 방식)
    if (sessionId && !/^\d+$/.test(sessionId)) {
      console.error('잘못된 sessionId 형식 (숫자여야 함):', sessionId);
      return null;
    }
    
    return sessionId;
  }
  
  async init() {
    console.log('AdminChatSession 초기화 시작...');
    console.log('ajax 객체 확인:', typeof ajax, ajax);
    console.log('SockJS 객체 확인:', typeof SockJS, SockJS);
    console.log('Stomp 객체 확인:', typeof Stomp, Stomp);
    
    // 필수 라이브러리 확인
    if (typeof ajax === 'undefined') {
      console.error('ajax 객체가 로드되지 않았습니다. common.js를 확인해주세요.');
      this.showError('필수 스크립트가 로드되지 않았습니다. 페이지를 새로고침해주세요.');
      return;
    }
    
    if (typeof SockJS === 'undefined') {
      console.error('SockJS 라이브러리가 로드되지 않았습니다.');
      this.showError('WebSocket 라이브러리가 로드되지 않았습니다. 페이지를 새로고침해주세요.');
      return;
    }
    
    if (typeof Stomp === 'undefined') {
      console.error('Stomp 라이브러리가 로드되지 않았습니다.');
      this.showError('WebSocket 라이브러리가 로드되지 않았습니다. 페이지를 새로고침해주세요.');
      return;
    }
        
    try {
      await this.loadCategoryNames(); // 카테고리 목록 로드
      await this.loadMessageTypes(); // 메시지 타입 코드 로드
      this.bindEvents();
      this.setupReadDetection();
      
      // WebSocket 연결만 수행 (세션 연결은 WebSocket 연결 완료 후 수행)
      this.connectWebSocket();
    } catch (error) {
      console.error('AdminChatSession 초기화 실패:', error);
      this.showError('채팅 초기화에 실패했습니다. 페이지를 새로고침해주세요.');
    }
  }
    
    bindEvents() {
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');
    
    if (messageInput && sendBtn) {
      messageInput.addEventListener('input', () => {
        sendBtn.disabled = !messageInput.value.trim();
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
    }
    
    // 드래그 기능 설정
    this.setupDragToMove();
  }
  
  setupReadDetection() {
    // 창 활성화/비활성화 감지
    window.addEventListener('focus', () => {
      this.isWindowActive = true;
      this.debouncedReadCheck();
      this.notifyPresence('ACTIVE', 'WINDOW_FOCUS');
    });

    window.addEventListener('blur', () => {
      this.isWindowActive = false;
    });

    // 페이지 가시성 변경 감지 (탭 전환, 브라우저 최소화 등)
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'hidden') {
        this.notifyPresence('INACTIVE', 'PAGE_HIDE');
            } else if (document.visibilityState === 'visible') {
        this.notifyPresence('ACTIVE', 'PAGE_SHOW');
      }
    });

    // 페이지 언로드 감지 (창 닫기, 새로고침 등) - 상담 종료 처리
    window.addEventListener('beforeunload', () => {
      // 상담 종료 중이면 presence 업데이트 건너뛰기
      if (window.isExiting) {
        console.log('상담 종료 중이므로 presence 업데이트를 건너뜁니다.');
        return;
      }
      
      this.notifyPresence('INACTIVE', 'PAGE_UNLOAD');
      // 관리자 창 닫기 시 상담 종료 처리
      if (this.currentSession) {
        this.endChatSession('ADMIN_CLOSE');
      }
    });

    // 스크롤 이벤트로 읽음 감지 (디바운싱 적용)
    const messageArea = document.getElementById('chatMessages');
    if (messageArea) {
      messageArea.addEventListener('scroll', () => {
        this.debouncedReadCheck();
      });
    }

    // 마우스 클릭 이벤트로 읽음 감지
    document.addEventListener('click', () => {
      this.debouncedReadCheck();
    });

    // 키보드 이벤트로 읽음 감지
    document.addEventListener('keydown', () => {
      this.debouncedReadCheck();
    });
  }

  // 통일된 디바운싱 읽음 체크 함수
  debouncedReadCheck() {
    if (this.readTimeout) {
      clearTimeout(this.readTimeout);
    }
    this.readTimeout = setTimeout(() => {
      this.checkAndMarkAsRead();
    }, 200); // 500ms 디바운싱
  }

  checkAndMarkAsRead() {
    // 창이 활성화되어 있고, 스크롤이 맨 아래에 있으면 읽음 처리
    if (!this.isWindowActive || !this.currentSession) return;

    // 너무 자주 체크하지 않도록 제한 (500ms 간격으로 단축)
    const now = Date.now();
    if (now - this.lastReadCheck < 500) return; // 1000ms → 500ms로 단축
    this.lastReadCheck = now;

    // 읽음 처리 조건 확인
    if (!this.shouldMarkAsRead()) return;

    // 고객이 보낸 메시지가 있는 경우에만 읽음 처리
    const unreadCustomerMessages = this.messages.filter(m => 
      m.senderType === 'M' && m.isRead !== 'Y'
    );
    
    if (unreadCustomerMessages.length > 0) {
      // 즉시 읽음 처리
      this.markAsReadWithDelay();
    }
  }

  // 읽음 처리 조건 확인 함수
  shouldMarkAsRead() {
    // 창이 활성화되어 있어야 함
    if (!this.isWindowActive) return false;
    
    // 메시지 영역이 존재해야 함
    const messageArea = document.getElementById('chatMessages');
    if (!messageArea) return false;
    
    // 스크롤이 맨 아래에 있어야 함 (5px 여유)
    const isAtBottom = messageArea.scrollHeight - messageArea.scrollTop - messageArea.clientHeight < 5;
    if (!isAtBottom) return false;
    
    return true;
  }
    
    /**
   * 세션 연결 시 기존 고객 메시지들을 모두 읽음 처리
   */
  async markAllCustomerMessagesAsRead() {
    if (!this.currentSession) return;
    
    try {
      console.log('기존 고객 메시지들을 읽음 처리 시작...');
      
      // 현재 로드된 고객 메시지 개수 확인
      const customerMessages = this.messages.filter(m => m.senderType === 'M');
      if (customerMessages.length === 0) {
        console.log('읽을 고객 메시지가 없습니다.');
        return;
      }
      
      // 백엔드에 읽음 처리 요청
      await ajax.post(`/api/admin/chat/sessions/${this.currentSession}/read`, {});
      
      // WebSocket으로 읽음 이벤트 전송
      if (this.stompClient && this.isConnected) {
        this.stompClient.send('/app/chat.read', {}, JSON.stringify({
          sessionId: this.currentSession,
          receiverId: this.adminId,
          senderType: 'A'
        }));
      }
      
      // 로컬 상태 업데이트
      this.lastAdminReadCount = customerMessages.length;
      this.unreadCount = 0;
      
      // 모든 읽음 카운트 제거
      const readCountElements = document.querySelectorAll('.read-count');
      readCountElements.forEach(element => {
        element.remove();
      });
      
      console.log('기존 고객 메시지 읽음 처리 완료:', customerMessages.length);
    } catch (error) {
      console.error('기존 고객 메시지 읽음 처리 실패:', error);
    }
  }
  
  async markAsRead() {
    if (!this.currentSession) return;

    // 읽지 않은 고객 메시지가 있는지 확인
    const currentUserMessageCount = this.messages.filter(m => 
      m.senderType === 'M'
    ).length;
    const actualUnreadCount = Math.max(0, currentUserMessageCount - this.lastAdminReadCount);

    // 이미 모든 메시지를 읽었다면 처리하지 않음
    if (actualUnreadCount === 0) return;

    try {
      // 백엔드에 읽음 처리 요청
      await ajax.post(`/api/admin/chat/sessions/${this.currentSession}/read`, {});

      // WebSocket으로 읽음 이벤트 전송
      if (this.stompClient && this.isConnected) {
        this.stompClient.send('/app/chat.read', {}, JSON.stringify({
          sessionId: this.currentSession,
          receiverId: this.adminId,
          senderType: 'A'
        }));
      }

      console.log('관리자 읽음 처리 완료 - 읽지 않은 메시지:', actualUnreadCount);
    } catch (error) {
      console.error('관리자 읽음 처리 실패:', error);
    }
  }

  // 읽음 처리 지연 함수 (실제 확인 시간 고려)
  markAsReadWithDelay() {
    if (this.readDelayTimeout) {
      clearTimeout(this.readDelayTimeout);
    }
    
    this.readDelayTimeout = setTimeout(() => {
      this.markAsRead();
    }, 800); // 2초 지연 후 읽음 처리
  }
  
  async connectToSpecificSession(sessionId) {
    try {
      console.log('특정 세션에 연결 시도:', sessionId);
      console.log('ajax 객체 확인:', typeof ajax, ajax);
      
      if (typeof ajax === 'undefined') {
        throw new Error('ajax 객체가 로드되지 않았습니다. common.js를 확인해주세요.');
      }
      
      // 세션 정보 조회 (관리자 전용 API 사용)
      console.log('세션 정보 조회 API 호출:', `/api/admin/chat/sessions/${sessionId}`);
      const response = await ajax.get(`/api/admin/chat/sessions/${sessionId}`);
      console.log('세션 정보 조회 응답:', response);
      
      if (response && response.code === '00' && response.data) {
        const session = response.data;
        console.log('세션 데이터:', session);
        
        this.currentSession = sessionId;
        
        // 세션 정보 업데이트
        this.updateSessionInfo(session);
        
        // 세션이 대기 중이면 진행중으로 변경
        if (session.statusId === 1) { // WAITING 상태
          console.log('세션 상태를 진행중으로 변경 시도');
          await this.updateSessionStatus('ACTIVE');
        }
        
        // 채팅 히스토리 로드 (내부에서 고객 메시지 읽음 처리 포함)
        console.log('채팅 히스토리 로드 시도');
        await this.loadChatHistory();
        
        console.log('특정 세션 연결 완료:', this.currentSession);
      } else {
        console.error('세션 정보 조회 실패:', response);
        const errorMessage = response?.message || '해당 상담을 찾을 수 없습니다.';
        this.showError(errorMessage);
      }
    } catch (error) {
      console.error('특정 세션 연결 실패:', error);
      console.error('에러 스택:', error.stack);
      
      let errorMessage = '상담에 연결할 수 없습니다.';
      
      if (error.message.includes('JSON 파싱 실패')) {
        errorMessage = '서버 응답 오류: API 엔드포인트를 확인해주세요.';
      } else if (error.message.includes('404')) {
        errorMessage = '상담 세션을 찾을 수 없습니다.';
      } else if (error.message.includes('403')) {
        errorMessage = '접근 권한이 없습니다.';
      } else if (error.message.includes('500')) {
        errorMessage = '서버 오류가 발생했습니다.';
      } else {
        errorMessage = error.message || '상담에 연결할 수 없습니다.';
      }
      
      this.showError(errorMessage);
    }
  }
  
  async findWaitingSession() {
    try {
      console.log('대기 중인 세션 조회 시작...');
      
      const response = await ajax.get('/api/admin/chat/sessions/waiting');
      console.log('대기 세션 조회 응답:', response);
      
      if (response && response.code === '00' && response.data && response.data.length > 0) {
        const waitingSession = response.data[0]; // 첫 번째 대기 세션 선택
        console.log('대기 세션 발견:', waitingSession);
        
        this.currentSession = waitingSession.sessionId;
        
        // 세션 정보 업데이트
        this.updateSessionInfo(waitingSession);
        
        // 세션 상태를 진행중으로 변경
        await this.updateSessionStatus('ACTIVE');
        
        // 채팅 히스토리 로드 (내부에서 고객 메시지 읽음 처리 포함)
        await this.loadChatHistory();
        
        console.log('대기 세션 연결 완료:', this.currentSession);
      } else {
        console.log('대기 중인 세션이 없습니다. 응답:', response);
        this.showError('대기 중인 상담이 없습니다.');
      }
    } catch (error) {
      console.error('대기 세션 조회 실패:', error);
      this.showError('대기 중인 상담을 찾을 수 없습니다. 오류: ' + error.message);
    }
  }
  
  updateSessionInfo(session) {
    // 고객 정보 업데이트
    document.getElementById('customerName').textContent = session.memberName || '고객';
    document.getElementById('customerEmail').textContent = session.memberEmail || '-';
    document.getElementById('customerPhone').textContent = session.memberPhone || '-';
    document.getElementById('customerJoinDate').textContent = session.memberJoinDate ? 
      new Date(session.memberJoinDate).toLocaleDateString('ko-KR') : '-';
    
    // 상담 정보 업데이트
    document.getElementById('categoryName').textContent = session.categoryName || this.categoryNames['GENERAL'] || '일반 문의';
    document.getElementById('startTime').textContent = new Date(session.startTime).toLocaleString('ko-KR');
    document.getElementById('adminName').textContent = session.adminName || '상담원';
  }
  
  async updateSessionStatus(status) {
    if (!this.currentSession) return;
    
    try {
              await ajax.put(`/api/admin/chat/sessions/${this.currentSession}/status`, {
        status: status
      });
      
      console.log('세션 상태 업데이트 완료:', status);
    } catch (error) {
      console.error('세션 상태 업데이트 실패:', error);
    }
  }
  
  connectWebSocket() {
    try {
      console.log('관리자 WebSocket 연결 시작...');
      
      if (typeof SockJS === 'undefined') {
        throw new Error('SockJS 라이브러리가 로드되지 않았습니다.');
      }
      
      if (typeof Stomp === 'undefined') {
        throw new Error('Stomp 라이브러리가 로드되지 않았습니다.');
      }
      
      const socket = new SockJS('/ws');
      console.log('SockJS 소켓 생성 성공:', socket);
      
      this.stompClient = Stomp.over(socket);
      console.log('STOMP 클라이언트 생성 성공:', this.stompClient);
      
      this.stompClient.connect({}, async (frame) => {
        console.log('관리자 WebSocket 연결 성공:', frame);
        this.isConnected = true;
        
        // WebSocket 연결 완료 후 세션 연결 시도
        if (this.sessionId) {
          console.log('WebSocket 연결 완료 후 특정 세션 연결 시도:', this.sessionId);
          try {
            await this.connectToSpecificSession(this.sessionId);
          } catch (error) {
            console.error('WebSocket 연결 후 세션 연결 실패:', error);
          }
        } else {
          console.log('WebSocket 연결 완료 후 대기 세션 찾기 시도');
          try {
            await this.findWaitingSession();
          } catch (error) {
            console.error('WebSocket 연결 후 대기 세션 찾기 실패:', error);
          }
        }
        
        if (this.currentSession) {
          console.log('현재 세션이 있으므로 채팅 구독 설정');
          // 기존 구독 해제
          if (this.chatSubscription) {
            this.chatSubscription.unsubscribe();
            this.chatSubscription = null;
          }
          
          // 채팅 메시지 구독
          this.chatSubscription = this.stompClient.subscribe(`/topic/chat/${this.currentSession}`, (message) => {
            const messageData = JSON.parse(message.body);
            console.log('WebSocket 메시지 수신:', messageData);
            // 히스토리 로드가 완료된 후에만 WebSocket 메시지 처리
            if (this.historyLoaded) {
              this.displayMessage(messageData);
            } else {
              console.log('히스토리 로드 중이므로 메시지 처리 지연');
            }
          });
          
          // 관리자 참가 메시지 전송
          this.sendUserJoin();
        } else {
          console.log('현재 세션이 없으므로 채팅 구독 설정 안함');
        }
        
      }, (error) => {
        console.error('관리자 WebSocket 연결 실패:', error);
        console.error('연결 오류 타입:', typeof error);
        console.error('연결 오류 메시지:', error.message);
        this.isConnected = false;
        this.chatSubscription = null;
      });
      
    } catch (error) {
      console.error('WebSocket 연결 오류:', error);
      console.error('오류 스택:', error.stack);
      this.isConnected = false;
      this.chatSubscription = null;
    }
  }
  
  sendUserJoin() {
    if (!this.currentSession || !this.isConnected) return;
    
    this.stompClient.send('/app/chat.addUser', {}, JSON.stringify({
      sessionId: this.currentSession,
      senderId: this.adminId,
      senderName: '상담원',
      senderType: 'A'
    }));
  }
  
    sendMessage() {
    const messageInput = document.getElementById('messageInput');
    if (!messageInput || !this.currentSession || !this.isConnected) return;
        
    const message = messageInput.value.trim();
    if (!message) return;
        
    const messageData = {
      sessionId: this.currentSession,
      senderId: this.adminId,
      senderName: '상담원',
      content: message,
      senderType: 'A',
      messageTypeId: this.messageTypes['TEXT'], // 동적으로 로드한 TEXT 타입의 codeId 사용
      isRead: 'N', // 읽지 않음 상태로 설정
      timestamp: new Date().toISOString() // 클라이언트 타임스탬프
    };
    
    console.log('관리자 메시지 전송:', messageData);
    
    // 즉시 화면에 메시지 표시 (사용자 경험 개선)
    // 단, 이때는 messageId가 없으므로 읽음 카운트 업데이트하지 않음
    this.displayMessage(messageData);
    
    // 입력 필드 초기화 (즉시)
    messageInput.disabled = true;
    messageInput.value = '';
    document.getElementById('sendBtn').disabled = true;
    
    // WebSocket으로 서버에 전송
    try {
      this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(messageData));
      console.log('메시지 서버 전송 완료');
      
      // 전송 성공 후 입력 필드 다시 활성화
      messageInput.disabled = false;
    } catch (error) {
      console.error('메시지 서버 전송 실패:', error);
      // 전송 실패 시 메시지에 오류 표시
      this.markMessageAsError(messageData);
      // 전송 실패 시 입력 필드 다시 활성화
      messageInput.disabled = false;
    }
  }
    
  displayMessage(message) {
    const messageArea = document.getElementById('chatMessages');
    if (!messageArea) {
      console.error('메시지 영역을 찾을 수 없습니다.');
      return;
    }
    
    // READ_EVENT는 읽음 카운트 처리만 하고 화면에 표시하지 않음
    if (message.content === 'READ_EVENT') {
      // 내가 보낸(A) 메시지들을 읽음 처리로 동기화
      this.messages = this.messages.map(m => 
        m.senderType === 'A' ? {...m, isRead: 'Y'} : m
      );
      this.handleReadEvent(message);
      return;
    }
    
    // 강화된 중복 메시지 체크
    const isDuplicate = this.checkDuplicateMessage(message);
    if (isDuplicate) {
      console.log('중복 메시지 감지, 무시:', message);
      return;
    }
    
    // 메시지를 내부 배열에 저장
    this.messages.push(message);
    
    const messageElement = document.createElement('div');
    // 발신자 타입에 따른 클래스 설정
    let messageClass = 'message ';
    if (message.senderType === 'S') {
      messageClass += 'system';
      
      // 세션 종료 메시지 감지
      if (message.content && message.content.includes('상담이 종료되었습니다')) {
        console.log('상담 종료 메시지 감지 - 관리자 창 닫기');
        setTimeout(() => {
          this.handleSessionEnd();
        }, 1000); // 1초 후 종료 처리
      }
    } else if (message.senderType === 'A') {
      messageClass += 'admin'; // 관리자가 보낸 메시지
    } else {
      messageClass += 'user'; // 고객이 보낸 메시지
    }
    messageElement.className = messageClass;
    
    const time = new Date(message.timestamp).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit'
    });
    
    // 발신자 이름 표시 (시스템 메시지가 아닌 경우)
    const senderName = message.senderType === 'S' ? '' : 
      `<div class="message-sender">${this.escapeHtml(message.senderName || '알 수 없음')}</div>`;
    
    // 읽음 카운트 표시 (내가 보낸 메시지인 경우)
    const readCountHtml = this.getReadCountHtml(message);
    
    messageElement.innerHTML = `
      ${senderName}
      <div class="message-content">${this.escapeHtml(message.content)}</div>
      <div class="message-footer">
        <div class="message-time">${time}</div>
        ${readCountHtml}
      </div>
    `;
    
    messageArea.appendChild(messageElement);
    this.scrollToBottom();
    
    // 모든 메시지에 대해 읽음 카운트 업데이트 (내가 보낸 메시지든 상대방이 보낸 메시지든)
    this.updateReadCounts();
  }
  
  displayHistoryMessage(message) {
    // 히스토리 메시지 표시 (중복 체크 없이)
    const messageArea = document.getElementById('chatMessages');
    if (!messageArea) {
      console.error('메시지 영역을 찾을 수 없습니다.');
      return;
    }
    
    // 메시지를 내부 배열에 저장
    this.messages.push(message);
    
    const messageElement = document.createElement('div');
    // 발신자 타입에 따른 클래스 설정
    let messageClass = 'message ';
    if (message.senderType === 'S') {
      messageClass += 'system';
      
      // 세션 종료 메시지 감지
      if (message.content && message.content.includes('상담이 종료되었습니다')) {
        console.log('상담 종료 메시지 감지 - 관리자 창 닫기');
        setTimeout(() => {
          this.handleSessionEnd();
        }, 1000); // 1초 후 종료 처리
      }
    } else if (message.senderType === 'A') {
      messageClass += 'admin'; // 관리자가 보낸 메시지
    } else {
      messageClass += 'user'; // 고객이 보낸 메시지
    }
    messageElement.className = messageClass;
    
    const time = new Date(message.timestamp).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit'
    });
    
    // 발신자 이름 표시 (시스템 메시지가 아닌 경우)
    const senderName = message.senderType === 'S' ? '' : 
      `<div class="message-sender">${this.escapeHtml(message.senderName || '알 수 없음')}</div>`;
    
    // 히스토리 로드 시에도 읽음 카운트 표시 (이미 읽음 처리된 경우는 표시하지 않음)
    const readCountHtml = this.getReadCountHtml(message);
    
    messageElement.innerHTML = `
      ${senderName}
      <div class="message-content">${this.escapeHtml(message.content)}</div>
      <div class="message-footer">
        <div class="message-time">${time}</div>
        ${readCountHtml}
      </div>
    `;
    
    messageArea.appendChild(messageElement);
  }
  

  
  async loadChatHistory() {
    if (!this.currentSession) return;
    
    try {
      const data = await ajax.get(`/api/chat/sessions/${this.currentSession}/messages`);
      
      if (data.code === '00' && data.data) {
        const messageArea = document.getElementById('chatMessages');
        if (!messageArea) return;
        
        // 로딩 메시지 제거
        const loadingElement = messageArea.querySelector('.loading');
        if (loadingElement) {
          loadingElement.remove();
        }
        
        // 히스토리 로드 중에는 WebSocket 메시지 처리 안함
        this.historyLoaded = false;
        
        // 메시지 배열 초기화 (히스토리 로드 시)
        this.messages = [];
        
        // 고객 메시지가 있는지 확인하고 먼저 읽음 처리
        const customerMessages = data.data.filter(message => message.senderType === 'M');
        if (customerMessages.length > 0) {
          console.log('기존 고객 메시지 발견:', customerMessages.length, '개');
          
          // 백엔드에 읽음 처리 요청
          try {
            await ajax.post(`/api/admin/chat/sessions/${this.currentSession}/read`, {});
            console.log('기존 고객 메시지 읽음 처리 완료');
          } catch (error) {
            console.error('기존 고객 메시지 읽음 처리 실패:', error);
          }
        }
        
        // 메시지 표시 (중복 체크 없이 히스토리는 모두 표시)
        data.data.forEach(message => {
          // 히스토리 로드 시에는 중복 체크 없이 바로 표시
          this.displayHistoryMessage(message);
        });
        
        // 히스토리 로드 완료
        this.historyLoaded = true;
        
        // 스크롤을 맨 아래로 이동
        this.scrollToBottom();
      }
    } catch (error) {
      console.error('메시지 히스토리 로드 실패:', error);
      this.historyLoaded = true; // 에러 시에도 플래그 설정
    }
  }
  
  scrollToBottom() {
    const messageArea = document.getElementById('chatMessages');
    if (messageArea) {
      messageArea.scrollTop = messageArea.scrollHeight;
    }
  }
  
  handleReadEvent(message) {
    // 상대방이 메시지를 읽었을 때 모든 읽음 카운트 제거
    console.log('읽음 이벤트 처리:', message);
    
    // 읽음 이벤트의 receiverId를 확인하여 누가 읽었는지 판단
    const receiverId = message.receiverId || message.senderId;
    
    if (receiverId === this.adminId) { // 관리자 ID
      // 고객이 내 메시지를 읽었음 - 내 메시지들을 읽음 상태로 변경
      this.messages = this.messages.map(m => 
        m.senderType === 'A' ? {...m, isRead: 'Y'} : m
      );
      console.log('고객이 내 메시지를 읽었습니다. 모든 내 메시지를 읽음 처리');
    } else {
      // 내가 고객 메시지를 읽었음 - 고객 메시지들을 읽음 상태로 변경
      this.messages = this.messages.map(m => 
        m.senderType === 'M' ? {...m, isRead: 'Y'} : m
      );
      console.log('내가 고객 메시지를 읽었습니다. 모든 고객 메시지를 읽음 처리');
    }
    
    console.log('읽음 상태가 업데이트되었습니다.');
    
    // 읽음 카운트 업데이트
    this.updateReadCounts();
  }
  
  updateReadCounts() {
    // 모든 메시지의 읽음 카운트를 다시 계산하여 표시
    const messageElements = document.querySelectorAll('.message');
    
    // 내가 보낸 메시지 중 아직 읽지 않은 메시지들
    const unreadMyMessages = this.messages.filter(m => 
      m.senderType === 'A' && 
      m.isRead !== 'Y'
    );
    
    // 상대방이 보낸 메시지 중 아직 읽지 않은 메시지들
    const unreadOtherMessages = this.messages.filter(m => 
      m.senderType === 'M' && 
      m.isRead !== 'Y'
    );
    
    messageElements.forEach((element, index) => {
      const message = this.messages[index];
      if (!message) return;
      
      // 기존 읽음 카운트 제거
      const existingCount = element.querySelector('.read-count');
      if (existingCount) {
        existingCount.remove();
      }
      
      // 내가 보낸 메시지이고 아직 읽지 않은 경우
      if (message.senderType === 'A' && message.isRead !== 'Y') {
        const currentIndex = unreadMyMessages.findIndex(m => 
          m.timestamp === message.timestamp && 
          m.content === message.content
        );
        
        const sequentialCount = currentIndex + 1;
        
        if (sequentialCount > 0) {
          const readCountElement = document.createElement('div');
          readCountElement.className = 'read-count';
          readCountElement.textContent = sequentialCount;
          
          const footer = element.querySelector('.message-footer');
          if (footer) {
            footer.appendChild(readCountElement);
          }
        }
      }
      // 상대방이 보낸 메시지이고 아직 읽지 않은 경우
      else if (message.senderType === 'M' && message.isRead !== 'Y') {
        const currentIndex = unreadOtherMessages.findIndex(m => 
          m.timestamp === message.timestamp && 
          m.content === message.content
        );
        
        const sequentialCount = currentIndex + 1;
        
        if (sequentialCount > 0) {
          const readCountElement = document.createElement('div');
          readCountElement.className = 'read-count';
          readCountElement.textContent = sequentialCount;
          
          const footer = element.querySelector('.message-footer');
          if (footer) {
            footer.appendChild(readCountElement);
          }
        }
      }
    });
    
    console.log('읽음 카운트 업데이트 완료 - 내 메시지:', unreadMyMessages.length, '상대방 메시지:', unreadOtherMessages.length);
  }
  
  getReadCountHtml(message) {
    // 아직 읽지 않은 메시지에 대해서만 읽음 카운트 표시
    if (message.isRead !== 'Y') {
      // 내가 보낸 메시지인 경우
      if (message.senderType === 'A') {
        const unreadMessages = this.messages.filter(m => 
          m.senderType === 'A' && 
          m.isRead !== 'Y'
        );
        
        const currentIndex = unreadMessages.findIndex(m => 
          m.timestamp === message.timestamp && 
          m.content === message.content
        );
        
        const sequentialCount = currentIndex + 1;
        return sequentialCount > 0 ? `<div class="read-count">${sequentialCount}</div>` : '';
      }
      
      // 상대방이 보낸 메시지인 경우
      if (message.senderType === 'M') {
        const unreadMessages = this.messages.filter(m => 
          m.senderType === 'M' && 
          m.isRead !== 'Y'
        );
        
        const currentIndex = unreadMessages.findIndex(m => 
          m.timestamp === message.timestamp && 
          m.content === message.content
        );
        
        const sequentialCount = currentIndex + 1;
        return sequentialCount > 0 ? `<div class="read-count">${sequentialCount}</div>` : '';
      }
    }
    
    // 읽은 메시지는 아무것도 표시하지 않음
    return '';
  }
  
  /**
   * 강화된 중복 메시지 체크
   */
  checkDuplicateMessage(message) {
    // 시스템 메시지는 중복 체크에서 제외
    if (message.senderType === 'S') {
      return false;
    }
    
    // 1. 메시지 ID 기반 체크 (가장 정확)
    if (message.messageId) {
      const existingMessage = this.messages.find(m => m.messageId === message.messageId);
      if (existingMessage) {
        return true;
      }
    }
    
    // 2. 내용 + 발신자 + 시간 기반 체크 (더 엄격한 조건)
    const existingMessage = this.messages.find(m => 
      m.content === message.content && 
      m.senderId === message.senderId &&
      m.senderType === message.senderType &&
      Math.abs(new Date(m.timestamp) - new Date(message.timestamp)) < 5000 // 5초 이내로 확장
    );
    
    if (existingMessage) {
      return true;
    }
    
    // 3. 최근 메시지와 동일한 내용 체크 (추가 안전장치)
    if (this.messages.length > 0) {
      const lastMessage = this.messages[this.messages.length - 1];
      if (lastMessage.content === message.content && 
          lastMessage.senderId === message.senderId &&
          lastMessage.senderType === message.senderType &&
          Math.abs(new Date(lastMessage.timestamp) - new Date(message.timestamp)) < 1000) { // 1초 이내
        return true;
      }
    }
    
    return false;
  }
  
  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  // 메시지 전송 실패 시 오류 표시
  markMessageAsError(messageData) {
    const messageElements = document.querySelectorAll('.message');
    for (let element of messageElements) {
      const contentElement = element.querySelector('.message-content');
      if (contentElement && contentElement.textContent === messageData.content) {
        element.classList.add('error');
        const errorIcon = document.createElement('i');
        errorIcon.className = 'fas fa-exclamation-triangle error-icon';
        errorIcon.title = '전송 실패';
        element.appendChild(errorIcon);
        break;
      }
    }
  }
  
  showError(message) {
    const messageArea = document.getElementById('chatMessages');
    if (!messageArea) {
      console.error('메시지 영역을 찾을 수 없습니다.');
      return;
    }
    
    console.log('에러 메시지 표시:', message);
    
    messageArea.innerHTML = `
      <div class="error-message">
        <i class="fas fa-exclamation-triangle"></i>
        ${this.escapeHtml(message)}
      </div>
    `;
  }

  // 일시 이탈/복귀 상태 알림
  async notifyPresence(state, reason) {
    if (!this.currentSession) return;
    
    // 상담 종료 중이면 presence 업데이트 건너뛰기
    if (window.isExiting) {
      console.log('상담 종료 중이므로 presence 업데이트를 건너뜁니다.');
      return;
    }

    try {
      // 중복 전송 방지 (INACTIVE는 한 번만, ACTIVE는 1.5초 간격)
      if (state === 'INACTIVE' && this.presenceHiddenSent) return;
      if (state === 'ACTIVE') {
        const now = Date.now();
        if (this.presenceVisibleSentAt && (now - this.presenceVisibleSentAt) < 1500) return;
        this.presenceVisibleSentAt = now;
      }

      const payload = {
        side: 'ADMIN',
        state: state,
        reason: reason,
        graceSeconds: 300 // 5분 유예시간
      };

      await ajax.post(`/api/admin/chat/sessions/${this.currentSession}/presence`, payload);
      
      if (state === 'INACTIVE') {
        this.presenceHiddenSent = true;
        console.log('관리자 일시 이탈 상태 알림 전송:', reason);
      } else {
        this.presenceHiddenSent = false;
        console.log('관리자 복귀 상태 알림 전송:', reason);
      }
    } catch (error) {
      console.error('관리자 일시 이탈/복귀 상태 알림 실패:', error);
    }
  }

  // 상담 종료 처리
  async endChatSession(exitReasonId) {
    if (!this.currentSession) return;

    try {
      console.log('관리자 상담 종료 시도:', exitReasonId);
      
      const payload = {
        exitReasonId: exitReasonId,
        endedBy: 'A'
      };

      await ajax.post(`/api/chat/sessions/${this.currentSession}/end`, payload);
      
      console.log('관리자 상담 종료 완료:', exitReasonId);
    } catch (error) {
      console.error('관리자 상담 종료 실패:', error);
    }
  }

  // 세션 종료 처리 (다른 쪽에서 종료한 경우)
  handleSessionEnd() {
    console.log('상담이 종료되었습니다 - 관리자 창 닫기');
    
    // 타이머 정리
    if (this.readTimeout) {
      clearTimeout(this.readTimeout);
      this.readTimeout = null;
    }
    
    if (this.readDelayTimeout) {
      clearTimeout(this.readDelayTimeout);
      this.readDelayTimeout = null;
    }
    
    if (this.scrollTimeout) {
      clearTimeout(this.scrollTimeout);
      this.scrollTimeout = null;
    }
    
    // WebSocket 연결 해제
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect();
    }
    
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
    
    // 3초 후 창 닫기
    setTimeout(() => {
      if (window.parent !== window) {
        // iframe인 경우 부모 창에 메시지 전송
        try {
          window.parent.postMessage({ type: 'closeAdminChat' }, '*');
        } catch (error) {
          console.log('부모 창에 메시지 전송 실패:', error);
          window.close();
        }
      } else {
        // 팝업창인 경우 창 닫기
        window.close();
      }
    }, 3000);
  }

  /**
   * 메시지 타입 코드 로드
   */
  async loadMessageTypes() {
    try {
      console.log('메시지 타입 코드 로드 시작');
      
      const response = await ajax.get('/api/chat/message-types');
      
      if (response && response.code === '00' && response.data) {
        // code -> codeId 매핑 생성
        this.messageTypes = {};
        response.data.forEach(type => {
          this.messageTypes[type.code] = type.codeId;
        });
        
        console.log('메시지 타입 코드 로드 완료:', this.messageTypes);
      } else {
        console.error('메시지 타입 코드 로드 실패:', response?.message);
        // 기본값 설정
        this.messageTypes = {
          'TEXT': 1,
          'IMAGE': 2,
          'FILE': 3,
          'SYSTEM': 4
        };
      }
    } catch (error) {
      console.error('메시지 타입 코드 로드 오류:', error);
      // 기본값 설정
      this.messageTypes = {
        'TEXT': 1,
        'IMAGE': 2,
        'FILE': 3,
        'SYSTEM': 4
      };
    }
  }
}

// 페이지 로드 시 초기화 (중복 제거)

// 보안 기능 초기화
function initSecurityFeatures() {
  console.log('관리자 채팅창 보안 기능 초기화...');
  
  // 새로고침 방지 - 상담 종료 처리로 대체
  
  // 뒤로가기 방지
  window.addEventListener('popstate', function(e) {
    e.preventDefault();
    history.pushState(null, null, window.location.href);
    showSecurityWarning('뒤로가기가 제한됩니다.');
  });
  
  // 우클릭 방지
  document.addEventListener('contextmenu', function(e) {
    e.preventDefault();
    showSecurityWarning('우클릭이 제한됩니다.');
    return false;
  });
  
  // F5, Ctrl+R, Ctrl+Shift+R 방지
  document.addEventListener('keydown', function(e) {
    if (e.key === 'F5' || 
        (e.ctrlKey && e.key === 'r') || 
        (e.ctrlKey && e.shiftKey && e.key === 'R')) {
      e.preventDefault();
      showSecurityWarning('새로고침이 제한됩니다.');
      return false;
    }
    
    // Ctrl+N (새 창) 방지
    if (e.ctrlKey && e.key === 'n') {
      e.preventDefault();
      showSecurityWarning('새 창 열기가 제한됩니다.');
      return false;
    }
    
    // Ctrl+Shift+N (시크릿 창) 방지
    if (e.ctrlKey && e.shiftKey && e.key === 'N') {
      e.preventDefault();
      showSecurityWarning('시크릿 창 열기가 제한됩니다.');
      return false;
    }
  });
  
  // 개발자 도구 방지 (F12, Ctrl+Shift+I, Ctrl+U)
  document.addEventListener('keydown', function(e) {
    if (e.key === 'F12' || 
        (e.ctrlKey && e.shiftKey && e.key === 'I') ||
        (e.ctrlKey && e.key === 'u')) {
      e.preventDefault();
      showSecurityWarning('개발자 도구 사용이 제한됩니다.');
      return false;
    }
  });
  
  // 드래그 방지 (텍스트 선택 방지)
  document.addEventListener('selectstart', function(e) {
    e.preventDefault();
    return false;
  });
  
  // 복사 방지
  document.addEventListener('copy', function(e) {
    e.preventDefault();
    showSecurityWarning('복사가 제한됩니다.');
    return false;
  });
  
  // 잘라내기 방지
  document.addEventListener('cut', function(e) {
    e.preventDefault();
    showSecurityWarning('잘라내기가 제한됩니다.');
    return false;
  });
  
  // 붙여넣기 방지 (입력 필드 제외)
  document.addEventListener('paste', function(e) {
    const target = e.target;
    if (!target.matches('textarea, input[type="text"], input[type="password"]')) {
      e.preventDefault();
      showSecurityWarning('붙여넣기가 제한됩니다.');
      return false;
    }
  });
  
  // 초기 히스토리 상태 설정
  history.pushState(null, null, window.location.href);
  
  console.log('관리자 채팅창 보안 기능 초기화 완료');
}

// 보안 경고 메시지 표시
function showSecurityWarning(message) {
  const warningElement = document.getElementById('securityWarning');
  if (warningElement) {
    const spanElement = warningElement.querySelector('span');
    if (spanElement) {
      spanElement.textContent = message;
    }
    warningElement.style.display = 'flex';
    
    // 3초 후 자동 숨김
    setTimeout(() => {
      warningElement.style.display = 'none';
    }, 3000);
  }
}

// 종료 사유 모달 표시
function showExitModal() {
  const exitModal = document.getElementById('exitModal');
  if (exitModal) {
    exitModal.style.display = 'flex';
  }
}

// 종료 사유 모달 숨김
function hideExitModal() {
  const exitModal = document.getElementById('exitModal');
  if (exitModal) {
    exitModal.style.display = 'none';
  }
}

// 종료 취소
function cancelExit() {
  hideExitModal();
}

// 종료 확인
async function confirmExit() {
  const selectedReason = document.querySelector('input[name="exitReason"]:checked');
  
  if (!selectedReason) {
    alert('종료 사유를 선택해 주세요.');
    return;
  }
  
  const exitReasonId = parseInt(selectedReason.value);
  console.log('선택된 종료 사유 ID:', exitReasonId);
  
  try {
    // 종료 프로세스 시작 플래그 설정
    window.isExiting = true;
    
    // 상담 종료 API 호출
    if (window.adminChatSession && window.adminChatSession.currentSession) {
      await window.adminChatSession.endChatSession(exitReasonId);
    }
    
    // 모달 숨김
    hideExitModal();
    
    // iframe 방식인 경우 부모 창의 iframe 제거
    if (window.parent !== window) {
      // iframe 내부에서 실행되는 경우
      try {
        window.parent.postMessage({ type: 'closeAdminChat' }, '*');
      } catch (error) {
        console.log('부모 창에 메시지 전송 실패:', error);
      }
    } else {
      // 팝업창인 경우 창 닫기
      window.close();
    }
  } catch (error) {
    console.error('상담 종료 실패:', error);
    alert('상담 종료 중 오류가 발생했습니다.');
    // 오류 발생 시 플래그 초기화
    window.isExiting = false;
  }
  
  /**
   * 채팅창 드래그 이동 기능 설정
   */
  setupDragToMove() {
    const chatContainer = document.querySelector('.chat-container');
    const dragHandle = document.querySelector('.drag-handle');
    
    if (!chatContainer || !dragHandle) {
      console.warn('드래그 기능을 위한 DOM 요소를 찾을 수 없습니다.');
      return;
    }

    let isDragging = false;
    let initialX;
    let initialY;
    let lastDeltaX = 0;
    let lastDeltaY = 0;

    // 드래그 핸들에서 마우스 다운 이벤트
    dragHandle.addEventListener('mousedown', (e) => {
      e.preventDefault();
      e.stopPropagation();
      
      isDragging = true;
      initialX = e.clientX;
      initialY = e.clientY;
      
      // 드래그 중임을 시각적으로 표시
      dragHandle.style.cursor = 'grabbing';
      dragHandle.style.color = 'rgba(255, 255, 255, 1)';
      
      // 부모 창에 드래그 시작 메시지 전달
      if (window.parent && window.parent !== window) {
        window.parent.postMessage({
          type: 'adminChatDragStart',
          x: e.clientX,
          y: e.clientY
        }, '*');
      }
    });

    // 마우스 이동 이벤트
    document.addEventListener('mousemove', (e) => {
      if (isDragging) {
        e.preventDefault();
        
        // 부모 창에 드래그 이동 메시지 전달
        if (window.parent && window.parent !== window) {
          const deltaX = e.clientX - initialX;
          const deltaY = e.clientY - initialY;
          
          // 연속적인 드래그를 위해 누적값 계산
          lastDeltaX += deltaX;
          lastDeltaY += deltaY;
          
          window.parent.postMessage({
            type: 'adminChatDragMove',
            x: e.clientX,
            y: e.clientY,
            deltaX: deltaX,
            deltaY: deltaY
          }, '*');
          
          // 다음 드래그를 위해 초기값 업데이트
          initialX = e.clientX;
          initialY = e.clientY;
        }
      }
    });

    // 마우스 업 이벤트
    document.addEventListener('mouseup', () => {
      if (isDragging) {
        isDragging = false;
        
        // 드래그 종료 시 시각적 피드백 복원
        dragHandle.style.cursor = 'grab';
        dragHandle.style.color = 'rgba(255, 255, 255, 0.8)';
        
        // 부모 창에 드래그 종료 메시지 전달
        if (window.parent && window.parent !== window) {
          window.parent.postMessage({
            type: 'adminChatDragEnd'
          }, '*');
        }
      }
    });
    
    // 초기 커서 설정
    dragHandle.style.cursor = 'grab';
  }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    window.adminChatSession = new AdminChatSession();
  initSecurityFeatures();
});
