/**
 * 관리자용 공지사항 목록 페이지 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('관리자용 공지사항 목록 페이지 로드됨');
    
    // 페이지 초기화
    initAdminNoticeList();
});

/**
 * 관리자용 공지사항 목록 페이지 초기화
 */
function initAdminNoticeList() {
    console.log('관리자용 공지사항 목록 페이지 초기화');
    
    // 검색 폼 초기화
    initSearchForm();
    
    // 필터 초기화
    initFilters();
    
    // 정렬 초기화
    initSorting();
    
    // 페이지네이션 초기화
    initPagination();
    
    // 이벤트 리스너 설정
    setupEventListeners();
}

/**
 * 검색 폼 초기화
 */
function initSearchForm() {
    console.log('검색 폼 초기화');
    
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        // 검색어 입력 필드 자동 포커스
        const searchInput = searchForm.querySelector('input[name="searchKeyword"]');
        if (searchInput && !searchInput.value) {
            searchInput.focus();
        }
        
        // 검색 타입 변경 시 검색어 필드 플레이스홀더 업데이트
        const searchTypeSelect = searchForm.querySelector('select[name="searchType"]');
        if (searchTypeSelect) {
            updateSearchPlaceholder(searchTypeSelect.value);
            
            searchTypeSelect.addEventListener('change', function() {
                updateSearchPlaceholder(this.value);
            });
        }
    }
}

/**
 * 검색 타입에 따른 플레이스홀더 업데이트
 */
function updateSearchPlaceholder(searchType) {
    const searchInput = document.querySelector('input[name="searchKeyword"]');
    if (!searchInput) return;
    
    const placeholders = {
        'title': '제목을 입력하세요',
        'content': '내용을 입력하세요',
        'author': '작성자를 입력하세요',
        'all': '제목, 내용, 작성자로 검색하세요'
    };
    
    searchInput.placeholder = placeholders[searchType] || placeholders['all'];
}

/**
 * 필터 초기화
 */
function initFilters() {
    console.log('필터 초기화');
    
    // 카테고리 필터
    const categoryFilter = document.querySelector('select[name="categoryId"]');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', function() {
            console.log('카테고리 필터 변경:', this.value);
            applyFilters();
        });
    }
    
    // 상태 필터
    const statusFilter = document.querySelector('select[name="statusId"]');
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            console.log('상태 필터 변경:', this.value);
            applyFilters();
        });
    }
    
    // 중요도 필터
    const importantFilter = document.querySelector('input[name="isImportant"]');
    if (importantFilter) {
        importantFilter.addEventListener('change', function() {
            console.log('중요도 필터 변경:', this.checked);
            applyFilters();
        });
    }
    
    // 고정 필터
    const fixedFilter = document.querySelector('input[name="isFixed"]');
    if (fixedFilter) {
        fixedFilter.addEventListener('change', function() {
            console.log('고정 필터 변경:', this.checked);
            applyFilters();
        });
    }
}

/**
 * 정렬 초기화
 */
function initSorting() {
    console.log('정렬 초기화');
    
    const sortSelect = document.querySelector('select[name="sortBy"]');
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            console.log('정렬 변경:', this.value);
            applyFilters();
        });
    }
    
    const orderSelect = document.querySelector('select[name="sortOrder"]');
    if (orderSelect) {
        orderSelect.addEventListener('change', function() {
            console.log('정렬 순서 변경:', this.value);
            applyFilters();
        });
    }
}

/**
 * 페이지네이션 초기화
 */
function initPagination() {
    console.log('페이지네이션 초기화');
    
    // 페이지 크기 변경
    const pageSizeSelect = document.querySelector('select[name="pageSize"]');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', function() {
            console.log('페이지 크기 변경:', this.value);
            // 페이지를 1로 리셋하고 필터 적용
            const pageInput = document.querySelector('input[name="page"]');
            if (pageInput) {
                pageInput.value = '1';
            }
            applyFilters();
        });
    }
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    console.log('이벤트 리스너 설정');
    
    // 검색 폼 제출
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            console.log('검색 폼 제출');
            applyFilters();
        });
    }
    
    // 검색어 입력 필드 엔터 키 처리
    const searchInput = document.querySelector('input[name="searchKeyword"]');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                applyFilters();
            }
        });
    }
    
    // 필터 초기화 버튼
    const resetBtn = document.querySelector('.reset-btn');
    if (resetBtn) {
        resetBtn.addEventListener('click', function(e) {
            e.preventDefault();
            resetFilters();
        });
    }
    
    // 공지사항 항목 클릭 이벤트
    setupNoticeItemClicks();
}

/**
 * 공지사항 항목 클릭 이벤트 설정
 */
