/**
 * 상품 상세 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // 수량 증가
    function increaseQuantity() {
        const quantityInput = document.getElementById('quantity');
        const maxQuantity = parseInt(quantityInput.max);
        const currentQuantity = parseInt(quantityInput.value);
        
        if (currentQuantity < maxQuantity) {
            quantityInput.value = currentQuantity + 1;
        }
    }
    
    // 수량 감소
    function decreaseQuantity() {
        const quantityInput = document.getElementById('quantity');
        const currentQuantity = parseInt(quantityInput.value);
        
        if (currentQuantity > 1) {
            quantityInput.value = currentQuantity - 1;
        }
    }
    
    // 장바구니 담기 기능
    function addToCart(productId) {
        // 로그인 체크 (세션에서 확인)
        const isLoggedIn = document.querySelector('[data-logged-in]')?.dataset.loggedIn === 'true';
        
        if (!isLoggedIn) {
            alert('로그인이 필요한 서비스입니다.');
            window.location.href = '/login';
            return;
        }
        
        // 수량 가져오기
        const quantity = parseInt(document.getElementById('quantity').value) || 1;
        
        // 장바구니에 추가 요청
        const formData = new FormData();
        formData.append('productId', productId);
        formData.append('quantity', quantity);
        
        fetch('/cart/add', {
            method: 'POST',
            body: formData
        })
        .then(response => response.text())
        .then(result => {
            if (result === 'success') {
                showNotification('장바구니에 추가되었습니다.', 'success');
                updateCartCount();
            } else {
                showNotification(result || '장바구니 추가에 실패했습니다.', 'error');
            }
        })
        .catch(error => {
            console.error('장바구니 추가 실패:', error);
            showNotification('장바구니 추가 중 오류가 발생했습니다.', 'error');
        });
    }
    
    // 바로 구매 기능
    function buyNow(productId) {
        // 로그인 체크
        const isLoggedIn = document.querySelector('[data-logged-in]')?.dataset.loggedIn === 'true';
        
        if (!isLoggedIn) {
            alert('로그인이 필요한 서비스입니다.');
            window.location.href = '/login';
            return;
        }
        
        // 바로 구매 페이지로 이동
        window.location.href = `/order/buy-now?productId=${productId}&quantity=1`;
    }
    
    // 전역 함수로 노출 (HTML에서 호출)
    window.addToCart = addToCart;
    window.buyNow = buyNow;
    window.increaseQuantity = increaseQuantity;
    window.decreaseQuantity = decreaseQuantity;
    
    // 알림 표시 함수
    function showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 1rem 1.5rem;
            border-radius: 6px;
            color: white;
            font-weight: 500;
            z-index: 10000;
            animation: slideIn 0.3s ease-out;
            max-width: 300px;
        `;
        
        // 타입별 스타일
        const styles = {
            success: 'background: #28a745;',
            error: 'background: #dc3545;',
            info: 'background: #17a2b8;',
            warning: 'background: #ffc107; color: #1a1a1a;'
        };
        
        notification.style.cssText += styles[type] || styles.info;
        notification.textContent = message;
        
        // 애니메이션 스타일 추가
        const style = document.createElement('style');
        style.textContent = `
            @keyframes slideIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            @keyframes slideOut {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }
        `;
        document.head.appendChild(style);
        
        document.body.appendChild(notification);
        
        // 3초 후 자동 제거
        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease-in';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.remove();
                }
            }, 300);
        }, 3000);
    }
    
    // 장바구니 개수 업데이트
    function updateCartCount() {
        fetch('/cart/count')
            .then(response => response.text())
            .then(count => {
                const cartCountElement = document.querySelector('.cart-count');
                if (cartCountElement) {
                    cartCountElement.textContent = count;
                    cartCountElement.style.display = count > 0 ? 'block' : 'none';
                }
            })
            .catch(error => {
                console.error('장바구니 개수 조회 실패:', error);
            });
    }
    
    // 상품 이미지 확대 기능
    const mainImage = document.querySelector('.product-main-img');
    if (mainImage) {
        mainImage.addEventListener('click', function() {
            showImageModal(this.src, this.alt);
        });
        
        // 커서 스타일 변경
        mainImage.style.cursor = 'pointer';
    }
    
    // 이미지 모달 표시
    function showImageModal(imageSrc, imageAlt) {
        const modal = document.createElement('div');
        modal.className = 'image-modal';
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.9);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10000;
            cursor: pointer;
        `;
        
        const image = document.createElement('img');
        image.src = imageSrc;
        image.alt = imageAlt;
        image.style.cssText = `
            max-width: 90%;
            max-height: 90%;
            object-fit: contain;
            border-radius: 8px;
        `;
        
        modal.appendChild(image);
        document.body.appendChild(modal);
        
        // 클릭 시 모달 닫기
        modal.addEventListener('click', function() {
            document.body.removeChild(modal);
        });
        
        // ESC 키로 모달 닫기
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                if (modal.parentNode) {
                    document.body.removeChild(modal);
                }
            }
        });
    }
    
    // 상품 설명 더보기/접기 기능
    const descriptionText = document.querySelector('.description-text');
    if (descriptionText && descriptionText.textContent.length > 200) {
        const originalText = descriptionText.textContent;
        const shortText = originalText.substring(0, 200) + '...';
        
        descriptionText.textContent = shortText;
        
        const toggleButton = document.createElement('button');
        toggleButton.className = 'btn btn-secondary description-toggle';
        toggleButton.textContent = '더보기';
        toggleButton.style.cssText = `
            margin-top: 0.5rem;
            padding: 0.5rem 1rem;
            font-size: 0.9rem;
        `;
        
        let isExpanded = false;
        toggleButton.addEventListener('click', function() {
            if (isExpanded) {
                descriptionText.textContent = shortText;
                this.textContent = '더보기';
            } else {
                descriptionText.textContent = originalText;
                this.textContent = '접기';
            }
            isExpanded = !isExpanded;
        });
        
        descriptionText.parentNode.appendChild(toggleButton);
    }
    
    // 관련 상품 로드 (선택사항)
    function loadRelatedProducts() {
        const productCategory = document.querySelector('.category-tag')?.textContent;
        if (!productCategory) return;
        
        fetch(`/api/products/related?category=${encodeURIComponent(productCategory)}&limit=4`)
            .then(response => response.json())
            .then(data => {
                if (data.success && data.products.length > 0) {
                    displayRelatedProducts(data.products);
                }
            })
            .catch(error => {
                console.error('관련 상품 로드 실패:', error);
            });
    }
    
    // 관련 상품 표시
    function displayRelatedProducts(products) {
        const relatedGrid = document.querySelector('.related-grid');
        if (!relatedGrid) return;
        
        relatedGrid.innerHTML = '';
        
        products.forEach(product => {
            const productCard = document.createElement('div');
            productCard.className = 'related-product-card';
            productCard.style.cssText = `
                background: white;
                border: 1px solid #eee;
                border-radius: 8px;
                overflow: hidden;
                transition: all 0.3s;
                cursor: pointer;
            `;
            
            productCard.innerHTML = `
                <div style="height: 150px; overflow: hidden;">
                    <img src="${product.imageUrl || '/images/no-image.png'}" 
                         alt="${product.pname}" 
                         style="width: 100%; height: 100%; object-fit: cover;">
                </div>
                <div style="padding: 1rem;">
                    <h4 style="margin: 0 0 0.5rem 0; font-size: 1rem;">${product.pname}</h4>
                    <p style="margin: 0; color: #666; font-size: 0.9rem;">${product.price.toLocaleString()}원</p>
                </div>
            `;
            
            productCard.addEventListener('click', function() {
                window.location.href = `/products/${product.productId}`;
            });
            
            productCard.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-2px)';
                this.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
            });
            
            productCard.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
                this.style.boxShadow = 'none';
            });
            
            relatedGrid.appendChild(productCard);
        });
    }
    
    // 페이지 공유 기능
    function shareProduct() {
        const productName = document.querySelector('.product-name')?.textContent;
        const currentUrl = window.location.href;
        
        if (navigator.share) {
            navigator.share({
                title: productName,
                url: currentUrl
            });
        } else {
            // 클립보드에 복사
            navigator.clipboard.writeText(currentUrl).then(() => {
                showNotification('링크가 클립보드에 복사되었습니다.', 'success');
            }).catch(() => {
                showNotification('링크 복사에 실패했습니다.', 'error');
            });
        }
    }
    
    // 공유 버튼 추가
    const shareButton = document.createElement('button');
    shareButton.className = 'btn btn-secondary';
    shareButton.innerHTML = `
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="18" cy="5" r="3"></circle>
            <circle cx="6" cy="12" r="3"></circle>
            <circle cx="18" cy="19" r="3"></circle>
            <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
            <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
        </svg>
        공유하기
    `;
    shareButton.addEventListener('click', shareProduct);
    
    // 구매 액션 영역에 공유 버튼 추가
    const purchaseActions = document.querySelector('.purchase-actions');
    if (purchaseActions) {
        purchaseActions.appendChild(shareButton);
    }
    
    // 페이지 로드 시 관련 상품 로드
    // loadRelatedProducts();
    
    // 페이지 로드 완료 메시지
    console.log('상품 상세 페이지가 로드되었습니다.');
}); 