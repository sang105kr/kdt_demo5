document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    const submitBtn = document.querySelector('button[type="submit"]');
    const resetBtn = document.querySelector('button[type="reset"]');
    const listBtn = document.querySelector('a[href="/board"]');
    
    // 폼 제출 이벤트 처리
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // 폼 데이터 수집
            const formData = new FormData(form);
            const bcategory = formData.get('bcategory');
            const title = formData.get('title');
            const bcontent = formData.get('bcontent');
            
            // 클라이언트 사이드 검증
            if (!bcategory || bcategory.trim() === '') {
                alert('카테고리를 선택해주세요.');
                return;
            }
            
            if (!title || title.trim() === '') {
                alert('제목을 입력해주세요.');
                return;
            }
            
            if (!bcontent || bcontent.trim() === '') {
                alert('내용을 입력해주세요.');
                return;
            }
            
            // 제출 버튼 비활성화
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = '등록 중...';
            }
            
            // 폼 제출
            form.submit();
        });
    }
    
    // 취소 버튼 이벤트
    if (resetBtn) {
        resetBtn.addEventListener('click', function(e) {
            e.preventDefault();
            if (confirm('입력한 내용을 모두 지우시겠습니까?')) {
                form.reset();
            }
        });
    }
    
    // 목록 버튼 이벤트
    if (listBtn) {
        listBtn.addEventListener('click', function(e) {
            e.preventDefault();
            if (confirm('목록으로 이동하시겠습니까? 입력한 내용이 저장되지 않습니다.')) {
                location.href = '/board';
            }
        });
    }
    
    // 실시간 폼 검증
    const titleInput = document.getElementById('title');
    const contentTextarea = document.getElementById('bcontent');
    const categorySelect = document.getElementById('bcategory');
    
    if (titleInput) {
        titleInput.addEventListener('input', function() {
            const value = this.value.trim();
            if (value.length > 100) {
                this.value = value.substring(0, 100);
                alert('제목은 100자를 초과할 수 없습니다.');
            }
        });
    }
    
    if (contentTextarea) {
        contentTextarea.addEventListener('input', function() {
            const value = this.value;
            // 내용 길이 제한 (예: 4000자)
            if (value.length > 4000) {
                this.value = value.substring(0, 4000);
                alert('내용은 4000자를 초과할 수 없습니다.');
            }
        });
    }
    
    // 카테고리 선택 시 유효성 검사
    if (categorySelect) {
        categorySelect.addEventListener('change', function() {
            const selectedValue = this.value;
            if (selectedValue === '') {
                this.classList.add('error');
            } else {
                this.classList.remove('error');
            }
        });
    }
});