/**************************************/
/* 문자열의 바이트 길이 반환
/**************************************/
function getBytesSize(str){
  const encoder = new TextEncoder();
  const byteArray = encoder.encode(str);
  return byteArray.length;
}

/*-----------------------------------------------------------------------*
/* 자바스크립트 로딩하기
/*-----------------------------------------------------------------------*/
function loadScript(url){
    return new Promise((resolve,reject)=>{
        //비동기 코드
        const scriptEle = document.createElement('script');
        scriptEle.src = url;
        scriptEle.defer = true;

        //로딩 성공시
        scriptEle.addEventListener('load',e=>resolve(`${url} 로딩성공!`));
        //로딩 실패시
        scriptEle.addEventListener('error',e=>reject( new Error(`${url} 로딩실패!`)));

        document.head.appendChild(scriptEle);
    });
}

/*-----------------------------------------------------------------------*
 * client-server간 http api 비동기 통신
 *-----------------------------------------------------------------------*/
const ajax = {
  get: async url => {
    const option = {
      method: 'GET',
      headers: {
        Accept: 'application/json',
      },
      credentials: 'include', // 세션 쿠키 포함
    };
    try {
      const res = await fetch(url, option);
      if(!res.ok) {
        throw new Error(`응답오류! : ${res.status}`)
      }
      const json = await res.json();
      return json;
    } catch (err) {
      console.error('AJAX GET 오류:', err.message);
      throw err; // 오류를 다시 던져서 호출자가 처리할 수 있도록 함
    }
  },
  post: async (url, payload) => {
    const option = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      credentials: 'include', // 세션 쿠키 포함
      body: JSON.stringify(payload), // jsobject => json포맷의 문자열
    };
    
    try {
      const res = await fetch(url, option);
      
      if(!res.ok) {
        throw new Error(`응답오류! : ${res.status}`)
      }
      const json = await res.json();
      return json;
    } catch (err) {
      console.error('AJAX POST 오류:', err.message);
      throw err; // 오류를 다시 던져서 호출자가 처리할 수 있도록 함
    }
  },
  put: async (url, payload) => {
    const option = {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      credentials: 'include', // 세션 쿠키 포함
      body: JSON.stringify(payload),
    };
    try {
      const res = await fetch(url, option);
      if(!res.ok) {
        throw new Error(`응답오류! : ${res.status}`)
      }
      const json = await res.json();
      return json;
    } catch (err) {
      console.error(err.message);
    }
  },
  patch: async (url, payload) => {
    const option = {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      credentials: 'include', // 세션 쿠키 포함
      body: JSON.stringify(payload),
    };
    try {
      const res = await fetch(url, option);
      if(!res.ok) {
        throw new Error(`응답오류! : ${res.status}`)
      }
      const json = await res.json();
      return json;
    } catch (err) {
      console.error(err.message);
    }
  },
  delete: async url => {
    const option = {
      method: 'DELETE',
      headers: {
        Accept: 'application/json',
      },
      credentials: 'include', // 세션 쿠키 포함
    };
    try {
      const res = await fetch(url, option);
      if(!res.ok) {
        throw new Error(`응답오류! : ${res.status}`)
      }
      const json = await res.json();
      return json;
    } catch (err) {
      console.error(err.message);
    }
  },
};

/*-----------------------------------------------------------------------*
/* 페이징
/*-----------------------------------------------------------------------*/
class PaginationState {

  constructor(totalRecords = 0, recordsPerPage = 10, pagesPerPage = 10) {
      this.totalRecords = totalRecords; // 전체 레코드수
      this.recordsPerPage = recordsPerPage; //페이지당 레코드수
      this.pagesPerPage = pagesPerPage; //페이지당 페이지수
      this.currentPage = 1; //현재 페이지 : 현재 페이지의 스타일을 달리주기위해 필요
      this.currentPageGroupStart = 1; //현재 페이지의 시작페이지 : 한페이지의 시작페이지와 끝페이지 계산에 필요
  }

  // 전체 페이지수 계산
  get totalPages() {
      return Math.ceil(this.totalRecords / this.recordsPerPage);
  }

  // 첫번째 페이지 그룹 체크
  get isFirstGroup() {
      return this.currentPageGroupStart === 1;
  }

  // 마지막 페이지 그룹 체크
  get isLastGroup() {
      return this.currentPageGroupStart + this.pagesPerPage > this.totalPages;
  }

