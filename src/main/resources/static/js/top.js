// Top 메뉴 프로필 드롭다운 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // 드롭다운 토글 함수
    window.toggleProfileDropdown = function() {
        const dropdown = document.querySelector('.profile-dropdown');
        const menu = document.getElementById('profileDropdown');
        
        if (dropdown && menu) {
            dropdown.classList.toggle('active');
            
            // 다른 드롭다운이 열려있으면 닫기
            const otherDropdowns = document.querySelectorAll('.profile-dropdown.active');
            otherDropdowns.forEach(function(otherDropdown) {
                if (otherDropdown !== dropdown) {
                    otherDropdown.classList.remove('active');
                }
            });
        }
    };
    
    // 드롭다운 외부 클릭 시 닫기
    document.addEventListener('click', function(event) {
        const dropdowns = document.querySelectorAll('.profile-dropdown');
        
        dropdowns.forEach(function(dropdown) {
            const trigger = dropdown.querySelector('.profile-trigger');
            const menu = dropdown.querySelector('.profile-dropdown-menu');
            
            if (dropdown && !dropdown.contains(event.target)) {
                dropdown.classList.remove('active');
            }
        });
    });
    
    // ESC 키로 드롭다운 닫기
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            const dropdowns = document.querySelectorAll('.profile-dropdown.active');
            dropdowns.forEach(function(dropdown) {
                dropdown.classList.remove('active');
            });
        }
    });
    
    // 드롭다운 아이템 클릭 시 드롭다운 닫기
    const dropdownItems = document.querySelectorAll('.dropdown-item');
    dropdownItems.forEach(function(item) {
        item.addEventListener('click', function() {
            const dropdown = this.closest('.profile-dropdown');
            if (dropdown) {
                dropdown.classList.remove('active');
            }
        });
    });
    
    // 프로필 이미지 로드 에러 처리
    const profileImages = document.querySelectorAll('.profile-img, .dropdown-img');
    profileImages.forEach(function(img) {
        img.addEventListener('error', function() {
            // 이미지 로드 실패 시 기본 아바타로 대체
            const parent = this.parentElement;
            if (parent) {
                const nickname = this.getAttribute('data-nickname') || '?';
                parent.innerHTML = `
                    <div class="default-avatar">
                        <span class="avatar-text">${nickname.charAt(0).toUpperCase()}</span>
                    </div>
                `;
            }
        });
    });
    
    // 호버 효과 (선택사항)
    const profileTriggers = document.querySelectorAll('.profile-trigger');
    profileTriggers.forEach(function(trigger) {
        trigger.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-1px)';
        });
        
        trigger.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
    
    // 드롭다운 메뉴 애니메이션 개선
    const dropdownMenus = document.querySelectorAll('.profile-dropdown-menu');
    dropdownMenus.forEach(function(menu) {
        menu.addEventListener('transitionend', function() {
            if (!this.parentElement.classList.contains('active')) {
                this.style.visibility = 'hidden';
            }
        });
    });
});

// 로그아웃 확인
document.addEventListener('DOMContentLoaded', function() {
    const logoutLinks = document.querySelectorAll('#logout');
    logoutLinks.forEach(function(link) {
        link.addEventListener('click', function(e) {
            if (!confirm('로그아웃하시겠습니까?')) {
                e.preventDefault();
            }
        });
    });
});

// 프로필 이미지 새로고침 (프로필 사진 변경 후)
function refreshProfileImage() {
    console.log('프로필 이미지 새로고침 시작'); // 디버깅 로그
    const profileImages = document.querySelectorAll('.profile-img, .dropdown-img');
    console.log('찾은 프로필 이미지 개수:', profileImages.length); // 디버깅 로그
    
    profileImages.forEach(function(img) {
        console.log('이미지 URL:', img.src); // 디버깅 로그
        if (img.src.includes('/member/profile-image/view')) {
            // 기존 파라미터를 유지하면서 타임스탬프만 추가
            const url = new URL(img.src, window.location.origin);
            url.searchParams.set('t', new Date().getTime());
            const newSrc = url.toString();
            console.log('새 이미지 URL:', newSrc); // 디버깅 로그
            img.src = newSrc;
        }
    });
}

// 전역 함수로 등록
window.refreshProfileImage = refreshProfileImage; 