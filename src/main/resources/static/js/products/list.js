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
        console.log('상품 목록 페이지 JavaScript 초기화 시작');
        
        // 통합 검색 드롭다운 초기화 (검색 히스토리 + 자동완성)
        console.log('통합 검색 드롭다운 초기화 중...');
        initSearchDropdown();
        
        // 실시간 검색 초기화
        console.log('실시간 검색 초기화 중...');
        initRealTimeSearch();
        
        // 필터 변경 이벤트 초기화
        console.log('필터 이벤트 초기화 중...');
        initFilterEvents();
        
        // 페이징 초기화
        console.log('페이징 초기화 중...');
        initPagination();
        
        // 장바구니 버튼 이벤트 초기화
        console.log('장바구니 버튼 이벤트 초기화 중...');
        initCartEvents();
        
        // 인기 검색어 실시간 갱신 초기화
        console.log('인기 검색어 실시간 갱신 초기화 중...');
        initPopularKeywordsRefresh();
        
        console.log('상품 목록 페이지 JavaScript 초기화 완료');
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
        
        // 검색 폼 제출 시 히스토리 저장
        searchForm.addEventListener('submit', function(e) {
            const keyword = searchInput.value.trim();
            if (keyword) {
                saveSearchHistory(keyword);
            }
        });
    }
    
    /**
     * 검색 히스토리 저장
     */
    async function saveSearchHistory(keyword) {
        try {
            // 로그인한 사용자는 서버에 저장
            if (typeof isCurrentUserLoggedIn === 'function' && isCurrentUserLoggedIn()) {
                const result = await ajax.post(`/api/products/search-history?keyword=${encodeURIComponent(keyword)}`, {});
                
                if (!result || result.code !== '00') {
                    console.warn('서버 검색 히스토리 저장 실패:', result?.message);
                }
            } else {
                // 비로그인 사용자는 localStorage에 저장
                saveSearchHistoryToLocalStorage(keyword);
            }
        } catch (error) {
            console.error('검색 히스토리 저장 중 오류:', error);
            // 서버 저장 실패 시 localStorage에 저장
            saveSearchHistoryToLocalStorage(keyword);
        }
    }
    
    /**
     * localStorage에 검색 히스토리 저장
     */
    function saveSearchHistoryToLocalStorage(keyword) {
        try {
            const storageKey = 'searchHistory';
            let history = JSON.parse(localStorage.getItem(storageKey) || '[]');
            
            // 중복 제거 (최신이 맨 앞에 오도록)
            history = history.filter(item => item !== keyword);
            history.unshift(keyword);
            
            // 최대 10개까지만 저장
            if (history.length > 10) {
                history = history.slice(0, 10);
            }
            
            localStorage.setItem(storageKey, JSON.stringify(history));
            console.log('localStorage에 검색 히스토리 저장:', keyword);
        } catch (error) {
            console.error('localStorage 저장 실패:', error);
        }
    }
    
    /**
     * localStorage에서 검색 히스토리 조회
     */
    function getSearchHistoryFromLocalStorage() {
        try {
            const storageKey = 'searchHistory';
            const history = JSON.parse(localStorage.getItem(storageKey) || '[]');
            return history;
        } catch (error) {
            console.error('localStorage 조회 실패:', error);
            return [];
        }
    }
    
    /**
     * localStorage에서 특정 검색어 삭제
     */
    function deleteSearchHistoryFromLocalStorage(keyword) {
        try {
            const storageKey = 'searchHistory';
            let history = JSON.parse(localStorage.getItem(storageKey) || '[]');
            
            history = history.filter(item => item !== keyword);
            localStorage.setItem(storageKey, JSON.stringify(history));
            
            console.log('localStorage에서 검색어 삭제:', keyword);
        } catch (error) {
            console.error('localStorage 삭제 실패:', error);
        }
    }
    
    /**
     * localStorage에서 모든 검색 히스토리 삭제
     */
    function clearSearchHistoryFromLocalStorage() {
        try {
            const storageKey = 'searchHistory';
            localStorage.removeItem(storageKey);
            console.log('localStorage에서 모든 검색 히스토리 삭제');
        } catch (error) {
            console.error('localStorage 전체 삭제 실패:', error);
        }
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
            const result = await ajax.post(`/api/cart/add/${productId}?quantity=1`, {});
            
            if (result && result.code === '00') {
                showNotification(result.message || '장바구니에 추가되었습니다.', 'success');
                updateCartCount();
            } else {
                showNotification(result?.message || '장바구니 추가에 실패했습니다.', 'error');
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
     * 장바구니 개수 업데이트 (common.js의 통합 함수 사용)
     */
    async function updateCartCount() {
        if (typeof window.updateCartCount === 'function') {
            await window.updateCartCount();
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
                
                const response = await ajax.get(url.toString());
                const html = response.data || response;
                
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
            const response = await ajax.get(newUrl);
            
            if (response && response.code === '00') {
                const html = response.data || response;
                
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

    /**
     * 인기 검색어 실시간 갱신 초기화
     */
    function initPopularKeywordsRefresh() {
        // 초기 로드
        refreshPopularKeywords();
        
        // 5분마다 자동 갱신
        setInterval(refreshPopularKeywords, 5 * 60 * 1000);
    }
    
    /**
     * 인기 검색어 갱신
     */
    async function refreshPopularKeywords() {
        try {
            const data = await ajax.get('/api/products/popular-keywords');
            
            if (data && Array.isArray(data.data)) {
                updatePopularKeywordsDisplay(data.data);
            }
        } catch (error) {
            console.error('인기 검색어 갱신 실패:', error);
        }
    }
    
    /**
     * 인기 검색어 표시 업데이트
     */
    function updatePopularKeywordsDisplay(keywords) {
        const popularKeywordsContainer = document.querySelector('.popular-keywords');
        if (!popularKeywordsContainer) return;
        
        const keywordTagsContainer = popularKeywordsContainer.querySelector('.keyword-tags');
        if (!keywordTagsContainer) return;
        
        // 기존 태그들 제거
        keywordTagsContainer.innerHTML = '';
        
        // 새로운 키워드들 추가
        keywords.forEach(keyword => {
            const tag = document.createElement('a');
            tag.href = `/products?keyword=${encodeURIComponent(keyword)}`;
            tag.className = 'keyword-tag';
            tag.textContent = keyword;
            keywordTagsContainer.appendChild(tag);
        });
        
        // 애니메이션 효과
        popularKeywordsContainer.style.opacity = '0.7';
        setTimeout(() => {
            popularKeywordsContainer.style.opacity = '1';
        }, 200);
    }

    /**
     * 통합 검색 드롭다운 초기화 (검색 히스토리 + 자동완성)
     */
    function initSearchDropdown() {
        if (!searchInput) return;
        
        // 중복 요청 방지 플래그
        let isRequestingHistory = false;
        let historyRequestTimeout = null;
        let autocompleteTimeout = null;
        
        // 통합 드롭다운 컨테이너 생성
        const dropdownContainer = document.createElement('div');
        dropdownContainer.className = 'search-dropdown';
        dropdownContainer.style.display = 'none';
        
        // 부모 요소를 relative로 설정
        if (searchInput.parentNode.style.position !== 'relative') {
            searchInput.parentNode.style.position = 'relative';
        }
        searchInput.parentNode.appendChild(dropdownContainer);
        
        // 키보드 네비게이션 변수
        let selectedIndex = -1;
        let currentItems = [];
        
        // 검색 히스토리 요청 함수
        function requestSearchHistory() {
            if (isRequestingHistory) return;
            
            if (historyRequestTimeout) {
                clearTimeout(historyRequestTimeout);
            }
            
            historyRequestTimeout = setTimeout(() => {
                if (typeof isCurrentUserLoggedIn === 'function' && isCurrentUserLoggedIn()) {
                    fetchSearchHistoryAjax();
                } else {
                    // 비로그인 사용자는 localStorage에서 조회
                    const localHistory = getSearchHistoryFromLocalStorage();
                    showSearchHistory(localHistory);
                }
            }, 200);
        }
        
        // 자동완성 요청 함수
        function requestAutocomplete(query) {
            if (autocompleteTimeout) {
                clearTimeout(autocompleteTimeout);
            }
            
            autocompleteTimeout = setTimeout(() => {
                fetchAutocomplete(query);
            }, 300);
        }
        
        // 포커스 이벤트 - 검색 히스토리 표시
        searchInput.addEventListener('focus', function() {
            const query = this.value.trim();
            if (query === '') {
                requestSearchHistory();
            } else {
                requestAutocomplete(query);
            }
        });
        
        // 입력 이벤트 - 값에 따라 히스토리 또는 자동완성
        searchInput.addEventListener('input', function() {
            const query = this.value.trim();
            selectedIndex = -1; // 선택 인덱스 초기화
            
            if (query === '') {
                // 입력값이 없으면 검색 히스토리
                if (typeof isCurrentUserLoggedIn === 'function' && isCurrentUserLoggedIn()) {
                    requestSearchHistory();
                } else {
                    hideDropdown();
                }
            } else if (query.length >= 2) {
                // 2글자 이상이면 자동완성
                requestAutocomplete(query);
            } else {
                hideDropdown();
            }
        });
        
        // 키보드 네비게이션
        searchInput.addEventListener('keydown', function(e) {
            if (dropdownContainer.style.display === 'none') return;
            
            const items = dropdownContainer.querySelectorAll('.dropdown-item');
            
            switch(e.key) {
                case 'ArrowDown':
                    e.preventDefault();
                    selectedIndex = Math.min(selectedIndex + 1, items.length - 1);
                    updateSelection(items);
                    break;
                    
                case 'ArrowUp':
                    e.preventDefault();
                    selectedIndex = Math.max(selectedIndex - 1, -1);
                    updateSelection(items);
                    break;
                    
                case 'Enter':
                    e.preventDefault();
                    if (selectedIndex >= 0 && items[selectedIndex]) {
                        items[selectedIndex].click();
                    } else {
                        searchForm.submit();
                    }
                    break;
                    
                case 'Escape':
                    hideDropdown();
                    selectedIndex = -1;
                    break;
            }
        });
        
        // 포커스 아웃 이벤트
        searchInput.addEventListener('blur', function() {
            setTimeout(() => {
                hideDropdown();
            }, 200);
        });
        
        // 선택 상태 업데이트
        function updateSelection(items) {
            items.forEach((item, index) => {
                if (index === selectedIndex) {
                    item.classList.add('selected');
                    item.scrollIntoView({ block: 'nearest' });
                } else {
                    item.classList.remove('selected');
                }
            });
        }
        
        // 검색 히스토리 데이터 가져오기
        function fetchSearchHistoryAjax() {
            isRequestingHistory = true;
            
            ajax.get('/api/products/search-history')
                .then(data => {
                    if (data && Array.isArray(data.data)) {
                        showSearchHistory(data.data);
                    } else {
                        showSearchHistory([]);
                    }
                })
                .catch(err => {
                    console.error('검색 히스토리 요청 오류:', err);
                    showSearchHistory([]);
                })
                .finally(() => {
                    isRequestingHistory = false;
                });
        }
        
        // 자동완성 데이터 가져오기
        async function fetchAutocomplete(query) {
            try {
                const response = await ajax.get(`/api/products/autocomplete?prefix=${encodeURIComponent(query)}`);
                
                if (response && response.data && Array.isArray(response.data) && response.data.length > 0) {
                    showAutocomplete(response.data);
                } else {
                    hideDropdown();
                }
            } catch (error) {
                console.error('자동완성 검색 실패:', error);
                hideDropdown();
            }
        }
        
        // 검색 히스토리 표시
        function showSearchHistory(historyList) {
            dropdownContainer.innerHTML = '';
            selectedIndex = -1;
            
            // 섹션 제목
            const title = document.createElement('div');
            title.className = 'dropdown-section-title';
            title.innerHTML = '<i class="icon">🕒</i> 최근 검색어';
            dropdownContainer.appendChild(title);
            
            if (!historyList || historyList.length === 0) {
                const emptyMessage = document.createElement('div');
                emptyMessage.className = 'dropdown-empty';
                emptyMessage.textContent = '검색 기록이 없습니다';
                dropdownContainer.appendChild(emptyMessage);
            } else {
                historyList.forEach((keyword, index) => {
                    const item = document.createElement('div');
                    item.className = 'dropdown-item history-item';
                    
                    // 검색어 텍스트
                    const keywordText = document.createElement('span');
                    keywordText.className = 'keyword-text';
                    keywordText.textContent = keyword;
                    item.appendChild(keywordText);
                    
                    // 개별 삭제 버튼
                    const deleteBtn = document.createElement('button');
                    deleteBtn.className = 'delete-btn';
                    deleteBtn.innerHTML = '×';
                    deleteBtn.title = '삭제';
                    deleteBtn.addEventListener('click', function(e) {
                        e.stopPropagation();
                        deleteSearchHistoryItem(keyword);
                    });
                    item.appendChild(deleteBtn);
                    
                    // 클릭 시 검색 실행
                    item.addEventListener('click', function(e) {
                        if (e.target !== deleteBtn) {
                            searchInput.value = keyword;
                            hideDropdown();
                            searchForm.submit();
                        }
                    });
                    
                    dropdownContainer.appendChild(item);
                });
                
                // 전체 삭제 버튼
                const clearAllBtn = document.createElement('div');
                clearAllBtn.className = 'dropdown-action';
                clearAllBtn.innerHTML = '<i class="icon">🗑️</i> 검색 기록 모두 지우기';
                clearAllBtn.addEventListener('click', function() {
                    clearSearchHistory();
                });
                dropdownContainer.appendChild(clearAllBtn);
            }
            
            dropdownContainer.style.display = 'block';
        }
        
        // 자동완성 표시
        function showAutocomplete(suggestions) {
            dropdownContainer.innerHTML = '';
            selectedIndex = -1;
            
            // 섹션 제목
            const title = document.createElement('div');
            title.className = 'dropdown-section-title';
            title.innerHTML = '<i class="icon">💡</i> 추천 검색어';
            dropdownContainer.appendChild(title);
            
            suggestions.forEach((suggestion, index) => {
                const item = document.createElement('div');
                item.className = 'dropdown-item autocomplete-item';
                
                // 하이라이팅된 HTML을 그대로 표시
                item.innerHTML = suggestion;
                
                item.addEventListener('click', function() {
                    // HTML 태그 제거하고 순수 텍스트만 추출
                    const tempDiv = document.createElement('div');
                    tempDiv.innerHTML = suggestion;
                    const plainText = tempDiv.textContent || tempDiv.innerText || '';
                    
                    searchInput.value = plainText;
                    hideDropdown();
                    selectedIndex = -1;
                    searchForm.submit();
                });
                
                dropdownContainer.appendChild(item);
            });
            
            dropdownContainer.style.display = 'block';
        }
        
        // 드롭다운 숨기기
        function hideDropdown() {
            dropdownContainer.style.display = 'none';
            selectedIndex = -1;
        }
        
        // 개별 검색 히스토리 삭제
        async function deleteSearchHistoryItem(keyword) {
            try {
                if (typeof isCurrentUserLoggedIn === 'function' && isCurrentUserLoggedIn()) {
                    // 로그인한 사용자는 서버에서 삭제
                    const result = await ajax.delete('/api/products/search-history/delete', { keyword: keyword });
                    
                    if (result && result.code === '00') {
                        // 삭제 후 히스토리 다시 로드
                        fetchSearchHistoryAjax();
                        showNotification('검색어가 삭제되었습니다.', 'success');
                    } else {
                        showNotification(result?.message || '삭제에 실패했습니다.', 'error');
                    }
                } else {
                    // 비로그인 사용자는 localStorage에서 삭제
                    deleteSearchHistoryFromLocalStorage(keyword);
                    const localHistory = getSearchHistoryFromLocalStorage();
                    showSearchHistory(localHistory);
                    showNotification('검색어가 삭제되었습니다.', 'success');
                }
            } catch (error) {
                console.error('검색 히스토리 삭제 실패:', error);
                showNotification('삭제 중 오류가 발생했습니다.', 'error');
            }
        }
        
        // 검색 히스토리 모두 지우기
        async function clearSearchHistory() {
            try {
                if (typeof isCurrentUserLoggedIn === 'function' && isCurrentUserLoggedIn()) {
                    // 로그인한 사용자는 서버에서 삭제
                    const result = await ajax.delete('/api/products/search-history');
                    
                    if (result && result.code === '00') {
                        hideDropdown();
                        showNotification('검색 기록이 모두 삭제되었습니다.', 'success');
                    } else {
                        showNotification(result?.message || '검색 기록 삭제에 실패했습니다.', 'error');
                    }
                } else {
                    // 비로그인 사용자는 localStorage에서 삭제
                    clearSearchHistoryFromLocalStorage();
                    hideDropdown();
                    showNotification('검색 기록이 모두 삭제되었습니다.', 'success');
                }
            } catch (error) {
                showNotification('검색 기록 삭제 중 오류가 발생했습니다.', 'error');
            }
        }
    }
}); 