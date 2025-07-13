/**
 * Metal UI - Menu Dropdown JavaScript
 * 메뉴 드롭다운 기능을 위한 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // 메뉴 드롭다운 기능
    const menuItems = document.querySelectorAll('.nav-item');
    
    menuItems.forEach(item => {
        const submenu = item.querySelector('.submenu');
        
        if (submenu) {
            // 마우스 진입 시 서브메뉴 표시
            item.addEventListener('mouseenter', function() {
                submenu.classList.remove('hidden');
                submenu.classList.add('block');
            });
            
            // 마우스 이탈 시 서브메뉴 숨김
            item.addEventListener('mouseleave', function() {
                submenu.classList.add('hidden');
                submenu.classList.remove('block');
            });
            
            // 모바일에서 터치 이벤트 처리
            item.addEventListener('click', function(e) {
                if (window.innerWidth <= 1024) {
                    e.preventDefault();
                    submenu.classList.toggle('hidden');
                    submenu.classList.toggle('block');
                }
            });
        }
    });
    
    // 배너 캐러셀 기능 (기존 기능 유지)
    const carousel = document.querySelector('.carousel');
    if (carousel) {
        const items = carousel.querySelectorAll('.carousel-item');
        const indicators = carousel.querySelectorAll('.carousel-indicator');
        const prevBtn = carousel.querySelector('#prev');
        const nextBtn = carousel.querySelector('#next');
        
        let currentIndex = 0;
        
        function showItem(index) {
            items.forEach((item, i) => {
                item.classList.toggle('active', i === index);
            });
            
            indicators.forEach((indicator, i) => {
                indicator.classList.toggle('bg-gold', i === index);
                indicator.classList.toggle('bg-steel', i !== index);
            });
        }
        
        function nextItem() {
            currentIndex = (currentIndex + 1) % items.length;
            showItem(currentIndex);
        }
        
        function prevItem() {
            currentIndex = (currentIndex - 1 + items.length) % items.length;
            showItem(currentIndex);
        }
        
        // 이벤트 리스너 추가
        if (prevBtn) {
            prevBtn.addEventListener('click', prevItem);
        }
        
        if (nextBtn) {
            nextBtn.addEventListener('click', nextItem);
        }
        
        indicators.forEach((indicator, index) => {
            indicator.addEventListener('click', () => {
                currentIndex = index;
                showItem(currentIndex);
            });
        });
        
        // 자동 슬라이드 (5초마다)
        setInterval(nextItem, 5000);
        
        // 초기 상태 설정
        showItem(0);
    }
    
    // 로그아웃 기능
    const logoutBtn = document.getElementById('logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            // MetalUI 모달 사용
            if (window.MetalUI) {
                const modal = MetalUI.createModal({
                    title: '로그아웃',
                    content: '정말 로그아웃하시겠습니까?',
                    onConfirm: () => {
                        window.location.href = this.href;
                    }
                });
                MetalUI.showModal(modal);
            } else {
                // MetalUI가 없는 경우 기본 확인
                if (confirm('정말 로그아웃하시겠습니까?')) {
                    window.location.href = this.href;
                }
            }
        });
    }
    
    // 반응형 메뉴 토글 (모바일)
    const mobileMenuToggle = document.getElementById('mobile-menu-toggle');
    const mobileMenu = document.getElementById('mobile-menu');
    
    if (mobileMenuToggle && mobileMenu) {
        mobileMenuToggle.addEventListener('click', function() {
            if (mobileMenu.style.display === 'none' || !mobileMenu.style.display) {
                mobileMenu.style.display = 'block';
                if (window.MetalUI) {
                    MetalUI.slideDown(mobileMenu);
                }
            } else {
                if (window.MetalUI) {
                    MetalUI.slideUp(mobileMenu);
                } else {
                    mobileMenu.style.display = 'none';
                }
            }
        });
    }
    
    // 검색 기능
    const searchInput = document.querySelector('input[placeholder="검색..."]');
    if (searchInput) {
        const debouncedSearch = window.MetalUI ? 
            MetalUI.debounce(performSearch, 300) : 
            debounce(performSearch, 300);
            
        searchInput.addEventListener('input', debouncedSearch);
        
        function performSearch() {
            const query = searchInput.value.trim();
            if (query.length > 0) {
                // 검색 API 호출 또는 페이지 이동
                window.location.href = `/products/search?q=${encodeURIComponent(query)}`;
            }
        }
    }
    
    // 디바운스 함수 (MetalUI가 없는 경우)
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
    
    // 페이지 로드 완료 시 로딩 스피너 숨김
    const loadingSpinner = document.getElementById('loading-spinner');
    if (loadingSpinner) {
        setTimeout(() => {
            loadingSpinner.style.display = 'none';
        }, 500);
    }
    
    // 스크롤 시 헤더 고정
    const header = document.querySelector('#tc');
    if (header) {
        let lastScrollTop = 0;
        
        window.addEventListener('scroll', function() {
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            
            if (scrollTop > lastScrollTop && scrollTop > 100) {
                // 아래로 스크롤
                header.style.transform = 'translateY(-100%)';
            } else {
                // 위로 스크롤
                header.style.transform = 'translateY(0)';
            }
            
            lastScrollTop = scrollTop;
        });
    }
}); 