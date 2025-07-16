/**
 * 관리자 홈페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자 홈페이지 로드됨');
    
    // 메뉴 아이템 호버 효과
    const menuItems = document.querySelectorAll('.menu-item');
    
    menuItems.forEach(item => {
        item.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-4px)';
        });
        
        item.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
    
    // 관리자 권한 확인
    checkAdminPermission();
});

/**
 * 관리자 권한 확인
 */
function checkAdminPermission() {
    const memberGubun = document.getElementById('root').getAttribute('data-s-gubun');
    
    if (!memberGubun || memberGubun !== 'ADMIN') {
        console.warn('관리자 권한이 없습니다.');
        // 필요시 리다이렉트 또는 경고 메시지 표시
    }
}

/**
 * 페이지 로드 시 애니메이션 효과
 */
function animatePageLoad() {
    const adminContainer = document.querySelector('.admin-container');
    if (adminContainer) {
        adminContainer.style.opacity = '0';
        adminContainer.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            adminContainer.style.transition = 'all 0.5s ease';
            adminContainer.style.opacity = '1';
            adminContainer.style.transform = 'translateY(0)';
        }, 100);
    }
}

// 페이지 로드 시 애니메이션 실행
window.addEventListener('load', animatePageLoad); 