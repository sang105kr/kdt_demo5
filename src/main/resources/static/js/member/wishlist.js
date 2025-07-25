// 위시리스트 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('위시리스트 페이지 로드됨');
    
    // 초기화
    initializeWishlistPage();
    setupEventListeners();
    animateWishlistItems();
    formatPrices();
});

/**
 * 위시리스트 페이지 초기화
 */
function initializeWishlistPage() {
    // 위시리스트 개수 업데이트 (Top 메뉴)
    updateWishlistCount();
    
    // 페이지 로드 애니메이션
    const container = document.querySelector('.wishlist-container');
    if (container) {
        container.style.opacity = '0';
        setTimeout(() => {
            container.style.transition = 'opacity 0.5s ease';
            container.style.opacity = '1';
        }, 100);
    }
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // 위시리스트 제거 버튼
    document.querySelectorAll('.remove-wishlist-btn').forEach(button => {
        button.addEventListener('click', handleRemoveFromWishlist);
    });
    
    // 장바구니 담기 버튼
    document.querySelectorAll('.btn-cart').forEach(button => {
        button.addEventListener('click', handleAddToCart);
    });
    
    // 전체 삭제 버튼
    const clearAllBtn = document.querySelector('.clear-all-btn');
    if (clearAllBtn) {
        clearAllBtn.addEventListener('click', handleClearAllWishlist);
    }
    
    // 카드 호버 효과
    document.querySelectorAll('.wishlist-card').forEach(card => {
        card.addEventListener('mouseenter', handleCardHover);
        card.addEventListener('mouseleave', handleCardLeave);
    });
}

/**
 * 위시리스트에서 제거
 */
