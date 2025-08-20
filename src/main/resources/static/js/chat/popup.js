/**
 * 팝업 채팅 JavaScript
 * 독립적인 팝업 창에서 동작하는 채팅 기능
 */

class ChatPopup {
  constructor() {
    this.currentSession = null;
    this.stompClient = null;
    this.isConnected = false;
    this.selectedCategory = null;
    this.messages = []; // 메시지 목록 저장
    this.unreadCount = 0; // 읽지 않은 메시지 개수
    this.lastReadCount = 0; // 마지막으로 읽힌 메시지 개수
    this.lastAdminReadCount = 0; // 마지막으로 읽힌 상담자 메시지 개수
    this.isWindowActive = true; // 창이 활성화되어 있는지 여부
    this.readTimeout = null; // 읽음 처리 타이머
    this.readDelayTimeout = null; // 읽음 처리 지연 타이머
    this.scrollTimeout = null; // 스크롤 디바운싱 타이머
    this.lastReadCheck = 0; // 마지막 읽음 체크 시간
    this.lastEventType = 'unknown'; // 마지막 이벤트 타입
    this.presenceHiddenSent = false; // 일시 이탈 알림 전송 여부
    this.presenceVisibleSentAt = 0; // 복귀 알림 마지막 전송 시간
    this.isInitialized = false; // 초기화 완료 여부
    this.messageTypes = {}; // 메시지 타입 코드 저장 (code -> codeId 매핑)
    
    this.init().catch(error => {
      console.error('채팅 팝업 초기화 실패:', error);
      this.showError('채팅 초기화에 실패했습니다. 페이지를 새로고침해주세요.');
    });
  }

  async init() {
    // DOM 요소 존재 확인을 먼저 수행
    if (!this.validateDOMElements()) {
      return;
    }
    
    this.bindEvents();
    this.setupReadDetection();
    await this.loadLoginInfo();
    await this.loadMessageTypes(); // 메시지 타입 코드 로드
    this.startChat();
    this.isInitialized = true;
  }

  // 메시지 타입 코드 로드
  async loadMessageTypes() {
    try {
      console.log('메시지 타입 코드 로드 시작');
      
      const response = await fetch('/api/chat/message-types');
      const data = await response.json();
      
      if (data.code === '00' && data.data) {
        // code -> codeId 매핑 생성
        this.messageTypes = {};
        data.data.forEach(type => {
          this.messageTypes[type.code] = type.codeId;
        });
        
        console.log('메시지 타입 코드 로드 완료:', this.messageTypes);
      } else {
        console.error('메시지 타입 코드 로드 실패:', data.message);
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

  validateDOMElements() {
    const messageInput = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');
    const chatMessages = document.getElementById('chatMessages');

    if (!messageInput || !sendBtn || !chatMessages) {
      console.error('필수 DOM 요소를 찾을 수 없습니다:', {
        messageInput: !!messageInput,
        sendBtn: !!sendBtn,
        chatMessages: !!chatMessages
      });
      this.showError('페이지 로드에 실패했습니다. 페이지를 새로고침해주세요.');
      return false;
    }
    return true;
  }

  bindEvents() {
    // DOM 요소 존재 확인
    const messageInput = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');
    const closeBtn = document.querySelector('.close-btn');

    // 창 닫기 버튼 이벤트 (onclick 대신 addEventListener 사용)
    if (closeBtn) {
      closeBtn.addEventListener('click', (e) => {
        e.preventDefault();
        console.log('창 닫기 버튼 클릭됨');
        closeChat();
      });
    }

    // 드래그 기능 추가
    this.setupDragToMove();

    // 메시지 입력 이벤트
    messageInput.addEventListener('input', (e) => {
      const hasText = e.target.value.trim().length > 0;
      sendBtn.disabled = !hasText;
      
      // 자동 높이 조절
      e.target.style.height = 'auto';
      e.target.style.height = Math.min(e.target.scrollHeight, 100) + 'px';
    });

    // Enter 키 이벤트
    messageInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
          e.preventDefault();
          console.log('Enter 키 입력 감지 - 메시지 전송 시작');
          this.sendMessage();
      }
    });

    // 전송 버튼 이벤트
    sendBtn.addEventListener('click', () => {
          console.log('전송 버튼 클릭 - 메시지 전송 시작');
          this.sendMessage();
    });

    // 창 닫기 이벤트는 initSecurityFeatures에서 처리
  }

