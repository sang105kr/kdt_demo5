/**
 * 상품 검색 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // 검색 입력 필드
    const searchInput = document.querySelector('.search-input');
    const searchForm = document.querySelector('.search-form');
    
    // 검색 폼 제출 이벤트
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            const keyword = searchInput.value.trim();
            const category = document.querySelector('.category-select').value;
            
            // 검색어가 없고 카테고리도 선택되지 않은 경우
            if (!keyword && !category) {
                e.preventDefault();
                alert('검색어를 입력하거나 카테고리를 선택해주세요.');
                searchInput.focus();
                return;
            }
            
            // 검색어가 있는 경우 최소 2글자 이상 체크
            if (keyword && keyword.length < 2) {
                e.preventDefault();
                alert('검색어는 2글자 이상 입력해주세요.');
                searchInput.focus();
                return;
            }
        });
    }
    
    // 검색 입력 필드 포커스 시 플레이스홀더 변경
    if (searchInput) {
        searchInput.addEventListener('focus', function() {
            this.placeholder = '상품명 또는 설명을 입력하세요 (최소 2글자)';
        });
        
        searchInput.addEventListener('blur', function() {
            this.placeholder = '상품명 또는 설명을 입력하세요';
        });
    }
    
    // 카테고리 카드 클릭 시 애니메이션
    const categoryCards = document.querySelectorAll('.category-card');
    categoryCards.forEach(card => {
        card.addEventListener('click', function(e) {
            // 클릭 효과 추가
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
    });
    
    // 검색 히스토리 기능 (선택사항)
    const searchHistory = JSON.parse(localStorage.getItem('searchHistory') || '[]');
    
    function addToSearchHistory(keyword) {
        if (!keyword || keyword.trim() === '') return;
        
        const trimmedKeyword = keyword.trim();
        
        // 중복 제거
        const filteredHistory = searchHistory.filter(item => item !== trimmedKeyword);
        
        // 최신 검색어를 맨 앞에 추가
        filteredHistory.unshift(trimmedKeyword);
        
        // 최대 10개까지만 저장
        const newHistory = filteredHistory.slice(0, 10);
        
        localStorage.setItem('searchHistory', JSON.stringify(newHistory));
    }
    
    // 검색 실행 시 히스토리에 추가
    if (searchForm) {
        searchForm.addEventListener('submit', function() {
            const keyword = searchInput.value.trim();
            if (keyword) {
                addToSearchHistory(keyword);
            }
        });
    }
    
    // 검색어 자동완성 기능 (선택사항)
    function showSearchSuggestions() {
        if (searchHistory.length === 0) return;
        
        // 기존 제안 목록 제거
        const existingSuggestions = document.querySelector('.search-suggestions');
        if (existingSuggestions) {
            existingSuggestions.remove();
        }
        
        // 제안 목록 생성
        const suggestionsList = document.createElement('div');
        suggestionsList.className = 'search-suggestions';
        suggestionsList.style.cssText = `
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: white;
            border: 1px solid #ddd;
            border-top: none;
            border-radius: 0 0 8px 8px;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
            z-index: 1000;
            max-height: 200px;
            overflow-y: auto;
        `;
        
        searchHistory.forEach(keyword => {
            const suggestionItem = document.createElement('div');
            suggestionItem.className = 'suggestion-item';
            suggestionItem.style.cssText = `
                padding: 0.75rem 1rem;
                cursor: pointer;
                border-bottom: 1px solid #eee;
                transition: background-color 0.2s;
            `;
            suggestionItem.textContent = keyword;
            
            suggestionItem.addEventListener('mouseenter', function() {
                this.style.backgroundColor = '#f8f9fa';
            });
            
            suggestionItem.addEventListener('mouseleave', function() {
                this.style.backgroundColor = 'white';
            });
            
            suggestionItem.addEventListener('click', function() {
                searchInput.value = keyword;
                suggestionsList.remove();
                searchForm.submit();
            });
            
            suggestionsList.appendChild(suggestionItem);
        });
        
        // 검색 입력 그룹에 제안 목록 추가
        const searchInputGroup = document.querySelector('.search-input-group');
        if (searchInputGroup) {
            searchInputGroup.style.position = 'relative';
            searchInputGroup.appendChild(suggestionsList);
        }
    }
    
    // 검색 입력 필드 포커스 시 제안 표시
    if (searchInput) {
        searchInput.addEventListener('focus', showSearchSuggestions);
        
        // 다른 곳 클릭 시 제안 숨기기
        document.addEventListener('click', function(e) {
            if (!e.target.closest('.search-input-group')) {
                const suggestions = document.querySelector('.search-suggestions');
                if (suggestions) {
                    suggestions.remove();
                }
            }
        });
    }
    
    // 키보드 네비게이션 (선택사항)
    if (searchInput) {
        searchInput.addEventListener('keydown', function(e) {
            const suggestions = document.querySelector('.search-suggestions');
            if (!suggestions) return;
            
            const suggestionItems = suggestions.querySelectorAll('.suggestion-item');
            const currentHighlighted = suggestions.querySelector('.highlighted');
            
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                if (!currentHighlighted) {
                    suggestionItems[0].classList.add('highlighted');
                } else {
                    const currentIndex = Array.from(suggestionItems).indexOf(currentHighlighted);
                    const nextIndex = (currentIndex + 1) % suggestionItems.length;
                    currentHighlighted.classList.remove('highlighted');
                    suggestionItems[nextIndex].classList.add('highlighted');
                }
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                if (currentHighlighted) {
                    const currentIndex = Array.from(suggestionItems).indexOf(currentHighlighted);
                    const prevIndex = currentIndex === 0 ? suggestionItems.length - 1 : currentIndex - 1;
                    currentHighlighted.classList.remove('highlighted');
                    suggestionItems[prevIndex].classList.add('highlighted');
                }
            } else if (e.key === 'Enter' && currentHighlighted) {
                e.preventDefault();
                searchInput.value = currentHighlighted.textContent;
                suggestions.remove();
                searchForm.submit();
            } else if (e.key === 'Escape') {
                suggestions.remove();
            }
        });
    }
    
    // 하이라이트 스타일 추가
    const style = document.createElement('style');
    style.textContent = `
        .suggestion-item.highlighted {
            background-color: #1a1a1a !important;
            color: white !important;
        }
    `;
    document.head.appendChild(style);
    
    // 페이지 로드 완료 메시지
    console.log('상품 검색 페이지가 로드되었습니다.');
}); 