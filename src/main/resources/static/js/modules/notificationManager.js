/**
 * NotificationManager - 알림 메시지 관리를 중앙화하는 모듈
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

class NotificationManager {
  constructor() {
    this.notifications = [];
    this.container = null;
    this.maxNotifications = 5;
    this.autoHideDelay = 5000;
    this.initialized = false;
    
    // DOM이 준비된 후 초기화
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', () => this.init());
    } else {
      this.init();
    }
  }

  /**
   * NotificationManager 초기화
   */
  init() {
    if (this.initialized) return;
    
    this.createContainer();
    this.loadNotificationStyles();
    this.initialized = true;
  }

  /**
   * 알림 컨테이너 생성
   */
  createContainer() {
    // 이미 컨테이너가 존재하는지 확인
    if (document.getElementById('notification-container')) {
      this.container = document.getElementById('notification-container');
      return;
    }
    
    this.container = document.createElement('div');
    this.container.id = 'notification-container';
    this.container.className = 'notification-container';
    
    // body가 존재하는지 확인
    if (document.body) {
      document.body.appendChild(this.container);
    } else {
      // body가 아직 준비되지 않은 경우 대기
      const checkBody = () => {
        if (document.body) {
          document.body.appendChild(this.container);
        } else {
          setTimeout(checkBody, 10);
        }
      };
      checkBody();
    }
  }

  /**
   * 알림 스타일 로드
   */
  loadNotificationStyles() {
    const notificationStyles = `
      .notification-container {
        position: fixed;
        top: var(--space-lg);
        right: var(--space-lg);
        z-index: var(--z-notification);
        display: flex;
        flex-direction: column;
        gap: var(--space-sm);
        max-width: 400px;
        pointer-events: none;
      }

      .notification {
        background: var(--color-white);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-md);
        padding: var(--space-md);
        box-shadow: var(--shadow-lg);
        margin-bottom: var(--space-sm);
        pointer-events: auto;
        opacity: 0;
        transform: translateX(100%);
        transition: all var(--transition-normal);
        position: relative;
        overflow: hidden;
      }

      .notification.show {
        opacity: 1;
        transform: translateX(0);
      }

      .notification.hide {
        opacity: 0;
        transform: translateX(100%);
      }

      .notification.success {
        border-left: 4px solid var(--color-success);
      }

      .notification.error {
        border-left: 4px solid var(--color-error);
      }

      .notification.warning {
        border-left: 4px solid var(--color-warning);
      }

      .notification.info {
        border-left: 4px solid var(--color-info);
      }

      .notification-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: var(--space-xs);
      }

      .notification-title {
        font-weight: 600;
        font-size: var(--font-size-sm);
        color: var(--color-text);
        margin: 0;
        flex: 1;
      }

      .notification-close {
        background: none;
        border: none;
        color: var(--color-text-muted);
        cursor: pointer;
        padding: var(--space-xs);
        margin-left: var(--space-sm);
        border-radius: var(--radius-sm);
        transition: all var(--transition-fast);
        font-size: var(--font-size-sm);
      }

      .notification-close:hover {
        background: var(--color-light-gray);
        color: var(--color-text);
      }

      .notification-message {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
        line-height: var(--line-height-relaxed);
        margin: 0;
      }

      .notification-progress {
        position: absolute;
        bottom: 0;
        left: 0;
        height: 2px;
        background: var(--color-primary);
        transition: width linear;
      }

      .notification.success .notification-progress {
        background: var(--color-success);
      }

      .notification.error .notification-progress {
        background: var(--color-error);
      }

      .notification.warning .notification-progress {
        background: var(--color-warning);
      }

      .notification.info .notification-progress {
        background: var(--color-info);
      }

      /* 반응형 디자인 */
      @media (max-width: 768px) {
        .notification-container {
          top: var(--space-sm);
          right: var(--space-sm);
          left: var(--space-sm);
          max-width: none;
        }

        .notification {
          padding: var(--space-sm);
        }
      }
    `;

    // CSSManager를 사용하여 스타일 추가
    if (window.CSSManager) {
      window.CSSManager.addStyle('notifications', notificationStyles);
    }
  }

  /**
   * 알림 생성
   * @param {Object} options - 알림 옵션
   * @returns {HTMLElement} 알림 엘리먼트
   */
  createNotification(options = {}) {
    const {
      type = 'info',
      title = '',
      message = '',
      duration = this.autoHideDelay,
      closable = true,
      showProgress = true
    } = options;

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    const progressBar = showProgress ? '<div class="notification-progress"></div>' : '';
    
    notification.innerHTML = `
      <div class="notification-header">
        <h4 class="notification-title">${title}</h4>
        ${closable ? '<button class="notification-close">&times;</button>' : ''}
      </div>
      <p class="notification-message">${message}</p>
      ${progressBar}
    `;

    // 닫기 버튼 이벤트
    if (closable) {
      const closeBtn = notification.querySelector('.notification-close');
      closeBtn.addEventListener('click', () => {
        this.removeNotification(notification);
      });
    }

    // 프로그레스 바 애니메이션
    if (showProgress && duration > 0) {
      const progressBar = notification.querySelector('.notification-progress');
      if (progressBar) {
        progressBar.style.width = '100%';
        progressBar.style.transition = `width ${duration}ms linear`;
        
        setTimeout(() => {
          progressBar.style.width = '0%';
        }, 100);
      }
    }

    return notification;
  }

  /**
   * 알림 표시
   * @param {Object} options - 알림 옵션
   * @returns {HTMLElement} 알림 엘리먼트
   */
  show(options = {}) {
    const notification = this.createNotification(options);
    
    // 최대 알림 개수 제한
    if (this.notifications.length >= this.maxNotifications) {
      this.removeNotification(this.notifications[0]);
    }

    this.container.appendChild(notification);
    this.notifications.push(notification);

    // 애니메이션 적용
    requestAnimationFrame(() => {
      notification.classList.add('show');
    });

    // 자동 숨김
    if (options.duration !== 0) {
      setTimeout(() => {
        this.removeNotification(notification);
      }, options.duration || this.autoHideDelay);
    }

    return notification;
  }

  /**
   * 성공 알림
   * @param {string} message - 메시지
   * @param {string} title - 제목
   * @param {Object} options - 추가 옵션
   */
  success(message, title = '성공', options = {}) {
    return this.show({
      type: 'success',
      title,
      message,
      ...options
    });
  }

  /**
   * 오류 알림
   * @param {string} message - 메시지
   * @param {string} title - 제목
   * @param {Object} options - 추가 옵션
   */
  error(message, title = '오류', options = {}) {
    return this.show({
      type: 'error',
      title,
      message,
      duration: 0, // 오류는 수동으로 닫기
      ...options
    });
  }

  /**
   * 경고 알림
   * @param {string} message - 메시지
   * @param {string} title - 제목
   * @param {Object} options - 추가 옵션
   */
  warning(message, title = '경고', options = {}) {
    return this.show({
      type: 'warning',
      title,
      message,
      ...options
    });
  }

  /**
   * 정보 알림
   * @param {string} message - 메시지
   * @param {string} title - 제목
   * @param {Object} options - 추가 옵션
   */
  info(message, title = '정보', options = {}) {
    return this.show({
      type: 'info',
      title,
      message,
      ...options
    });
  }

  /**
   * 알림 제거
   * @param {HTMLElement} notification - 알림 엘리먼트
   */
  removeNotification(notification) {
    if (!notification || !notification.parentNode) return;

    notification.classList.add('hide');
    
    setTimeout(() => {
      if (notification.parentNode) {
        notification.parentNode.removeChild(notification);
      }
      
      const index = this.notifications.indexOf(notification);
      if (index > -1) {
        this.notifications.splice(index, 1);
      }
    }, 300);
  }

  /**
   * 모든 알림 제거
   */
  clearAll() {
    this.notifications.forEach(notification => {
      this.removeNotification(notification);
    });
  }

  /**
   * 특정 타입의 알림 제거
   * @param {string} type - 알림 타입
   */
  clearByType(type) {
    this.notifications
      .filter(notification => notification.classList.contains(type))
      .forEach(notification => {
        this.removeNotification(notification);
      });
  }

  /**
   * 설정 업데이트
   * @param {Object} config - 설정 객체
   */
  updateConfig(config) {
    Object.assign(this, config);
  }

  /**
   * 정리
   */
  destroy() {
    this.clearAll();
    if (this.container && this.container.parentNode) {
      this.container.parentNode.removeChild(this.container);
    }
    this.notifications = [];
  }
}

// 전역 인스턴스 생성 - DOM이 준비된 후에 실행
const initNotificationManager = () => {
  if (!window.NotificationManager) {
    window.NotificationManager = new NotificationManager();
    
    // 편의 함수들
    window.notify = {
      success: (message, title, options) => window.NotificationManager.success(message, title, options),
      error: (message, title, options) => window.NotificationManager.error(message, title, options),
      warning: (message, title, options) => window.NotificationManager.warning(message, title, options),
      info: (message, title, options) => window.NotificationManager.info(message, title, options),
      clear: () => window.NotificationManager.clearAll()
    };
  }
};

// DOM이 준비된 후 초기화
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initNotificationManager);
} else {
  initNotificationManager();
}

// 모듈 내보내기
if (typeof module !== 'undefined' && module.exports) {
  module.exports = NotificationManager;
} 