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
    const currentQuantity = parseInt(quantityInput.value);
    const maxQuantity = parseInt(quantityInput.max);
    
    let newQuantity = currentQuantity + change;
    
    // 최소/최대 수량 제한
    if (newQuantity < 1) {
        newQuantity = 1;
    } else if (newQuantity > maxQuantity) {
        newQuantity = maxQuantity;
        alert('재고 수량을 초과할 수 없습니다.');
        return;
    }
    
    updateQuantityDirect(cartItemId, newQuantity);
}

/**
 * 직접 수량 업데이트
 */
function updateQuantityDirect(cartItemId, quantity) {
    fetch(`/cart/update/${cartItemId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `quantity=${quantity}`
    })
    .then(response => response.text())
    .then(result => {
        if (result === 'success') {
            // 페이지 새로고침으로 업데이트된 정보 반영
            location.reload();
        } else {
            alert(result);
        }
    })
    .catch(error => {
        console.error('수량 업데이트 실패:', error);
        alert('수량 업데이트에 실패했습니다.');
    });
}

/**
 * 장바구니에서 상품 삭제
 */
function removeFromCart(cartItemId) {
    if (!confirm('정말로 이 상품을 장바구니에서 삭제하시겠습니까?')) {
        return;
    }
    
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
            alert(result);
        }
    })
    .catch(error => {
        console.error('상품 삭제 실패:', error);
        alert('상품 삭제에 실패했습니다.');
    });
}

/**
 * 장바구니 전체 비우기
 */
function clearCart() {
    if (!confirm('정말로 장바구니를 비우시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.')) {
        return;
    }
    
    fetch('/cart/clear', {
        method: 'POST'
    })
    .then(response => response.text())
    .then(result => {
        if (result === 'success') {
            location.reload();
        } else {
            alert(result);
        }
    })
    .catch(error => {
        console.error('장바구니 비우기 실패:', error);
        alert('장바구니 비우기에 실패했습니다.');
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