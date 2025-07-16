/**
 * 상품 검색 결과 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // 필터 및 정렬 기능
    const filterSelect = document.querySelector('.filter-select');
    const sortSelect = document.querySelector('.sort-select');
    
    // 카테고리 필터링
    function filterByCategory(category) {
        const currentUrl = new URL(window.location);
        
        if (category) {
            currentUrl.searchParams.set('category', category);
            currentUrl.searchParams.delete('keyword'); // 카테고리 선택 시 키워드 제거
        } else {
            currentUrl.searchParams.delete('category');
        }
        
        // 페이지를 1로 리셋
        currentUrl.searchParams.set('page', '1');
        
        window.location.href = currentUrl.toString();
    }
    
    // 상품 정렬
    function sortProducts(sortType) {
        const currentUrl = new URL(window.location);
        currentUrl.searchParams.set('sort', sortType);
        currentUrl.searchParams.set('page', '1'); // 정렬 변경 시 페이지 리셋
        
        window.location.href = currentUrl.toString();
    }
    
    // 전역 함수로 노출 (HTML에서 호출)
    window.filterByCategory = filterByCategory;
    window.sortProducts = sortProducts;
    
    // 상품 카드 호버 효과
    const productCards = document.querySelectorAll('.product-card');
    productCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-4px)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
    
    // 상품 이미지 로드 실패 처리
    const productImages = document.querySelectorAll('.product-img');
    productImages.forEach(img => {
        img.addEventListener('error', function() {
            this.style.display = 'none';
            const placeholder = this.parentElement.querySelector('.product-img-placeholder');
            if (placeholder) {
                placeholder.style.display = 'flex';
            }
        });
    });
    
    // 페이지네이션 애니메이션
    const paginationLinks = document.querySelectorAll('.pagination-btn, .page-number a');
    paginationLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            // 로딩 표시
            showLoadingIndicator();
        });
    });
    
    // 로딩 인디케이터
    function showLoadingIndicator() {
        const loadingDiv = document.createElement('div');
        loadingDiv.className = 'loading-indicator';
        loadingDiv.innerHTML = `
            <div style="
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 3px;
                background: #1a1a1a;
                z-index: 9999;
                animation: loading 1s infinite;
            "></div>
        `;
        
        const style = document.createElement('style');
        style.textContent = `
            @keyframes loading {
                0% { transform: translateX(-100%); }
                100% { transform: translateX(100%); }
            }
        `;
        
        document.head.appendChild(style);
        document.body.appendChild(loadingDiv);
        
        // 3초 후 자동 제거
        setTimeout(() => {
            if (loadingDiv.parentNode) {
                loadingDiv.remove();
            }
        }, 3000);
    }
    
    // 검색 결과 개수에 따른 메시지 표시
    const resultsCount = document.querySelector('.results-count');
    if (resultsCount) {
        const countText = resultsCount.textContent;
        const count = parseInt(countText.match(/\d+/)[0]);
        
        if (count === 0) {
            // 검색 결과가 없을 때 추가 안내
            const noResultsDiv = document.querySelector('.no-results');
            if (noResultsDiv) {
                const suggestionDiv = document.createElement('div');
                suggestionDiv.className = 'search-suggestions';
                suggestionDiv.innerHTML = `
                    <h4 style="margin: 1rem 0 0.5rem 0; color: #666;">검색 팁:</h4>
                    <ul style="text-align: left; max-width: 400px; margin: 0 auto; color: #666;">
                        <li>검색어의 철자가 정확한지 확인해보세요</li>
                        <li>더 일반적인 검색어로 다시 시도해보세요</li>
                        <li>상품명 대신 카테고리로 검색해보세요</li>
                    </ul>
                `;
                noResultsDiv.appendChild(suggestionDiv);
            }
        } else if (count > 50) {
            // 검색 결과가 많을 때 필터 안내
            const filterBar = document.querySelector('.filter-sort-bar');
            if (filterBar) {
                const filterTip = document.createElement('div');
                filterTip.className = 'filter-tip';
                filterTip.innerHTML = `
                    <small style="color: #666; font-style: italic;">
                        💡 많은 검색 결과가 있습니다. 카테고리 필터를 사용하여 원하는 상품을 찾아보세요.
                    </small>
                `;
                filterBar.appendChild(filterTip);
            }
        }
    }
    
    // 무한 스크롤 기능 (선택사항)
    let isLoading = false;
    let currentPage = parseInt(new URLSearchParams(window.location.search).get('page')) || 1;
    const totalPages = parseInt(document.querySelector('.pagination-info')?.textContent.match(/\d+/g)?.[1]) || 1;
    
    function loadMoreProducts() {
        if (isLoading || currentPage >= totalPages) return;
        
        isLoading = true;
        currentPage++;
        
        const currentUrl = new URL(window.location);
        currentUrl.searchParams.set('page', currentPage);
        
        // AJAX로 다음 페이지 로드
        fetch(currentUrl.toString())
            .then(response => response.text())
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newProducts = doc.querySelectorAll('.product-card');
                
                if (newProducts.length > 0) {
                    const productsGrid = document.querySelector('.products-grid');
                    newProducts.forEach(product => {
                        productsGrid.appendChild(product.cloneNode(true));
                    });
                    
                    // 페이지네이션 업데이트
                    updatePagination();
                }
            })
            .catch(error => {
                console.error('추가 상품 로드 실패:', error);
                currentPage--; // 실패 시 페이지 번호 되돌리기
            })
            .finally(() => {
                isLoading = false;
            });
    }
    
    function updatePagination() {
        const paginationInfo = document.querySelector('.pagination-info');
        if (paginationInfo) {
            paginationInfo.textContent = `${currentPage} / ${totalPages} 페이지`;
        }
        
        // 마지막 페이지에 도달하면 무한 스크롤 비활성화
        if (currentPage >= totalPages) {
            window.removeEventListener('scroll', handleScroll);
        }
    }
    
    function handleScroll() {
        if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 1000) {
            loadMoreProducts();
        }
    }
    
    // 무한 스크롤 활성화 (선택사항)
    // window.addEventListener('scroll', handleScroll);
    
    // 상품 좋아요 기능 (선택사항)
    function toggleLike(productId) {
        const likeButton = document.querySelector(`[data-product-id="${productId}"]`);
        if (!likeButton) return;
        
        const isLiked = likeButton.classList.contains('liked');
        
        // 서버에 좋아요 상태 전송
        fetch('/api/products/like', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                productId: productId,
                liked: !isLiked
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                if (isLiked) {
                    likeButton.classList.remove('liked');
                    likeButton.textContent = '♡';
                } else {
                    likeButton.classList.add('liked');
                    likeButton.textContent = '♥';
                }
            }
        })
        .catch(error => {
            console.error('좋아요 처리 실패:', error);
        });
    }
    
    // 상품 비교 기능 (선택사항)
    const compareList = JSON.parse(localStorage.getItem('compareList') || '[]');
    
    function addToCompare(productId) {
        if (compareList.length >= 4) {
            alert('최대 4개 상품까지 비교할 수 있습니다.');
            return;
        }
        
        if (!compareList.includes(productId)) {
            compareList.push(productId);
            localStorage.setItem('compareList', JSON.stringify(compareList));
            updateCompareButton();
        }
    }
    
    function updateCompareButton() {
        const compareButton = document.querySelector('.compare-button');
        if (compareButton) {
            compareButton.textContent = `비교하기 (${compareList.length})`;
            compareButton.disabled = compareList.length < 2;
        }
    }
    
    // 전역 함수로 노출
    window.toggleLike = toggleLike;
    window.addToCompare = addToCompare;
    
    // 페이지 로드 완료 메시지
    console.log('상품 검색 결과 페이지가 로드되었습니다.');
}); 