  // 페이지 시작 끝 계산
  get visiblePages() {
      const pages = [];
      const end = Math.min(
          this.currentPageGroupStart + this.pagesPerPage - 1,
          this.totalPages
      );

      for (let i = this.currentPageGroupStart; i <= end; i++) {
          pages.push(i);
      }
      return pages;
  }
}

class PaginationUI {

  // 첫번째 매개변수 : 페이지를 표시할 컨테이너를 id값으로 지정
  // 두번째 매개변수 : 목록을 표시하는 함수 지정 ( 함수의 매개변수 : 요청페이지)
  constructor(containerId, onPageChange) {
      this.container = document.getElementById(containerId); // 페이징 표시할 요소
      this.onPageChange = onPageChange;                      // 페이지 갱신시 표시할 목록을 표시할 함수
      this.state = new PaginationState();
  }

  // 페이지당 레코드수 설정
  setRecordsPerPage(recordsPerPage) {
    this.state.recordsPerPage = recordsPerPage;
  }

  // 페이지당 페이지수 설정
  setPagesPerPage(pagesPerPage) {
    this.state.pagesPerPage = pagesPerPage;
  }

  // 총건수 설정
  setTotalRecords(totalRecords) {
      this.state.totalRecords = totalRecords;
  }

  createButton(label, onClick, isActive = false, isDisabled = false) {
      const button = document.createElement('button');
      button.textContent = label;
      button.addEventListener('click', onClick);
      button.disabled = isDisabled;
      if (isActive) button.classList.add('active'); // 현재 페이지 버튼 스탈을 다르게 반영하기 위함
      return button;
  }

  // 페이지 번호 클릭 시
  handlePageClick(pageNumber) {
      this.state.currentPage = pageNumber;
      this.onPageChange(pageNumber);
      this.render();
  }

  // 처음 클릭 시
  handleFirstClick() {
      this.state.currentPageGroupStart = 1;
      this.state.currentPage = 1;
      this.onPageChange(1);
      this.render();
  }

  // 이전 클릭 시
  handlePrevClick() {
      if (!this.state.isFirstGroup) {
          this.state.currentPageGroupStart -= this.state.pagesPerPage;
          this.state.currentPage = this.state.currentPageGroupStart + this.state.pagesPerPage -1;
          this.onPageChange(this.state.currentPage);
          this.render();
      }
  }

  // 다음 클릭 시
  handleNextClick() {
      if (!this.state.isLastGroup) {
          this.state.currentPageGroupStart += this.state.pagesPerPage;
          this.state.currentPage = this.state.currentPageGroupStart;
          this.onPageChange(this.state.currentPage);
          this.render();
      }
  }

  // 끝 클릭 시
  handleLastClick() {
      const lastGroupStart =
          this.state.totalPages - (this.state.totalPages % this.state.pagesPerPage) + 1;
      this.state.currentPageGroupStart = lastGroupStart;
      this.state.currentPage = this.state.totalPages;
      this.onPageChange(this.state.currentPage);
      this.render();
  }

  // 목록 표시
  render() {
      this.container.innerHTML = '';
      const nav = document.createElement('nav');
      nav.className = 'pagination';

      // 처음,이전 버튼 표시
      if (!this.state.isFirstGroup) {
          nav.appendChild(this.createButton('처음', () => this.handleFirstClick()));
          nav.appendChild(this.createButton('이전', () => this.handlePrevClick()));
      }

      // 페이지 번호 버튼 표시
      this.state.visiblePages.forEach(pageNum => {
          nav.appendChild(
              this.createButton(
                  pageNum.toString(),
                  () => this.handlePageClick(pageNum),
                  pageNum === this.state.currentPage
              )
          );
      });

      // 다음,끝 버튼 표시
      if (!this.state.isLastGroup) {
          nav.appendChild(this.createButton('다음', () => this.handleNextClick()));
          nav.appendChild(this.createButton('끝', () => this.handleLastClick()));
      }

      this.container.appendChild(nav);
  }
}