  /**
   * 채팅창 드래그 이동 기능 설정
   */
  setupDragToMove() {
    const dragHandle = document.querySelector('.drag-handle');
    
    if (!dragHandle) {
      console.warn('드래그 핸들을 찾을 수 없습니다.');
      return;
    }

    let isDragging = false;
    let initialX;
    let initialY;

    // 드래그 핸들에서 마우스 다운 이벤트
    dragHandle.addEventListener('mousedown', (e) => {
      e.preventDefault();
      e.stopPropagation();
      
      isDragging = true;
      initialX = e.clientX;
      initialY = e.clientY;
      
      // 드래그 중임을 시각적으로 표시
      dragHandle.style.cursor = 'grabbing';
      dragHandle.style.color = 'white';
      
      // 부모 창에 드래그 시작 메시지 전달
      if (window.parent && window.parent !== window) {
        window.parent.postMessage({
          type: 'chatDragStart',
          x: initialX,
          y: initialY
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
          
          window.parent.postMessage({
            type: 'chatDragMove',
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
        
        // 커서 스타일 복원
        dragHandle.style.cursor = 'grab';
        dragHandle.style.color = 'rgba(255, 255, 255, 0.8)';
        
        // 부모 창에 드래그 종료 메시지 전달
        if (window.parent && window.parent !== window) {
          window.parent.postMessage({
            type: 'chatDragEnd'
          }, '*');
        }
      }
    });
  }

    setupReadDetection() {
    // 창 활성화/비활성화 감지
    window.addEventListener('focus', () => {
      this.isWindowActive = true;
      this.debouncedReadCheck('focus');
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

    // 페이지 언로드 감지 (창 닫기, 새로고침 등) - 상담 종료 모달로 대체

    // 스크롤 이벤트로 읽음 감지 (디바운싱 적용)
    const messageArea = document.getElementById('chatMessages');
    if (messageArea) {
      messageArea.addEventListener('scroll', () => {
        this.debouncedReadCheck('scroll');
      });
    }

    // 마우스 클릭 이벤트로 읽음 감지
    document.addEventListener('click', () => {
      this.debouncedReadCheck('click');
    });

    // 키보드 이벤트로 읽음 감지
    document.addEventListener('keydown', () => {
      this.debouncedReadCheck('keydown');
    });
  }

  // 읽음 처리 지연 함수 (상황에 따른 조건부 지연)
  markAsReadWithDelay() {
    if (this.readDelayTimeout) {
      clearTimeout(this.readDelayTimeout);
    }
    
    // 상황에 따른 지연 시간 결정
    let delayTime = 800; // 기본 800ms
    
    // 스크롤 이벤트로 인한 읽음 처리인 경우 더 빠르게
    if (this.lastEventType === 'scroll') {
      delayTime = 300; // 300ms
    }
    
    // 클릭 이벤트로 인한 읽음 처리인 경우 즉시
    if (this.lastEventType === 'click') {
      delayTime = 100; // 100ms
    }
    
    // 키보드 이벤트로 인한 읽음 처리인 경우 중간
    if (this.lastEventType === 'keydown') {
      delayTime = 500; // 500ms
    }
    
    this.readDelayTimeout = setTimeout(() => {
      this.markAsRead();
    }, delayTime);
  }

  // 통일된 디바운싱 읽음 체크 함수 (이벤트 타입 추적)
  debouncedReadCheck(eventType = 'unknown') {
    if (this.readTimeout) {
      clearTimeout(this.readTimeout);
    }
    
    // 이벤트 타입 저장
    this.lastEventType = eventType;
    
    this.readTimeout = setTimeout(() => {
      this.checkAndMarkAsRead();
    }, 200);
  }

  checkAndMarkAsRead() {
    // 세션이 없으면 처리하지 않음
    if (!this.currentSession) return;

    // 너무 자주 체크하지 않도록 제한 (500ms 간격으로 단축)
    const now = Date.now();
    if (now - this.lastReadCheck < 500) return; // 1000ms → 500ms로 단축
    this.lastReadCheck = now;

    // 읽음 처리 조건 확인
    if (!this.shouldMarkAsRead()) return;

    // 읽지 않은 상담자 메시지가 있는지 확인
    const unreadAdminMessages = this.messages.filter(m => 
      m.senderType === 'A' && m.isRead !== 'Y'
    );

    if (unreadAdminMessages.length > 0) {
      console.log('읽지 않은 상담자 메시지 발견:', unreadAdminMessages.length, '개');
      this.markAsReadWithDelay(); // 지연 후 읽음 처리
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

  async markAsRead() {
    if (!this.currentSession || !this.loginMember) return;

    // 읽지 않은 상담자 메시지가 있는지 확인
    const unreadAdminMessages = this.messages.filter(m => 
      m.senderType === 'A' && m.isRead !== 'Y'
    );

    // 이미 모든 메시지를 읽었다면 처리하지 않음
    if (unreadAdminMessages.length === 0) return;

    try {
      console.log('읽음 처리 시작:', unreadAdminMessages.length, '개 메시지');

      // 백엔드에 읽음 처리 요청
      await ajax.post(`/api/chat/sessions/${this.currentSession}/read`, {});

      // WebSocket으로 읽음 이벤트 전송
      if (this.stompClient && this.isConnected) {
        this.stompClient.send('/app/chat.read', {}, JSON.stringify({
          sessionId: this.currentSession,
          receiverId: this.loginMember.memberId,
          senderType: 'M'
        }));
      }

      // 로컬 메시지 상태를 읽음으로 업데이트
      this.messages = this.messages.map(m => 
        m.senderType === 'A' ? {...m, isRead: 'Y'} : m
      );

      // 읽음 카운트 업데이트
      this.updateReadCounts();

      console.log('읽음 처리 완료 - 읽지 않은 메시지:', unreadAdminMessages.length);
    } catch (error) {
      console.error('읽음 처리 실패:', error);
    }
  }

  async loadLoginInfo() {
    // 부모 창에서 로그인 정보 가져오기
    try {
      const loginMember = JSON.parse(sessionStorage.getItem('loginMember') || 'null');
      const selectedCategory = sessionStorage.getItem('selectedCategory');
      
      if (!loginMember || !loginMember.memberId) {
        this.showError('로그인이 필요합니다.');
        return;
      }
      
      if (!selectedCategory) {
        this.showError('카테고리가 선택되지 않았습니다.');
        return;
      }
      
      this.loginMember = loginMember;
      this.selectedCategory = selectedCategory;
      
      // 팝업 창 제목에 카테고리명 추가
      await this.updateWindowTitle();
      
      console.log('로그인 정보:', loginMember);
      console.log('선택된 카테고리:', selectedCategory);
      
    } catch (error) {
      console.error('로그인 정보 로드 실패:', error);
      this.showError('로그인 정보를 불러올 수 없습니다.');
    }
  }
  
  async updateWindowTitle() {
    try {
      // 백엔드에서 카테고리명 조회
      const data = await ajax.get(`/api/chat/category/${this.selectedCategory}`);
      
      if (data.code === '00' && data.data && data.data.categoryName) {
        const categoryName = data.data.categoryName;
        document.title = `1:1 상담 - ${categoryName} - KDT Demo`;
        
        // 헤더 제목도 업데이트
        const headerTitle = document.querySelector('.chat-header h2');
        if (headerTitle) {
          headerTitle.textContent = `1:1 상담 - ${categoryName}`;
        }
      } else {
        // API 호출 실패 시 기본값 사용
        document.title = `1:1 상담 - ${this.selectedCategory} - KDT Demo`;
      }
    } catch (error) {
      console.error('카테고리명 조회 실패:', error);
      // 오류 시 기본값 사용
      document.title = `1:1 상담 - ${this.selectedCategory} - KDT Demo`;
    }
  }

  async startChat() {
    if (!this.loginMember || !this.selectedCategory) {
      return;
    }

    try {
      // 채팅 세션 생성
      await this.createChatSession();
    } catch (error) {
      console.error('채팅 시작 실패:', error);
      this.showError('채팅을 시작할 수 없습니다.');
    }
  }

  async createChatSession() {
    try {
      console.log('채팅 세션 생성 시작:', this.selectedCategory);
      
      const data = await ajax.post('/api/chat/session/popup', {
        category: this.selectedCategory
      });

      console.log('API 응답 데이터:', data);
      
      if (data.code === '00' && data.data && data.data.sessionId) {
        this.currentSession = data.data.sessionId;
        console.log('세션 생성 성공:', this.currentSession);
        
        // WebSocket 연결
        this.connectWebSocket();
      } else {
        throw new Error(data.message || '채팅 세션 생성 실패');
      }
    } catch (error) {
      console.error('채팅 세션 생성 오류:', error);
      throw error;
    }
  }

  connectWebSocket() {
    console.log('=== WebSocket 연결 시작 ===');
    
    if (this.stompClient && this.stompClient.connected) {
      console.log('기존 WebSocket 연결 해제');
      this.stompClient.disconnect();
    }

    try {
      console.log('SockJS 소켓 생성 시도...');
    const socket = new SockJS('/ws');
      console.log('SockJS 소켓 생성 성공:', socket);
      
      console.log('STOMP 클라이언트 생성 시도...');
    this.stompClient = Stomp.over(socket);
      console.log('STOMP 클라이언트 생성 성공:', this.stompClient);
    
      console.log('STOMP 연결 시도...');
    this.stompClient.connect({}, 
      (frame) => {
          console.log('=== WebSocket 연결 성공 ===');
          console.log('연결 프레임:', frame);
        this.isConnected = true;
          
          console.log('채팅 세션 구독 시도...');
        this.subscribeToChat();
          
          console.log('사용자 참가 메시지 전송 시도...');
        this.sendUserJoin();
          
          console.log('로딩 메시지 제거...');
          this.clearLoading();
          
          console.log('기존 메시지 로드 시도...');
          this.loadChatHistory();
      },
      (error) => {
          console.error('=== WebSocket 연결 실패 ===');
          console.error('연결 오류:', error);
          console.error('오류 타입:', typeof error);
          console.error('오류 메시지:', error.message);
          console.error('오류 스택:', error.stack);
        this.isConnected = false;
          this.showError('연결에 실패했습니다. 페이지를 새로고침해주세요.');
        }
      );
    } catch (error) {
      console.error('=== WebSocket 초기화 실패 ===');
      console.error('초기화 오류:', error);
      this.isConnected = false;
      this.showError('연결 초기화에 실패했습니다. 페이지를 새로고침해주세요.');
    }
  }

  subscribeToChat() {
    if (!this.currentSession) return;
    
    this.stompClient.subscribe(`/topic/chat/${this.currentSession}`, (message) => {
      const chatMessage = JSON.parse(message.body);
      this.displayMessage(chatMessage);
    });
  }

  sendUserJoin() {
    if (!this.currentSession || !this.isConnected || !this.loginMember) return;
    
    console.log('사용자 참가 메시지 전송:', {
      sessionId: this.currentSession,
      senderId: this.loginMember.memberId,
      senderName: this.loginMember.nickname,
      senderType: 'M'
    });
    
    this.stompClient.send('/app/chat.addUser', {}, JSON.stringify({
      sessionId: this.currentSession,
      senderId: this.loginMember.memberId,
      senderName: this.loginMember.nickname,
      senderType: 'M'
    }));
  }

  sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');
    
    if (!messageInput || !sendBtn) {
      console.error('메시지 전송 실패: DOM 요소를 찾을 수 없습니다.');
      return;
    }
    
    const message = messageInput.value.trim();
    
    if (!message) {
      console.log('메시지가 비어있습니다.');
      return;
    }
    
    if (!this.isInitialized) {
      console.error('채팅이 아직 초기화되지 않았습니다.');
      this.showError('채팅을 초기화하는 중입니다. 잠시 후 다시 시도해주세요.');
      return;
    }
    
    if (!this.currentSession) {
      console.error('채팅 세션이 없습니다.');
      this.showError('채팅 세션을 찾을 수 없습니다. 페이지를 새로고침해주세요.');
      return;
    }
    
    if (!this.isConnected) {
      console.error('WebSocket 연결이 끊어졌습니다.');
      this.showError('연결이 끊어졌습니다. 페이지를 새로고침해주세요.');
      return;
    }
    
    if (!this.loginMember) {
      console.error('로그인 정보가 없습니다.');
      this.showError('로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요.');
      return;
    }
    
    const messageData = {
      sessionId: this.currentSession,
      senderId: this.loginMember.memberId,
      senderName: this.loginMember.nickname,
      content: message,
      senderType: 'M',
      messageTypeId: this.messageTypes['TEXT'], // 동적으로 로드한 TEXT 타입의 codeId 사용
      isRead: 'N', // 읽지 않음 상태로 설정
      timestamp: new Date().toISOString() // 클라이언트 타임스탬프
    };
    
    console.log('메시지 전송:', messageData);
    console.log('현재 WebSocket 연결 상태:', this.isConnected);
    console.log('현재 세션 ID:', this.currentSession);
    
    // 즉시 화면에 메시지 표시 (사용자 경험 개선)
    // 단, 이때는 messageId가 없으므로 읽음 카운트 업데이트하지 않음
    this.displayMessage(messageData);
    
    // 입력 필드 비활성화 (전송 중)
    messageInput.disabled = true;
    sendBtn.disabled = true;
    
    // WebSocket으로 서버에 전송
    try {
      this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(messageData));
      console.log('메시지 서버 전송 완료');
      
      // 전송 성공 후 입력 필드 초기화 및 활성화
      messageInput.value = '';
      messageInput.style.height = 'auto';
      messageInput.disabled = false;
    } catch (error) {
      console.error('메시지 서버 전송 실패:', error);
      // 전송 실패 시 메시지에 오류 표시
      this.markMessageAsError(messageData);
      
      // 전송 실패 시 입력 필드 활성화
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
      // 내가 보낸(M) 메시지들을 읽음 처리로 동기화
      this.messages = this.messages.map(m => 
        m.senderType === 'M' ? {...m, isRead: 'Y'} : m
      );
      this.handleReadEvent(message);
      return;
    }
    
    // 시스템 메시지는 중복 체크에서 제외
    if (message.senderType !== 'S') {
      // 중복 메시지 체크 (메시지 ID가 있으면 ID로, 없으면 내용+발신자+시간으로)
      const existingMessage = this.messages.find(m => {
        if (message.messageId && m.messageId) {
          return m.messageId === message.messageId;
        }
        // 내가 보낸 메시지의 경우 더 엄격한 중복 체크
        if (message.senderType === 'M' && m.senderType === 'M' && 
            message.senderId === this.loginMember?.memberId) {
          return m.content === message.content && 
                 Math.abs(new Date(m.timestamp) - new Date(message.timestamp)) < 1000; // 1초 이내
        }
        // 상담자 메시지의 경우 일반적인 중복 체크
        return m.content === message.content && 
               m.senderId === message.senderId &&
               m.senderType === message.senderType &&
               Math.abs(new Date(m.timestamp) - new Date(message.timestamp)) < 3000; // 3초 이내
      });
      
      if (existingMessage) {
        console.log('중복 메시지 무시:', message.content);
        // 서버에서 온 메시지에 messageId가 있다면 기존 메시지 업데이트
        if (message.messageId && !existingMessage.messageId) {
          existingMessage.messageId = message.messageId;
          console.log('기존 메시지에 messageId 추가:', message.messageId);
        }
        return;
      }
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
        console.log('상담 종료 메시지 감지 - 창 닫기');
        setTimeout(() => {
          this.handleSessionEnd();
        }, 1000); // 1초 후 종료 처리
      }
    } else if (message.senderId === this.loginMember.memberId) {
      messageClass += 'user'; // 내가 보낸 메시지
      // 내가 보낸 메시지는 기본적으로 안읽음으로 설정
      if (message.isRead !== 'Y') {
        this.unreadCount++;
      }
    } else {
      messageClass += 'admin'; // 상대방이 보낸 메시지
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
  
  handleReadEvent(message) {
    // 상대방이 메시지를 읽었을 때 모든 읽음 카운트 제거
    console.log('읽음 이벤트 처리:', message);
    
    // 읽음 이벤트의 receiverId를 확인하여 누가 읽었는지 판단
    const receiverId = message.receiverId || message.senderId;
    
    if (receiverId === this.loginMember.memberId) {
      // 상담자가 내 메시지를 읽었음 - 내 메시지들을 읽음 상태로 변경
      this.messages = this.messages.map(m => 
        m.senderType === 'M' && m.senderId === this.loginMember.memberId 
          ? {...m, isRead: 'Y'} 
          : m
      );
      console.log('상담자가 내 메시지를 읽었습니다. 모든 내 메시지를 읽음 처리');
    } else {
      // 내가 상담자 메시지를 읽었음 - 상담자 메시지들을 읽음 상태로 변경
      this.messages = this.messages.map(m => 
        m.senderType === 'A' ? {...m, isRead: 'Y'} : m
      );
      console.log('내가 상담자 메시지를 읽었습니다. 모든 상담자 메시지를 읽음 처리');
    }
    
    // 읽음 카운트 업데이트
    this.updateReadCounts();
    
    console.log('모든 읽음 카운트가 제거되었습니다.');
  }
  
  getReadCountHtml(message) {
    // 모든 메시지에 읽음 카운트 표시 (내가 보낸 메시지든 상대방이 보낸 메시지든)
    if (message.isRead !== 'Y') {
      // 내가 보낸 메시지인 경우
      if (message.senderType === 'M' && message.senderId === this.loginMember?.memberId) {
        const unreadMessages = this.messages.filter(m => 
          m.senderType === 'M' && 
          m.senderId === this.loginMember?.memberId && 
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
    }
    
    return '';
  }
  
  updateReadCounts() {
    // 모든 메시지의 읽음 카운트를 다시 계산하여 표시
    const messageElements = document.querySelectorAll('.message');
    
    // 내가 보낸 메시지 중 아직 읽지 않은 메시지들
    const unreadMyMessages = this.messages.filter(m => 
      m.senderType === 'M' && 
      m.senderId === this.loginMember?.memberId && 
      m.isRead !== 'Y'
    );
    
    // 상대방이 보낸 메시지 중 아직 읽지 않은 메시지들
    const unreadOtherMessages = this.messages.filter(m => 
      m.senderType === 'A' && 
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
      if (message.senderType === 'M' && message.senderId === this.loginMember?.memberId && message.isRead !== 'Y') {
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
      else if (message.senderType === 'A' && message.isRead !== 'Y') {
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

  async loadChatHistory() {
    if (!this.currentSession) return;
    
    try {
      const data = await ajax.get(`/api/chat/sessions/${this.currentSession}/messages`);
      
      if (data.code === '00' && data.data) {
        const messages = data.data;
        const messageArea = document.getElementById('chatMessages');
        
        if (!messageArea) {
          console.error('메시지 영역을 찾을 수 없습니다.');
          return;
        }
        
        // 기존 메시지 제거 (로딩 메시지 포함)
        messageArea.innerHTML = '';
        
        messages.forEach(message => {
          this.displayMessage(message);
        });
      }
    } catch (error) {
      console.error('메시지 히스토리 로드 실패:', error);
    }
  }

  clearLoading() {
    const messageArea = document.getElementById('chatMessages');
    if (!messageArea) {
      console.error('메시지 영역을 찾을 수 없습니다.');
      return;
    }
    
    const loadingElement = messageArea.querySelector('.loading');
    if (loadingElement) {
      loadingElement.remove();
    }
  }

  showError(message) {
    const messageArea = document.getElementById('chatMessages');
    if (!messageArea) {
      console.error('메시지 영역을 찾을 수 없습니다. 에러 메시지를 표시할 수 없습니다.');
      return;
    }
    
    messageArea.innerHTML = `
      <div class="error-message">
        <i class="fas fa-exclamation-triangle"></i>
        ${message}
      </div>
    `;
  }

  scrollToBottom() {
    const messageArea = document.getElementById('chatMessages');
    if (!messageArea) {
      console.error('메시지 영역을 찾을 수 없습니다.');
      return;
    }
    messageArea.scrollTop = messageArea.scrollHeight;
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

  // 일시 이탈/복귀 상태 알림
  async notifyPresence(state, reason) {
    if (!this.currentSession || !this.loginMember) return;
    
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
        side: 'MEMBER',
        state: state,
        reason: reason,
        graceSeconds: 300 // 5분 유예시간
      };

      await ajax.post(`/api/chat/sessions/${this.currentSession}/presence`, payload);
      
      if (state === 'INACTIVE') {
        this.presenceHiddenSent = true;
        console.log('일시 이탈 상태 알림 전송:', reason);
      } else {
        this.presenceHiddenSent = false;
        console.log('복귀 상태 알림 전송:', reason);
      }
    } catch (error) {
      console.error('일시 이탈/복귀 상태 알림 실패:', error);
    }
  }

  // 상담 종료
  async endChatSession(exitReasonId) {
    if (!this.currentSession || !this.loginMember) return;

    try {
      console.log('상담 종료 시작:', { sessionId: this.currentSession, exitReasonId });

      // 상담 종료 API 호출
      const payload = {
        sessionId: this.currentSession,
        exitReasonId: exitReasonId,
        endedBy: 'M',
        memberId: this.loginMember.memberId
      };

      await ajax.post(`/api/chat/sessions/${this.currentSession}/end`, payload);

      // WebSocket 연결 해제
      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.disconnect();
      }

      console.log('상담 종료 완료');
    } catch (error) {
      console.error('상담 종료 실패:', error);
      throw error;
    }
  }

  // 세션 종료 처리 (다른 쪽에서 종료한 경우)
  handleSessionEnd() {
    console.log('상담이 종료되었습니다 - 창 닫기');
    
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
          window.parent.postMessage({ type: 'closeChat' }, '*');
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

  // 정리 작업
  cleanup() {
    console.log('채팅 팝업 정리 작업 시작');
    
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
    
    // 상담 종료 처리 (기본 사유로 - WINDOW_CLOSE의 code_id를 사용)
    if (this.currentSession && this.loginMember) {
      // 기본 종료 사유: WINDOW_CLOSE (code_id: 7)
      this.endChatSession(7).catch(error => {
        console.error('창 닫기 시 상담 종료 실패:', error);
      });
    }
  }
}

// 팝업 창 닫기 함수
function closeChat() {
  console.log('closeChat 함수 호출됨');
  
  // 모달이 이미 표시되어 있으면 무시
  const exitModal = document.getElementById('exitModal');
  if (exitModal && exitModal.style.display === 'flex') {
    console.log('모달이 이미 표시되어 있음');
    return;
  }
  
  // 종료 사유 모달 표시
  showExitModal();
}

// 보안 기능 초기화
function initSecurityFeatures() {
  console.log('채팅창 보안 기능 초기화...');
  
  // 창 닫기 시 종료 사유 모달 표시
  let exitModalShown = false;
  let isExiting = false;
  
  // beforeunload 이벤트 - 타이틀바 종료 버튼 클릭 시 종료사유 모달 표시
  window.addEventListener('beforeunload', function(e) {
    console.log('beforeunload 이벤트 발생');
    
    // 상담 종료 중이면 종료 확인 메시지 표시하지 않음
    if (window.isExiting) {
      console.log('상담 종료 중이므로 종료 확인 메시지를 표시하지 않습니다.');
      return;
    }
    
    // 상담 세션이 있는 경우에만 종료 확인
    if (window.chatPopup && window.chatPopup.currentSession) {
      console.log('상담 세션 존재 - 종료 확인 메시지 표시');
      // 입력창 초기화하여 브라우저의 "변경사항" 경고 방지
      const messageInput = document.getElementById('messageInput');
      if (messageInput) {
        messageInput.value = '';
      }
      
      // 기본 종료 확인 메시지 표시
      e.preventDefault();
      e.returnValue = '상담을 종료하시겠습니까? 종료 사유를 선택해 주세요.';
      return e.returnValue;
    }
  });
  
  // visibilitychange 이벤트로 창 닫기 감지 (추가 방법)
  document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'hidden') {
      console.log('페이지 숨김 상태 - 창 닫기 가능성');
      // 창이 닫히는 중인지 확인하기 위한 플래그 설정
      window.isClosing = true;
    }
  });
  
  // unload 이벤트 - 창이 실제로 닫힐 때 상담 종료 처리
  window.addEventListener('unload', function() {
    console.log('unload 이벤트 발생');
    
    // 상담 종료 중이면 API 호출 건너뛰기
    if (window.isExiting) {
      console.log('상담 종료 중이므로 unload 이벤트에서 API 호출을 건너뜁니다.');
      return;
    }
    
    if (window.chatPopup && window.chatPopup.currentSession) {
      console.log('상담 종료 API 호출');
      // 상담 종료 API 호출 (sendBeacon 사용)
      const url = `/api/chat/sessions/${window.chatPopup.currentSession}/end`;
      const payload = JSON.stringify({ 
        exitReasonId: 150, // WINDOW_CLOSE에 해당하는 code_id
        endedBy: 'MEMBER' 
      });
      
      if (navigator.sendBeacon) {
        const blob = new Blob([payload], { type: 'application/json' });
        navigator.sendBeacon(url, blob);
      }
    }
  });
  
  // pagehide 이벤트 - 추가 창 닫기 감지
  window.addEventListener('pagehide', function(e) {
    console.log('pagehide 이벤트 발생', e.persisted);
    
    // 상담 종료 중이면 API 호출 건너뛰기
    if (window.isExiting) {
      console.log('상담 종료 중이므로 pagehide 이벤트에서 API 호출을 건너뜁니다.');
      return;
    }
    
    // persisted가 false면 창이 닫히는 것
    if (!e.persisted && window.chatPopup && window.chatPopup.currentSession) {
      console.log('창 닫기 감지 - 상담 종료 처리');
      // 상담 종료 API 호출
      const url = `/api/chat/sessions/${window.chatPopup.currentSession}/end`;
      const payload = JSON.stringify({ 
        exitReasonId: 150, // WINDOW_CLOSE에 해당하는 code_id
        endedBy: 'MEMBER' 
      });
      
      if (navigator.sendBeacon) {
        const blob = new Blob([payload], { type: 'application/json' });
        navigator.sendBeacon(url, blob);
      }
    }
  });
  
  // 기존 pagehide 이벤트는 위에서 처리하므로 제거
  
  // 창 닫기 감지를 위한 주기적 체크 (추가 안전장치)
  let windowFocusCheck = setInterval(function() {
    if (window.chatPopup && window.chatPopup.currentSession) {
      // 창이 포커스를 잃었는지 확인
      if (!document.hasFocus()) {
        console.log('창 포커스 손실 감지');
      }
    }
  }, 1000);
  
  // 페이지 언로드 시 인터벌 정리
  window.addEventListener('beforeunload', function() {
    if (windowFocusCheck) {
      clearInterval(windowFocusCheck);
    }
  });
  
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
  
  console.log('채팅창 보안 기능 초기화 완료');
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
async function showExitModal() {
  console.log('showExitModal 함수 호출됨');
  const exitModal = document.getElementById('exitModal');
  console.log('exitModal 요소:', exitModal);
  
  if (exitModal) {
    // 동적으로 종료사유 목록 로드
    await loadExitReasons();
    
    console.log('모달 표시 전 display 값:', exitModal.style.display);
    exitModal.style.display = 'flex';
    console.log('모달 표시 후 display 값:', exitModal.style.display);
    
    // 첫 번째 옵션을 기본 선택
    const firstOption = exitModal.querySelector('input[type="radio"]');
    if (firstOption) {
      firstOption.checked = true;
      console.log('첫 번째 라디오 버튼 선택됨');
    } else {
      console.log('라디오 버튼을 찾을 수 없음');
    }
    
    console.log('모달 표시 완료');
  } else {
    console.error('exitModal 요소를 찾을 수 없습니다!');
    console.log('현재 DOM 구조:', document.body.innerHTML);
  }
}

// 종료 사유 목록 로드
async function loadExitReasons() {
  try {
    console.log('종료 사유 목록 로드 시작');
    const data = await ajax.get('/api/chat/exit-reasons');
    
    if (data.code === '00' && data.data && data.data.length > 0) {
      const exitReasons = data.data;
      const exitReasonOptions = document.querySelector('.exit-reason-options');
      
      if (exitReasonOptions) {
        // 기존 옵션들 제거
        exitReasonOptions.innerHTML = '';
        
        // 동적으로 옵션 생성
        exitReasons.forEach(reason => {
          const optionDiv = document.createElement('div');
          optionDiv.className = 'exit-reason-option';
          
          optionDiv.innerHTML = `
            <input type="radio" id="reason-${reason.codeId}" name="exitReason" value="${reason.codeId}">
            <label for="reason-${reason.codeId}">${reason.decode}</label>
          `;
          
          exitReasonOptions.appendChild(optionDiv);
        });
        
        console.log('종료 사유 목록 로드 완료:', exitReasons.length, '개');
        return true; // 성공
      }
    } else {
      console.error('종료 사유 목록 로드 실패:', data);
      // 실패 시 기본 옵션 사용
      useDefaultExitReasons();
      return false;
    }
  } catch (error) {
    console.error('종료 사유 목록 로드 오류:', error);
    // 오류 시 기본 옵션 사용
    useDefaultExitReasons();
    return false;
  }
}

// 기본 종료 사유 옵션 (API 실패 시 사용)
function useDefaultExitReasons() {
  const exitReasonOptions = document.querySelector('.exit-reason-options');
  if (exitReasonOptions) {
    exitReasonOptions.innerHTML = `
      <div class="exit-reason-option">
        <input type="radio" id="reason-solved" name="exitReason" value="1">
        <label for="reason-solved">✅ 해결됨</label>
      </div>
      <div class="exit-reason-option">
        <input type="radio" id="reason-unsatisfied" name="exitReason" value="2">
        <label for="reason-unsatisfied">❌ 불만족</label>
      </div>
      <div class="exit-reason-option">
        <input type="radio" id="reason-other-method" name="exitReason" value="3">
        <label for="reason-other-method">🔄 다른 방법으로 문의</label>
      </div>
      <div class="exit-reason-option">
        <input type="radio" id="reason-later" name="exitReason" value="4">
        <label for="reason-later">⏰ 나중에 다시 문의</label>
      </div>
      <div class="exit-reason-option">
        <input type="radio" id="reason-phone" name="exitReason" value="5">
        <label for="reason-phone">📞 전화 상담 희망</label>
      </div>
      <div class="exit-reason-option">
        <input type="radio" id="reason-just-exit" name="exitReason" value="6">
        <label for="reason-just-exit">🚪 그냥 종료</label>
      </div>
    `;
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
    if (window.chatPopup && window.chatPopup.currentSession) {
      await window.chatPopup.endChatSession(exitReasonId);
    }
    
    // 모달 숨김
    hideExitModal();
    
    // iframe 방식인 경우 부모 창의 iframe 제거
    if (window.parent !== window) {
      // iframe 내부에서 실행되는 경우
      try {
        window.parent.postMessage({ type: 'closeChat' }, '*');
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
}

// 풀스크린 모드 비활성화 (iframe 모달에서는 사용하지 않음)
function tryFullscreenMode() {
  console.log('풀스크린 모드 비활성화됨 (iframe 모달 방식)');
  // 풀스크린 요청하지 않음
}

// DOM 로드 완료 후 초기화
function initializeChatPopup() {
  // 풀스크린 모드 시도 (브라우저 UI 숨기기)
  tryFullscreenMode();
  
  // 보안 기능 초기화
  initSecurityFeatures();
  
  // DOM 요소 존재 확인
  const messageInput = document.getElementById('messageInput');
  const sendBtn = document.getElementById('sendBtn');
  const chatMessages = document.getElementById('chatMessages');
  
  console.log('DOM 요소 확인:', {
    messageInput: !!messageInput,
    sendBtn: !!sendBtn,
    chatMessages: !!chatMessages
  });
  
  // 전체 HTML 구조 확인
  console.log('현재 HTML 구조:', document.body.innerHTML);
  
  if (!messageInput || !sendBtn || !chatMessages) {
    console.error('필수 DOM 요소가 없습니다. 페이지를 새로고침해주세요.');
    console.error('누락된 요소:', {
      messageInput: !messageInput ? 'messageInput 없음' : 'OK',
      sendBtn: !sendBtn ? 'sendBtn 없음' : 'OK',
      chatMessages: !chatMessages ? 'chatMessages 없음' : 'OK'
    });
    return false;
  }
  
  try {
    window.chatPopup = new ChatPopup();
    console.log('팝업 채팅 초기화 완료');
    return true;
  } catch (error) {
    console.error('팝업 채팅 초기화 실패:', error);
    return false;
  }
}

// 여러 방법으로 초기화 시도
document.addEventListener('DOMContentLoaded', () => {
  if (!initializeChatPopup()) {
    // DOMContentLoaded에서 실패하면 약간 지연 후 재시도
    setTimeout(() => {
      if (!initializeChatPopup()) {
        console.error('채팅 팝업 초기화에 실패했습니다.');
      }
    }, 100);
  }
});

// 추가 안전장치: window.onload에서도 시도
window.addEventListener('load', () => {
  if (!window.chatPopup) {
    console.log('window.onload에서 채팅 팝업 초기화 시도');
    initializeChatPopup();
  }
});
