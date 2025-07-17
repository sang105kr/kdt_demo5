// 프로필 사진 관리 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('profileImage');
    const previewSection = document.querySelector('.preview-section');
    const imagePreview = document.getElementById('imagePreview');
    
    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            
            if (file) {
                // 파일 타입 검증
                if (!file.type.startsWith('image/')) {
                    alert('이미지 파일만 선택할 수 있습니다.');
                    fileInput.value = '';
                    previewSection.style.display = 'none';
                    return;
                }
                
                // 파일 크기 검증 (5MB)
                if (file.size > 5 * 1024 * 1024) {
                    alert('파일 크기는 5MB 이하여야 합니다.');
                    fileInput.value = '';
                    previewSection.style.display = 'none';
                    return;
                }
                
                // 미리보기 표시
                const reader = new FileReader();
                reader.onload = function(e) {
                    imagePreview.src = e.target.result;
                    previewSection.style.display = 'block';
                };
                reader.readAsDataURL(file);
            } else {
                previewSection.style.display = 'none';
            }
        });
    }
    
    // 파일 드래그 앤 드롭 기능
    const dragDropForm = document.querySelector('.upload-form');
    if (dragDropForm) {
        dragDropForm.addEventListener('dragover', function(e) {
            e.preventDefault();
            dragDropForm.classList.add('drag-over');
        });
        
        dragDropForm.addEventListener('dragleave', function(e) {
            e.preventDefault();
            dragDropForm.classList.remove('drag-over');
        });
        
        dragDropForm.addEventListener('drop', function(e) {
            e.preventDefault();
            dragDropForm.classList.remove('drag-over');
            
            const files = e.dataTransfer.files;
            if (files.length > 0) {
                const file = files[0];
                
                // 파일 타입 검증
                if (!file.type.startsWith('image/')) {
                    alert('이미지 파일만 선택할 수 있습니다.');
                    return;
                }
                
                // 파일 크기 검증
                if (file.size > 5 * 1024 * 1024) {
                    alert('파일 크기는 5MB 이하여야 합니다.');
                    return;
                }
                
                // 파일 입력 필드에 설정
                fileInput.files = files;
                
                // 미리보기 표시
                const reader = new FileReader();
                reader.onload = function(e) {
                    imagePreview.src = e.target.result;
                    previewSection.style.display = 'block';
                };
                reader.readAsDataURL(file);
            }
        });
    }
    
    // 삭제 확인
    const deleteForm = document.querySelector('form[action*="delete"]');
    if (deleteForm) {
        deleteForm.addEventListener('submit', function(e) {
            if (!confirm('프로필 사진을 삭제하시겠습니까?')) {
                e.preventDefault();
            } else {
                // 삭제 후 top 메뉴 프로필 이미지 새로고침
                setTimeout(function() {
                    if (window.refreshProfileImage) {
                        window.refreshProfileImage();
                    }
                }, 1000);
            }
        });
    }
    
    // 업로드 성공 후 top 메뉴 프로필 이미지 새로고침
    const uploadForm = document.querySelector('form[action*="upload"]');
    if (uploadForm) {
        uploadForm.addEventListener('submit', function() {
            // 업로드 성공 후 이미지 새로고침
            setTimeout(function() {
                if (window.refreshProfileImage) {
                    window.refreshProfileImage();
                }
            }, 1000);
        });
    }
    
    // 이미지 로드 에러 처리
    const profileImages = document.querySelectorAll('.profile-img');
    profileImages.forEach(function(img) {
        img.addEventListener('error', function() {
            // 이미지 로드 실패 시 기본 아바타로 대체
            this.style.display = 'none';
            const parent = this.parentElement;
            if (parent) {
                parent.innerHTML = `
                    <div class="default-avatar">
                        <span class="avatar-text">?</span>
                    </div>
                    <p>이미지를 불러올 수 없습니다.</p>
                `;
            }
        });
    });
});

// CSS 추가 (드래그 앤 드롭 스타일)
const style = document.createElement('style');
style.textContent = `
    .upload-form.drag-over {
        border: 2px dashed #667eea;
        background-color: rgba(102, 126, 234, 0.1);
    }
    
    .upload-form {
        transition: all 0.3s ease;
    }
`;
document.head.appendChild(style); 