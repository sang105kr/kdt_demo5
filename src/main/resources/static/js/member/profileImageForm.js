/**
 * 프로필 이미지 관리 JavaScript
 * 파일 업로드, 미리보기, 드래그 앤 드롭 기능 제공
 */

class ProfileImageManager {
    constructor() {
        this.fileInput = null;
        this.fileInputWrapper = null;
        this.fileInputPlaceholder = null;
        this.fileInputInfo = null;
        this.previewSection = null;
        this.previewImage = null;
        this.uploadForm = null;
        this.uploadBtn = null;
        this.deleteForm = null;
        
        this.maxFileSize = 10 * 1024 * 1024; // 10MB
        this.allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
        
        this.init();
    }
    
    /**
     * 초기화
     */
    init() {
        this.initElements();
        this.bindEvents();
        console.log('프로필 이미지 관리자 초기화 완료');
    }
    
    /**
     * DOM 요소 초기화
     */
    initElements() {
        this.fileInput = document.getElementById('profileImage');
        this.fileInputWrapper = document.querySelector('.file-input-wrapper');
        this.fileInputPlaceholder = document.querySelector('.file-input-placeholder');
        this.fileInputInfo = document.querySelector('.file-input-info');
        this.previewSection = document.querySelector('.preview-section');
        this.previewImage = document.getElementById('previewImage');
        this.uploadForm = document.querySelector('.upload-form');
        this.uploadBtn = document.querySelector('.btn-upload');
        this.deleteForm = document.querySelector('.delete-form');
    }
    
    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        if (this.fileInput) {
            this.fileInput.addEventListener('change', (e) => this.handleFileSelect(e));
        }
        
        if (this.fileInputWrapper) {
            // 드래그 앤 드롭 이벤트
            this.fileInputWrapper.addEventListener('dragover', (e) => this.handleDragOver(e));
            this.fileInputWrapper.addEventListener('dragleave', (e) => this.handleDragLeave(e));
            this.fileInputWrapper.addEventListener('drop', (e) => this.handleDrop(e));
            
            // 클릭 이벤트 - file input의 클릭과 중복되지 않도록 수정
            this.fileInputWrapper.addEventListener('click', (e) => {
                // 실제 파일 input이 클릭된 경우 중복 방지
                if (e.target === this.fileInput) {
                    return;
                }
                if (this.fileInput) {
                    this.fileInput.click();
                }
            });
        }
        
        if (this.uploadForm) {
            this.uploadForm.addEventListener('submit', (e) => this.handleFormSubmit(e));
        }
        
