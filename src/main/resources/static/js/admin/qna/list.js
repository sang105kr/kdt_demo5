/**
 * 관리자 Q&A 목록 페이지 JavaScript
 */

// Q&A 삭제 함수
function deleteQna(qnaId) {
    if (!confirm('정말로 이 Q&A를 삭제하시겠습니까?')) {
        return;
    }

    fetch(`/admin/qna/${qnaId}/delete`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }
    })
    .then(response => response.text())
    .then(result => {
        if (result === 'success') {
            alert('Q&A가 성공적으로 삭제되었습니다.');
            location.reload();
        } else if (result === 'unauthorized') {
            alert('권한이 없습니다.');
        } else if (result === 'failed') {
            alert('삭제에 실패했습니다.');
        } else {
            alert('오류가 발생했습니다.');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('오류가 발생했습니다.');
    });
}

// Q&A 상태 변경 함수
function updateQnaStatus(qnaId, status) {
    fetch(`/admin/qna/${qnaId}/status`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `status=${status}`
    })
    .then(response => response.text())
    .then(result => {
        if (result === 'success') {
            alert('상태가 성공적으로 변경되었습니다.');
            location.reload();
        } else if (result === 'unauthorized') {
            alert('권한이 없습니다.');
        } else if (result === 'failed') {
            alert('상태 변경에 실패했습니다.');
        } else {
            alert('오류가 발생했습니다.');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('오류가 발생했습니다.');
    });
}

// 검색 폼 제출 시 빈 값 처리
document.addEventListener('DOMContentLoaded', function() {
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            const category = document.getElementById('category');
            const status = document.getElementById('status');
            const keyword = document.getElementById('keyword');
            
            // 빈 값인 경우 파라미터에서 제거
            if (category && category.value === '') {
                category.disabled = true;
            }
            if (status && status.value === '') {
                status.disabled = true;
            }
            if (keyword && keyword.value.trim() === '') {
                keyword.disabled = true;
            }
        });
    }

    // 테이블 행 클릭 이벤트 (상세보기로 이동)
    const tableRows = document.querySelectorAll('.qna-table tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('click', function(e) {
            // 버튼 클릭 시에는 상세보기로 이동하지 않음
            if (e.target.tagName === 'BUTTON' || e.target.tagName === 'A' || e.target.closest('button') || e.target.closest('a')) {
                return;
            }
            
            const qnaId = this.querySelector('td:first-child').textContent;
            window.location.href = `/admin/qna/${qnaId}`;
        });
    });

    // 테이블 행에 커서 포인터 스타일 적용
    tableRows.forEach(row => {
        row.style.cursor = 'pointer';
    });
});

// 페이지 로드 완료 후 실행
window.addEventListener('load', function() {
    console.log('관리자 Q&A 목록 페이지 로드 완료');
    
    // 통계 카드 애니메이션
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach((card, index) => {
        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
});