// ===== 모달 유틸리티 =====
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (!modal) return;
  modal.classList.add('show');
  document.body.style.overflow = 'hidden';
}
function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (!modal) return;
  modal.classList.remove('show');
  document.body.style.overflow = '';
}
// ESC, 배경 클릭 닫기
window.addEventListener('click', function(e) {
  const modals = document.querySelectorAll('.modal-backdrop');
  modals.forEach(backdrop => {
    if (e.target === backdrop) {
      backdrop.style.display = 'none';
      document.body.style.overflow = '';
    }
  });
});
window.addEventListener('keydown', function(e) {
  if (e.key === 'Escape') {
    document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
      backdrop.style.display = 'none';
      document.body.style.overflow = '';
    });
  }
});

// ===== 공통 모달 유틸리티 =====
function showModal({ title = '확인', message = '', onConfirm, onCancel }) {
  // 기존 모달이 있으면 제거
  const existing = document.getElementById('common-modal-backdrop');
  if (existing) existing.remove();

  // 모달 백드롭
  const backdrop = document.createElement('div');
  backdrop.id = 'common-modal-backdrop';
  backdrop.style.position = 'fixed';
  backdrop.style.top = 0;
  backdrop.style.left = 0;
  backdrop.style.width = '100vw';
  backdrop.style.height = '100vh';
  backdrop.style.background = 'rgba(0,0,0,0.3)';
  backdrop.style.display = 'flex';
  backdrop.style.alignItems = 'center';
  backdrop.style.justifyContent = 'center';
  backdrop.style.zIndex = 9999;

  // 모달 박스
  const modal = document.createElement('div');
  modal.style.background = '#fff';
  modal.style.borderRadius = '8px';
  modal.style.boxShadow = '0 2px 16px rgba(0,0,0,0.2)';
  modal.style.padding = '2em 2em 1.5em 2em';
  modal.style.minWidth = '320px';
  modal.style.maxWidth = '90vw';
  modal.style.textAlign = 'center';

  // 제목
  const titleEl = document.createElement('div');
  titleEl.textContent = title;
  titleEl.style.fontWeight = 'bold';
  titleEl.style.fontSize = '1.2em';
  titleEl.style.marginBottom = '1em';
  modal.appendChild(titleEl);

  // 메시지
  const msgEl = document.createElement('div');
  msgEl.style.marginBottom = '1.5em';
  msgEl.style.lineHeight = '1.5';
  msgEl.style.whiteSpace = 'pre-line'; // 줄바꿈 문자(\n) 처리
  msgEl.textContent = message;
  modal.appendChild(msgEl);

  // 버튼 영역
  const btnArea = document.createElement('div');
  btnArea.style.display = 'flex';
  btnArea.style.justifyContent = 'center';
  btnArea.style.gap = '1em';

  // 확인 버튼
  const okBtn = document.createElement('button');
  okBtn.textContent = '확인';
  okBtn.className = 'btn btn--primary';
  okBtn.onclick = () => {
    backdrop.remove();
    if (typeof onConfirm === 'function') onConfirm();
  };
  btnArea.appendChild(okBtn);

  // 취소 버튼 (onCancel이 있는 경우에만 표시)
  if (typeof onCancel === 'function') {
    const cancelBtn = document.createElement('button');
    cancelBtn.textContent = '취소';
    cancelBtn.className = 'btn btn--outline';
    cancelBtn.onclick = () => {
      backdrop.remove();
      onCancel();
    };
    btnArea.appendChild(cancelBtn);
  }

  modal.appendChild(btnArea);
  backdrop.appendChild(modal);
  
  // 배경 클릭 시 모달 닫기 (취소 버튼이 있는 경우에만)
  if (typeof onCancel === 'function') {
    backdrop.onclick = (e) => {
      if (e.target === backdrop) {
        backdrop.remove();
        onCancel();
      }
    };
  }
  
  // 모달 박스 클릭 시 이벤트 전파 방지
  modal.onclick = (e) => {
    e.stopPropagation();
  };
  
  // ESC 키로 모달 닫기 (취소 버튼이 있는 경우에만)
  const handleEscKey = (e) => {
    if (e.key === 'Escape') {
      backdrop.remove();
      document.removeEventListener('keydown', handleEscKey);
      if (typeof onCancel === 'function') {
        onCancel();
      }
    }
  };
  
  if (typeof onCancel === 'function') {
    document.addEventListener('keydown', handleEscKey);
  }
  
  document.body.appendChild(backdrop);
}

// ===== 페이지네이션 유틸리티 =====
function goToPage(page, callback) {
  if (typeof callback === 'function') {
    callback(page);
  }
}

