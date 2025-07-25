/**
 * 상품 목록 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // 전역 변수
    let searchTimeout;
    let currentSearchTerm = '';
    
    // 요소 참조
    const searchInput = document.getElementById('searchInput');
    const searchForm = document.querySelector('.search-form');
    const productGrid = document.querySelector('.product-grid');
    const paginationContainer = document.getElementById('pagination');
    
    // 초기화
    init();
    
    function init() {
        // 자동완성 초기화
        initAutocomplete();
        
        // 실시간 검색 초기화
        initRealTimeSearch();
        
        // 필터 변경 이벤트 초기화
        initFilterEvents();
        
        // 페이징 초기화
        initPagination();
        
        // 장바구니 버튼 이벤트 초기화
        initCartEvents();
    }
    
    /**
     * 자동완성 기능 초기화
     */
    function initAutocomplete() {
        if (!searchInput) return;
        
        // 자동완성 컨테이너 생성
        const autocompleteContainer = document.createElement('div');
        autocompleteContainer.className = 'autocomplete-dropdown';
        autocompleteContainer.style.display = 'none';
        
        searchInput.parentNode.style.position = 'relative';
        searchInput.parentNode.appendChild(autocompleteContainer);
        
        // 검색어 입력 이벤트
        searchInput.addEventListener('input', function() {
            const query = this.value.trim();
            
            if (query.length < 2) {
                autocompleteContainer.style.display = 'none';
                return;
            }
            
            // 디바운싱
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                fetchAutocomplete(query);
            }, 300);
        });
        
        // 포커스 아웃 이벤트
        searchInput.addEventListener('blur', function() {
            setTimeout(() => {
                autocompleteContainer.style.display = 'none';
            }, 200);
        });
        
        // 자동완성 데이터 가져오기
        async function fetchAutocomplete(query) {
            try {
                const response = await fetch(`/products/autocomplete?prefix=${encodeURIComponent(query)}`);
                const suggestions = await response.json();
                
                if (suggestions.length > 0) {
                    displayAutocomplete(suggestions, autocompleteContainer);
                } else {
                    autocompleteContainer.style.display = 'none';
                }
            } catch (error) {
                console.error('자동완성 검색 실패:', error);
            }
        }
        
        // 자동완성 표시
        function displayAutocomplete(suggestions, container) {
            container.innerHTML = '';
            
            suggestions.forEach(suggestion => {
                const item = document.createElement('div');
                item.className = 'autocomplete-item';
                
                // 하이라이팅된 HTML을 그대로 표시
                item.innerHTML = suggestion;
                
                item.addEventListener('click', function() {
                    // HTML 태그 제거하고 순수 텍스트만 추출
                    const tempDiv = document.createElement('div');
                    tempDiv.innerHTML = suggestion;
                    const plainText = tempDiv.textContent || tempDiv.innerText || '';
                    
                    searchInput.value = plainText;
                    container.style.display = 'none';
                    searchForm.submit();
                });
                
                container.appendChild(item);
            });
            
            container.style.display = 'block';
        }
    }
    
    /**
     * 실시간 검색 초기화
     */
    function initRealTimeSearch() {
        if (!searchInput) return;
        
        // 검색어 변경 감지
        searchInput.addEventListener('input', function() {
            const query = this.value.trim();
            
            if (query !== currentSearchTerm) {
                currentSearchTerm = query;
                
                // URL 업데이트 (히스토리 API 사용)
                updateSearchURL(query);
            }
        });
    }
    
    /**
     * 검색 URL 업데이트
     */
    function updateSearchURL(query) {
        const url = new URL(window.location);
        
        if (query) {
            url.searchParams.set('keyword', query);
        } else {
            url.searchParams.delete('keyword');
        }
        
        // 페이지 초기화
        url.searchParams.delete('page');
        
        // 히스토리 업데이트 (페이지 이동 없이)
        window.history.replaceState({}, '', url);
    }
    
    /**
     * 필터 변경 이벤트 초기화
     */
    function initFilterEvents() {
        const filterSelects = document.querySelectorAll('.filter-select, .rating-select');
        
        filterSelects.forEach(select => {
            select.addEventListener('change', function() {
                // 자동 제출
                searchForm.submit();
            });
        });
        
        // 가격 필터 실시간 적용
        const priceInputs = document.querySelectorAll('.price-input');
        priceInputs.forEach(input => {
            input.addEventListener('blur', function() {
                if (this.value) {
                    searchForm.submit();
                }
            });
        });
    }
    
    /**
     * 페이징 초기화
     */
    function initPagination() {
        if (!paginationContainer) return;
        
        // 페이징 데이터 가져오기
        const searchResult = window.searchResultData;
        if (!searchResult) return;
        
        // 페이징 UI 생성
        const pagination = new PaginationUI({
            container: paginationContainer,
            currentPage: searchResult.currentPage,
            totalPages: searchResult.totalPages,
            onPageChange: (page) => {
                navigateToPage(page);
            }
        });
        
        pagination.render();
    }
    
    /**
     * 페이지 이동
     */
    function navigateToPage(page) {
        const url = new URL(window.location);
        url.searchParams.set('page', page);
        window.location.href = url.toString();
    }
    
    /**
     * 장바구니 버튼 이벤트 초기화
     */
    function initCartEvents() {
        // 장바구니 버튼 이벤트 리스너
        document.addEventListener('click', function(e) {
            if (e.target.classList.contains('add-to-cart-btn') || e.target.closest('.add-to-cart-btn')) {
                const button = e.target.classList.contains('add-to-cart-btn') ? e.target : e.target.closest('.add-to-cart-btn');
                const productId = button.dataset.productId;
                if (productId) {
                    addToCart(productId);
                }
            }
            
            if (e.target.classList.contains('buy-now-btn') || e.target.closest('.buy-now-btn')) {
                const button = e.target.classList.contains('buy-now-btn') ? e.target : e.target.closest('.buy-now-btn');
                const productId = button.dataset.productId;
                if (productId) {
                    buyNow(productId);
                }
            }
        });
    }
    
    /**
     * 장바구니에 상품 추가
     */
    async function addToCart(productId) {
        // 로그인 체크
        if (!isCurrentUserLoggedIn()) {
            showLoginRequired();
            return;
        }
        
        try {
            const response = await fetch(`/cart/add/${productId}?quantity=1`, {
                method: 'POST'
            });
            
            const result = await response.text();
            
            if (result === 'success') {
                showNotification('장바구니에 추가되었습니다.', 'success');
                updateCartCount();
            } else {
                showNotification(result || '장바구니 추가에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('장바구니 추가 실패:', error);
            showNotification('장바구니 추가 중 오류가 발생했습니다.', 'error');
        }
    }
    
    /**
     * 바로 구매
     */
    function buyNow(productId) {
        // 로그인 체크
        if (!isCurrentUserLoggedIn()) {
            showLoginRequired();
            return;
        }
        
        // 바로 구매 페이지로 이동
        window.location.href = `/orders/direct/${productId}?quantity=1`;
    }
    

    
    /**
     * 장바구니 개수 업데이트
     */
    async function updateCartCount() {
        try {
            const response = await fetch('/cart/count');
            const data = await response.json();
            
            const cartCountElement = document.querySelector('.cart-count');
            if (cartCountElement) {
                cartCountElement.textContent = data.count || 0;
                cartCountElement.style.display = (data.count > 0) ? 'block' : 'none';
            }
        } catch (error) {
            console.error('장바구니 개수 조회 실패:', error);
        }
    }
    
    /**
     * 알림 표시
     */
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
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        `;
        
        // 타입별 스타일
        const styles = {
            success: 'background: #28a745;',
            error: 'background: #dc3545;',
            warning: 'background: #ffc107; color: #212529;',
            info: 'background: #17a2b8;'
        };
        
        notification.style.cssText += styles[type] || styles.info;
        notification.textContent = message;
        
        // 애니메이션 스타일 추가
        if (!document.querySelector('#notification-styles')) {
            const style = document.createElement('style');
            style.id = 'notification-styles';
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
        }
        
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
    
    /**
     * 상품 카드 호버 효과
     */
    function initProductCardEffects() {
        const productCards = document.querySelectorAll('.product-card');
        
        productCards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-4px)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
            });
        });
    }
    
    // 상품 카드 효과 초기화
    initProductCardEffects();
    
    /**
     * 무한 스크롤 (선택사항)
     */
    function initInfiniteScroll() {
        let isLoading = false;
        let currentPage = 1;
        
        window.addEventListener('scroll', function() {
            if (isLoading) return;
            
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            const windowHeight = window.innerHeight;
            const documentHeight = document.documentElement.scrollHeight;
            
            if (scrollTop + windowHeight >= documentHeight - 100) {
                loadMoreProducts();
            }
        });
        
        async function loadMoreProducts() {
            if (isLoading) return;
            
            isLoading = true;
            currentPage++;
            
            try {
                const url = new URL(window.location);
                url.searchParams.set('page', currentPage);
                
                const response = await fetch(url.toString());
                const html = await response.text();
                
                // 새로운 상품들을 파싱하여 추가
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newProducts = doc.querySelectorAll('.product-card');
                
                if (newProducts.length > 0) {
                    newProducts.forEach(product => {
                        productGrid.appendChild(product.cloneNode(true));
                    });
                    
                    // 새로운 카드들에 이벤트 리스너 추가
                    initProductCardEffects();
                }
            } catch (error) {
                console.error('추가 상품 로드 실패:', error);
            } finally {
                isLoading = false;
            }
        }
    }
    
    // 무한 스크롤 초기화 (선택사항)
    // initInfiniteScroll();

    // 전역 함수들
    // 전역 함수로 노출 (HTML에서 onclick으로 호출)
    window.addToCart = addToCart;
    window.buyNow = buyNow;
    window.resetFilters = resetFilters;
    window.applyFilters = applyFilters;
    
    /**
     * 필터 적용
     */
    function applyFilters() {
        performSearch();
    }
    
    /**
     * 필터 초기화
     */
    function resetFilters() {
        // 모든 필터 입력값 초기화
        const form = document.querySelector('.search-form');
        if (form) {
            // 검색어 초기화
            const searchInput = form.querySelector('input[name="keyword"]');
            if (searchInput) searchInput.value = '';
            
            // 카테고리 초기화
            const categorySelect = form.querySelector('select[name="category"]');
            if (categorySelect) categorySelect.value = '';
            
            // 정렬 초기화
            const sortBySelect = form.querySelector('select[name="sortBy"]');
            if (sortBySelect) sortBySelect.value = 'date';
            
            // 순서 초기화
            const sortOrderSelect = form.querySelector('select[name="sortOrder"]');
            if (sortOrderSelect) sortOrderSelect.value = 'desc';
            
            // 최소 평점 초기화
            const minRatingSelect = form.querySelector('select[name="minRating"]');
            if (minRatingSelect) minRatingSelect.value = '';
            
            // 가격 범위 초기화
            const minPriceInput = form.querySelector('input[name="minPrice"]');
            if (minPriceInput) minPriceInput.value = '';
            
            const maxPriceInput = form.querySelector('input[name="maxPrice"]');
            if (maxPriceInput) maxPriceInput.value = '';
        }
        
        // AJAX로 초기화된 상태로 검색 실행 (페이지 새로고침 없이)
        performSearch();
    }
    
    /**
     * AJAX 검색 수행
     */
    async function performSearch() {
        const form = document.querySelector('.search-form');
        if (!form) return;
        
        try {
            // 폼 데이터 수집
            const formData = new FormData(form);
            const searchParams = new URLSearchParams();
            
            // 빈 값이 아닌 파라미터만 추가
            for (const [key, value] of formData.entries()) {
                if (value && value.trim() !== '') {
                    searchParams.append(key, value);
                }
            }
            
            // 기본값 설정
            if (!searchParams.has('sortBy')) {
                searchParams.append('sortBy', 'date');
            }
            if (!searchParams.has('sortOrder')) {
                searchParams.append('sortOrder', 'desc');
            }
            
            // URL 업데이트 (브라우저 히스토리에 추가)
            const newUrl = `/products?${searchParams.toString()}`;
            window.history.pushState({}, '', newUrl);
            
            // 로딩 상태 표시
            showLoadingState();
            
            // AJAX 요청
            const response = await fetch(newUrl, {
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            if (response.ok) {
                const html = await response.text();
                
                // HTML 파싱하여 상품 그리드만 업데이트
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                
                // 상품 그리드 업데이트
                const newProductGrid = doc.querySelector('.product-grid');
                const currentProductGrid = document.querySelector('.product-grid');
                if (newProductGrid && currentProductGrid) {
                    currentProductGrid.innerHTML = newProductGrid.innerHTML;
                }
                
                // 검색 결과 정보 업데이트
                const newResultInfo = doc.querySelector('.result-info');
                const currentResultInfo = document.querySelector('.result-info');
                if (newResultInfo && currentResultInfo) {
                    currentResultInfo.innerHTML = newResultInfo.innerHTML;
                } else if (newResultInfo && !currentResultInfo) {
                    // 결과 정보가 없었다면 새로 추가
                    const resultSection = document.querySelector('.result-info');
                    if (resultSection) {
                        resultSection.innerHTML = newResultInfo.innerHTML;
                    }
                }
                
                // 페이징 업데이트
                const newPagination = doc.querySelector('.pagination');
                const currentPagination = document.querySelector('.pagination');
                if (newPagination && currentPagination) {
                    currentPagination.innerHTML = newPagination.innerHTML;
                }
                
                // 성공 메시지 표시
                showNotification('필터가 초기화되었습니다.', 'success');
                
                // 상품 카드 이벤트 재초기화
                initProductCardEffects();
                
            } else {
                throw new Error('검색 요청 실패');
            }
            
        } catch (error) {
            console.error('검색 실패:', error);
            showNotification('검색 중 오류가 발생했습니다.', 'error');
        } finally {
            hideLoadingState();
        }
    }
    
    /**
     * 로딩 상태 표시
     */
    function showLoadingState() {
        const productGrid = document.querySelector('.product-grid');
        if (productGrid) {
            productGrid.style.opacity = '0.6';
            productGrid.style.pointerEvents = 'none';
        }
    }
    
    /**
     * 로딩 상태 숨김
     */
    function hideLoadingState() {
        const productGrid = document.querySelector('.product-grid');
        if (productGrid) {
            productGrid.style.opacity = '1';
            productGrid.style.pointerEvents = 'auto';
        }
    }
}); 