        if (this.deleteForm) {
            this.deleteForm.addEventListener('submit', (e) => this.handleDeleteSubmit(e));
        }
    }
    
    /**
     * 파일 선택 처리
     */
    handleFileSelect(event) {
        const file = event.target.files[0];
        this.processFile(file);
    }
    
    /**
     * 드래그 오버 처리
     */
    handleDragOver(event) {
        event.preventDefault();
        event.stopPropagation();
        this.fileInputWrapper.classList.add('drag-over');
    }
    
    /**
     * 드래그 떠남 처리
     */
    handleDragLeave(event) {
        event.preventDefault();
        event.stopPropagation();
        this.fileInputWrapper.classList.remove('drag-over');
    }
    
    /**
     * 드롭 처리
     */
    handleDrop(event) {
        event.preventDefault();
        event.stopPropagation();
        this.fileInputWrapper.classList.remove('drag-over');
        
        const files = event.dataTransfer.files;
        if (files.length > 0) {
            const file = files[0];
            
            // 파일 입력 필드에 설정
            const dataTransfer = new DataTransfer();
            dataTransfer.items.add(file);
            this.fileInput.files = dataTransfer.files;
            
            this.processFile(file);
        }
    }
    
    /**
     * 파일 처리
     */
    processFile(file) {
        if (!file) {
            this.resetFileDisplay();
            return;
        }
        
        // 파일 유효성 검사
        const validation = this.validateFile(file);
        if (!validation.valid) {
            this.showError(validation.message);
            this.resetFileDisplay();
            return;
        }
        
        // 파일 정보 표시
        this.updateFileDisplay(file);
        
        // 미리보기 생성
        this.createPreview(file);
    }
    
    /**
     * 파일 유효성 검사
     */
    validateFile(file) {
        // 파일 타입 검사
        if (!this.allowedTypes.includes(file.type.toLowerCase())) {
            return {
                valid: false,
                message: '지원하지 않는 파일 형식입니다. (지원 형식: JPG, PNG, GIF, WEBP)'
            };
        }
        
        // 파일 크기 검사
        if (file.size > this.maxFileSize) {
            return {
                valid: false,
                message: '파일 크기는 10MB를 초과할 수 없습니다.'
            };
        }
        
        return { valid: true };
    }
    
    /**
     * 파일 표시 업데이트
     */
    updateFileDisplay(file) {
        if (this.fileInputPlaceholder) {
            this.fileInputPlaceholder.style.display = 'none';
        }
        
        if (this.fileInputInfo) {
            this.fileInputInfo.style.display = 'flex';
            
            const fileName = this.fileInputInfo.querySelector('.file-name');
            const fileSize = this.fileInputInfo.querySelector('.file-size');
            
            if (fileName) {
                fileName.textContent = file.name;
            }
            
            if (fileSize) {
                fileSize.textContent = this.formatFileSize(file.size);
            }
        }
    }
    
    /**
     * 파일 표시 초기화
     */
    resetFileDisplay() {
        if (this.fileInputPlaceholder) {
            this.fileInputPlaceholder.style.display = 'block';
        }
        
        if (this.fileInputInfo) {
            this.fileInputInfo.style.display = 'none';
        }
        
        this.hidePreview();
        
        if (this.fileInput) {
            this.fileInput.value = '';
        }
    }
    
    /**
     * 미리보기 생성
     */
    createPreview(file) {
        if (!this.previewImage || !this.previewSection) {
            return;
        }
        
        const reader = new FileReader();
        reader.onload = (e) => {
            this.previewImage.src = e.target.result;
            this.showPreview();
        };
        reader.readAsDataURL(file);
    }
    
    /**
     * 미리보기 표시
     */
    showPreview() {
        if (this.previewSection) {
            this.previewSection.style.display = 'block';
            // 애니메이션을 위한 약간의 지연
            setTimeout(() => {
                this.previewSection.style.opacity = '1';
            }, 10);
        }
    }
    
    /**
     * 미리보기 숨김
     */
    hidePreview() {
        if (this.previewSection) {
            this.previewSection.style.display = 'none';
            this.previewSection.style.opacity = '0';
        }
    }
    
    /**
     * 폼 제출 처리 - AJAX로 변경
     */
    async handleFormSubmit(event) {
        event.preventDefault(); // 기본 제출 방지
        
        // 파일이 선택되지 않은 경우 체크
        if (!this.fileInput.files[0]) {
            this.showError('업로드할 파일을 선택해주세요.');
            return false;
        }
        
        // 버튼 로딩 상태
        this.setLoadingState(this.uploadBtn, true, '업로드 중...');
        
        // FormData 생성
        const formData = new FormData();
        formData.append('profileImage', this.fileInput.files[0]);
        
        try {
            // FormData는 fetch로 직접 전송 (ajax.post는 JSON용)
            const response = await fetch('/api/member/mypage/profile-image', {
                method: 'POST',
                body: formData
            });
            
            const result = await response.json();
            
            if (result.code === '00') { // SUCCESS
                const data = result.data;
                console.log('프로필 이미지 업로드 성공:', data);
                this.showSuccess(data.message || '프로필 이미지가 업로드되었습니다.');
                
                // 이미지 새로고침 전에 약간의 지연
                setTimeout(() => {
                    this.refreshProfileImage();
                    
                    // 추가: 현재 이미지 표시 영역 강제 새로고침
                    this.forceRefreshCurrentImage();
                }, 300);
                
                this.resetForm();
                
                // 파일 정보 표시 (선택사항)
                if (data.fileInfo) {
                    console.log('업로드된 파일 정보:', data.fileInfo);
                }
            } else {
                console.error('프로필 이미지 업로드 실패:', result);
                this.showError(result.message || '업로드에 실패했습니다.');
            }
        } catch (error) {
            console.error('업로드 오류:', error);
            this.showError('업로드 중 오류가 발생했습니다.');
        } finally {
            this.setLoadingState(this.uploadBtn, false);
        }
    }
    
    /**
     * 삭제 폼 제출 처리 - AJAX로 변경
     */
    async handleDeleteSubmit(event) {
        event.preventDefault(); // 기본 제출 방지
        
        if (!confirm('프로필 이미지를 삭제하시겠습니까?')) {
            return false;
        }
        
        const deleteBtn = this.deleteForm.querySelector('.btn-delete');
        this.setLoadingState(deleteBtn, true, '삭제 중...');
        
        // AJAX 요청 - common.js의 ajax 객체 사용
        try {
            const response = await ajax.delete('/api/member/mypage/profile-image');
            
            if (response.code === '00') { // SUCCESS
                const data = response.data;
                this.showSuccess(data.message || '프로필 이미지가 삭제되었습니다.');
                this.refreshProfileImage();
                this.resetForm();
            } else {
                this.showError(response.message || '삭제에 실패했습니다.');
            }
        } catch (error) {
            console.error('삭제 오류:', error);
            this.showError('삭제 중 오류가 발생했습니다.');
        } finally {
            this.setLoadingState(deleteBtn, false);
        }
    }
    
    /**
     * 버튼 로딩 상태 설정
     */
    setLoadingState(button, isLoading, loadingText) {
        if (!button) return;
        
        if (isLoading) {
            button.disabled = true;
            button.classList.add('loading');
            if (loadingText) {
                button.dataset.originalText = button.textContent;
                button.innerHTML = `<i class="fas fa-spinner fa-spin"></i> ${loadingText}`;
            }
        } else {
            button.disabled = false;
            button.classList.remove('loading');
            if (button.dataset.originalText) {
                button.textContent = button.dataset.originalText;
                delete button.dataset.originalText;
            }
        }
    }
    
    /**
     * 파일 크기 포맷팅
     */
    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    }
    
    /**
     * 에러 메시지 표시
     */
    showError(message) {
        // 기존 에러 메시지 제거
        this.removeErrorMessages();
        
        // 새 에러 메시지 생성
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-error error-message-temp';
        errorDiv.innerHTML = `
            <i class="fas fa-exclamation-circle"></i>
            <span>${message}</span>
        `;
        
        // 파일 입력 영역 다음에 삽입
        const formGroup = this.fileInputWrapper.closest('.form-group');
        if (formGroup) {
            formGroup.appendChild(errorDiv);
        }
        
        // 3초 후 자동 제거
        setTimeout(() => {
            this.removeErrorMessages();
        }, 3000);
    }
    
    /**
     * 임시 에러 메시지 제거
     */
    removeErrorMessages() {
        const tempErrors = document.querySelectorAll('.error-message-temp');
        tempErrors.forEach(error => error.remove());
    }
    
    /**
     * 성공 메시지 표시
     */
    showSuccess(message) {
        // 기존 메시지 제거
        this.removeErrorMessages();
        
        // 새 성공 메시지 생성
        const successDiv = document.createElement('div');
        successDiv.className = 'alert alert-success success-message-temp';
        successDiv.innerHTML = `
            <i class="fas fa-check-circle"></i>
            <span>${message}</span>
        `;
        
        // 파일 입력 영역 다음에 삽입
        const formGroup = this.fileInputWrapper.closest('.form-group');
        if (formGroup) {
            formGroup.appendChild(successDiv);
        }
        
        // 3초 후 자동 제거
        setTimeout(() => {
            this.removeErrorMessages();
        }, 3000);
    }
    
    /**
     * 프로필 이미지 새로고침
     */
    refreshProfileImage() {
        console.log('프로필 이미지 새로고침 시작');
        
        // 약간의 지연을 두어 서버 처리가 완료되도록 함
        setTimeout(() => {
            // 현재 프로필 이미지 새로고침 - 더 구체적인 선택자 사용
            const currentImages = document.querySelectorAll('.profile-image, .current-image-card img, .image-display img');
            console.log('찾은 이미지 요소들:', currentImages.length);
            
            currentImages.forEach((img, index) => {
                console.log(`이미지 ${index + 1}:`, img.src, img.className);
                
                if (img.src.includes('/member/mypage/profile-image/view/')) {
                    const url = new URL(img.src, window.location.origin);
                    // 캐시 무효화를 위한 타임스탬프 추가
                    url.searchParams.set('t', new Date().getTime());
                    url.searchParams.set('v', Math.random().toString(36).substr(2, 9));
                    img.src = url.toString();
                    console.log('프로필 이미지 새로고침:', url.toString());
                }
            });
            
            // 상단 메뉴 프로필 이미지도 새로고침
            this.refreshTopMenuProfileImage();
            
            // 페이지 전체 새로고침 (최후의 수단)
            console.log('이미지 새로고침 완료');
        }, 1000); // 1초 지연으로 증가
    }
    
    /**
     * 폼 초기화
     */
    resetForm() {
        // 파일 입력 초기화
        if (this.fileInput) {
            this.fileInput.value = '';
        }
        
        // 파일 표시 초기화
        this.resetFileDisplay();
        
        // 미리보기 숨기기
        this.hidePreview();
        
        // 에러 메시지 제거
        this.removeErrorMessages();
    }

    /**
     * 현재 이미지 표시 영역 강제 새로고침
     */
    forceRefreshCurrentImage() {
        console.log('현재 이미지 강제 새로고침 시작');
        
        // 현재 이미지 컨테이너 찾기
        const imageDisplay = document.querySelector('.image-display');
        if (!imageDisplay) {
            console.log('이미지 디스플레이 영역을 찾을 수 없습니다.');
            return;
        }
        
        // 현재 이미지 찾기
        const currentImage = imageDisplay.querySelector('.profile-image');
        if (currentImage) {
            console.log('현재 이미지 요소 발견:', currentImage.src);
            
            // 이미지 강제 새로고침
            const originalSrc = currentImage.src;
            currentImage.src = '';
            currentImage.style.opacity = '0';
            
            setTimeout(() => {
                const url = new URL(originalSrc, window.location.origin);
                url.searchParams.set('t', new Date().getTime());
                url.searchParams.set('v', Math.random().toString(36).substr(2, 9));
                currentImage.src = url.toString();
                currentImage.style.opacity = '1';
                console.log('이미지 강제 새로고침 완료:', url.toString());
            }, 100);
        } else {
            console.log('현재 이미지 요소를 찾을 수 없습니다. 이미지가 없을 수 있습니다.');
            
            // 이미지가 없는 경우, 서버에서 최신 상태를 확인하기 위해 페이지 일부 새로고침
            this.refreshImageDisplaySection();
        }
    }
    
    /**
     * 이미지 디스플레이 섹션 새로고침
     */
    refreshImageDisplaySection() {
        console.log('이미지 디스플레이 섹션 새로고침 시도');
        
        // AJAX로 현재 페이지의 이미지 섹션만 새로고침
        fetch(window.location.href)
            .then(response => response.text())
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newImageDisplay = doc.querySelector('.image-display');
                const currentImageDisplay = document.querySelector('.image-display');
                
                if (newImageDisplay && currentImageDisplay) {
                    currentImageDisplay.innerHTML = newImageDisplay.innerHTML;
                    console.log('이미지 디스플레이 섹션 새로고침 완료');
                }
            })
            .catch(error => {
                console.error('이미지 디스플레이 섹션 새로고침 실패:', error);
            });
    }
}

