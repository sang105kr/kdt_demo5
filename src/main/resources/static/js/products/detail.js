/**
 * 상품 상세 페이지 JavaScript
 */

// 구매 상태에 따른 UI 업데이트
function updateReviewUI() {
    // 서버에서 전달받은 isPurchased 값을 사용
    const isPurchased = document.querySelector('[data-is-purchased]')?.dataset.isPurchased === 'true';
    
    // 구매한 사용자와 구매하지 않은 사용자에게 다른 메시지 표시
    const reviewActions = document.querySelector('.reviews-actions');
    if (reviewActions) {
        const purchasedActions = reviewActions.querySelector('.purchased-user-actions');
        const nonPurchasedActions = reviewActions.querySelector('.non-purchased-user-actions');
        
        if (purchasedActions && nonPurchasedActions) {
            if (isPurchased) {
                purchasedActions.style.display = 'block';
                nonPurchasedActions.style.display = 'none';
            } else {
                purchasedActions.style.display = 'none';
                nonPurchasedActions.style.display = 'block';
            }
        }
    }
    
    // 리뷰가 없는 경우의 메시지도 업데이트
    const reviewsPlaceholder = document.querySelector('.reviews-placeholder');
    if (reviewsPlaceholder) {
        const purchasedPrompt = reviewsPlaceholder.querySelector('.purchased-user-prompt');
        const nonPurchasedPrompt = reviewsPlaceholder.querySelector('.non-purchased-user-prompt');
        
        if (purchasedPrompt && nonPurchasedPrompt) {
            if (isPurchased) {
                purchasedPrompt.style.display = 'block';
                nonPurchasedPrompt.style.display = 'none';
            } else {
                purchasedPrompt.style.display = 'none';
                nonPurchasedPrompt.style.display = 'block';
            }
        }
    }
}

