// 장바구니 목록 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('장바구니 목록 페이지 로드됨');
    
    // 수량 변경 이벤트 설정
    setupQuantityControls();
    
    // 메시지 자동 숨김
    setupMessageAutoHide();
});

/**
 * 수량 컨트롤 설정
 */
function setupQuantityControls() {
    const quantityInputs = document.querySelectorAll('.quantity-input');
    
    quantityInputs.forEach(input => {
        input.addEventListener('change', function() {
            const cartItemId = this.closest('.cart-item').dataset.cartItemId;
            const quantity = parseInt(this.value);
            
            if (quantity > 0) {
                updateQuantityDirect(cartItemId, quantity);
            } else {
                this.value = 1;
            }
        });
    });
}

/**
 * 수량 증가/감소
 */
function updateQuantity(cartItemId, change) {
    const cartItem = document.querySelector(`[data-cart-item-id="${cartItemId}"]`);
    const quantityInput = cartItem.querySelector('.quantity-input');
    const currentQuantity = parseInt(quantityInput.value) || 0;
    const maxQuantity = parseInt(quantityInput.max) || 999;
    
    let newQuantity = currentQuantity + change;
    
    // 최소/최대 수량 제한
    if (newQuantity < 1) {
        newQuantity = 1;
    } else if (newQuantity > maxQuantity) {
        newQuantity = maxQuantity;
        showModal({
            message: '재고 수량을 초과할 수 없습니다.'
        });
        return;
    }
    
    updateQuantityDirect(cartItemId, newQuantity);
}

/**
 * 직접 수량 업데이트
 */
async function updateQuantityDirect(cartItemId, quantity) {
    // 수량을 숫자로 변환
    const numericQuantity = parseInt(quantity);
    
    // NaN 체크
    if (isNaN(numericQuantity) || numericQuantity < 1) {
        console.error('유효하지 않은 수량:', quantity);
        showErrorMessage('유효하지 않은 수량입니다.');
        return;
    }
    
    console.log('수량 업데이트 시작:', { cartItemId, quantity: numericQuantity });
    
    // 해당 cart-item 요소 찾기
    const cartItem = document.querySelector(`[data-cart-item-id="${cartItemId}"]`);
    if (!cartItem) {
        console.error('장바구니 아이템을 찾을 수 없습니다:', cartItemId);
        return;
    }
    
    // 로딩 상태 표시 (버튼 비활성화)
    const quantityInput = cartItem.querySelector('.quantity-input');
    const minusBtn = cartItem.querySelector('.quantity-btn.minus');
    const plusBtn = cartItem.querySelector('.quantity-btn.plus');
    
    quantityInput.disabled = true;
    minusBtn.disabled = true;
    plusBtn.disabled = true;
    
    try {
        // AJAX 요청
        const response = await ajax.post(`/cart/update/${cartItemId}`, { quantity: numericQuantity });
        console.log('수량 업데이트 응답:', response);
        
        if (response.success) {
            // 성공 시 DOM 업데이트
            updateCartItemUI(cartItem, response);
            updateCartSummary(response);
            showSuccessMessage('수량이 업데이트되었습니다.');
        } else {
            // 실패 시 원래 값으로 복원
            quantityInput.value = quantityInput.defaultValue;
            showErrorMessage(response.message || '수량 업데이트에 실패했습니다.');
        }
    } catch (error) {
        console.error('수량 업데이트 실패:', error);
        // 실패 시 원래 값으로 복원
        quantityInput.value = quantityInput.defaultValue;
        showErrorMessage('수량 업데이트에 실패했습니다.');
    } finally {        // 로딩 상태 해제
        quantityInput.disabled = false;
        minusBtn.disabled = false;
        plusBtn.disabled = false;
    }
}

/**
 * 장바구니 아이템 UI 업데이트
 */
function updateCartItemUI(cartItem, response) {
    // 수량 업데이트
    const quantityInput = cartItem.querySelector('.quantity-input');
    quantityInput.value = response.updatedQuantity;
    quantityInput.defaultValue = response.updatedQuantity;
    
    // 소계 업데이트
    const totalAmount = cartItem.querySelector('.total-amount');
    if (totalAmount) {
        totalAmount.textContent = formatNumber(response.updatedTotalPrice) + '원';
    }
}

/**
 * 장바구니 요약 정보 업데이트
 */
