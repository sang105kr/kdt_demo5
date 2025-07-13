/**
 * METAL DESIGN SYSTEM - JavaScript Utilities
 * 메탈 느낌의 모던한 디자인 시스템을 위한 공통 JavaScript 유틸리티
 */

// ===== DOM UTILITIES =====
const MetalUI = {
  // DOM 요소 생성 헬퍼
  createElement(tag, className, textContent) {
    const element = document.createElement(tag);
    if (className) element.className = className;
    if (textContent) element.textContent = textContent;
    return element;
  },

  // 요소 선택 헬퍼
  $(selector) {
    return document.querySelector(selector);
  },

  $$(selector) {
    return document.querySelectorAll(selector);
  },

  // 부모 요소 찾기
  findParent(element, selector) {
    while (element && element !== document) {
      if (element.matches(selector)) return element;
      element = element.parentElement;
    }
    return null;
  },

  // ===== ANIMATION UTILITIES =====
  fadeIn(element, duration = 300) {
    element.style.opacity = '0';
    element.style.display = 'block';
    
    let start = null;
    const animate = (timestamp) => {
      if (!start) start = timestamp;
      const progress = timestamp - start;
      const opacity = Math.min(progress / duration, 1);
      
      element.style.opacity = opacity;
      
      if (progress < duration) {
        requestAnimationFrame(animate);
      }
    };
    
    requestAnimationFrame(animate);
  },

  fadeOut(element, duration = 300) {
    let start = null;
    const animate = (timestamp) => {
      if (!start) start = timestamp;
      const progress = timestamp - start;
      const opacity = Math.max(1 - progress / duration, 0);
      
      element.style.opacity = opacity;
      
      if (progress < duration) {
        requestAnimationFrame(animate);
      } else {
        element.style.display = 'none';
      }
    };
    
    requestAnimationFrame(animate);
  },

  slideDown(element, duration = 300) {
    element.style.height = '0';
    element.style.overflow = 'hidden';
    element.style.display = 'block';
    
    const targetHeight = element.scrollHeight;
    let start = null;
    
    const animate = (timestamp) => {
      if (!start) start = timestamp;
      const progress = timestamp - start;
      const height = Math.min((progress / duration) * targetHeight, targetHeight);
      
      element.style.height = height + 'px';
      
      if (progress < duration) {
        requestAnimationFrame(animate);
      } else {
        element.style.height = 'auto';
        element.style.overflow = 'visible';
      }
    };
    
    requestAnimationFrame(animate);
  },

  slideUp(element, duration = 300) {
    const targetHeight = element.scrollHeight;
    element.style.height = targetHeight + 'px';
    element.style.overflow = 'hidden';
    
    let start = null;
    
    const animate = (timestamp) => {
      if (!start) start = timestamp;
      const progress = timestamp - start;
      const height = Math.max(targetHeight - (progress / duration) * targetHeight, 0);
      
      element.style.height = height + 'px';
      
      if (progress < duration) {
        requestAnimationFrame(animate);
      } else {
        element.style.display = 'none';
        element.style.height = 'auto';
        element.style.overflow = 'visible';
      }
    };
    
    requestAnimationFrame(animate);
  },

  // ===== MODAL UTILITIES =====
  createModal(options = {}) {
    const {
      title = 'Modal',
      content = '',
      size = 'medium',
      closable = true,
      onClose = null,
      onConfirm = null,
      confirmText = '확인',
      cancelText = '취소'
    } = options;

    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
      <div class="modal-dialog ${size === 'large' ? 'w-3/4' : size === 'small' ? 'w-1/3' : 'w-1/2'}">
        <div class="modal-header">
          <h3 class="modal-title">${title}</h3>
          ${closable ? '<button class="modal-close">&times;</button>' : ''}
        </div>
        <div class="modal-body">
          ${typeof content === 'string' ? content : ''}
        </div>
        ${onConfirm ? `
          <div class="modal-footer">
            <button class="btn btn-secondary" data-action="cancel">${cancelText}</button>
            <button class="btn btn-primary" data-action="confirm">${confirmText}</button>
          </div>
        ` : ''}
      </div>
    `;

    // 이벤트 리스너 추가
    if (closable) {
      const closeBtn = modal.querySelector('.modal-close');
      closeBtn.addEventListener('click', () => {
        MetalUI.closeModal(modal);
        if (onClose) onClose();
      });
    }

    if (onConfirm) {
      const confirmBtn = modal.querySelector('[data-action="confirm"]');
      const cancelBtn = modal.querySelector('[data-action="cancel"]');
      
      confirmBtn.addEventListener('click', () => {
        onConfirm();
        MetalUI.closeModal(modal);
      });
      
      cancelBtn.addEventListener('click', () => {
        MetalUI.closeModal(modal);
        if (onClose) onClose();
      });
    }

    // 배경 클릭으로 닫기
    modal.addEventListener('click', (e) => {
      if (e.target === modal && closable) {
        MetalUI.closeModal(modal);
        if (onClose) onClose();
      }
    });

    return modal;
  },

  showModal(modal) {
    document.body.appendChild(modal);
    // 강제 리플로우 후 클래스 추가
    modal.offsetHeight;
    modal.classList.add('show');
  },

  closeModal(modal) {
    modal.classList.remove('show');
    setTimeout(() => {
      if (modal.parentNode) {
        modal.parentNode.removeChild(modal);
      }
    }, 300);
  },

  // ===== TOAST NOTIFICATION =====
  showToast(message, type = 'info', duration = 3000) {
    const toast = document.createElement('div');
    toast.className = `alert alert-${type} fixed top-4 right-4 z-50 max-w-sm`;
    toast.textContent = message;
    
    document.body.appendChild(toast);
    
    // 애니메이션 효과
    toast.style.transform = 'translateX(100%)';
    toast.style.opacity = '0';
    
    setTimeout(() => {
      toast.style.transform = 'translateX(0)';
      toast.style.opacity = '1';
    }, 10);
    
    setTimeout(() => {
      toast.style.transform = 'translateX(100%)';
      toast.style.opacity = '0';
      setTimeout(() => {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast);
        }
      }, 300);
    }, duration);
  },

  // ===== FORM UTILITIES =====
  validateForm(form) {
    const inputs = form.querySelectorAll('input, select, textarea');
    let isValid = true;
    const errors = [];

    inputs.forEach(input => {
      const value = input.value.trim();
      const required = input.hasAttribute('required');
      const minLength = input.getAttribute('minlength');
      const maxLength = input.getAttribute('maxlength');
      const pattern = input.getAttribute('pattern');
      const type = input.getAttribute('type');

      // 필수 필드 검증
      if (required && !value) {
        isValid = false;
        errors.push(`${input.name || '필드'}는 필수입니다.`);
        this.showFieldError(input, '필수 필드입니다.');
      } else {
        this.clearFieldError(input);
      }

      // 길이 검증
      if (value) {
        if (minLength && value.length < parseInt(minLength)) {
          isValid = false;
          errors.push(`${input.name || '필드'}는 최소 ${minLength}자 이상이어야 합니다.`);
          this.showFieldError(input, `최소 ${minLength}자 이상이어야 합니다.`);
        }
        
        if (maxLength && value.length > parseInt(maxLength)) {
          isValid = false;
          errors.push(`${input.name || '필드'}는 최대 ${maxLength}자까지 입력 가능합니다.`);
          this.showFieldError(input, `최대 ${maxLength}자까지 입력 가능합니다.`);
        }
      }

      // 패턴 검증
      if (pattern && value && !new RegExp(pattern).test(value)) {
        isValid = false;
        errors.push(`${input.name || '필드'}의 형식이 올바르지 않습니다.`);
        this.showFieldError(input, '형식이 올바르지 않습니다.');
      }

      // 이메일 검증
      if (type === 'email' && value && !this.isValidEmail(value)) {
        isValid = false;
        errors.push('올바른 이메일 형식이 아닙니다.');
        this.showFieldError(input, '올바른 이메일 형식이 아닙니다.');
      }
    });

    return { isValid, errors };
  },

  showFieldError(input, message) {
    this.clearFieldError(input);
    input.classList.add('border-danger');
    
    const errorDiv = document.createElement('div');
    errorDiv.className = 'form-error';
    errorDiv.textContent = message;
    input.parentNode.appendChild(errorDiv);
  },

  clearFieldError(input) {
    input.classList.remove('border-danger');
    const errorDiv = input.parentNode.querySelector('.form-error');
    if (errorDiv) {
      errorDiv.remove();
    }
  },

  isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  },

  // ===== AJAX UTILITIES =====
  async fetch(url, options = {}) {
    const defaultOptions = {
      headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
      }
    };

    const config = { ...defaultOptions, ...options };
    
    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      } else {
        return await response.text();
      }
    } catch (error) {
      console.error('Fetch error:', error);
      throw error;
    }
  },

  // ===== STORAGE UTILITIES =====
  setStorage(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error('Storage error:', error);
    }
  },

  getStorage(key, defaultValue = null) {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : defaultValue;
    } catch (error) {
      console.error('Storage error:', error);
      return defaultValue;
    }
  },

  removeStorage(key) {
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.error('Storage error:', error);
    }
  },

  // ===== UTILITY FUNCTIONS =====
  debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  },

  throttle(func, limit) {
    let inThrottle;
    return function() {
      const args = arguments;
      const context = this;
      if (!inThrottle) {
        func.apply(context, args);
        inThrottle = true;
        setTimeout(() => inThrottle = false, limit);
      }
    };
  },

  formatCurrency(amount, currency = 'KRW') {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: currency
    }).format(amount);
  },

  formatDate(date, options = {}) {
    const defaultOptions = {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    };
    
    return new Intl.DateTimeFormat('ko-KR', { ...defaultOptions, ...options }).format(new Date(date));
  },

  // ===== EVENT DELEGATION =====
  on(event, selector, handler) {
    document.addEventListener(event, (e) => {
      const target = e.target.closest(selector);
      if (target) {
        handler.call(target, e);
      }
    });
  },

  // ===== INITIALIZATION =====
  init() {
    // 전역 이벤트 리스너 설정
    this.setupGlobalEvents();
    
    // 자동 초기화
    this.autoInit();
  },

  setupGlobalEvents() {
    // 폼 제출 이벤트
    this.on('submit', 'form[data-validate]', (e) => {
      e.preventDefault();
      const { isValid, errors } = this.validateForm(e.target);
      
      if (!isValid) {
        this.showToast(errors[0], 'danger');
        return false;
      }
      
      // 폼 제출 처리
      this.handleFormSubmit(e.target);
    });

    // 버튼 클릭 이벤트
    this.on('click', '[data-modal]', (e) => {
      e.preventDefault();
      const modalConfig = JSON.parse(e.target.dataset.modal);
      const modal = this.createModal(modalConfig);
      this.showModal(modal);
    });

    // 토스트 닫기 이벤트
    this.on('click', '.alert .close', (e) => {
      e.target.closest('.alert').remove();
    });
  },

  autoInit() {
    // 자동으로 초기화할 요소들
    this.initTooltips();
    this.initDropdowns();
    this.initTabs();
  },

  initTooltips() {
    const tooltips = document.querySelectorAll('[data-tooltip]');
    tooltips.forEach(element => {
      const text = element.dataset.tooltip;
      element.classList.add('tooltip');
      
      if (!element.querySelector('.tooltip-text')) {
        const tooltipText = document.createElement('span');
        tooltipText.className = 'tooltip-text';
        tooltipText.textContent = text;
        element.appendChild(tooltipText);
      }
    });
  },

  initDropdowns() {
    const dropdowns = document.querySelectorAll('[data-dropdown]');
    dropdowns.forEach(dropdown => {
      const toggle = dropdown.querySelector('[data-dropdown-toggle]');
      const menu = dropdown.querySelector('[data-dropdown-menu]');
      
      if (toggle && menu) {
        toggle.addEventListener('click', (e) => {
          e.preventDefault();
          menu.classList.toggle('show');
        });
      }
    });
  },

  initTabs() {
    const tabContainers = document.querySelectorAll('[data-tabs]');
    tabContainers.forEach(container => {
      const tabs = container.querySelectorAll('[data-tab]');
      const contents = container.querySelectorAll('[data-tab-content]');
      
      tabs.forEach(tab => {
        tab.addEventListener('click', (e) => {
          e.preventDefault();
          const target = tab.dataset.tab;
          
          // 모든 탭 비활성화
          tabs.forEach(t => t.classList.remove('active'));
          contents.forEach(c => c.classList.remove('active'));
          
          // 선택된 탭 활성화
          tab.classList.add('active');
          const content = container.querySelector(`[data-tab-content="${target}"]`);
          if (content) content.classList.add('active');
        });
      });
    });
  },

  async handleFormSubmit(form) {
    const submitBtn = form.querySelector('[type="submit"]');
    const originalText = submitBtn.textContent;
    
    // 로딩 상태 표시
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner spinner-sm"></span> 처리중...';
    
    try {
      const formData = new FormData(form);
      const response = await this.fetch(form.action, {
        method: form.method || 'POST',
        body: formData
      });
      
      this.showToast('처리되었습니다.', 'success');
      
      // 성공 후 처리 (리다이렉트 또는 콜백)
      if (response.redirect) {
        window.location.href = response.redirect;
      }
      
    } catch (error) {
      this.showToast('오류가 발생했습니다.', 'danger');
      console.error('Form submission error:', error);
    } finally {
      // 버튼 상태 복원
      submitBtn.disabled = false;
      submitBtn.textContent = originalText;
    }
  }
};

// 전역 객체로 노출
window.MetalUI = MetalUI;

// DOM 로드 완료 시 초기화
document.addEventListener('DOMContentLoaded', () => {
  MetalUI.init();
});

// 모듈로 내보내기 (ES6 모듈 환경에서)
if (typeof module !== 'undefined' && module.exports) {
  module.exports = MetalUI;
} 