// Q&A 목록 페이지 JavaScript - 모노크롬 스타일

document.addEventListener('DOMContentLoaded', function() {
    initializeQnaPage();
});

// 전역 변수
let currentQnaId = null;

/**
 * Q&A 페이지 초기화
 */
function initializeQnaPage() {
    setupSearchForm();
    setupFilterSelects();
    setupQnaItems();
    setupPagination();
    setupAccessibility();
}

/**
 * 검색 폼 설정
 */
function setupSearchForm() {
    const searchForm = document.querySelector('.search-form');
    const searchInput = document.querySelector('.search-input');
    
    if (searchForm && searchInput) {
        // 검색 입력창 포커스 효과
        searchInput.addEventListener('focus', function() {
            this.parentElement.style.boxShadow = '0 0 0 3px rgba(0, 0, 0, 0.1)';
        });
        
        searchInput.addEventListener('blur', function() {
            this.parentElement.style.boxShadow = '';
        });
        
        // 엔터 키로 검색
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchForm.submit();
            }
        });
        
        // 검색 버튼 클릭 효과
        const searchBtn = document.querySelector('.search-btn');
        if (searchBtn) {
            searchBtn.addEventListener('click', function() {
                this.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 150);
            });
        }
    }
}

/**
 * 필터 셀렉트 설정
 */
function setupFilterSelects() {
    const filterSelects = document.querySelectorAll('.filter-select');
    
    filterSelects.forEach(select => {
        select.addEventListener('change', function() {
            // 필터 변경 시 자동 제출
            this.closest('form').submit();
        });
        
        // 포커스 효과
        select.addEventListener('focus', function() {
            this.style.borderColor = '#000000';
            this.style.boxShadow = '0 0 0 2px rgba(0, 0, 0, 0.1)';
        });
        
        select.addEventListener('blur', function() {
            this.style.borderColor = '';
            this.style.boxShadow = '';
        });
    });
}

/**
 * Q&A 아이템 설정
 */
function setupQnaItems() {
    const qnaItems = document.querySelectorAll('.qna-item');
    
    qnaItems.forEach(item => {
        // 호버 효과 개선
        item.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
            this.style.boxShadow = '0 4px 16px rgba(0, 0, 0, 0.15)';
        });
        
        item.addEventListener('mouseleave', function() {
            this.style.transform = '';
            this.style.boxShadow = '';
        });
        
        // 클릭 효과
        item.addEventListener('click', function() {
            this.style.transform = 'scale(0.98)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
        
        // 키보드 접근성
        item.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.click();
            }
        });
        
        // 탭 인덱스 설정
        item.setAttribute('tabindex', '0');
    });
}

/**
 * 페이징 설정
 */
function setupPagination() {
    const pageButtons = document.querySelectorAll('.page-btn');
    
    pageButtons.forEach(button => {
        // 클릭 효과
        button.addEventListener('click', function() {
            if (!this.classList.contains('active') && !this.classList.contains('disabled')) {
                this.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 150);
            }
        });
        
        // 호버 효과
        button.addEventListener('mouseenter', function() {
            if (!this.classList.contains('active') && !this.classList.contains('disabled')) {
                this.style.backgroundColor = '#f5f5f5';
            }
        });
        
        button.addEventListener('mouseleave', function() {
            if (!this.classList.contains('active') && !this.classList.contains('disabled')) {
                this.style.backgroundColor = '';
            }
        });
    });
}

/**
 * 접근성 설정
 */
function setupAccessibility() {
    // 스킵 링크 추가
    const skipLink = document.createElement('a');
    skipLink.href = '#content';
    skipLink.textContent = '본문으로 건너뛰기';
    skipLink.className = 'skip-link';
    skipLink.style.cssText = `
        position: absolute;
        top: -40px;
        left: 6px;
        background: #000000;
        color: #ffffff;
        padding: 8px;
        text-decoration: none;
        border-radius: 4px;
        z-index: 1000;
    `;
    
    skipLink.addEventListener('focus', function() {
        this.style.top = '6px';
    });
    
    skipLink.addEventListener('blur', function() {
        this.style.top = '-40px';
    });
    
    document.body.insertBefore(skipLink, document.body.firstChild);
    
    // 포커스 표시 개선
    const focusableElements = document.querySelectorAll('a, button, input, select, textarea, [tabindex]');
    focusableElements.forEach(element => {
        element.addEventListener('focus', function() {
            this.style.outline = '2px solid #000000';
            this.style.outlineOffset = '2px';
        });
        
        element.addEventListener('blur', function() {
            this.style.outline = '';
            this.style.outlineOffset = '';
        });
    });
}

/**
 * 로딩 상태 표시
 */
function showLoading() {
    const loadingOverlay = document.createElement('div');
    loadingOverlay.id = 'loading-overlay';
    loadingOverlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(255, 255, 255, 0.8);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 9999;
    `;
    
    const spinner = document.createElement('div');
    spinner.style.cssText = `
        width: 40px;
        height: 40px;
        border: 3px solid #f3f3f3;
        border-top: 3px solid #000000;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    `;
    
    const style = document.createElement('style');
    style.textContent = `
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    `;
    
    document.head.appendChild(style);
    loadingOverlay.appendChild(spinner);
    document.body.appendChild(loadingOverlay);
}

