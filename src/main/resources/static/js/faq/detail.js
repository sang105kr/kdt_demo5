/**
 * FAQ 상세 페이지 JavaScript
 */

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('FAQ 상세 페이지 로드됨');
    initializeFaqDetail();
});

/**
 * FAQ 상세 페이지 초기화
 */
function initializeFaqDetail() {
    // 도움됨/도움안됨 버튼 이벤트 리스너 등록
    setupHelpfulButtons();
    
    // 관련 FAQ 링크 이벤트 리스너 등록
    setupRelatedFaqLinks();
}

/**
 * 도움됨/도움안됨 버튼 설정
 */
function setupHelpfulButtons() {
    const helpfulBtns = document.querySelectorAll('.helpful-btn');
    const unhelpfulBtns = document.querySelectorAll('.unhelpful-btn');
    
    helpfulBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            // 이미 클릭된 버튼이면 중복 실행 방지
            if (this.classList.contains('clicked') || this.disabled) {
                return;
            }
            
            const faqId = this.dataset.faqId;
            incrementHelpful(faqId);
        });
    });
    
    unhelpfulBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            // 이미 클릭된 버튼이면 중복 실행 방지
            if (this.classList.contains('clicked') || this.disabled) {
                return;
            }
            
            const faqId = this.dataset.faqId;
            incrementUnhelpful(faqId);
        });
    });
}

/**
 * 관련 FAQ 링크 설정
 */
function setupRelatedFaqLinks() {
    const relatedFaqLinks = document.querySelectorAll('.related-faq-link');
    
    relatedFaqLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            // 링크 클릭 시 페이지 전환
            console.log('관련 FAQ로 이동:', this.href);
        });
    });
}

// API 호출 상태 추적
let isHelpfulRequesting = false;
let isUnhelpfulRequesting = false;

/**
 * 도움됨 수 증가
 */
