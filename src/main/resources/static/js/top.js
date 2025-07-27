// 새로운 Top 메뉴 시스템 JavaScript
// 역할별 드롭다운 및 빠른 액션 기능

document.addEventListener('DOMContentLoaded', function() {
    console.log('Top 메뉴 시스템 로드됨');
    
    // 전역 변수 초기화
    let activeDropdown = null;
    
    // ===== 공통 드롭다운 관리 함수 =====
    function closeAllDropdowns() {
        const dropdowns = document.querySelectorAll('.profile-dropdown');
        dropdowns.forEach(dropdown => {
            dropdown.classList.remove('active');
            const menu = dropdown.querySelector('.profile-dropdown-menu');
            if (menu) {
                menu.style.display = 'none';
            }
        });
        activeDropdown = null;
    }
    
    function openDropdown(dropdown) {
        closeAllDropdowns();
        if (dropdown) {
            dropdown.classList.add('active');
            const menu = dropdown.querySelector('.profile-dropdown-menu');
            if (menu) {
                menu.style.display = 'block';
            }
            activeDropdown = dropdown;
        }
    }
    
    // ===== 고객 드롭다운 =====
    window.toggleCustomerDropdown = function() {
        console.log('고객 드롭다운 토글');
        const dropdown = document.querySelector('.profile-dropdown.customer-profile');
        const menu = document.getElementById('customerDropdown');
        
        if (dropdown && menu) {
            if (dropdown.classList.contains('active')) {
                closeAllDropdowns();
            } else {
                openDropdown(dropdown);
            }
        } else {
            console.error('고객 드롭다운 요소를 찾을 수 없습니다');
        }
    };
    
    // ===== 관리자 드롭다운 =====
    window.toggleAdminDropdown = function() {
        console.log('관리자 드롭다운 토글');
        const dropdown = document.querySelector('.profile-dropdown.admin-profile');
        const menu = document.getElementById('adminDropdown');
        
        if (dropdown && menu) {
            if (dropdown.classList.contains('active')) {
                closeAllDropdowns();
            } else {
                openDropdown(dropdown);
            }
        } else {
            console.error('관리자 드롭다운 요소를 찾을 수 없습니다');
        }
    };
    
    // ===== 알림 관리 =====
    window.toggleNotifications = function(event) {
        console.log('고객 알림 토글');
        
        // 이벤트 버블링 방지
        if (event) {
            event.stopPropagation();
            event.preventDefault();
        }
        
        const notificationPanel = document.getElementById('notificationPanel');
        
        if (notificationPanel) {
            if (notificationPanel.classList.contains('active')) {
                notificationPanel.classList.remove('active');
                setTimeout(() => {
                    if (!notificationPanel.classList.contains('active')) {
                        notificationPanel.style.display = 'none';
                    }
                }, 250);
            } else {
                // 다른 드롭다운 닫기
                closeAllDropdowns();
                closeAllNotificationPanels();
                
                notificationPanel.style.display = 'block';
                // 브라우저가 display를 적용할 시간을 줌
                requestAnimationFrame(() => {
                    notificationPanel.classList.add('active');
                });
                
                // 임시 알림 데이터 표시
                showCustomerNotifications();
            }
        } else {
            // 알림 패널이 없으면 동적 생성
            createNotificationPanel();
        }
    };
    
    window.toggleSystemAlerts = function(event) {
        console.log('시스템 알림 토글');
        
        // 이벤트 버블링 방지
        if (event) {
            event.stopPropagation();
            event.preventDefault();
        }
        
        const alertPanel = document.getElementById('systemAlertPanel');
        
        if (alertPanel) {
            if (alertPanel.classList.contains('active')) {
                alertPanel.classList.remove('active');
                setTimeout(() => {
                    if (!alertPanel.classList.contains('active')) {
                        alertPanel.style.display = 'none';
                    }
                }, 250);
            } else {
                closeAllDropdowns();
                closeAllNotificationPanels();
                
                alertPanel.style.display = 'block';
                // 브라우저가 display를 적용할 시간을 줌
                requestAnimationFrame(() => {
                    alertPanel.classList.add('active');
                });
                
                showSystemAlerts();
            }
        } else {
            createSystemAlertPanel();
        }
    };
    
    function closeAllNotificationPanels() {
        const panels = document.querySelectorAll('.notification-panel, .system-alert-panel');
        panels.forEach(panel => {
            panel.classList.remove('active');
            setTimeout(() => {
                if (!panel.classList.contains('active')) {
                    panel.style.display = 'none';
                }
            }, 250);
        });
    }
    
    // ===== 알림 패널 생성 =====
    function createNotificationPanel() {
        const panel = document.createElement('div');
        panel.id = 'notificationPanel';
        panel.className = 'notification-panel';
        panel.innerHTML = `
            <div class="notification-header">
                <h3>🔔 알림</h3>
                <button onclick="markAllAsRead()" class="mark-read-btn">모두 읽음</button>
            </div>
            <div class="notification-list" id="notificationList">
                <div class="loading">알림을 불러오는 중...</div>
            </div>
        `;
        
        // 패널 내부 클릭 시 이벤트 전파 방지
        panel.addEventListener('click', function(event) {
            event.stopPropagation();
        });
        
        // 알림 버튼을 컨테이너로 만들어 드롭다운 방식으로 배치
        const notificationBtn = document.querySelector('.notification-btn');
        if (notificationBtn) {
            // 알림 버튼을 relative 포지션으로 만들어 드롭다운 컨테이너로 사용
            notificationBtn.style.position = 'relative';
            notificationBtn.appendChild(panel);
            
            // 다른 패널들 닫기
            closeAllDropdowns();
            closeAllNotificationPanels();
            
            panel.style.display = 'block';
            // 브라우저가 display를 적용할 시간을 줌
            requestAnimationFrame(() => {
                panel.classList.add('active');
            });
            
            showCustomerNotifications();
        }
    }
    
    function createSystemAlertPanel() {
        const panel = document.createElement('div');
        panel.id = 'systemAlertPanel';
        panel.className = 'system-alert-panel';
        panel.innerHTML = `
            <div class="alert-header">
                <h3>🚨 시스템 알림</h3>
                <button onclick="clearSystemAlerts()" class="clear-btn">모두 지우기</button>
            </div>
            <div class="alert-list" id="systemAlertList">
                <div class="loading">시스템 알림을 불러오는 중...</div>
            </div>
        `;
        
        // 패널 내부 클릭 시 이벤트 전파 방지
        panel.addEventListener('click', function(event) {
            event.stopPropagation();
        });
        
        const alertBtn = document.querySelector('.system-alerts');
        if (alertBtn) {
            // 시스템 알림 버튼도 드롭다운 컨테이너로 사용
            alertBtn.style.position = 'relative';
            alertBtn.appendChild(panel);
            
            // 다른 패널들 닫기
            closeAllDropdowns();
            closeAllNotificationPanels();
            
            panel.style.display = 'block';
            // 브라우저가 display를 적용할 시간을 줌
            requestAnimationFrame(() => {
                panel.classList.add('active');
            });
            
            showSystemAlerts();
        }
    }
    
    // ===== 알림 데이터 표시 =====
    async function showCustomerNotifications() {
        const notificationList = document.getElementById('notificationList');
        if (notificationList) {
            try {
                // 로딩 상태 표시
                notificationList.innerHTML = '<div class="loading">알림을 불러오는 중...</div>';
                
                // 서버에서 알림 데이터 가져오기
                const response = await fetch('/api/notification');
                const result = await response.json();
                
                if (response.ok && result.code === '00') {
                    const notifications = result.data || [];
                    
                    if (notifications.length === 0) {
                        notificationList.innerHTML = '<div class="no-notifications">새로운 알림이 없습니다</div>';
                    } else {
                        notificationList.innerHTML = notifications.map(notif => `
                            <div class="notification-item ${notif.isRead ? 'read' : 'unread'}" data-id="${notif.notificationId}">
                                <div class="notification-icon">${getNotificationIcon(notif.notificationTypeName)}</div>
                                <div class="notification-content">
                                    <div class="notification-title">${notif.title}</div>
                                    <div class="notification-message">${notif.message}</div>
                                    <div class="notification-time">${formatTimeAgo(notif.createdDate)}</div>
                                </div>
                                ${!notif.isRead ? '<div class="unread-indicator"></div>' : ''}
                            </div>
                        `).join('');
                    }
                } else {
                    notificationList.innerHTML = '<div class="error">알림을 불러올 수 없습니다</div>';
                }
            } catch (error) {
                console.error('알림 데이터 로드 실패:', error);
                notificationList.innerHTML = '<div class="error">알림을 불러올 수 없습니다</div>';
            }
        }
    }
    
    function showSystemAlerts() {
        const alertList = document.getElementById('systemAlertList');
        if (alertList) {
            const alerts = [
                { id: 1, level: 'critical', message: '서버 CPU 사용률 95% 초과', time: '5분 전' },
                { id: 2, level: 'warning', message: '대기 중인 주문 10건 초과', time: '15분 전' }
            ];
            
            alertList.innerHTML = alerts.map(alert => `
                <div class="alert-item ${alert.level}" data-id="${alert.id}">
                    <div class="alert-icon">${getAlertIcon(alert.level)}</div>
                    <div class="alert-content">
                        <div class="alert-message">${alert.message}</div>
                        <div class="alert-time">${alert.time}</div>
                    </div>
                </div>
            `).join('');
        }
    }
    
    // ===== 유틸리티 함수 =====
    function getNotificationIcon(typeName) {
        const icons = {
            '주문': '📦',
            '결제': '💳',
            '배송': '🚚',
            '리뷰': '⭐',
            '상품': '🛍️',
            '시스템': '⚙️',
            '관리자알림': '🔔'
        };
        return icons[typeName] || '📢';
    }
    
    function formatTimeAgo(createdDate) {
        if (!createdDate) return '';
        
        const now = new Date();
        const created = new Date(createdDate);
        const diffMs = now - created;
        const diffMinutes = Math.floor(diffMs / (1000 * 60));
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
        const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
        
        if (diffMinutes < 1) return '방금 전';
        if (diffMinutes < 60) return `${diffMinutes}분 전`;
        if (diffHours < 24) return `${diffHours}시간 전`;
        if (diffDays < 7) return `${diffDays}일 전`;
        
        return created.toLocaleDateString('ko-KR');
    }
    
    function getAlertIcon(level) {
        const icons = {
            critical: '🚨',
            warning: '⚠️',
            info: 'ℹ️'
        };
        return icons[level] || '📢';
    }
    
    // ===== 전역 액션 함수 =====
    window.markAllAsRead = async function() {
        console.log('모든 알림 읽음 처리');
        
        try {
            // 서버에 모든 알림 읽음 처리 요청
            const response = await fetch('/api/notification/read-all', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });
            
            if (response.ok) {
                // UI 업데이트
                const notificationItems = document.querySelectorAll('.notification-item.unread');
                notificationItems.forEach(item => {
                    item.classList.remove('unread');
                    item.classList.add('read');
                    const indicator = item.querySelector('.unread-indicator');
                    if (indicator) indicator.remove();
                });
                
                // 알림 뱃지 업데이트 (common.js의 통합 함수 사용)
                if (typeof window.updateNotificationCount === 'function') {
                    await window.updateNotificationCount();
                } else {
                    const badge = document.getElementById('notificationBadge');
                    if (badge) badge.textContent = '0';
                }
            }
        } catch (error) {
            console.error('알림 읽음 처리 실패:', error);
        }
    };
    
    window.clearSystemAlerts = function() {
        console.log('시스템 알림 모두 지우기');
        const alertList = document.getElementById('systemAlertList');
        if (alertList) {
            alertList.innerHTML = '<div class="no-alerts">모든 알림이 지워졌습니다</div>';
        }
        
        const badge = document.getElementById('systemAlertCount');
        if (badge) badge.textContent = '0';
    };
    
    // ===== 이벤트 리스너 =====
    
    // 외부 클릭 시 모든 드롭다운/패널 닫기
    document.addEventListener('click', function(event) {
        // 알림 버튼과 관련된 요소들 확인
        const isNotificationClick = event.target.closest('.notification-btn') || 
                                   event.target.closest('[onclick*="toggleNotifications"]');
        const isSystemAlertClick = event.target.closest('.system-alerts') || 
                                  event.target.closest('[onclick*="toggleSystemAlerts"]');
        const isProfileClick = event.target.closest('.profile-dropdown');
        const isPanel = event.target.closest('.notification-panel, .system-alert-panel, .profile-dropdown-menu');
        
        // 알림 버튼이나 패널 내부 클릭이 아닌 경우에만 닫기
        if (!isNotificationClick && !isSystemAlertClick && !isProfileClick && !isPanel) {
            closeAllDropdowns();
            closeAllNotificationPanels();
        }
    });
    
    // ESC 키로 모든 패널 닫기
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeAllDropdowns();
            closeAllNotificationPanels();
        }
    });
    
    // 드롭다운 아이템 클릭 시 드롭다운 닫기
    document.addEventListener('click', function(event) {
        if (event.target.classList.contains('dropdown-item')) {
            // 로그아웃이 아닌 경우에만 드롭다운 닫기
            if (!event.target.classList.contains('logout-item')) {
                setTimeout(() => closeAllDropdowns(), 100);
            }
        }
    });
    
    // ===== 초기화 =====
    console.log('Top 메뉴 JavaScript 초기화 완료');
});

// ===== 레거시 함수 지원 =====
// 기존 코드와의 호환성을 위해 유지
window.toggleProfileDropdown = function() {
    // 현재 활성화된 드롭다운 타입 감지
    const customerDropdown = document.querySelector('.profile-dropdown.customer-profile');
    const adminDropdown = document.querySelector('.profile-dropdown.admin-profile');
    
    if (customerDropdown) {
        window.toggleCustomerDropdown();
    } else if (adminDropdown) {
        window.toggleAdminDropdown();
    }
}; 