// 전역 함수: 알림 표시 - 모노크롬 스타일
function showNotification(message, type = 'info') {
    // 기존 알림 제거
    const existingNotifications = document.querySelectorAll('.notification-toast');
    existingNotifications.forEach(notification => {
        notification.remove();
    });
    
    // 알림 요소 생성
    const notification = document.createElement('div');
    notification.className = `notification-toast notification-${type}`;
    
    // 아이콘 설정
    let icon = '';
    switch (type) {
        case 'success':
            icon = '✓';
            break;
        case 'error':
            icon = '✕';
            break;
        case 'warning':
            icon = '⚠';
            break;
        default:
            icon = 'ℹ';
    }
    
    notification.innerHTML = `
        <div class="notification-content">
            <span class="notification-icon">${icon}</span>
            <span class="notification-message">${message}</span>
        </div>
    `;
    
    // 스타일 적용
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : type === 'warning' ? '#ffc107' : '#007bff'};
        color: white;
        padding: 12px 20px;
        border-radius: 6px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        z-index: 10000;
        font-weight: 500;
        opacity: 0;
        transform: translateX(100%);
        transition: all 0.3s ease;
        max-width: 400px;
        word-wrap: break-word;
    `;
    
    // DOM에 추가
    document.body.appendChild(notification);
    
    // 애니메이션
    requestAnimationFrame(() => {
        notification.style.opacity = '1';
        notification.style.transform = 'translateX(0)';
    });
    
    // 자동 제거
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 300);
    }, 3000);
}


// 전역 함수: 로그인 필요 알림
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

    // 전역 함수로 노출
    window.increaseQuantity = increaseQuantity;
    window.decreaseQuantity = decreaseQuantity;

    // 장바구니 담기 기능
    async function addToCart(productId) {
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
    async function toggleWishlist(productId, buttonElement) {
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

    // 장바구니 개수 업데이트 (common.js의 통합 함수 사용)
    async function updateCartCount() {
        if (typeof window.updateCartCount === 'function') {
            await window.updateCartCount();
        }
    }

    // 전역 함수로 노출
    window.updateCartCount = updateCartCount;

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
        console.log('탭 초기화 시작');
        const tabButtons = document.querySelectorAll('.tab-btn');
        const tabPanes = document.querySelectorAll('.tab-pane');

        console.log('탭 버튼 개수:', tabButtons.length);
        console.log('탭 패널 개수:', tabPanes.length);

        // URL에서 탭 파라미터 가져오기
        const urlParams = new URLSearchParams(window.location.search);
        const activeTab = urlParams.get('tab') || 'description';

        tabButtons.forEach((button, index) => {
            console.log(`탭 버튼 ${index}:`, button.textContent, 'data-tab:', button.getAttribute('data-tab'));
            button.addEventListener('click', function() {
                console.log('탭 클릭됨:', this.textContent, 'target:', this.getAttribute('data-tab'));
                const targetTab = this.getAttribute('data-tab');

                // URL 업데이트 (페이지 새로고침 없이)
                const newUrl = new URL(window.location);
                newUrl.searchParams.set('tab', targetTab);
                window.history.pushState({}, '', newUrl);

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
                    console.log('탭 패널 활성화:', targetTab);
                } else {
                    console.error('탭 패널을 찾을 수 없음:', targetTab);
                }
            });
        });

        // 초기 탭 설정
        const initialTabButton = document.querySelector(`[data-tab="${activeTab}"]`);
        const initialTabPane = document.getElementById(activeTab);
        
        if (initialTabButton && initialTabPane) {
            // 모든 탭에서 active 클래스 제거
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabPanes.forEach(pane => pane.classList.remove('active'));
            
            // 초기 탭 활성화
            initialTabButton.classList.add('active');
            initialTabPane.classList.add('active');
            console.log('초기 탭 설정:', activeTab);
        }
    }

    // 페이지 로드 시 탭 초기화
    initializeTabs();

    // 브라우저 뒤로가기/앞으로가기 처리
    window.addEventListener('popstate', function() {
        const urlParams = new URLSearchParams(window.location.search);
        const activeTab = urlParams.get('tab') || 'description';
        
        const tabButtons = document.querySelectorAll('.tab-btn');
        const tabPanes = document.querySelectorAll('.tab-pane');
        
        // 모든 탭에서 active 클래스 제거
        tabButtons.forEach(btn => btn.classList.remove('active'));
        tabPanes.forEach(pane => pane.classList.remove('active'));
        
        // 해당 탭 활성화
        const targetButton = document.querySelector(`[data-tab="${activeTab}"]`);
        const targetPane = document.getElementById(activeTab);
        
        if (targetButton && targetPane) {
            targetButton.classList.add('active');
            targetPane.classList.add('active');
            console.log('브라우저 네비게이션으로 탭 변경:', activeTab);
        }
    });

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
    window.addToCart = addToCart;
    window.buyNow = buyNow;
    window.toggleWishlist = toggleWishlist;
    window.shareProduct = shareProduct;

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
    async function loadRelatedProducts() {
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

    // 리뷰 정렬 함수
    function sortReviews(sortType) {
        console.log('리뷰 정렬:', sortType);
        // 현재 URL에서 정렬 파라미터만 변경 (페이지 새로고침 없이)
        const url = new URL(window.location);
        url.searchParams.set('reviewSort', sortType);
        window.history.pushState({}, '', url);
        
        // 정렬 버튼 상태 업데이트
        const sortButtons = document.querySelectorAll('.sort-select');
        sortButtons.forEach(button => {
            if (button.value === sortType) {
                button.classList.add('active');
            } else {
                button.classList.remove('active');
            }
        });
        
        // TODO: AJAX로 리뷰 목록 새로고침 (현재는 서버에서 처리 필요)
        showNotification('정렬이 적용되었습니다.', 'info');
    }

    // 리뷰 도움됨 표시
    async function markHelpful(reviewId) {
        const isLoggedIn = isCurrentUserLoggedIn();
        if (!isLoggedIn) {
            showLoginRequired();
            return;
        }

        try {
            const result = await ajax.post(`/api/reviews/${reviewId}/helpful`);
            if (result.code === '00') {
                // 도움됨 카운트 업데이트
                const helpfulBtn = document.querySelector(`button[onclick*="markHelpful(${reviewId})"]`);
                if (helpfulBtn) {
                    const helpfulCount = helpfulBtn.querySelector('.helpful-count');
                    if (helpfulCount) {
                        const currentCount = parseInt(helpfulCount.textContent) || 0;
                        helpfulCount.textContent = currentCount + 1;
                    }
                }
                showNotification('도움됨으로 표시되었습니다.', 'success');
            } else {
                showNotification(result.message || '처리에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('도움됨 처리 실패:', error);
            showNotification('처리 중 오류가 발생했습니다.', 'error');
        }
    }

    // 리뷰 필터 함수
    function filterReviews(filterType) {
        console.log('리뷰 필터:', filterType);
        
        // 필터 버튼 상태 업데이트
        const filterButtons = document.querySelectorAll('.reviews-filter .filter-btn');
        filterButtons.forEach(btn => {
            if (btn.getAttribute('data-filter') === filterType) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
        
        // 리뷰 아이템들 필터링
        const reviewItems = document.querySelectorAll('.review-item');
        reviewItems.forEach(item => {
            const rating = parseInt(item.getAttribute('data-rating')) || 0;
            
            if (filterType === 'all') {
                item.style.display = 'block';
            } else if (filterType === rating.toString()) {
                item.style.display = 'block';
            } else {
                item.style.display = 'none';
            }
        });
        
        // 필터링된 리뷰 개수 표시
        const visibleReviews = document.querySelectorAll('.review-item[style*="block"], .review-item:not([style*="none"])');
        showNotification(`${visibleReviews.length}개의 리뷰가 표시됩니다.`, 'info');
    }

    // 전역 함수로 노출
    window.sortReviews = sortReviews;
    window.markHelpful = markHelpful;
    window.reportReview = reportReview;
    // 신고 사유 입력 모달 함수
    function showReportReasonModal() {
        return new Promise((resolve) => {
            // 기존 모달이 있으면 제거
            const existing = document.getElementById('report-reason-modal');
            if (existing) existing.remove();

            // 모달 백드롭
            const backdrop = document.createElement('div');
            backdrop.id = 'report-reason-modal';
            backdrop.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                width: 100vw;
                height: 100vh;
                background: rgba(0,0,0,0.5);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 10000;
            `;

            // 모달 박스
            const modal = document.createElement('div');
            modal.style.cssText = `
                background: white;
                border-radius: 8px;
                padding: 2rem;
                min-width: 400px;
                max-width: 90vw;
                box-shadow: 0 4px 20px rgba(0,0,0,0.3);
            `;

            modal.innerHTML = `
                <h3 style="margin: 0 0 1rem 0; color: #333;">신고 사유</h3>
                <p style="margin: 0 0 1rem 0; color: #666; font-size: 0.9rem;">
                    신고 사유를 선택하거나 직접 입력해주세요.
                </p>
                <div style="margin-bottom: 1rem;">
                    <select id="report-reason-select" style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; margin-bottom: 0.5rem;">
                        <option value="">신고 사유를 선택하세요</option>
                        <option value="부적절한 내용">부적절한 내용</option>
                        <option value="스팸/광고">스팸/광고</option>
                        <option value="욕설/비방">욕설/비방</option>
                        <option value="개인정보 노출">개인정보 노출</option>
                        <option value="저작권 침해">저작권 침해</option>
                        <option value="기타">기타</option>
                    </select>
                    <textarea id="report-reason-text" 
                              placeholder="상세한 신고 사유를 입력해주세요 (최대 200자)"
                              style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; height: 80px; resize: vertical;"
                              maxlength="200"></textarea>
                </div>
                <div style="display: flex; gap: 0.5rem; justify-content: flex-end;">
                    <button id="report-cancel-btn" style="padding: 0.5rem 1rem; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer;">
                        취소
                    </button>
                    <button id="report-submit-btn" style="padding: 0.5rem 1rem; background: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer;">
                        신고하기
                    </button>
                </div>
            `;

            backdrop.appendChild(modal);
            document.body.appendChild(backdrop);

            // 이벤트 리스너
            const select = modal.querySelector('#report-reason-select');
            const textarea = modal.querySelector('#report-reason-text');
            const cancelBtn = modal.querySelector('#report-cancel-btn');
            const submitBtn = modal.querySelector('#report-submit-btn');

            // 선택된 사유가 변경되면 텍스트에 반영
            select.addEventListener('change', function() {
                if (this.value === '기타') {
                    textarea.focus();
                } else {
                    textarea.value = this.value;
                }
            });

            // 취소 버튼
            cancelBtn.addEventListener('click', () => {
                backdrop.remove();
                resolve(null);
            });

            // 제출 버튼
            submitBtn.addEventListener('click', () => {
                const reason = textarea.value.trim();
                if (!reason) {
                    alert('신고 사유를 입력해주세요.');
                    return;
                }
                backdrop.remove();
                resolve(reason);
            });

            // ESC 키로 취소
            const handleEsc = (e) => {
                if (e.key === 'Escape') {
                    backdrop.remove();
                    document.removeEventListener('keydown', handleEsc);
                    resolve(null);
                }
            };
            document.addEventListener('keydown', handleEsc);

            // 배경 클릭으로 취소
            backdrop.addEventListener('click', (e) => {
                if (e.target === backdrop) {
                    backdrop.remove();
                    resolve(null);
                }
            });
        });
    }

    window.filterReviews = filterReviews;

    // 구매 상태 확인 및 UI 업데이트
    const productId = document.querySelector('[data-product-id]')?.dataset.productId;
    if (productId) {
        updateReviewUI();
    }

    // 페이지 로드 완료 메시지
    console.log('상품 상세 페이지가 로드되었습니다.');
});