// ===== 로그아웃 확인 모달 =====
function confirmLogout(event) {
  event.preventDefault();
  
  showModal({
    title: '로그아웃',
    message: '정말 로그아웃 하시겠습니까?',
    onConfirm: () => {
      // POST 방식으로 로그아웃 처리
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = '/logout';
      
      document.body.appendChild(form);
      form.submit();
    },
    onCancel: () => {
      // 취소 시 아무것도 하지 않음
    }
  });
}

// ===== 캐러셀(배너) 기능 =====
document.addEventListener('DOMContentLoaded', () => {
  const carouselItems = Array.from(document.querySelectorAll('.carousel-item'));
  const prevBtn = document.getElementById('prev');
  const nextBtn = document.getElementById('next');
  const radioButtons = Array.from(document.querySelectorAll('.carousel-radio'));
  if (!carouselItems.length || !prevBtn || !nextBtn) return; // 캐러셀이 없는 페이지는 무시

  let currentIndex = 0;
  let intervalId = null;

  function setCurrentImage(index) {
    carouselItems.forEach((item, i) => {
      item.classList.toggle('active', i === index);
    });
    if (radioButtons.length) {
      radioButtons.forEach((radio, i) => {
        radio.checked = (i === index);
      });
    }
    currentIndex = index;
  }

  function showPrev() {
    const newIndex = (currentIndex - 1 + carouselItems.length) % carouselItems.length;
    setCurrentImage(newIndex);
    resetInterval();
  }

  function showNext() {
    const newIndex = (currentIndex + 1) % carouselItems.length;
    setCurrentImage(newIndex);
    resetInterval();
  }

  function startInterval() {
    intervalId = setInterval(() => {
      showNext();
    }, 5000);
  }

  function resetInterval() {
    if (intervalId) clearInterval(intervalId);
    startInterval();
  }

  prevBtn.addEventListener('click', showPrev);
  nextBtn.addEventListener('click', showNext);

  // radio 버튼 클릭 시 해당 인덱스 이미지로 이동
  if (radioButtons.length) {
    radioButtons.forEach((radio, idx) => {
      radio.addEventListener('click', () => {
        setCurrentImage(idx);
        resetInterval();
      });
    });
  }

  setCurrentImage(0);
  startInterval();
});

/**
 * 권한 체크 유틸리티 함수들
 */

// 현재 사용자가 관리자인지 확인
function isCurrentUserAdmin() {
    const rootElement = document.getElementById('root');
    return rootElement && rootElement.dataset.sIsAdmin === 'true';
}

// 현재 사용자가 VIP인지 확인
function isCurrentUserVip() {
    const rootElement = document.getElementById('root');
    return rootElement && rootElement.dataset.sIsVip === 'true';
}

// 현재 사용자가 일반 회원인지 확인
function isCurrentUserNormal() {
    const rootElement = document.getElementById('root');
    return rootElement && rootElement.dataset.sIsNormal === 'true';
}

// 현재 사용자가 로그인했는지 확인
function isCurrentUserLoggedIn() {
    const rootElement = document.getElementById('root');
    return rootElement && rootElement.dataset.sIsLoggedIn === 'true';
}

// 현재 사용자의 회원 ID 가져오기
function getCurrentUserId() {
    const rootElement = document.getElementById('root');
    return rootElement ? rootElement.dataset.sMemberId : null;
}

// 현재 사용자의 이메일 가져오기
function getCurrentUserEmail() {
    const rootElement = document.getElementById('root');
    return rootElement ? rootElement.dataset.sEmail : null;
}

// 현재 사용자의 닉네임 가져오기
function getCurrentUserNickname() {
    const rootElement = document.getElementById('root');
    return rootElement ? rootElement.dataset.sNickname : null;
}

// 현재 사용자의 회원 구분 가져오기
function getCurrentUserGubun() {
    const rootElement = document.getElementById('root');
    return rootElement ? rootElement.dataset.sGubun : null;
}

// 권한에 따른 조건부 실행
function executeIfAdmin(callback) {
    if (isCurrentUserAdmin()) {
        callback();
    } else {
        console.warn('관리자 권한이 필요합니다.');
    }
}

function executeIfVip(callback) {
    if (isCurrentUserVip()) {
        callback();
    } else {
        console.warn('VIP 권한이 필요합니다.');
    }
}