async function handleRemoveFromWishlist(event) {
    event.preventDefault();
    event.stopPropagation();
    
    const button = event.currentTarget;
    const productId = button.dataset.productId;
    const wishlistItem = button.closest('.wishlist-item');
    
    if (!productId) {
        console.error('Product ID not found');
        return;
    }
    
    // 확인 다이얼로그
    const productName = wishlistItem.querySelector('.product-name a')?.textContent || '상품';
    
    showModal({
        title: '위시리스트 제거',
        message: `"${productName}"을(를) 위시리스트에서 제거하시겠습니까?`,
        onConfirm: () => {
            // 실제 제거 로직 실행
            executeWishlistRemove(button, productId, wishlistItem);
        },
        onCancel: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
}

// 위시리스트 제거 실행 함수
async function executeWishlistRemove(button, productId, wishlistItem) {
    try {
        // 로딩 상태 표시
        button.disabled = true;
        wishlistItem.style.opacity = '0.6';
        
        // API 호출
        const response = await fetch(`/api/wishlist/remove/${productId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        const result = await response.json();
        
        if (response.ok && result.code === '00') {
            // 성공 시 아이템 제거 애니메이션
            animateItemRemoval(wishlistItem);
            
            // 위시리스트 개수 업데이트
            updateWishlistCount();
            
            // 성공 메시지 표시 (details의 message 또는 기본 메시지 사용)
            const message = result.details?.message || '위시리스트에서 제거되었습니다.';
            showMessage(message, 'success');
            
            // 만약 마지막 아이템이었다면 페이지 리로드
            setTimeout(() => {
                const remainingItems = document.querySelectorAll('.wishlist-item').length;
                if (remainingItems <= 1) {
                    location.reload();
                }
            }, 600);
            
        } else {
            throw new Error(result.message || '위시리스트 제거에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('위시리스트 제거 오류:', error);
        showMessage(error.message || '위시리스트 제거에 실패했습니다.', 'error');
        
        // 상태 복원
        button.disabled = false;
        wishlistItem.style.opacity = '1';
    }
}

/**
 * 장바구니에 추가
 */
async function handleAddToCart(event) {
    event.preventDefault();
    
    const button = event.currentTarget;
    const productId = button.dataset.productId;
    const wishlistItem = button.closest('.wishlist-item');
    const productName = wishlistItem.querySelector('.product-name a')?.textContent || '상품';
    
    if (!productId) {
        console.error('Product ID not found');
        return;
    }
    
    try {
        // 로딩 상태 표시
        const originalText = button.textContent;
        button.textContent = '추가 중...';
        button.disabled = true;
        
        // API 호출 (장바구니 API 필요)
        const response = await fetch('/api/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                productId: parseInt(productId),
                quantity: 1
            })
        });
        
        const result = await response.text();
        
        if (response.ok && result === 'success') {
            // 성공 메시지 표시
            showMessage(`"${productName}"이(가) 장바구니에 추가되었습니다.`, 'success');
            
            // 장바구니 개수 업데이트 (cart API 필요)
            updateCartCount();
            
        } else {
            throw new Error(result.message || '장바구니 추가에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('장바구니 추가 오류:', error);
        showMessage(error.message || '장바구니 추가에 실패했습니다.', 'error');
    } finally {
        // 상태 복원
        button.textContent = '장바구니 담기';
        button.disabled = false;
    }
}

/**
 * 전체 위시리스트 삭제
 */
async function handleClearAllWishlist(event) {
    event.preventDefault();
    
    const totalItems = document.querySelectorAll('.wishlist-item').length;
    
    showModal({
        title: '전체 위시리스트 삭제',
        message: `위시리스트의 모든 상품(${totalItems}개)을 삭제하시겠습니까?`,
        onConfirm: () => {
            // 실제 전체 삭제 로직 실행
            executeClearAllWishlist();
        },
        onCancel: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
}

// 전체 위시리스트 삭제 실행 함수
async function executeClearAllWishlist() {
    
    const button = document.querySelector('.clear-all-btn');
    
    if (!button) {
        console.error('전체 삭제 버튼을 찾을 수 없습니다.');
        return;
    }
    
    try {
        // 로딩 상태 표시
        const originalText = button.textContent;
        button.textContent = '삭제 중...';
        button.disabled = true;
        
        // 전체 컨테이너에 로딩 클래스 추가
        const container = document.querySelector('.wishlist-container');
        if (container) {
            container.classList.add('loading');
        }
        
        // API 호출
        const response = await fetch('/api/wishlist/clear', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        const result = await response.json();
        
        if (response.ok && result.code === '00') {
            // 성공 메시지 표시 (details의 message 또는 기본 메시지 사용)
            const message = result.details?.message || '위시리스트가 모두 삭제되었습니다.';
            showMessage(message, 'success');
            
            // 위시리스트 개수 업데이트
            updateWishlistCount();
            
            // 페이지 리로드
            setTimeout(() => {
                location.reload();
            }, 1000);
            
        } else {
            throw new Error(result.message || '위시리스트 전체 삭제에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('위시리스트 전체 삭제 오류:', error);
        showMessage(error.message || '위시리스트 전체 삭제에 실패했습니다.', 'error');
        
        // 상태 복원
        if (button) {
            button.textContent = '전체 삭제';
            button.disabled = false;
        }
        
        const container = document.querySelector('.wishlist-container');
        if (container) {
            container.classList.remove('loading');
        }
    }
}

/**
 * 아이템 제거 애니메이션
 */
function animateItemRemoval(item) {
    item.style.transition = 'all 0.5s ease';
    item.style.transform = 'translateX(100%)';
    item.style.opacity = '0';
    
    setTimeout(() => {
        item.remove();
    }, 500);
}

/**
 * 위시리스트 아이템 애니메이션
 */
function animateWishlistItems() {
    const items = document.querySelectorAll('.wishlist-item');
    
    items.forEach((item, index) => {
        // CSS에서 이미 애니메이션이 정의되어 있으므로 추가 설정 불필요
        // 하지만 동적으로 추가된 아이템의 경우를 위해 함수 유지
    });
}

/**
 * 카드 호버 효과
 */
function handleCardHover(event) {
    const card = event.currentTarget;
    const removeBtn = card.querySelector('.remove-wishlist-btn');
    
    if (removeBtn) {
        removeBtn.style.opacity = '1';
        removeBtn.style.transform = 'scale(1.1)';
    }
}

function handleCardLeave(event) {
    const card = event.currentTarget;
    const removeBtn = card.querySelector('.remove-wishlist-btn');
    
    if (removeBtn) {
        removeBtn.style.opacity = '';
        removeBtn.style.transform = '';
    }
}

/**
 * 가격 포맷팅
 */
function formatPrices() {
    const priceElements = document.querySelectorAll('.price-amount');
    
    priceElements.forEach(element => {
        const text = element.textContent;
        if (text && text.includes('원')) {
            // 이미 포맷팅되어 있으므로 추가 처리 불필요
            // Thymeleaf에서 서버사이드 포맷팅을 사용하고 있음
        }
    });
}

/**
 * 위시리스트 개수 업데이트 (Top 메뉴 연동)
 */
async function updateWishlistCount() {
    try {
        const response = await fetch('/api/wishlist/count');
        if (response.ok) {
            const result = await response.json();
            
            // Top 메뉴의 위시리스트 개수 업데이트
            const countElement = document.querySelector('.wishlist-count');
            if (countElement && result.code === '00') {
                const count = result.data || 0;
                countElement.textContent = count.toString();
                
                                 // 개수가 0이면 숨김, 아니면 표시
                 if (count > 0) {
                     countElement.style.display = 'flex';
                 } else {
                     countElement.style.display = 'none';
                 }
            }
        }
    } catch (error) {
        console.error('위시리스트 개수 업데이트 오류:', error);
    }
}

/**
 * 장바구니 개수 업데이트 (Top 메뉴 연동)
 */
async function updateCartCount() {
    try {
        const response = await fetch('/cart/count');
        if (response.ok) {
            const result = await response.json();
            
            // Top 메뉴의 장바구니 개수 업데이트
            const countElement = document.querySelector('.cart-count');
            if (countElement && result.count !== undefined) {
                countElement.textContent = result.count || '0';
                
                // 개수가 0이면 숨김, 아니면 표시
                if (result.count > 0) {
                    countElement.style.display = 'flex';
                } else {
                    countElement.style.display = 'none';
                }
            }
        }
    } catch (error) {
        console.error('장바구니 개수 업데이트 오류:', error);
    }
}

/**
 * 메시지 표시 함수
 */
function showMessage(message, type = 'info') {
    // 기존 메시지 제거
    const existingMessage = document.querySelector('.temp-message');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    // 메시지 요소 생성
    const messageElement = document.createElement('div');
    messageElement.className = `message ${type} temp-message`;
    messageElement.innerHTML = `<span>${message}</span>`;
    
    // 메시지 삽입
    const container = document.querySelector('.wishlist-container');
    const pageHeader = container.querySelector('.page-header');
    pageHeader.after(messageElement);
    
    // 애니메이션
    messageElement.style.opacity = '0';
    messageElement.style.transform = 'translateY(-20px)';
    messageElement.style.transition = 'all 0.3s ease';
    
    setTimeout(() => {
        messageElement.style.opacity = '1';
        messageElement.style.transform = 'translateY(0)';
    }, 100);
    
    // 자동 제거
    setTimeout(() => {
        if (messageElement.parentNode) {
            messageElement.style.opacity = '0';
            messageElement.style.transform = 'translateY(-20px)';
            setTimeout(() => {
                if (messageElement.parentNode) {
                    messageElement.remove();
                }
            }, 300);
        }
    }, 3000);
}

/**
 * 스크롤 위치 복원
 */
window.addEventListener('beforeunload', function() {
    sessionStorage.setItem('wishlistScrollPosition', window.scrollY);
});

window.addEventListener('load', function() {
    const scrollPosition = sessionStorage.getItem('wishlistScrollPosition');
    if (scrollPosition) {
        window.scrollTo(0, parseInt(scrollPosition));
        sessionStorage.removeItem('wishlistScrollPosition');
    }
});

/**
 * 키보드 네비게이션 지원
 */
document.addEventListener('keydown', function(event) {
    // ESC 키로 확인 다이얼로그나 메시지 닫기
    if (event.key === 'Escape') {
        const tempMessage = document.querySelector('.temp-message');
        if (tempMessage) {
            tempMessage.remove();
        }
    }
});

/**
 * 반응형 그리드 조정
 */
function adjustGrid() {
    const grid = document.querySelector('.wishlist-grid');
    if (!grid) return;
    
    const containerWidth = grid.offsetWidth;
    const cardMinWidth = 320;
    const gap = 25;
    
    const columns = Math.floor((containerWidth + gap) / (cardMinWidth + gap));
    
    if (columns > 0) {
        grid.style.gridTemplateColumns = `repeat(${columns}, 1fr)`;
    }
}

// 리사이즈 이벤트
window.addEventListener('resize', adjustGrid);
window.addEventListener('load', adjustGrid); 