// 댓글 토글 함수
function toggleCommentForm(reviewId) {
    const commentsSection = document.getElementById(`comments-${reviewId}`);
    const commentList = document.getElementById(`comment-list-${reviewId}`);
    
    if (commentsSection.style.display === 'none') {
        // 댓글 섹션 표시
        commentsSection.style.display = 'block';
        
        // 댓글 목록 로드
        loadComments(reviewId);
        
        // 댓글 작성 폼의 글자 수 카운터 초기화
        const textarea = commentsSection.querySelector('textarea');
        const charCount = commentsSection.querySelector('.comment-char-count');
        
        if (textarea && charCount) {
            updateCommentCharCount(textarea, charCount);
            
            textarea.addEventListener('input', function() {
                updateCommentCharCount(this, charCount);
            });
        }
    } else {
        // 댓글 섹션 숨기기
        commentsSection.style.display = 'none';
    }
}

// 댓글 목록 로드
async function loadComments(reviewId) {
    try {
        const result = await ajax.get(`/api/reviews/${reviewId}/comments`);
        if (result.code === '00') {
            displayComments(reviewId, result.data.comments);
        } else {
            console.error('댓글 로드 실패:', result.message);
        }
    } catch (error) {
        console.error('댓글 로드 오류:', error);
    }
}

