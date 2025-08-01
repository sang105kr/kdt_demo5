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
        // productId가 문자열로 전달될 수 있으므로 숫자로 변환
        productId = parseInt(productId);
        
        // 로그인 체크 (세션에서 확인)
        const isLoggedIn = isCurrentUserLoggedIn();
        
        if (!isLoggedIn) {
            showLoginRequired();
            return;
        }
        
        // 수량 가져오기
        const quantity = parseInt(document.getElementById('quantity').value) || 1;
        
        // 장바구니에 추가 요청 (URL 경로에 productId 포함, quantity는 쿼리 파라미터)
        try {
            const result = await ajax.post(`/api/cart/add/${productId}?quantity=${quantity}`);
            
            if (result.code === '00') {
                showNotification(result.message || '장바구니에 추가되었습니다.', 'success');
                updateCartCount();
                
                // 구매 안내 메시지 표시
                setTimeout(() => {
                    showNotification('구매를 원하시면 상단 우측 "장바구니" 메뉴를 이용해주세요.', 'info');
                }, 1000);
            } else {
                showNotification(result.message || '장바구니 추가에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('장바구니 추가 실패:', error);
            showNotification('장바구니 추가 중 오류가 발생했습니다.', 'error');
        }
    }
    
    // 바로 구매 기능
    function buyNow(productId) {
        // productId가 문자열로 전달될 수 있으므로 숫자로 변환
        productId = parseInt(productId);
        
        // 로그인 체크
        const isLoggedIn = isCurrentUserLoggedIn();
        
        if (!isLoggedIn) {
            showLoginRequired();
            return;
        }
        
        // 바로 구매 페이지로 이동
        window.location.href = `/orders/direct/${productId}?quantity=1`;
    }
    
    // 위시리스트 토글 기능
    function toggleWishlist(productId, buttonElement) {
        // productId가 문자열로 전달될 수 있으므로 숫자로 변환
        productId = parseInt(productId);
        
        // 로그인 체크 (세션에서 확인)
        const isLoggedIn = isCurrentUserLoggedIn();
        
        if (!isLoggedIn) {
            showLoginRequired();
            return;
        }
        
        // 위시리스트 토글 요청
        try {
            const data = await ajax.post(`/api/wishlist/toggle/${productId}`);
            
            if (data.code === "00") {
                // 버튼 상태 업데이트 (details에서 isInWishlist 값 가져오기)
                const isWishlisted = data.details?.isInWishlist || data.data;
                updateWishlistButton(buttonElement, isWishlisted);
                
                // Top 메뉴 위시리스트 카운트 업데이트
                if (typeof updateWishlistCount === 'function') {
                    updateWishlistCount();
                }
                
                // 메시지는 details에서 가져오기
                const message = data.details?.message || 
                    (isWishlisted ? '위시리스트에 추가되었습니다.' : '위시리스트에서 제거되었습니다.');
                showNotification(message, 'success');
            } else {
                showNotification(data.message || '위시리스트 처리에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('위시리스트 처리 실패:', error);
            showNotification('위시리스트 처리 중 오류가 발생했습니다.', 'error');
        }
    }
    
    // 위시리스트 버튼 상태 업데이트
    function updateWishlistButton(buttonElement, isWishlisted) {
        if (!buttonElement) return;
        
        const heartIcon = buttonElement.querySelector('.heart-icon');
        const wishlistText = buttonElement.querySelector('.wishlist-text');
        
        if (isWishlisted) {
            // 위시리스트에 추가된 상태
            buttonElement.classList.add('wishlisted');
            if (heartIcon) {
                heartIcon.style.fill = 'currentColor';
            }
            if (wishlistText) {
                wishlistText.textContent = '위시리스트 ♥';
            }
        } else {
            // 위시리스트에서 제거된 상태
            buttonElement.classList.remove('wishlisted');
            if (heartIcon) {
                heartIcon.style.fill = 'none';
            }
            if (wishlistText) {
                wishlistText.textContent = '위시리스트';
            }
        }
    }
    
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
    
    // 장바구니 개수 업데이트 (common.js의 통합 함수 사용)
    async function updateCartCount() {
        if (typeof window.updateCartCount === 'function') {
            await window.updateCartCount();
        }
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
    
    // 이미지 갤러리 기능 (썸네일 클릭 시 메인 이미지 변경)
    function changeMainImage(imageSrc, thumbnailElement) {
        // 메인 이미지 변경
        const mainImage = document.getElementById('mainImage');
        if (mainImage) {
            mainImage.src = imageSrc;
        }
        
        // 모든 썸네일에서 active 클래스 제거
        const thumbnails = document.querySelectorAll('.thumbnail');
        thumbnails.forEach(thumb => thumb.classList.remove('active'));
        
        // 클릭된 썸네일에 active 클래스 추가
        if (thumbnailElement) {
            thumbnailElement.classList.add('active');
        }
    }
    
    // 전역 함수로 노출 (HTML에서 호출)
    window.changeMainImage = changeMainImage;
    
    // 탭 기능 구현
    function initializeTabs() {
        const tabButtons = document.querySelectorAll('.tab-btn');
        const tabPanes = document.querySelectorAll('.tab-pane');
        
        tabButtons.forEach(button => {
            button.addEventListener('click', function() {
                const targetTab = this.getAttribute('data-tab');
                
                // 모든 탭 버튼에서 active 클래스 제거
                tabButtons.forEach(btn => btn.classList.remove('active'));
                // 모든 탭 패널에서 active 클래스 제거
                tabPanes.forEach(pane => pane.classList.remove('active'));
                
                // 클릭된 버튼에 active 클래스 추가
                this.classList.add('active');
                
                // 해당 탭 패널에 active 클래스 추가
                const targetPane = document.getElementById(targetTab);
                if (targetPane) {
                    targetPane.classList.add('active');
                }
            });
        });
    }
    
    // 페이지 로드 시 탭 초기화
    initializeTabs();
    
    // 이미지 모달 표시
    function showImageModal(imageSrc, imageAlt) {
        const modal = document.getElementById('imageModal');
        const modalImage = document.getElementById('modalImage');
        
        if (modal && modalImage) {
            modalImage.src = imageSrc;
            modalImage.alt = imageAlt;
            modal.style.display = 'block';
            document.body.style.overflow = 'hidden'; // 스크롤 방지
        }
    }
    
    // 이미지 모달 닫기
    function closeImageModal() {
        const modal = document.getElementById('imageModal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = ''; // 스크롤 복원
        }
    }
    
    // 모달 외부 클릭 시 닫기
    document.addEventListener('DOMContentLoaded', function() {
        const modal = document.getElementById('imageModal');
        if (modal) {
            modal.addEventListener('click', function(e) {
                if (e.target === modal) {
                    closeImageModal();
                }
            });
        }
        
        // ESC 키로 모달 닫기
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeImageModal();
            }
        });
    });
    
    // 전역 함수로 노출
    window.showImageModal = showImageModal;
    window.closeImageModal = closeImageModal;
    
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
        
        try {
            const data = await ajax.get(`/api/products/related?category=${encodeURIComponent(productCategory)}&limit=4`);
            
            if (data.code === '00' && data.data && data.data.length > 0) {
                displayRelatedProducts(data.data);
            }
        } catch (error) {
            console.error('관련 상품 로드 실패:', error);
        }
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
    
    // 페이지 로드 시 관련 상품 로드
    // loadRelatedProducts();
    
    // 전역 함수로 노출 (HTML에서 호출)
    window.addToCart = addToCart;
    window.buyNow = buyNow;
    window.toggleWishlist = toggleWishlist;
    window.shareProduct = shareProduct;
    window.increaseQuantity = increaseQuantity;
    window.decreaseQuantity = decreaseQuantity;
    
    // 페이지 로드 완료 메시지
    console.log('상품 상세 페이지가 로드되었습니다.');
}); 