function setupNoticeItemClicks() {
    console.log('공지사항 항목 클릭 이벤트 설정');
    
    const noticeItems = document.querySelectorAll('.notice-row');
    noticeItems.forEach(item => {
        const titleLink = item.querySelector('.notice-title');
        if (titleLink) {
            titleLink.addEventListener('click', function(e) {
                console.log('공지사항 클릭:', this.href);
            });
        }
    });
}

/**
 * 필터 적용
 */
function applyFilters() {
    console.log('필터 적용');
    
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        // 검색어가 비어있으면 제거
        const searchKeyword = searchForm.querySelector('input[name="searchKeyword"]');
        if (searchKeyword && !searchKeyword.value.trim()) {
            searchKeyword.disabled = true;
        }
        
        // 체크되지 않은 체크박스 제거
        const checkboxes = searchForm.querySelectorAll('input[type="checkbox"]');
        checkboxes.forEach(checkbox => {
            if (!checkbox.checked) {
                checkbox.disabled = true;
            }
        });
        
        // 폼 제출
        searchForm.submit();
    }
}

/**
 * 필터 초기화
 */
function resetFilters() {
    console.log('필터 초기화');
    
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        // 검색어 초기화
        const searchKeyword = searchForm.querySelector('input[name="searchKeyword"]');
        if (searchKeyword) {
            searchKeyword.value = '';
        }
        
        // 검색 타입 초기화
        const searchType = searchForm.querySelector('select[name="searchType"]');
        if (searchType) {
            searchType.value = 'all';
        }
        
        // 카테고리 초기화
        const category = searchForm.querySelector('select[name="categoryId"]');
        if (category) {
            category.value = '';
        }
        
        // 상태 초기화
        const status = searchForm.querySelector('select[name="statusId"]');
        if (status) {
            status.value = '';
        }
        
        // 체크박스 초기화
        const checkboxes = searchForm.querySelectorAll('input[type="checkbox"]');
        checkboxes.forEach(checkbox => {
            checkbox.checked = false;
        });
        
        // 정렬 초기화
        const sortBy = searchForm.querySelector('select[name="sortBy"]');
        if (sortBy) {
            sortBy.value = 'cdate';
        }
        
        const sortOrder = searchForm.querySelector('select[name="sortOrder"]');
        if (sortOrder) {
            sortOrder.value = 'desc';
        }
        
        // 페이지 초기화
        const page = searchForm.querySelector('input[name="page"]');
        if (page) {
            page.value = '1';
        }
        
        // 페이지 크기 초기화
        const pageSize = searchForm.querySelector('select[name="pageSize"]');
        if (pageSize) {
            pageSize.value = '10';
        }
        
        // 폼 제출
        searchForm.submit();
    }
}

/**
 * 공지사항 삭제
 */
function deleteNotice(noticeId) {
    console.log('공지사항 삭제 요청:', noticeId);
    
    showModal({
        title: '공지사항 삭제',
        message: '정말로 이 공지사항을 삭제하시겠습니까?<br>삭제된 공지사항은 복구할 수 없습니다.',
        confirmText: '삭제',
        cancelText: '취소',
        confirmClass: 'btn--danger',
        onConfirm: function() {
            console.log('공지사항 삭제 확인:', noticeId);
            executeDeleteNotice(noticeId);
        }
    });
}

/**
 * 공지사항 삭제 실행
 */
function executeDeleteNotice(noticeId) {
    console.log('공지사항 삭제 실행:', noticeId);
    
    // 로딩 표시
    showToast('삭제 중입니다...', 'info');
    
    // AJAX 요청
    ajax.post(`/admin/notice/${noticeId}/delete`)
        .then(response => {
            console.log('삭제 성공:', response);
            
            if (response.success) {
                showToast('공지사항이 삭제되었습니다.', 'success');
                
                // 페이지 새로고침 또는 목록에서 제거
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                showToast(response.message || '삭제에 실패했습니다.', 'error');
            }
        })
        .catch(error => {
            console.error('삭제 오류:', error);
            showToast('삭제 중 오류가 발생했습니다.', 'error');
        });
}

/**
 * 페이지 이동
 */
function goToPage(page) {
    console.log('페이지 이동:', page);
    
    const pageInput = document.querySelector('input[name="page"]');
    if (pageInput) {
        pageInput.value = page;
        applyFilters();
    }
}

/**
 * 공지사항 항목 호버 효과
 */
function setupHoverEffects() {
    console.log('호버 효과 설정');
    
    const noticeRows = document.querySelectorAll('.notice-row');
    noticeRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.backgroundColor = 'var(--color-gray-50)';
        });
        
        row.addEventListener('mouseleave', function() {
            this.style.backgroundColor = '';
        });
    });
}

/**
 * 중요/고정 공지사항 강조 효과
 */