// 댓글 표시 (계층 구조)
function displayComments(reviewId, comments) {
    const commentList = document.getElementById(`comment-list-${reviewId}`);
    
    if (!comments || comments.length === 0) {
        commentList.innerHTML = '<p class="no-comments">아직 댓글이 없습니다.</p>';
        return;
    }
    
    // 계층 구조로 정리
    const topLevelComments = comments.filter(comment => comment.parentId == null);
    const replyComments = comments.filter(comment => comment.parentId != null);
    
    // 부모 댓글별로 대댓글 그룹화
    const repliesByParent = {};
    replyComments.forEach(reply => {
        if (!repliesByParent[reply.parentId]) {
            repliesByParent[reply.parentId] = [];
        }
        repliesByParent[reply.parentId].push(reply);
    });
    
    // 부모 댓글 작성자 정보 수집 (대댓글 표시용)
    const parentAuthors = {};
    topLevelComments.forEach(comment => {
        parentAuthors[comment.commentId] = comment;
    });
    
    // 대댓글의 원댓글 작성자 정보 수집 (대댓글에 대한 대댓글 표시용)
    const originalCommentAuthors = {};
    replyComments.forEach(reply => {
        // 대댓글의 원댓글 ID 찾기
        const originalCommentId = reply.parentId;
        if (parentAuthors[originalCommentId]) {
            originalCommentAuthors[reply.commentId] = parentAuthors[originalCommentId];
        }
    });
    
    // 부모 댓글은 최신순으로 정렬 (최신이 위로)
    topLevelComments.sort((a, b) => new Date(b.cdate) - new Date(a.cdate));
    
    // 대댓글은 시간순으로 정렬 (오래된 순, 오름차순)
    Object.keys(repliesByParent).forEach(parentId => {
        repliesByParent[parentId].sort((a, b) => new Date(a.cdate) - new Date(b.cdate));
    });
    
    // 댓글과 대댓글을 하나의 배열로 합치고 시간순으로 정렬
    const allComments = [];
    topLevelComments.forEach(comment => {
        // 부모 댓글 추가
        allComments.push({
            ...comment,
            isParent: true,
            sortDate: new Date(comment.cdate)
        });
        
        // 해당 부모 댓글의 대댓글들 추가
        const replies = repliesByParent[comment.commentId] || [];
        replies.forEach(reply => {
            allComments.push({
                ...reply,
                isParent: false,
                parentComment: comment,
                sortDate: new Date(reply.cdate)
            });
        });
    });
    
    // 전체 댓글을 시간순으로 정렬 (최신이 위로)
    allComments.sort((a, b) => b.sortDate - a.sortDate);
    
    const commentsHTML = allComments.map(comment => {
        if (comment.isParent) {
            // 부모 댓글
            const commentAuthorName = comment.memberNickname || `사용자 ${comment.memberId}`;
            const currentMemberId = document.getElementById('root').dataset.sMemberId;
            const isOwnComment = currentMemberId && comment.memberId && 
                                parseInt(currentMemberId) === parseInt(comment.memberId);
            
            console.log('부모 댓글 생성:', {
                commentId: comment.commentId,
                commentMemberId: comment.memberId,
                currentMemberId: currentMemberId,
                isOwnComment: isOwnComment
            });

            
            return `
                <div class="comment-item parent-comment" data-comment-id="${comment.commentId}" data-member-id="${comment.memberId}">
                    <div class="comment-header">
                        <div class="comment-author">
                            <span class="comment-author-name">${commentAuthorName}</span>
                            <span class="comment-date">${new Date(comment.cdate).toLocaleDateString()}</span>
                        </div>
                        <div class="comment-actions" style="display: none;">
                            ${isOwnComment ? `
                                <button class="btn btn-outline" onclick="editComment(${comment.commentId}, '${comment.content.replace(/'/g, "\\'")}')">
                                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                    </svg>
                                    수정
                                </button>
                                <button class="btn btn-danger" onclick="deleteComment(${comment.commentId})">
                                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <polyline points="3,6 5,6 21,6"></polyline>
                                        <path d="M19,6v14a2,2,0,0,1-2,2H7a2,2,0,0,1-2-2V6m3,0V4a2,2,0,0,1,2-2h4a2,2,0,0,1,2,2V6"></path>
                                    </svg>
                                    삭제
                                </button>
                            ` : '<!-- 본인 글이 아니므로 수정/삭제 버튼 숨김 -->'}
                        </div>
                    </div>
                    <div class="comment-content" id="comment-content-${comment.commentId}">${comment.content}</div>
                    <div class="comment-edit-form" id="comment-edit-form-${comment.commentId}" style="display: none;">
                        <textarea class="edit-textarea" rows="2" maxlength="500">${comment.content}</textarea>
                        <div class="edit-actions">
                            <div class="save-cancel-group">
                                <button class="btn btn-primary" onclick="saveCommentEdit(${comment.commentId})">
                                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <path d="M22 2L11 13"></path>
                                        <path d="M22 2L15 22L11 13L2 9L22 2Z"></path>
                                    </svg>
                                    저장
                                </button>
                                <button class="btn btn-outline" onclick="cancelCommentEdit(${comment.commentId})">
                                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <line x1="18" y1="6" x2="6" y2="18"></line>
                                        <line x1="6" y1="6" x2="18" y2="18"></line>
                                    </svg>
                                    취소
                                </button>
                            </div>
                        </div>
                    </div>
                    <div class="comment-reply-section">
                        <button class="reply-btn" onclick="toggleReplyForm(${comment.commentId})">
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="9,11 12,8 15,11"></polyline>
                                <path d="M20,4v7a4,4,0,0,1-4,4H6a4,4,0,0,1-4-4V4"></path>
                            </svg>
                            답글
                        </button>
                        <div class="reply-form" id="reply-form-${comment.commentId}" style="display: none;">
                            <textarea class="reply-textarea" rows="2" maxlength="500" placeholder="답글을 입력하세요..."></textarea>
                            <div class="reply-actions">
                                <div class="save-cancel-group">
                                    <span class="reply-char-count">0/500</span>
                                    <button class="btn btn-primary" onclick="submitReply(${comment.commentId})">
                                        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                            <path d="M22 2L11 13"></path>
                                            <path d="M22 2L15 22L11 13L2 9L22 2Z"></path>
                                        </svg>
                                        저장
                                    </button>
                                    <button class="btn btn-outline" onclick="cancelReply(${comment.commentId})">
                                        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                            <line x1="18" y1="6" x2="6" y2="18"></line>
                                            <line x1="6" y1="6" x2="18" y2="18"></line>
                                        </svg>
                                        취소
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        } else {
            // 대댓글
            const replyAuthorName = comment.memberNickname || `사용자 ${comment.memberId}`;
            const parentAuthorName = comment.parentComment.memberNickname || `사용자 ${comment.parentComment.memberId}`;
            const originalAuthorName = originalCommentAuthors[comment.commentId]?.memberNickname || 
                                     `사용자 ${originalCommentAuthors[comment.commentId]?.memberId}`;
            const currentMemberId = document.getElementById('root').dataset.sMemberId;
            const isOwnComment = currentMemberId && comment.memberId && 
                                parseInt(currentMemberId) === parseInt(comment.memberId);
            
            console.log('대댓글 생성:', {
                commentId: comment.commentId,
                commentMemberId: comment.memberId,
                currentMemberId: currentMemberId,
                isOwnComment: isOwnComment,
                parentAuthorName: parentAuthorName,
                originalAuthorName: originalAuthorName
            });

            // 대댓글 표시 텍스트 결정
            let replyDisplayText = '';
            if (originalCommentAuthors[comment.commentId]) {
                // 대댓글에 대한 대댓글인 경우: "→ 원댓글작성자"
                replyDisplayText = `→ ${originalAuthorName}`;
            } else {
                // 일반 대댓글인 경우: "→ 부모댓글작성자"
                replyDisplayText = `→ ${parentAuthorName}`;
            }
            
            return `
                <div class="comment-item reply-comment" data-comment-id="${comment.commentId}" data-member-id="${comment.memberId}" data-parent-id="${comment.parentId}">
                    <div class="comment-header">
                        <div class="comment-author">
                            <span class="comment-author-name">${replyAuthorName}</span>
                            <span class="comment-to-author">${replyDisplayText}</span>
                            <span class="comment-date">${new Date(comment.cdate).toLocaleDateString()}</span>
                        </div>
                        <div class="comment-actions" style="display: none;">
                            ${isOwnComment ? `
                                <button class="btn btn-outline" onclick="editComment(${comment.commentId}, '${comment.content.replace(/'/g, "\\'")}')">
                                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                    </svg>
                                    수정
                                </button>
                                <button class="btn btn-danger" onclick="deleteComment(${comment.commentId})">
                                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <polyline points="3,6 5,6 21,6"></polyline>
                                        <path d="M19,6v14a2,2,0,0,1-2,2H7a2,2,0,0,1-2-2V6m3,0V4a2,2,0,0,1,2-2h4a2,2,0,0,1,2,2V6"></path>
                                    </svg>
                                    삭제
                                </button>
                            ` : '<!-- 본인 글이 아니므로 수정/삭제 버튼 숨김 -->'}
                        </div>
                    </div>
                    <div class="comment-content" id="comment-content-${comment.commentId}">${comment.content}</div>
                    <div class="comment-edit-form" id="comment-edit-form-${comment.commentId}" style="display: none;">
                        <textarea class="edit-textarea" rows="2" maxlength="500">${comment.content}</textarea>
                        <div class="edit-actions">
                            <div class="save-cancel-group">
                                <button class="btn btn-primary" onclick="saveCommentEdit(${comment.commentId})">
                                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <path d="M22 2L11 13"></path>
                                        <path d="M22 2L15 22L11 13L2 9L22 2Z"></path>
                                    </svg>
                                    저장
                                </button>
                                <button class="btn btn-outline" onclick="cancelCommentEdit(${comment.commentId})">
                                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <line x1="18" y1="6" x2="6" y2="18"></line>
                                        <line x1="6" y1="6" x2="18" y2="18"></line>
                                    </svg>
                                    취소
                                </button>
                            </div>
                        </div>
                    </div>
                    <div class="reply-form" id="reply-form-${comment.commentId}" style="display: none;">
                        <textarea class="reply-textarea" rows="2" maxlength="500" placeholder="답글을 입력하세요..."></textarea>
                        <div class="reply-actions">
                            <div class="save-cancel-group">
                                <span class="reply-char-count">0/500</span>
                                <button class="btn btn-primary" onclick="submitReply(${comment.commentId})">
                                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <path d="M22 2L11 13"></path>
                                        <path d="M22 2L15 22L11 13L2 9L22 2Z"></path>
                                    </svg>
                                    저장
                                </button>
                                <button class="btn btn-outline" onclick="cancelReply(${comment.commentId})">
                                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <line x1="18" y1="6" x2="6" y2="18"></line>
                                        <line x1="6" y1="6" x2="18" y2="18"></line>
                                    </svg>
                                    취소
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }
    }).join('');
    
    commentList.innerHTML = commentsHTML;
    
    // 답글 폼의 글자 수 카운터 이벤트 리스너 추가
    document.querySelectorAll('.reply-textarea').forEach(textarea => {
        textarea.addEventListener('input', function() {
            const charCount = this.closest('.reply-form').querySelector('.reply-char-count');
            updateReplyCharCount(this, charCount);
        });
    });
}