/**
 * 로딩 상태 숨김
 */
function hideLoading() {
    const loadingOverlay = document.getElementById('loading-overlay');
    if (loadingOverlay) {
        loadingOverlay.remove();
    }
}

/**
 * 알림 메시지 표시 (common.js의 showToast 사용)
 */
function showNotification(message, type = 'info') {
    // common.js의 showToast 함수 사용
    if (typeof showToast === 'function') {
        showToast(message, type);
    } else {
        // fallback
        console.log(`${type.toUpperCase()}: ${message}`);
    }
}

/**
 * 스크롤 최상단으로 이동
 */
function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}

/**
 * Q&A 삭제 확인 모달
 */
function confirmDeleteQna(qnaId, qnaTitle) {
    currentQnaId = qnaId;
    
    // common.js의 showModal 함수 사용
    if (typeof showModal === 'function') {
        showModal({
            title: 'Q&A 삭제',
            message: `"${qnaTitle}" Q&A를 삭제하시겠습니까?\n\n삭제된 Q&A는 복구할 수 없습니다.`,
            onConfirm: () => {
                deleteQna(qnaId);
            },
            onCancel: () => {
                currentQnaId = null;
            }
        });
    } else {
        // fallback
        if (confirm(`"${qnaTitle}" Q&A를 삭제하시겠습니까?`)) {
            deleteQna(qnaId);
        }
    }
}

/**
 * Q&A 삭제 처리
 */
async function deleteQna(qnaId) {
    if (!qnaId) {
        if (typeof showToast === 'function') {
            showToast('삭제할 Q&A를 찾을 수 없습니다.', 'error');
        } else {
            alert('삭제할 Q&A를 찾을 수 없습니다.');
        }
        return;
    }
    
    try {
        showLoading();
        
        // common.js의 ajax 객체 사용
        if (typeof ajax !== 'undefined' && ajax.delete) {
            const result = await ajax.delete(`/api/qna/${qnaId}`);
            
            if (result && result.code === '00') {
                if (typeof showToast === 'function') {
                    showToast('Q&A가 성공적으로 삭제되었습니다.', 'success');
                } else {
                    alert('Q&A가 성공적으로 삭제되었습니다.');
                }
                
                // 페이지 새로고침 또는 목록에서 제거
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
                
            } else {
                throw new Error(result?.message || 'Q&A 삭제에 실패했습니다.');
            }
        } else {
            throw new Error('AJAX 기능을 사용할 수 없습니다.');
        }
        
    } catch (error) {
        console.error('Q&A 삭제 오류:', error);
        if (typeof showToast === 'function') {
            showToast(error.message || 'Q&A 삭제에 실패했습니다.', 'error');
        } else {
            alert(error.message || 'Q&A 삭제에 실패했습니다.');
        }
    } finally {
        hideLoading();
        currentQnaId = null;
    }
}

/**
 * Q&A 작성 버튼 클릭 처리
 */
function handleWriteButtonClick() {
    // 로그인 체크 (서버 사이드에서 처리되므로 단순히 링크 이동)
    const writeBtn = document.querySelector('.write-btn');
    if (writeBtn) {
        writeBtn.addEventListener('click', function(e) {
            // 클릭 효과
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
    }
}

// 페이지 로드 완료 후 추가 설정
window.addEventListener('load', function() {
    // 스크롤 최상단 버튼 추가
    const scrollTopBtn = document.createElement('button');
    scrollTopBtn.innerHTML = '<i class="fas fa-chevron-up"></i>';
    scrollTopBtn.title = '맨 위로';
    scrollTopBtn.className = 'scroll-top-btn';
    scrollTopBtn.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        width: 50px;
        height: 50px;
        background: #000000;
        color: #ffffff;
        border: none;
        border-radius: 50%;
        cursor: pointer;
        opacity: 0;
        visibility: hidden;
        transition: all 0.3s ease;
        z-index: 1000;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    `;
    
    scrollTopBtn.addEventListener('click', scrollToTop);
    document.body.appendChild(scrollTopBtn);
    
    // 스크롤 이벤트로 버튼 표시/숨김
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            scrollTopBtn.style.opacity = '1';
            scrollTopBtn.style.visibility = 'visible';
        } else {
            scrollTopBtn.style.opacity = '0';
            scrollTopBtn.style.visibility = 'hidden';
        }
    });
    
    // 호버 효과
    scrollTopBtn.addEventListener('mouseenter', function() {
        this.style.transform = 'scale(1.1)';
        this.style.boxShadow = '0 4px 16px rgba(0, 0, 0, 0.3)';
    });
    
    scrollTopBtn.addEventListener('mouseleave', function() {
        this.style.transform = '';
        this.style.boxShadow = '';
    });
});

// 전역 함수로 내보내기
window.confirmDeleteQna = confirmDeleteQna;
window.deleteQna = deleteQna;