function executeIfLoggedIn(callback) {
    if (isCurrentUserLoggedIn()) {
        callback();
    } else {
        console.warn('로그인이 필요합니다.');
        // 로그인 페이지로 리다이렉트
        window.location.href = '/login';
    }
}

// 권한에 따른 UI 요소 표시/숨김
function showAdminElements() {
    const adminElements = document.querySelectorAll('[data-require-admin]');
    adminElements.forEach(element => {
        if (isCurrentUserAdmin()) {
            element.style.display = '';
        } else {
            element.style.display = 'none';
        }
    });
}

function showVipElements() {
    const vipElements = document.querySelectorAll('[data-require-vip]');
    vipElements.forEach(element => {
        if (isCurrentUserVip() || isCurrentUserAdmin()) {
            element.style.display = '';
        } else {
            element.style.display = 'none';
        }
    });
}

function showLoggedInElements() {
    const loginElements = document.querySelectorAll('[data-require-login]');
    loginElements.forEach(element => {
        if (isCurrentUserLoggedIn()) {
            element.style.display = '';
        } else {
            element.style.display = 'none';
        }
    });
}

// 페이지 로드 시 권한에 따른 UI 초기화
document.addEventListener('DOMContentLoaded', function() {
    showAdminElements();
    showVipElements();
    showLoggedInElements();
});

// =================================================
// 위시리스트 관리 기능
// =================================================

/**
 * 위시리스트 토글 (추가/제거)
 */