// 답글 폼 토글
function toggleReplyForm(parentCommentId) {
    const replyForm = document.getElementById(`reply-form-${parentCommentId}`);
    const replyBtn = document.querySelector(`.comment-reply-section[data-comment-id="${parentCommentId}"] .reply-btn, .comment-item[data-comment-id="${parentCommentId}"] .reply-btn`);
    
    if (replyForm.style.display === 'none') {
        replyForm.style.display = 'block';
        // Update reply button text and icon
        if (replyBtn) {
            replyBtn.innerHTML = `
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
                답글 취소
            `;
        }
        replyForm.querySelector('.reply-textarea').focus();
    } else {
        replyForm.style.display = 'none';
        // Restore original button text and icon
        if (replyBtn) {
            replyBtn.innerHTML = `
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="9,11 12,8 15,11"></polyline>
                    <path d="M20,4v7a4,4,0,0,1-4,4H6a4,4,0,0,1-4-4V4"></path>
                </svg>
                답글
            `;
        }
        replyForm.querySelector('.reply-textarea').value = '';
        replyForm.querySelector('.reply-char-count').textContent = '0/500';
    }
}

// 답글 취소
function cancelReply(parentCommentId) {
    toggleReplyForm(parentCommentId);
}

// 답글 글자 수 카운터 업데이트
function updateReplyCharCount(textarea, counter) {
    const length = textarea.value.length;
    counter.textContent = `${length}/500`;
    
    if (length > 450) {
        counter.style.color = '#ffc107';
    } else if (length > 500) {
        counter.style.color = '#dc3545';
    } else {
        counter.style.color = '#666';
    }
}