async function incrementHelpful(faqId) {
    if (!faqId) {
        console.error('FAQ ID가 없습니다.');
        return;
    }
    
    // 중복 요청 방지
    if (isHelpfulRequesting) {
        console.log('도움됨 요청이 진행 중입니다.');
        return;
    }
    
    isHelpfulRequesting = true;
    console.log('도움됨 수 증가 요청:', faqId);
    
    try {
        const data = await ajax.post(`/api/faq/${faqId}/helpful`, {});
        console.log('도움됨 수 증가 성공:', data);
        console.log('API 응답 구조 확인:', {
            code: data.code,
            message: data.message,
            data: data.data,
            details: data.details
        });
        
        if (data.code === 'SUCCESS' || data.code === '00') {
            // 성공 메시지 표시
            showToast('도움됨으로 표시되었습니다.', 'success');
            
            // 버튼 상태 업데이트
            updateHelpfulButton(faqId, true);
            
            // 서버에서 받은 실제 값으로 통계 업데이트
            console.log('카운트 업데이트 시도:', {
                dataData: data.data,
                dataDetails: data.details,
                helpfulCountFromData: data.data?.helpfulCount,
                helpfulCountFromDetails: data.details?.helpfulCount
            });
            
            if (data.data && data.data.helpfulCount !== undefined) {
                console.log('data.data에서 helpfulCount 사용:', data.data.helpfulCount);
                updateHelpfulCountWithValue(faqId, data.data.helpfulCount);
            } else if (data.details && data.details.helpfulCount !== undefined) {
                console.log('data.details에서 helpfulCount 사용:', data.details.helpfulCount);
                updateHelpfulCountWithValue(faqId, data.details.helpfulCount);
            } else {
                console.log('helpfulCount를 찾을 수 없습니다.');
            }
        } else {
            showToast(data.message || '도움됨 수 증가에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('도움됨 수 증가 실패:', error);
        showToast('도움됨 수 증가 중 오류가 발생했습니다.', 'error');
    } finally {
        // 요청 완료 후 상태 초기화
        isHelpfulRequesting = false;
    }
}

/**
 * 도움안됨 수 증가
 */
async function incrementUnhelpful(faqId) {
    if (!faqId) {
        console.error('FAQ ID가 없습니다.');
        return;
    }
    
    // 중복 요청 방지
    if (isUnhelpfulRequesting) {
        console.log('도움안됨 요청이 진행 중입니다.');
        return;
    }
    
    isUnhelpfulRequesting = true;
    console.log('도움안됨 수 증가 요청:', faqId);
    
    try {
        const data = await ajax.post(`/api/faq/${faqId}/unhelpful`, {});
        console.log('도움안됨 수 증가 성공:', data);
        console.log('API 응답 구조 확인:', {
            code: data.code,
            message: data.message,
            data: data.data,
            details: data.details
        });
        
        if (data.code === 'SUCCESS' || data.code === '00') {
            // 성공 메시지 표시
            showToast('도움안됨으로 표시되었습니다.', 'success');
            
            // 버튼 상태 업데이트
            updateUnhelpfulButton(faqId, true);
            
            // 서버에서 받은 실제 값으로 통계 업데이트
            console.log('카운트 업데이트 시도:', {
                dataData: data.data,
                dataDetails: data.details,
                unhelpfulCountFromData: data.data?.unhelpfulCount,
                unhelpfulCountFromDetails: data.details?.unhelpfulCount
            });
            
            if (data.data && data.data.unhelpfulCount !== undefined) {
                console.log('data.data에서 unhelpfulCount 사용:', data.data.unhelpfulCount);
                updateUnhelpfulCountWithValue(faqId, data.data.unhelpfulCount);
            } else if (data.details && data.details.unhelpfulCount !== undefined) {
                console.log('data.details에서 unhelpfulCount 사용:', data.details.unhelpfulCount);
                updateUnhelpfulCountWithValue(faqId, data.details.unhelpfulCount);
            } else {
                console.log('unhelpfulCount를 찾을 수 없습니다.');
            }
        } else {
            showToast(data.message || '도움안됨 수 증가에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('도움안됨 수 증가 실패:', error);
        showToast('도움안됨 수 증가 중 오류가 발생했습니다.', 'error');
    } finally {
        // 요청 완료 후 상태 초기화
        isUnhelpfulRequesting = false;
    }
}

/**
 * 도움됨 버튼 상태 업데이트
 */
function updateHelpfulButton(faqId, isClicked) {
    const helpfulBtn = document.querySelector(`.helpful-btn[data-faq-id="${faqId}"]`);
    if (helpfulBtn) {
        if (isClicked) {
            helpfulBtn.classList.add('clicked');
            helpfulBtn.disabled = true;
            // 카운트 정보를 유지하면서 버튼 텍스트 업데이트
            const countSpan = helpfulBtn.querySelector('.helpful-count');
            const currentCount = countSpan ? countSpan.textContent : '0';
            helpfulBtn.innerHTML = `<i class="fas fa-thumbs-up"></i> 도움됨 (<span class="helpful-count">${currentCount}</span>) ✓`;
        }
    }
}

/**
 * 도움안됨 버튼 상태 업데이트
 */
function updateUnhelpfulButton(faqId, isClicked) {
    const unhelpfulBtn = document.querySelector(`.unhelpful-btn[data-faq-id="${faqId}"]`);
    if (unhelpfulBtn) {
        if (isClicked) {
            unhelpfulBtn.classList.add('clicked');
            unhelpfulBtn.disabled = true;
            // 카운트 정보를 유지하면서 버튼 텍스트 업데이트
            const countSpan = unhelpfulBtn.querySelector('.unhelpful-count');
            const currentCount = countSpan ? countSpan.textContent : '0';
            unhelpfulBtn.innerHTML = `<i class="fas fa-thumbs-down"></i> 도움안됨 (<span class="unhelpful-count">${currentCount}</span>) ✓`;
        }
    }
}

/**
 * 도움됨 수 업데이트 (서버 값 사용)
 */
function updateHelpfulCountWithValue(faqId, newCount) {
    console.log(`도움됨 카운트 업데이트: faqId=${faqId}, newCount=${newCount}`);
    
    // data-faq-id가 있는 버튼 내부의 .helpful-count를 찾기
    const helpfulBtn = document.querySelector(`.helpful-btn[data-faq-id="${faqId}"]`);
    console.log('찾은 helpful 버튼:', helpfulBtn);
    
    if (helpfulBtn) {
        console.log('버튼의 HTML 내용:', helpfulBtn.innerHTML);
        const helpfulCountSpan = helpfulBtn.querySelector('.helpful-count');
        console.log('찾은 helpful-count span:', helpfulCountSpan);
        
        if (helpfulCountSpan) {
            console.log(`helpful-count span 업데이트: ${helpfulCountSpan.textContent} -> ${newCount}`);
            helpfulCountSpan.textContent = newCount;
            console.log('업데이트 후 span 내용:', helpfulCountSpan.textContent);
        } else {
            console.log('helpful-count span을 찾을 수 없습니다.');
            console.log('버튼 내부의 모든 span 요소들:', helpfulBtn.querySelectorAll('span'));
        }
    } else {
        console.log(`data-faq-id="${faqId}"인 helpful-btn을 찾을 수 없습니다.`);
        console.log('페이지의 모든 helpful-btn들:', document.querySelectorAll('.helpful-btn'));
    }
}

/**
 * 도움안됨 수 업데이트 (서버 값 사용)
 */
function updateUnhelpfulCountWithValue(faqId, newCount) {
    console.log(`도움안됨 카운트 업데이트: faqId=${faqId}, newCount=${newCount}`);
    
    // data-faq-id가 있는 버튼 내부의 .unhelpful-count를 찾기
    const unhelpfulBtn = document.querySelector(`.unhelpful-btn[data-faq-id="${faqId}"]`);
    console.log('찾은 unhelpful 버튼:', unhelpfulBtn);
    
    if (unhelpfulBtn) {
        console.log('버튼의 HTML 내용:', unhelpfulBtn.innerHTML);
        const unhelpfulCountSpan = unhelpfulBtn.querySelector('.unhelpful-count');
        console.log('찾은 unhelpful-count span:', unhelpfulCountSpan);
        
        if (unhelpfulCountSpan) {
            console.log(`unhelpful-count span 업데이트: ${unhelpfulCountSpan.textContent} -> ${newCount}`);
            unhelpfulCountSpan.textContent = newCount;
            console.log('업데이트 후 span 내용:', unhelpfulCountSpan.textContent);
        } else {
            console.log('unhelpful-count span을 찾을 수 없습니다.');
            console.log('버튼 내부의 모든 span 요소들:', unhelpfulBtn.querySelectorAll('span'));
        }
    } else {
        console.log(`data-faq-id="${faqId}"인 unhelpful-btn을 찾을 수 없습니다.`);
        console.log('페이지의 모든 unhelpful-btn들:', document.querySelectorAll('.unhelpful-btn'));
    }
}

/**
 * 도움됨 수 업데이트 (기존 함수 - 호환성 유지)
 */
function updateHelpfulCount(faqId) {
    const helpfulBtn = document.querySelector(`.helpful-btn[data-faq-id="${faqId}"]`);
    if (helpfulBtn) {
        const helpfulCountSpan = helpfulBtn.querySelector('.helpful-count');
        if (helpfulCountSpan) {
            const currentCount = parseInt(helpfulCountSpan.textContent) || 0;
            helpfulCountSpan.textContent = currentCount + 1;
        }
    }
}

/**
 * 도움안됨 수 업데이트 (기존 함수 - 호환성 유지)
 */
function updateUnhelpfulCount(faqId) {
    const unhelpfulBtn = document.querySelector(`.unhelpful-btn[data-faq-id="${faqId}"]`);
    if (unhelpfulBtn) {
        const unhelpfulCountSpan = unhelpfulBtn.querySelector('.unhelpful-count');
        if (unhelpfulCountSpan) {
            const currentCount = parseInt(unhelpfulCountSpan.textContent) || 0;
            unhelpfulCountSpan.textContent = currentCount + 1;
        }
    }
}