function setupEmphasisEffects() {
    console.log('강조 효과 설정');
    
    const importantRows = document.querySelectorAll('.notice-row');
    importantRows.forEach(row => {
        const importantFlag = row.querySelector('.flag.important');
        const fixedFlag = row.querySelector('.flag.fixed');
        
        if (importantFlag) {
            const title = row.querySelector('.notice-title');
            if (title) {
                title.style.fontWeight = '600';
            }
        }
        
        if (fixedFlag) {
            const title = row.querySelector('.notice-title');
            if (title) {
                title.style.fontWeight = '500';
            }
        }
    });
}

/**
 * 검색 결과 하이라이트
 */
function highlightSearchResults() {
    console.log('검색 결과 하이라이트');
    
    const searchKeyword = document.querySelector('input[name="searchKeyword"]');
    if (searchKeyword && searchKeyword.value.trim()) {
        const keyword = searchKeyword.value.trim();
        const titleElements = document.querySelectorAll('.notice-title');
        
        titleElements.forEach(element => {
            const text = element.textContent;
            const highlightedText = text.replace(
                new RegExp(keyword, 'gi'),
                match => `<mark class="highlight">${match}</mark>`
            );
            element.innerHTML = highlightedText;
        });
    }
}

/**
 * 키보드 단축키 설정
 */
function setupKeyboardShortcuts() {
    console.log('키보드 단축키 설정');
    
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + F: 검색 필드 포커스
        if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
            e.preventDefault();
            const searchInput = document.querySelector('input[name="searchKeyword"]');
            if (searchInput) {
                searchInput.focus();
                searchInput.select();
            }
        }
        
        // ESC: 검색 필드 초기화
        if (e.key === 'Escape') {
            const searchInput = document.querySelector('input[name="searchKeyword"]');
            if (searchInput && document.activeElement === searchInput) {
                searchInput.value = '';
                searchInput.blur();
            }
        }
        
        // Ctrl/Cmd + N: 새 공지 작성
        if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
            e.preventDefault();
            window.location.href = '/admin/notice/write';
        }
    });
}

/**
 * 페이지 로드 완료 후 추가 초기화
 */
window.addEventListener('load', function() {
    console.log('페이지 로드 완료 - 추가 초기화');
    
    // 호버 효과 설정
    setupHoverEffects();
    
    // 강조 효과 설정
    setupEmphasisEffects();
    
    // 검색 결과 하이라이트
    highlightSearchResults();
    
    // 키보드 단축키 설정
    setupKeyboardShortcuts();
    
    // 페이지 로드 완료 로그
    console.log('관리자용 공지사항 목록 페이지 초기화 완료');
});

/**
 * URL 파라미터에서 검색 조건 복원
 */
function restoreSearchConditions() {
    console.log('검색 조건 복원');
    
    const urlParams = new URLSearchParams(window.location.search);
    
    // 검색어 복원
    const searchKeyword = urlParams.get('searchKeyword');
    if (searchKeyword) {
        const searchInput = document.querySelector('input[name="searchKeyword"]');
        if (searchInput) {
            searchInput.value = searchKeyword;
        }
    }
    
    // 검색 타입 복원
    const searchType = urlParams.get('searchType');
    if (searchType) {
        const searchTypeSelect = document.querySelector('select[name="searchType"]');
        if (searchTypeSelect) {
            searchTypeSelect.value = searchType;
        }
    }
    
    // 카테고리 복원
    const categoryId = urlParams.get('categoryId');
    if (categoryId) {
        const categorySelect = document.querySelector('select[name="categoryId"]');
        if (categorySelect) {
            categorySelect.value = categoryId;
        }
    }
    
    // 상태 복원
    const statusId = urlParams.get('statusId');
    if (statusId) {
        const statusSelect = document.querySelector('select[name="statusId"]');
        if (statusSelect) {
            statusSelect.value = statusId;
        }
    }
    
    // 체크박스 복원
    const isImportant = urlParams.get('isImportant');
    if (isImportant) {
        const importantCheckbox = document.querySelector('input[name="isImportant"]');
        if (importantCheckbox) {
            importantCheckbox.checked = true;
        }
    }
    
    const isFixed = urlParams.get('isFixed');
    if (isFixed) {
        const fixedCheckbox = document.querySelector('input[name="isFixed"]');
        if (fixedCheckbox) {
            fixedCheckbox.checked = true;
        }
    }
}

// 페이지 로드 시 검색 조건 복원
document.addEventListener('DOMContentLoaded', function() {
    restoreSearchConditions();
});

// 전역 함수로 노출 (HTML에서 직접 호출 가능)
window.deleteNotice = deleteNotice;
window.goToPage = goToPage;
window.resetFilters = resetFilters;
window.applyFilters = applyFilters;