// 답글 제출
async function submitReply(parentCommentId) {
    const replyForm = document.getElementById(`reply-form-${parentCommentId}`);
    const textarea = replyForm.querySelector('.reply-textarea');
    const content = textarea.value.trim();
    
    if (!content) {
        showNotification('답글 내용을 입력해주세요.', 'warning');
        return;
    }
    
    try {
        const result = await ajax.post(`/api/reviews/comments/${parentCommentId}/replies`, { content: content });
        
        if (result.code === '00') {
            // 폼 초기화
            textarea.value = '';
            replyForm.querySelector('.reply-char-count').textContent = '0/500';
            toggleReplyForm(parentCommentId);
            
            // 댓글 목록 새로고침
            const reviewId = replyForm.closest('.review-comments').id.replace('comments-', '');
            loadComments(reviewId);
            
            showNotification('답글이 작성되었습니다.', 'success');
        } else {
            showNotification(result.message || '답글 작성에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('답글 작성 실패:', error);
        showNotification('네트워크 오류가 발생했습니다.', 'error');
    }
}

// 댓글 글자 수 카운터 업데이트
function updateCommentCharCount(textarea, counter) {
    const length = textarea.value.length;
    counter.textContent = length;
    
    if (length > 450) {
        counter.style.color = '#ffc107';
    } else if (length > 500) {
        counter.style.color = '#dc3545';
    } else {
        counter.style.color = '#6c757d';
    }
    
    // 자동 높이 조절
    textarea.style.height = 'auto';
    textarea.style.height = Math.min(textarea.scrollHeight, 100) + 'px';
}

// 댓글 입력 이벤트 리스너
document.addEventListener('input', function(e) {
    if (e.target.name === 'content' && e.target.closest('.comment-form')) {
        const counter = e.target.closest('.comment-form').querySelector('.comment-char-count');
        const submitBtn = e.target.closest('.comment-form').querySelector('.comment-submit-btn');
        
        if (counter) {
            updateCommentCharCount(e.target, counter);
        }
        
        // 버튼 상태 업데이트
        if (submitBtn) {
            const content = e.target.value.trim();
            if (content.length > 0 && content.length <= 500) {
                submitBtn.disabled = false;
                submitBtn.style.opacity = '1';
            } else {
                submitBtn.disabled = true;
                submitBtn.style.opacity = '0.6';
            }
        }
    }
});

// 댓글 폼 포커스 시 자동 높이 조절
document.addEventListener('focusin', function(e) {
    if (e.target.name === 'content' && e.target.closest('.comment-form')) {
        const textarea = e.target;
        setTimeout(() => {
            textarea.style.height = 'auto';
            textarea.style.height = Math.min(textarea.scrollHeight, 100) + 'px';
        }, 10);
    }
});
    
// 댓글 액션 초기화
initializeCommentActions();

// 댓글 제출 버튼 초기 상태 설정
document.querySelectorAll('.comment-submit-btn').forEach(btn => {
    btn.disabled = true;
    btn.style.opacity = '0.6';
});

// 댓글 제출 (버튼 클릭 이벤트 핸들러)
async function submitComment(buttonElement) {
    const form = buttonElement.closest('.comment-form');
    const reviewId = form.dataset.reviewId;
    const textarea = form.querySelector('textarea[name="content"]');
    const content = textarea.value.trim();
    
    if (!content) {
        showNotification('댓글 내용을 입력해주세요.', 'warning');
        return;
    }
    
    // 버튼 비활성화
    buttonElement.disabled = true;
    buttonElement.style.opacity = '0.6';
    
    try {
        const result = await ajax.post(`/api/reviews/${reviewId}/comments`, { content: content });
        
        if (result.code === '00') {
            // 폼 초기화
            textarea.value = '';
            textarea.style.height = 'auto';
            form.querySelector('.comment-char-count').textContent = '0';
            
            // 댓글 목록 새로고침
            loadComments(reviewId);
            
            // 댓글 수 업데이트
            updateCommentCount(reviewId, 1);
            
            showNotification('댓글이 작성되었습니다.', 'success');
        } else {
            showNotification(result.message || '댓글 작성에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('댓글 작성 실패:', error);
        showNotification('네트워크 오류가 발생했습니다.', 'error');
    } finally {
        // 버튼 다시 활성화
        buttonElement.disabled = false;
        buttonElement.style.opacity = '1';
    }
}

// 댓글 수 업데이트
function updateCommentCount(reviewId, change) {
    // 댓글 수를 표시하는 요소 찾기
    const commentCountElement = document.querySelector(`[onclick*="toggleCommentForm(${reviewId})"] .comment-count`);
    if (commentCountElement) {
        const currentCount = parseInt(commentCountElement.textContent) || 0;
        commentCountElement.textContent = Math.max(0, currentCount + change);
    }
}

// 댓글 수정 모드 진입
function editComment(commentId, originalContent) {
    const commentItem = document.querySelector(`[data-comment-id="${commentId}"]`);
    const contentDiv = document.getElementById(`comment-content-${commentId}`);
    const editForm = document.getElementById(`comment-edit-form-${commentId}`);
    const actionsDiv = commentItem.querySelector('.comment-actions');
    
    // 수정 모드로 전환
    contentDiv.style.display = 'none';
    editForm.style.display = 'block';
    actionsDiv.style.display = 'none';
    
    // 텍스트에어리어에 원본 내용 설정
    const textarea = editForm.querySelector('.edit-textarea');
    textarea.value = originalContent;
    textarea.focus();
}

// 댓글 수정 취소
function cancelCommentEdit(commentId) {
    const commentItem = document.querySelector(`[data-comment-id="${commentId}"]`);
    const contentDiv = document.getElementById(`comment-content-${commentId}`);
    const editForm = document.getElementById(`comment-edit-form-${commentId}`);
    const actionsDiv = commentItem.querySelector('.comment-actions');
    
    // 원래 모드로 복원
    contentDiv.style.display = 'block';
    editForm.style.display = 'none';
    actionsDiv.style.display = 'none';
}

// 댓글 수정 저장
async function saveCommentEdit(commentId) {
    const editForm = document.getElementById(`comment-edit-form-${commentId}`);
    const textarea = editForm.querySelector('.edit-textarea');
    const content = textarea.value.trim();
    
    if (!content) {
        showNotification('댓글 내용을 입력해주세요.', 'warning');
        return;
    }
    
    try {
        const result = await ajax.put(`/api/reviews/comments/${commentId}`, { content: content });
        
        if (result.code === '00') {
            // 댓글 내용 업데이트
            const contentDiv = document.getElementById(`comment-content-${commentId}`);
            contentDiv.textContent = content;
            
            // 원래 모드로 복원
            cancelCommentEdit(commentId);
            
            showNotification('댓글이 수정되었습니다.', 'success');
        } else {
            showNotification(result.message || '댓글 수정에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('댓글 수정 실패:', error);
        showNotification('네트워크 오류가 발생했습니다.', 'error');
    }
}

// 댓글 삭제
async function deleteComment(commentId) {
    if (!confirm('이 댓글을 삭제하시겠습니까?')) {
        return;
    }
    
    try {
        const result = await ajax.delete(`/api/reviews/comments/${commentId}`);
        
        if (result.code === '00') {
            // 댓글 수 업데이트를 위해 reviewId 찾기
            const commentItem = document.querySelector(`[data-comment-id="${commentId}"]`);
            let reviewId = null;
            
            if (commentItem) {
                const commentList = commentItem.closest('.comment-list');
                if (commentList) {
                    // comment-list-{reviewId} 형태에서 reviewId 추출
                    const listId = commentList.id;
                    if (listId && listId.startsWith('comment-list-')) {
                        reviewId = listId.replace('comment-list-', '');
                    }
                }
            }
            
            // 댓글 목록 다시 로드
            if (reviewId) {
                await loadComments(reviewId);
                updateCommentCount(reviewId, -1);
            }
            
            showNotification('댓글이 삭제되었습니다.', 'success');
        } else {
            showNotification(result.message || '댓글 삭제에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('댓글 삭제 실패:', error);
        showNotification('네트워크 오류가 발생했습니다.', 'error');
    }
}

// 댓글 마우스 오버 시 수정/삭제 버튼 표시
function initializeCommentActions() {
    document.addEventListener('mouseover', function(e) {
        const commentItem = e.target.closest('.comment-item');
        if (commentItem) {
            const actionsDiv = commentItem.querySelector('.comment-actions');
            if (actionsDiv) {
                // 현재 로그인한 사용자가 댓글 작성자인지 확인
                const commentMemberId = commentItem.dataset.memberId;
                const currentMemberId = document.getElementById('root').dataset.sMemberId;
                
                console.log('댓글 마우스오버:', {
                    commentMemberId: commentMemberId,
                    currentMemberId: currentMemberId,
                    isMatch: currentMemberId && commentMemberId && 
                             parseInt(currentMemberId) === parseInt(commentMemberId)
                });
                
                // 로그인한 사용자이고, 댓글 작성자와 동일한 경우에만 수정/삭제 버튼 표시
                console.log(`currentMemberId=${currentMemberId}`)
                console.log(`commentMemberId=${commentMemberId}`)
                if (currentMemberId && commentMemberId && 
                    parseInt(currentMemberId) === parseInt(commentMemberId)) {
                    actionsDiv.style.display = 'block';
                } else {
                    actionsDiv.style.display = 'none';
                }
            }
        }
    });
    
    document.addEventListener('mouseout', function(e) {
        const commentItem = e.target.closest('.comment-item');
        if (commentItem) {
            const actionsDiv = commentItem.querySelector('.comment-actions');
            if (actionsDiv) {
                actionsDiv.style.display = 'none';
            }
        }
    });
}

// 리뷰 신고 함수
async function reportReview(reviewId) {
    if (!confirm('이 리뷰를 신고하시겠습니까?')) {
        return;
    }

    // 신고 사유 입력 모달 표시
    const reason = await showReportReasonModal();
    if (!reason) {
        return; // 사용자가 취소한 경우
    }

    try {
        const result = await ajax.post(`/api/reviews/${reviewId}/report`, { reason: reason });
        if (result.code === '00') {
            // 신고 카운트 업데이트
            const reportBtn = document.querySelector(`button[onclick*="reportReview(${reviewId})"]`);
            if (reportBtn) {
                const reportCount = reportBtn.querySelector('.report-count');
                if (reportCount) {
                    const currentCount = parseInt(reportCount.textContent) || 0;
                    reportCount.textContent = currentCount + 1;
                }
            }
            showNotification('리뷰가 신고되었습니다.', 'success');
        } else {
            showNotification(result.message || '신고 처리에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('신고 처리 중 오류:', error);
        showNotification('신고 처리 중 오류가 발생했습니다.', 'error');
    }
}
