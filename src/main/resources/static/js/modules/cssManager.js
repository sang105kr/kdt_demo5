/**
 * CSSManager - 동적 CSS 생성을 중앙화하고 관리하는 모듈
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

class CSSManager {
  constructor() {
    this.styles = new Map();
    this.styleElement = null;
    this.init();
  }

  /**
   * CSSManager 초기화
   */
  init() {
    this.createStyleElement();
    this.loadCommonStyles();
  }

  /**
   * 스타일 엘리먼트 생성
   */
  createStyleElement() {
    this.styleElement = document.createElement('style');
    this.styleElement.id = 'dynamic-styles';
    this.styleElement.setAttribute('data-manager', 'css-manager');
    document.head.appendChild(this.styleElement);
  }

  /**
   * 공통 스타일 로드
   */
  loadCommonStyles() {
    const commonStyles = `
      /* 동적 생성 스타일 - CSSManager */
      .dynamic-loading {
        opacity: 0.6;
        pointer-events: none;
        transition: opacity var(--transition-normal);
      }

      .dynamic-error {
        border-color: var(--color-error) !important;
        box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1) !important;
      }

      .dynamic-success {
        border-color: var(--color-success) !important;
        box-shadow: 0 0 0 2px rgba(34, 40, 49, 0.1) !important;
      }

      .dynamic-warning {
        border-color: var(--color-warning) !important;
        box-shadow: 0 0 0 2px rgba(57, 62, 70, 0.1) !important;
      }

      .dynamic-info {
        border-color: var(--color-info) !important;
        box-shadow: 0 0 0 2px rgba(102, 102, 102, 0.1) !important;
      }

      /* 모달 오버레이 */
      .modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        z-index: var(--z-modal);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        visibility: hidden;
        transition: all var(--transition-normal);
      }

      .modal-overlay.active {
        opacity: 1;
        visibility: visible;
      }

      /* 툴팁 */
      .tooltip {
        position: relative;
        display: inline-block;
      }

      .tooltip .tooltip-text {
        visibility: hidden;
        width: 200px;
        background: var(--color-gray-900);
        color: var(--color-white);
        text-align: center;
        border-radius: var(--radius-sm);
        padding: var(--space-sm);
        position: absolute;
        z-index: var(--z-tooltip);
        bottom: 125%;
        left: 50%;
        margin-left: -100px;
        opacity: 0;
        transition: opacity var(--transition-fast);
        font-size: var(--font-size-sm);
        line-height: var(--line-height-tight);
      }

      .tooltip .tooltip-text::after {
        content: "";
        position: absolute;
        top: 100%;
        left: 50%;
        margin-left: -5px;
        border-width: 5px;
        border-style: solid;
        border-color: var(--color-gray-900) transparent transparent transparent;
      }

      .tooltip:hover .tooltip-text {
        visibility: visible;
        opacity: 1;
      }

      /* 로딩 스피너 */
      .loading-spinner {
        display: inline-block;
        width: 20px;
        height: 20px;
        border: 2px solid var(--color-border);
        border-radius: 50%;
        border-top-color: var(--color-primary);
        animation: spin 1s ease-in-out infinite;
      }

      @keyframes spin {
        to { transform: rotate(360deg); }
      }

      /* 페이드 인 애니메이션 */
      .fade-in {
        animation: fadeIn var(--transition-normal);
      }

      @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
      }

      /* 슬라이드 인 애니메이션 */
      .slide-in {
        animation: slideIn var(--transition-normal);
      }

      @keyframes slideIn {
        from { 
          opacity: 0;
          transform: translateY(20px);
        }
        to { 
          opacity: 1;
          transform: translateY(0);
        }
      }

      /* 스케일 인 애니메이션 */
      .scale-in {
        animation: scaleIn var(--transition-normal);
      }

      @keyframes scaleIn {
        from { 
          opacity: 0;
          transform: scale(0.9);
        }
        to { 
          opacity: 1;
          transform: scale(1);
        }
      }
    `;

    this.addStyle('common', commonStyles);
  }

  /**
   * 스타일 추가
   * @param {string} name - 스타일 이름
   * @param {string} css - CSS 문자열
   */
  addStyle(name, css) {
    this.styles.set(name, css);
    this.updateStyles();
  }

  /**
   * 스타일 제거
   * @param {string} name - 스타일 이름
   */
  removeStyle(name) {
    this.styles.delete(name);
    this.updateStyles();
  }

  /**
   * 스타일 업데이트
   */
  updateStyles() {
    const allStyles = Array.from(this.styles.values()).join('\n');
    this.styleElement.textContent = allStyles;
  }

  /**
   * 인라인 스타일 적용
   * @param {HTMLElement} element - 대상 엘리먼트
   * @param {Object} styles - 스타일 객체
   */
  applyInlineStyles(element, styles) {
    Object.assign(element.style, styles);
  }

  /**
   * 클래스 토글
   * @param {HTMLElement} element - 대상 엘리먼트
   * @param {string} className - 클래스 이름
   * @param {boolean} force - 강제 적용 여부
   */
  toggleClass(element, className, force = null) {
    if (force === null) {
      element.classList.toggle(className);
    } else {
      element.classList.toggle(className, force);
    }
  }

  /**
   * 로딩 상태 적용
   * @param {HTMLElement} element - 대상 엘리먼트
   * @param {boolean} isLoading - 로딩 상태
   */
  setLoading(element, isLoading) {
    this.toggleClass(element, 'dynamic-loading', isLoading);
  }

  /**
   * 상태 스타일 적용
   * @param {HTMLElement} element - 대상 엘리먼트
   * @param {string} status - 상태 (success, error, warning, info)
   */
  setStatus(element, status) {
    const statusClasses = ['dynamic-success', 'dynamic-error', 'dynamic-warning', 'dynamic-info'];
    statusClasses.forEach(cls => element.classList.remove(cls));
    
    if (status) {
      element.classList.add(`dynamic-${status}`);
    }
  }

  /**
   * 애니메이션 클래스 적용
   * @param {HTMLElement} element - 대상 엘리먼트
   * @param {string} animation - 애니메이션 타입 (fade-in, slide-in, scale-in)
   * @param {number} duration - 지속 시간 (ms)
   */
  animate(element, animation, duration = 300) {
    element.classList.add(animation);
    
    setTimeout(() => {
      element.classList.remove(animation);
    }, duration);
  }

  /**
   * 모달 스타일 생성 - 모노크롬 색상 스키마
   * @param {string} modalId - 모달 ID
   * @param {Object} options - 옵션
   */
  createModalStyles(modalId, options = {}) {
    const {
      width = '500px',
      height = 'auto',
      maxWidth = '90vw',
      maxHeight = '90vh'
    } = options;

    const modalStyles = `
      #${modalId} {
        background: var(--color-surface);
        border-radius: var(--radius-lg);
        padding: var(--space-xl);
        width: ${width};
        height: ${height};
        max-width: ${maxWidth};
        max-height: ${maxHeight};
        overflow-y: auto;
        box-shadow: var(--shadow-lg);
        border: 1px solid var(--color-border);
        position: relative;
        z-index: var(--z-modal);
        animation: modalSlideIn 0.3s ease-out;
      }

      @keyframes modalSlideIn {
        from {
          opacity: 0;
          transform: translateY(-50px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      #${modalId} .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: var(--space-lg);
        padding-bottom: var(--space-md);
        border-bottom: 1px solid var(--color-border);
        background: var(--color-light-gray);
        border-radius: var(--radius-lg) var(--radius-lg) 0 0;
        margin: calc(-1 * var(--space-xl)) calc(-1 * var(--space-xl)) var(--space-lg) calc(-1 * var(--space-xl));
        padding: var(--space-lg) var(--space-xl);
      }

      #${modalId} .modal-title {
        font-size: var(--font-size-xl);
        font-weight: 600;
        color: var(--color-text);
        margin: 0;
      }

      #${modalId} .modal-close {
        background: none;
        border: none;
        font-size: var(--font-size-xl);
        color: var(--color-text-muted);
        cursor: pointer;
        padding: var(--space-xs);
        border-radius: var(--radius-sm);
        transition: all var(--transition-fast);
      }

      #${modalId} .modal-close:hover {
        background: var(--color-gray-200);
        color: var(--color-text);
      }

      #${modalId} .modal-content {
        margin-bottom: var(--space-lg);
        color: var(--color-text-secondary);
        line-height: var(--line-height-normal);
      }

      #${modalId} .modal-footer {
        display: flex;
        justify-content: flex-end;
        gap: var(--space-sm);
        padding-top: var(--space-md);
        border-top: 1px solid var(--color-border);
        background: var(--color-light-gray);
        margin: var(--space-lg) calc(-1 * var(--space-xl)) calc(-1 * var(--space-xl)) calc(-1 * var(--space-xl));
        padding: var(--space-lg) var(--space-xl);
        border-radius: 0 0 var(--radius-lg) var(--radius-lg);
      }

      #${modalId} .modal-footer .btn {
        padding: var(--space-sm) var(--space-lg);
        border-radius: var(--radius-md);
        font-size: var(--font-size-sm);
        font-weight: 500;
        transition: all var(--transition-fast);
        border: 1px solid var(--color-border);
        cursor: pointer;
      }

      #${modalId} .modal-footer .btn-primary {
        background: var(--color-primary);
        color: var(--color-white);
        border-color: var(--color-primary);
      }

      #${modalId} .modal-footer .btn-primary:hover {
        background: var(--color-gray-800);
        border-color: var(--color-gray-800);
      }

      #${modalId} .modal-footer .btn-secondary {
        background: var(--color-surface);
        color: var(--color-text);
        border-color: var(--color-border);
      }

      #${modalId} .modal-footer .btn-secondary:hover {
        background: var(--color-light-gray);
      }

      /* 반응형 디자인 */
      @media (max-width: 768px) {
        #${modalId} {
          width: 95%;
          margin: 5% auto;
        }
        
        #${modalId} .modal-header,
        #${modalId} .modal-footer {
          padding: var(--space-md);
          margin: calc(-1 * var(--space-md)) calc(-1 * var(--space-md)) var(--space-md) calc(-1 * var(--space-md));
        }
        
        #${modalId} .modal-footer {
          flex-direction: column;
          gap: var(--space-sm);
        }
        
        #${modalId} .modal-footer .btn {
          width: 100%;
          justify-content: center;
        }
      }
    `;

    this.addStyle(`modal-${modalId}`, modalStyles);
  }

  /**
   * 폼 유효성 검사 스타일
   * @param {string} formId - 폼 ID
   */
  createFormValidationStyles(formId) {
    const validationStyles = `
      #${formId} .form-control.error {
        border-color: var(--color-error);
        box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
      }

      #${formId} .form-control.success {
        border-color: var(--color-success);
        box-shadow: 0 0 0 2px rgba(34, 40, 49, 0.1);
      }

      #${formId} .error-message {
        color: var(--color-error);
        font-size: var(--font-size-sm);
        margin-top: var(--space-xs);
        display: block;
      }

      #${formId} .success-message {
        color: var(--color-success);
        font-size: var(--font-size-sm);
        margin-top: var(--space-xs);
        display: block;
      }

      #${formId} .form-group.error label {
        color: var(--color-error);
      }

      #${formId} .form-group.success label {
        color: var(--color-success);
      }
    `;

    this.addStyle(`form-validation-${formId}`, validationStyles);
  }

  /**
   * 테이블 스타일 생성
   * @param {string} tableId - 테이블 ID
   * @param {Object} options - 옵션
   */
  createTableStyles(tableId, options = {}) {
    const {
      striped = true,
      hoverable = true,
      bordered = false,
      compact = false
    } = options;

    let tableStyles = `
      #${tableId} {
        width: 100%;
        border-collapse: collapse;
        background: var(--color-white);
        border-radius: var(--radius-md);
        overflow: hidden;
        box-shadow: var(--shadow-sm);
        border: 1px solid var(--color-border);
      }

      #${tableId} th,
      #${tableId} td {
        padding: ${compact ? 'var(--space-sm)' : 'var(--space-md)'};
        text-align: left;
        border-bottom: 1px solid var(--color-border);
        font-size: var(--font-size-sm);
      }

      #${tableId} th {
        background: var(--color-light-gray);
        font-weight: 600;
        color: var(--color-text);
      }

      #${tableId} td {
        color: var(--color-text);
      }
    `;

    if (striped) {
      tableStyles += `
        #${tableId} tr:nth-child(even) {
          background: var(--color-white);
        }
      `;
    }

    if (hoverable) {
      tableStyles += `
        #${tableId} tr:hover {
          background: var(--color-light-gray);
        }
      `;
    }

    if (bordered) {
      tableStyles += `
        #${tableId} th,
        #${tableId} td {
          border: 1px solid var(--color-border);
        }
      `;
    }

    this.addStyle(`table-${tableId}`, tableStyles);
  }

  /**
   * 정리
   */
  destroy() {
    if (this.styleElement && this.styleElement.parentNode) {
      this.styleElement.parentNode.removeChild(this.styleElement);
    }
    this.styles.clear();
  }
}

// 전역 인스턴스 생성
window.CSSManager = new CSSManager();

// 모듈 내보내기
if (typeof module !== 'undefined' && module.exports) {
  module.exports = CSSManager;
} 