function updateCartSummary(response) {
    // 총 주문금액 업데이트
    const cartTotalAmount = document.querySelector('.summary-item.total span:last-child');
    if (cartTotalAmount) {
        cartTotalAmount.textContent = formatNumber(response.cartTotalAmount) + '원';
    }
    
    // 상품 개수 업데이트
    const itemCount = document.querySelector('.summary-item:first-child span:last-child');
    if (itemCount) {
        itemCount.textContent = response.itemCount + '개';
    }
}

/**
 * 숫자 포맷팅 (천 단위 콤마)
 */
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

/**
 * 성공 메시지 표시
 */
function showSuccessMessage(message) {
    showMessage(message, 'success');
}

/**
 * 오류 메시지 표시
 */
function showErrorMessage(message) {
    showMessage(message, 'error');
}

/**
 * 메시지 표시
 */
function showMessage(message, type) {
    // 기존 메시지 제거
    const existingMessage = document.querySelector('.message');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    // 새 메시지 생성
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.innerHTML = `<span>${message}</span>`;
    
    // 메시지 삽입
    const cartContainer = document.querySelector('.cart-container');
    const cartHeader = document.querySelector('.cart-header');
    cartContainer.insertBefore(messageDiv, cartHeader.nextSibling);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        messageDiv.style.opacity = '0';
        setTimeout(() => {
            messageDiv.remove();
        }, 300);
    }, 3000);
}

/**
 * 장바구니에서 상품 삭제
 */
function removeFromCart(cartItemId) {
    showModal({
        message: '정말로 이 상품을 장바구니에서 삭제하시겠습니까?',
        onConfirm: () => {
            fetch(`/cart/remove/${cartItemId}`, {
                method: 'POST'
            })
            .then(response => response.text())
            .then(result => {
                if (result === 'success') {
                    // 해당 아이템 제거
                    const cartItem = document.querySelector(`[data-cart-item-id="${cartItemId}"]`);
                    cartItem.remove();

                    // 장바구니가 비어있는지 확인
                    const remainingItems = document.querySelectorAll('.cart-item');
                    if (remainingItems.length === 0) {
                        location.reload(); // 빈 장바구니 페이지로 새로고침
                    } else {
                        // 총액 업데이트를 위해 페이지 새로고침
                        location.reload();
                    }
                } else {
                    showModal({ message: result });
                }
            })
            .catch(error => {
                console.error('상품 삭제 실패:', error);
                showModal({ message: '상품 삭제에 실패했습니다.' });
            });
        }
    });
}

/**
 * 장바구니 전체 비우기
 */
function clearCart() {
    showModal({
        message: '정말로 장바구니를 비우시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.',
        onConfirm: () => {
            fetch('/cart/clear', {
                method: 'POST'
            })
            .then(response => response.text())
            .then(result => {
                if (result === 'success') {
                    location.reload();
                } else {
                    showModal({ message: result });
                }
            })
            .catch(error => {
                console.error('장바구니 비우기 실패:', error);
                showModal({ message: '장바구니 비우기에 실패했습니다.' });
            });
        }
    });
}

/**
 * 할인 적용
 */
function applyDiscount(cartItemId, discountRate) {
    const discountPercent = Math.round(discountRate * 100);
    showModal({
        message: `${discountPercent}% 할인을 적용하시겠습니까?`,
        onConfirm: () => {
            fetch(`/cart/discount/${cartItemId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `discountRate=${discountRate}`
            })
            .then(response => response.text())
            .then(result => {
                if (result === 'success') {
                    location.reload();
                } else {
                    showModal({ message: result });
                }
            })
            .catch(error => {
                console.error('할인 적용 실패:', error);
                showModal({ message: '할인 적용에 실패했습니다.' });
            });
        }
    });
}

/**
 * 메시지 자동 숨김 설정
 */
function setupMessageAutoHide() {
    const messages = document.querySelectorAll('.message');
    
    messages.forEach(message => {
        setTimeout(() => {
            message.style.opacity = '0';
            setTimeout(() => {
                message.style.display = 'none';
            }, 300);
        }, 5000);
    });
}

/**
 * 장바구니 아이템 애니메이션
 */
function animateCartItems() {
    const cartItems = document.querySelectorAll('.cart-item');
    
    cartItems.forEach((item, index) => {
        item.style.opacity = '0';
        item.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            item.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateY(0)';
        }, index * 100);
    });
}

// 페이지 로드 시 애니메이션 실행
window.addEventListener('load', function() {
    animateCartItems();
}); 