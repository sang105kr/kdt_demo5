// 프로필 사진 관리 JavaScript
console.log('프로필 이미지 폼 JavaScript 로드됨'); // 디버깅 로그
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM 로드 완료'); // 디버깅 로그
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
                    
                    // 미리보기 이미지 로드 완료 후 부드러운 애니메이션
                    imagePreview.onload = function() {
                        previewSection.style.opacity = '0';
                        previewSection.style.transform = 'scale(0.9)';
                        previewSection.style.transition = 'all 0.3s ease';
                        
                        setTimeout(() => {
                            previewSection.style.opacity = '1';
                            previewSection.style.transform = 'scale(1)';
                        }, 50);
                    };
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
                    
                    // 미리보기 이미지 로드 완료 후 부드러운 애니메이션
                    imagePreview.onload = function() {
                        previewSection.style.opacity = '0';
                        previewSection.style.transform = 'scale(0.9)';
                        previewSection.style.transition = 'all 0.3s ease';
                        
                        setTimeout(() => {
                            previewSection.style.opacity = '1';
                            previewSection.style.transform = 'scale(1)';
                        }, 50);
                    };
                };
                reader.readAsDataURL(file);
            }
        });
    }
    
    // AJAX를 사용한 프로필 이미지 삭제
    const deleteForm = document.getElementById('profileDeleteForm');
    if (deleteForm) {
        deleteForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formElement = this;
            
            showModal({
                title: '프로필 사진 삭제',
                message: '프로필 사진을 삭제하시겠습니까?',
                onConfirm: () => {
                    const submitBtn = formElement.querySelector('button[type="submit"]');
                    const originalText = submitBtn.textContent;
                    
                    // 버튼 비활성화
                    submitBtn.disabled = true;
                    submitBtn.textContent = '삭제 중...';
                    
                    // 실제 삭제 로직 실행 (다음 코드 블록)
                    executeProfileImageDelete(formElement, submitBtn, originalText);
                },
                onCancel: () => {
                    // 취소 시 아무것도 하지 않음
                }
            });
        });
    }
    
    // AJAX를 사용한 프로필 이미지 업로드
    const uploadForm = document.getElementById('profileUploadForm');
    console.log('업로드 폼 찾기:', uploadForm); // 디버깅 로그
    
    if (uploadForm) {
        uploadForm.addEventListener('submit', function(e) {
            console.log('폼 제출 이벤트 발생'); // 디버깅 로그
            e.preventDefault();
            
            const formData = new FormData(this);
            const submitBtn = this.querySelector('button[type="submit"]');
            const originalText = submitBtn.textContent;
            const fileInput = this.querySelector('input[type="file"]');
            
            // 파일 선택 확인
            if (!fileInput.files[0]) {
                alert('업로드할 파일을 선택해주세요.');
                return;
            }
            
            // 버튼 비활성화
            submitBtn.disabled = true;
            submitBtn.textContent = '업로드 중...';
            
            console.log('AJAX 요청 시작:', this.action); // 디버깅 로그
            fetch(this.action, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                console.log('응답 상태:', response.status); // 디버깅 로그
                if (!response.ok) {
                    throw new Error('업로드 실패');
                }
                return response.text();
            })
            .then(html => {
                console.log('응답 HTML 받음, 길이:', html.length); // 디버깅 로그
                // 응답 HTML을 파싱하여 현재 프로필 이미지 섹션 업데이트
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newProfileSection = doc.querySelector('.current-profile-section');
                const currentProfileSection = document.querySelector('.current-profile-section');
                
                console.log('새 프로필 섹션:', newProfileSection); // 디버깅 로그
                console.log('현재 프로필 섹션:', currentProfileSection); // 디버깅 로그
                
                if (newProfileSection && currentProfileSection) {
                    console.log('프로필 섹션 업데이트 중...'); // 디버깅 로그
                    currentProfileSection.innerHTML = newProfileSection.innerHTML;
                    console.log('프로필 섹션 업데이트 완료'); // 디버깅 로그
                }
                
                // 메시지 표시
                const messageElement = doc.querySelector('.message');
                if (messageElement) {
                    const messageContainer = document.querySelector('.message') || 
                                           document.createElement('div');
                    messageContainer.className = messageElement.className;
                    messageContainer.innerHTML = messageElement.innerHTML;
                    
                    if (!document.querySelector('.message')) {
                        const container = document.querySelector('.profile-image-container');
                        container.insertBefore(messageContainer, container.firstChild);
                    }
                    
                    // 3초 후 메시지 제거
                    setTimeout(() => {
                        messageContainer.remove();
                    }, 3000);
                }
                
                // 폼 초기화
                fileInput.value = '';
                const previewSection = document.querySelector('.preview-section');
                if (previewSection) {
                    previewSection.style.display = 'none';
                }
                
                // top 메뉴 프로필 이미지 새로고침
                if (window.refreshProfileImage) {
                    window.refreshProfileImage();
                }
            })
            .catch(error => {
                console.error('업로드 오류:', error);
                alert('업로드 중 오류가 발생했습니다.');
            })
            .finally(() => {
                // 버튼 복원
                submitBtn.disabled = false;
                submitBtn.textContent = originalText;
            });
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

// 전역 업로드 핸들러 함수
window.handleUpload = function(event) {
    console.log('전역 업로드 핸들러 호출됨'); // 디버깅 로그
    event.preventDefault();
    
    const form = document.getElementById('profileUploadForm');
    if (!form) {
        console.error('업로드 폼을 찾을 수 없습니다.');
        return false;
    }
    
    const formData = new FormData(form);
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    const fileInput = form.querySelector('input[type="file"]');
    
    // 파일 선택 확인
    if (!fileInput.files[0]) {
        alert('업로드할 파일을 선택해주세요.');
        return false;
    }
    
    // 버튼 비활성화
    submitBtn.disabled = true;
    submitBtn.textContent = '업로드 중...';
    
    console.log('전역 핸들러에서 AJAX 요청 시작:', form.action); // 디버깅 로그
    fetch(form.action, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        console.log('전역 핸들러 응답 상태:', response.status); // 디버깅 로그
        if (!response.ok) {
            throw new Error('업로드 실패');
        }
        return response.text();
    })
    .then(html => {
        console.log('전역 핸들러 응답 HTML 받음, 길이:', html.length); // 디버깅 로그
        // 응답 HTML을 파싱하여 현재 프로필 이미지 섹션 업데이트
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const newProfileSection = doc.querySelector('.current-profile-section');
        const currentProfileSection = document.querySelector('.current-profile-section');
        
        console.log('전역 핸들러 새 프로필 섹션:', newProfileSection); // 디버깅 로그
        console.log('전역 핸들러 현재 프로필 섹션:', currentProfileSection); // 디버깅 로그
        
        if (newProfileSection && currentProfileSection) {
            console.log('전역 핸들러 프로필 섹션 업데이트 중...'); // 디버깅 로그
            currentProfileSection.innerHTML = newProfileSection.innerHTML;
            console.log('전역 핸들러 프로필 섹션 업데이트 완료'); // 디버깅 로그
        }
        
        // 메시지 표시
        const messageElement = doc.querySelector('.message');
        if (messageElement) {
            const messageContainer = document.querySelector('.message') || 
                                   document.createElement('div');
            messageContainer.className = messageElement.className;
            messageContainer.innerHTML = messageElement.innerHTML;
            
            if (!document.querySelector('.message')) {
                const container = document.querySelector('.profile-image-container');
                container.insertBefore(messageContainer, container.firstChild);
            }
            
            // 3초 후 메시지 제거
            setTimeout(() => {
                messageContainer.remove();
            }, 3000);
        }
        
        // 폼 초기화
        fileInput.value = '';
        const previewSection = document.querySelector('.preview-section');
        if (previewSection) {
            previewSection.style.display = 'none';
        }
        
        // top 메뉴 프로필 이미지 새로고침
        if (window.refreshProfileImage) {
            window.refreshProfileImage();
        }
        
        // 강제로 모든 프로필 이미지 새로고침
        setTimeout(() => {
            const allProfileImages = document.querySelectorAll('.profile-img, .dropdown-img');
            allProfileImages.forEach(function(img) {
                if (img.src.includes('/member/profile-image/view')) {
                    // 기존 파라미터를 유지하면서 타임스탬프만 추가
                    const url = new URL(img.src, window.location.origin);
                    url.searchParams.set('t', new Date().getTime());
                    img.src = url.toString();
                }
            });
        }, 500);
    })
    .catch(error => {
        console.error('전역 핸들러 업로드 오류:', error);
        alert('업로드 중 오류가 발생했습니다.');
    })
    .finally(() => {
        // 버튼 복원
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    });
    
    return false;
};

// 프로필 이미지 삭제 실행 함수
function executeProfileImageDelete(formElement, submitBtn, originalText) {
    fetch(formElement.action, {
        method: 'POST'
    })
    .then(response => response.text())
    .then(html => {
        // 응답 HTML을 파싱하여 현재 프로필 이미지 섹션 업데이트
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const newProfileSection = doc.querySelector('.current-profile-section');
        const currentProfileSection = document.querySelector('.current-profile-section');
        
        if (newProfileSection && currentProfileSection) {
            currentProfileSection.innerHTML = newProfileSection.innerHTML;
        }
        
        // 메시지 표시
        const messageElement = doc.querySelector('.message');
        if (messageElement) {
            const messageContainer = document.querySelector('.message') || 
                                   document.createElement('div');
            messageContainer.className = messageElement.className;
            messageContainer.innerHTML = messageElement.innerHTML;
            
            if (!document.querySelector('.message')) {
                const container = document.querySelector('.profile-image-container');
                container.insertBefore(messageContainer, container.firstChild);
            }
            
            // 3초 후 메시지 제거
            setTimeout(() => {
                messageContainer.remove();
            }, 3000);
        }
        
        // top 메뉴 프로필 이미지 새로고침
        if (window.refreshProfileImage) {
            window.refreshProfileImage();
        }
    })
    .catch(error => {
        console.error('삭제 오류:', error);
        alert('삭제 중 오류가 발생했습니다.');
    })
    .finally(() => {
        // 버튼 복원
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    });
} 