/**
 * Top 메뉴 프로필 이미지 새로고침
 */
function refreshTopMenuProfileImage() {
    // 더 구체적인 선택자 사용
    const topMenuProfileImages = document.querySelectorAll('.profile-img, .dropdown-img, .profile-image, .avatar-img');
    let refreshCount = 0;
    
    topMenuProfileImages.forEach(function(img) {
        if (img.src.includes('/member/mypage/profile-image/view/')) {
            // 타임스탬프와 랜덤 값을 추가하여 캐시 무효화
            const url = new URL(img.src, window.location.origin);
            url.searchParams.set('t', new Date().getTime());
            url.searchParams.set('v', Math.random().toString(36).substr(2, 9));
            img.src = url.toString();
            refreshCount++;
            console.log('Top 메뉴 이미지 새로고침:', url.toString());
        }
    });
    
    console.log(`Top 메뉴 프로필 이미지 새로고침 완료 (${refreshCount}개 이미지)`);
}

/**
 * 페이지 로드 완료 시 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('프로필 이미지 폼 페이지 로드 완료');
    
    // 프로필 이미지 관리자 초기화
    window.profileImageManager = new ProfileImageManager();
    
    // 전역 함수 등록
    window.refreshTopMenuProfileImage = refreshTopMenuProfileImage;
    
    // 페이지 언로드 시 로딩 상태 정리
    window.addEventListener('beforeunload', function() {
        const loadingButtons = document.querySelectorAll('.btn.loading');
        loadingButtons.forEach(btn => {
            btn.classList.remove('loading');
            btn.disabled = false;
        });
    });
});