async function toggleWishlist(productId, buttonElement) {
    if (!productId) {
        console.error('Product ID is required');
        return;
    }
    
    // 로그인 체크
    if (!isCurrentUserLoggedIn()) {
        showLoginRequired();
        return;
    }
    
    try {
        // 로딩 상태 표시
        setWishlistButtonLoading(buttonElement, true);
        
        // API 호출
        const result = await ajax.post(`/api/wishlist/toggle/${productId}`, {});
        
        if (result && result.code === '00') {
            // 버튼 상태 업데이트 (details에서 isInWishlist 가져오기)
            const isInWishlist = result.details?.isInWishlist || result.data;
            updateWishlistButtonState(buttonElement, isInWishlist);
            
            // 위시리스트 개수 업데이트
            updateWishlistCount();
            
            // 메시지 표시 (details의 message 또는 기본 ApiResponse message 사용)
            const message = result.details?.message || result.message;
            showToast(message, 'success');
            
        } else {
            throw new Error(result?.message || '위시리스트 처리에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('위시리스트 토글 오류:', error);
        showToast(error.message || '위시리스트 처리에 실패했습니다.', 'error');
    } finally {
        // 로딩 상태 해제
        setWishlistButtonLoading(buttonElement, false);
    }
}

/**
 * 위시리스트 추가
 */
async function addToWishlist(productId, buttonElement) {
    if (!productId) {
        console.error('Product ID is required');
        return;
    }
    
    // 로그인 체크
    if (!isCurrentUserLoggedIn()) {
        showLoginRequired();
        return;
    }
    
    try {
        // 로딩 상태 표시
        setWishlistButtonLoading(buttonElement, true);
        
        // API 호출
        const result = await ajax.post(`/api/wishlist/add/${productId}`, {});
        
        if (result && result.code === '00') {
            // 버튼 상태 업데이트
            updateWishlistButtonState(buttonElement, true);
            
            // 위시리스트 개수 업데이트
            updateWishlistCount();
            
            // 성공 메시지 표시 (details의 message 또는 기본 메시지 사용)
            const message = result.details?.message || '위시리스트에 추가되었습니다.';
            showToast(message, 'success');
            
        } else {
            throw new Error(result?.message || '위시리스트 추가에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('위시리스트 추가 오류:', error);
        showToast(error.message || '위시리스트 추가에 실패했습니다.', 'error');
    } finally {
        // 로딩 상태 해제
        setWishlistButtonLoading(buttonElement, false);
    }
}

/**
 * 위시리스트에서 제거
 */
async function removeFromWishlist(productId, buttonElement) {
    if (!productId) {
        console.error('Product ID is required');
        return;
    }
    
    try {
        // 로딩 상태 표시
        setWishlistButtonLoading(buttonElement, true);
        
        // API 호출
        const result = await ajax.delete(`/api/wishlist/remove/${productId}`);
        
        if (result && result.code === '00') {
            // 버튼 상태 업데이트
            updateWishlistButtonState(buttonElement, false);
            
            // 위시리스트 개수 업데이트
            updateWishlistCount();
            
            // 성공 메시지 표시 (details의 message 또는 기본 메시지 사용)
            const message = result.details?.message || '위시리스트에서 제거되었습니다.';
            showToast(message, 'success');
            
        } else {
            throw new Error(result?.message || '위시리스트 제거에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('위시리스트 제거 오류:', error);
        showToast(error.message || '위시리스트 제거에 실패했습니다.', 'error');
    } finally {
        // 로딩 상태 해제
        setWishlistButtonLoading(buttonElement, false);
    }
}

/**
 * 위시리스트 버튼 로딩 상태 설정
 */
function setWishlistButtonLoading(buttonElement, isLoading) {
    if (!buttonElement) return;
    
    if (isLoading) {
        buttonElement.classList.add('loading');
        buttonElement.disabled = true;
        
        const textElement = buttonElement.querySelector('.wishlist-text');
        if (textElement) {
            textElement.textContent = '처리중...';
        }
    } else {
        buttonElement.classList.remove('loading');
        buttonElement.disabled = false;
        
        const textElement = buttonElement.querySelector('.wishlist-text');
        if (textElement) {
            textElement.textContent = '위시리스트';
        }
    }
}

/**
 * 위시리스트 버튼 상태 업데이트
 */
function updateWishlistButtonState(buttonElement, inWishlist) {
    if (!buttonElement) return;
    
    if (inWishlist) {
        buttonElement.classList.add('active');
        const textElement = buttonElement.querySelector('.wishlist-text');
        if (textElement) {
            textElement.textContent = '위시리스트 ✓';
        }
    } else {
        buttonElement.classList.remove('active');
        const textElement = buttonElement.querySelector('.wishlist-text');
        if (textElement) {
            textElement.textContent = '위시리스트';
        }
    }
}

/**
 * 위시리스트 개수 업데이트 (Top 메뉴) - 기존 함수 유지 (호환성)
 */
async function updateWishlistCount() {
    await countManager.updateWishlistCount();
}

/**
 * 페이지 로드 시 위시리스트 상태 초기화
 */
async function initializeWishlistStates() {
    const wishlistButtons = document.querySelectorAll('.wishlist-btn[data-product-id]');
    
    if (wishlistButtons.length === 0) return;
    
    // 로그인하지 않은 경우 스킵
    if (!isCurrentUserLoggedIn()) {
        // 로그인하지 않은 경우 모든 위시리스트 버튼을 비활성 상태로 설정
        wishlistButtons.forEach(button => {
            updateWishlistButtonState(button, false);
        });
        return;
    }
    
    try {
        // 현재 페이지의 모든 상품 ID 수집
        const productIds = Array.from(wishlistButtons).map(btn => btn.dataset.productId);
        
        // 위시리스트 상태 일괄 조회
        const result = await ajax.post('/api/wishlist/check', { productIds });
        
        if (result && result.code === '00') {
            // 각 버튼의 상태 업데이트
            wishlistButtons.forEach(button => {
                const productId = button.dataset.productId;
                const inWishlist = result.data.includes(parseInt(productId));
                updateWishlistButtonState(button, inWishlist);
            });
        }
        
    } catch (error) {
        console.error('위시리스트 상태 초기화 오류:', error);
        // 오류 시 모든 버튼을 비활성 상태로 설정
        wishlistButtons.forEach(button => {
            updateWishlistButtonState(button, false);
        });
    }
}



/**
 * 로그인 필요 메시지 표시 (모달 버전)
 */
function showLoginRequired() {
    showModal({
        title: '로그인 필요',
        message: '로그인이 필요한 서비스입니다.\n로그인 페이지로 이동하시겠습니까?',
        onConfirm: () => {
            // 현재 페이지를 리턴 URL로 설정
            const returnUrl = encodeURIComponent(window.location.pathname + window.location.search);
            window.location.href = `/member/login?returnUrl=${returnUrl}`;
        },
        onCancel: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
}

/**
 * 토스트 메시지 표시
 */
function showToast(message, type = 'info') {
    // 기존 토스트 제거
    const existingToast = document.querySelector('.toast-message');
    if (existingToast) {
        existingToast.remove();
    }
    
    // 토스트 요소 생성
    const toast = document.createElement('div');
    toast.className = `toast-message toast-${type}`;
    toast.textContent = message;
    
    // 스타일 적용
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#007bff'};
        color: white;
        padding: 12px 20px;
        border-radius: 6px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        z-index: 10000;
        font-weight: 500;
        opacity: 0;
        transform: translateX(100%);
        transition: all 0.3s ease;
    `;
    
    // DOM에 추가
    document.body.appendChild(toast);
    
    // 애니메이션
    requestAnimationFrame(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateX(0)';
    });
    
    // 자동 제거
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.remove();
            }
        }, 300);
    }, 3000);
}

// 페이지 로드 시 모든 카운트 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 권한 요소 표시
    showAdminElements();
    showVipElements();
    showLoggedInElements();
    
    // 위시리스트 상태 초기화 (로그인 상태 확인 포함)
    initializeWishlistStates();
    
    // 모든 카운트 업데이트 (로그인 상태 확인 포함)
    updateAllCounts();
});

// 전역 함수로 내보내기 (기존 onclick 핸들러와 호환성)
window.toggleWishlist = toggleWishlist;
window.addToWishlist = addToWishlist;
window.removeFromWishlist = removeFromWishlist;
window.updateWishlistCount = updateWishlistCount;
window.updateCartCount = updateCartCount;
window.updateNotificationCount = updateNotificationCount;
window.updateAllCounts = updateAllCounts;

/**
 * 통합 카운트 관리 시스템
 */
class CountManager {
    constructor() {
        this.counts = {
            wishlist: 0,
            cart: 0,
            notification: 0
        };
        this.updateCallbacks = new Map();
    }

    /**
     * 모든 카운트 업데이트
     */
    async updateAllCounts() {
        // 로그인하지 않은 경우 모든 카운트를 0으로 설정
        if (!isCurrentUserLoggedIn()) {
            this.counts = { wishlist: 0, cart: 0, notification: 0 };
            this.updateCountDisplay('.wishlist-count', 0);
            this.updateCountDisplay('.cart-count', 0);
            this.updateCountDisplay('#notificationBadge', 0);
            return;
        }
        
        await Promise.all([
            this.updateWishlistCount(),
            this.updateCartCount(),
            this.updateNotificationCount()
        ]);
    }

    /**
     * 위시리스트 개수 업데이트
     */
    async updateWishlistCount() {
        // 로그인 체크
        if (!isCurrentUserLoggedIn()) {
            this.counts.wishlist = 0;
            this.updateCountDisplay('.wishlist-count', 0);
            return;
        }
        
        try {
            const response = await ajax.get('/api/wishlist/count');
            if (response && response.code === '00') {
                const count = response.data || 0;
                this.counts.wishlist = count;
                this.updateCountDisplay('.wishlist-count', count);
            }
        } catch (error) {
            console.error('위시리스트 개수 업데이트 오류:', error);
            // 오류 시 0으로 설정
            this.counts.wishlist = 0;
            this.updateCountDisplay('.wishlist-count', 0);
        }
    }

    /**
     * 장바구니 개수 업데이트
     */
    async updateCartCount() {
        // 로그인 체크
        if (!isCurrentUserLoggedIn()) {
            this.counts.cart = 0;
            this.updateCountDisplay('.cart-count', 0);
            return;
        }
        
        try {
            const response = await ajax.get('/api/cart/count');
            if (response && response.code === '00' && response.data.count !== undefined) {
                const count = response.data.count || 0;
                this.counts.cart = count;
                this.updateCountDisplay('.cart-count', count);
            }
        } catch (error) {
            console.error('장바구니 개수 업데이트 오류:', error);
            // 오류 시 0으로 설정
            this.counts.cart = 0;
            this.updateCountDisplay('.cart-count', 0);
        }
    }

    /**
     * 알림 개수 업데이트
     */
    async updateNotificationCount() {
        // 로그인 체크
        if (!isCurrentUserLoggedIn()) {
            this.counts.notification = 0;
            this.updateCountDisplay('#notificationBadge', 0);
            return;
        }
        
        try {
            const response = await ajax.get('/api/notification/count');
            if (response && response.code === '00') {
                const count = response.data || 0;
                this.counts.notification = count;
                this.updateCountDisplay('#notificationBadge', count);
            }
        } catch (error) {
            console.error('알림 개수 업데이트 오류:', error);
            // 오류 시 0으로 설정 (API가 없을 수도 있음)
            this.counts.notification = 0;
            this.updateCountDisplay('#notificationBadge', 0);
        }
    }

    /**
     * 카운트 표시 업데이트
     */
    updateCountDisplay(selector, count) {
        const elements = document.querySelectorAll(selector);
        elements.forEach(element => {
            element.textContent = count.toString();
            
            // 개수가 0이면 숨김, 아니면 표시
            if (count > 0) {
                element.style.display = 'flex';
                element.parentElement.classList.add('has-items');
            } else {
                element.style.display = 'none';
                element.parentElement.classList.remove('has-items');
            }
        });
    }

    /**
     * 특정 카운트만 업데이트
     */
    async updateCount(type) {
        switch (type) {
            case 'wishlist':
                await this.updateWishlistCount();
                break;
            case 'cart':
                await this.updateCartCount();
                break;
            case 'notification':
                await this.updateNotificationCount();
                break;
            default:
                console.warn('알 수 없는 카운트 타입:', type);
        }
    }

    /**
     * 현재 카운트 값 가져오기
     */
    getCount(type) {
        return this.counts[type] || 0;
    }

    /**
     * 모든 카운트 값 가져오기
     */
    getAllCounts() {
        return { ...this.counts };
    }
}

// 전역 카운트 매니저 인스턴스 생성
const countManager = new CountManager();

/**
 * 위시리스트 개수 업데이트 (Top 메뉴) - 기존 함수 유지 (호환성)
 */
async function updateWishlistCount() {
    await countManager.updateWishlistCount();
}

/**
 * 장바구니 개수 업데이트 (Top 메뉴) - 기존 함수 유지 (호환성)
 */
async function updateCartCount() {
    await countManager.updateCartCount();
}

/**
 * 알림 개수 업데이트 (Top 메뉴)
 */
async function updateNotificationCount() {
    await countManager.updateNotificationCount();
}

/**
 * 모든 카운트 업데이트
 */
async function updateAllCounts() {
    await countManager.updateAllCounts();
}

// 관리자용 카운트 업데이트 함수들
async function updateAdminCounts() {
  try {
    console.log('관리자 카운트 업데이트 시작...');
    const response = await ajax.get('/api/admin/dashboard/counts');
    console.log('API 응답:', response);
    
    if (response && response.code === '00') {
      const counts = response.data;
      console.log('카운트 데이터:', counts);
      
      // 시스템 알림 개수 업데이트
      const systemAlertCount = counts.systemAlertCount || 0;
      updateAdminCountDisplay('#systemAlertCount', systemAlertCount);
      
      // 처리 대기 주문 개수 업데이트
      const pendingOrderCount = counts.pendingOrderCount || 0;
      updateAdminCountDisplay('#pendingOrderCount', pendingOrderCount);
      
      // 신고 처리 개수 업데이트
      const pendingReportCount = counts.pendingReportCount || 0;
      updateAdminCountDisplay('#reportCount', pendingReportCount);
      
      console.log('관리자 카운트 업데이트 완료:', {
        systemAlertCount,
        pendingOrderCount,
        pendingReportCount
      });
    } else {
      console.error('API 응답 오류:', response);
    }
  } catch (error) {
    console.error('관리자 카운트 업데이트 실패:', error);
  }
}

function updateAdminCountDisplay(selector, count) {
  const element = document.querySelector(selector);
  if (element) {
    element.textContent = count;
    //element.style.display = count > 0 ? 'inline' : 'none';
    element.style.display = 'inline';
  }
}

// 페이지 로드 시 카운트 업데이트
document.addEventListener('DOMContentLoaded', function() {
  // 관리자 페이지인 경우 관리자 카운트 업데이트
  if (document.querySelector('.admin-top')) {
    updateAdminCounts();
    
    // 30초마다 자동 업데이트
    setInterval(updateAdminCounts, 30000);
  }
  
  // 일반 고객 페이지인 경우 위시리스트/장바구니/알림 카운트 업데이트
  if (document.querySelector('.customer-top')) {
    console.log('고객 페이지 카운트 업데이트 시작...');
    
    // 초기 카운트 업데이트
    updateAllCounts();
    
    // 위시리스트 상태 초기화
    initializeWishlistStates();
    
    // 30초마다 자동 업데이트
    setInterval(updateAllCounts, 30000);
  }
});