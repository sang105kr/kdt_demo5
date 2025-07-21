/**
 * 상품 등록 폼 JavaScript
 * 파일 업로드 미리보기 및 폼 검증 기능
 */

// File Preview Functions
function previewImages(input, previewId) {
    const preview = document.getElementById(previewId);
    if (!preview) return;
    
    // 기존 미리보기 초기화
    preview.innerHTML = '';
    
    if (input.files && input.files.length > 0) {
        console.log(`이미지 파일 ${input.files.length}개 선택됨`);
        
        Array.from(input.files).forEach((file, index) => {
            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    const thumbnail = document.createElement('div');
                    thumbnail.className = 'image-thumbnail';
                    thumbnail.innerHTML = `
                        <img src="${e.target.result}" alt="${file.name}">
                        <button type="button" class="remove-btn" data-index="${index}" title="삭제"></button>
                        <div class="thumbnail-info">
                            <div class="file-name">${file.name}</div>
                            <div class="file-size">${formatFileSize(file.size)}</div>
                        </div>
                    `;
                    
                    // 삭제 버튼 이벤트 추가
                    const removeBtn = thumbnail.querySelector('.remove-btn');
                    removeBtn.addEventListener('click', function() {
                        removeImageFile(input, index, thumbnail);
                    });
                    
                    preview.appendChild(thumbnail);
                    console.log(`이미지 썸네일 추가: ${file.name}`);
                };
                reader.readAsDataURL(file);
            } else {
                console.warn(`지원하지 않는 파일 형식: ${file.name} (${file.type})`);
            }
        });
    }
}

// 이미지 파일 삭제 함수
function removeImageFile(input, index, thumbnailElement) {
    // FileList는 읽기 전용이므로 DataTransfer를 사용하여 파일 제거
    const dt = new DataTransfer();
    const files = input.files;
    
    for (let i = 0; i < files.length; i++) {
        if (i !== index) {
            dt.items.add(files[i]);
        }
    }
    
    input.files = dt.files;
    
    // 썸네일 요소 제거
    thumbnailElement.remove();
    
    console.log(`이미지 파일 삭제됨: 인덱스 ${index}`);
    
    // 파일이 없으면 미리보기 영역 초기화
    if (input.files.length === 0) {
        const preview = document.getElementById('imagePreview');
        if (preview) {
            preview.innerHTML = '';
        }
    }
}

function previewFiles(input, previewId) {
    const preview = document.getElementById(previewId);
    if (!preview) return;
    
    // 기존 미리보기 초기화
    preview.innerHTML = '';
    
    if (input.files && input.files.length > 0) {
        console.log(`문서 파일 ${input.files.length}개 선택됨`);
        
        Array.from(input.files).forEach((file, index) => {
            const previewItem = document.createElement('div');
            previewItem.className = 'file-preview-item';
            previewItem.innerHTML = `
                <div class="file-icon">📄</div>
                <div class="file-info">
                    <div class="file-name">${file.name}</div>
                    <div class="file-meta">
                        <span class="file-size">${formatFileSize(file.size)}</span>
                        <span class="file-type">${file.type || 'Unknown'}</span>
                    </div>
                </div>
                <button type="button" class="remove-file" data-index="${index}" title="삭제">삭제</button>
            `;
            
            // 삭제 버튼 이벤트 추가
            const removeBtn = previewItem.querySelector('.remove-file');
            removeBtn.addEventListener('click', function() {
                removeFile(input, index, previewItem);
            });
            
            preview.appendChild(previewItem);
            console.log(`문서 미리보기 추가: ${file.name}`);
        });
    }
}

// 문서 파일 삭제 함수
function removeFile(input, index, previewElement) {
    // FileList는 읽기 전용이므로 DataTransfer를 사용하여 파일 제거
    const dt = new DataTransfer();
    const files = input.files;
    
    for (let i = 0; i < files.length; i++) {
        if (i !== index) {
            dt.items.add(files[i]);
        }
    }
    
    input.files = dt.files;
    
    // 미리보기 요소 제거
    previewElement.remove();
    
    console.log(`문서 파일 삭제됨: 인덱스 ${index}`);
    
    // 파일이 없으면 미리보기 영역 초기화
    if (input.files.length === 0) {
        const preview = input.id === 'manualFiles' ? 
            document.getElementById('manualPreview') : 
            document.getElementById('imagePreview');
        if (preview) {
            preview.innerHTML = '';
        }
    }
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Form Validation
function validateForm() {
    const form = document.querySelector('.product-form');
    if (!form) return true;
    
    const requiredFields = form.querySelectorAll('[required]');
    let isValid = true;
    
    requiredFields.forEach(field => {
        let fieldValue = field.value;
        let isEmpty = false;
        
        // 필드 타입에 따른 검증
        if (field.type === 'number') {
            // 숫자 필드는 trim() 사용하지 않음
            isEmpty = fieldValue === '' || fieldValue === null || fieldValue === undefined;
        } else {
            // 텍스트 필드는 trim() 사용
            isEmpty = !fieldValue.trim();
        }
        
        if (isEmpty) {
            field.classList.add('error');
            isValid = false;
            console.log(`필수 필드 누락: ${field.id} (값: "${fieldValue}")`);
        } else {
            field.classList.remove('error');
        }
    });
    
    return isValid;
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    
    // Form submission
    const form = document.querySelector('.product-form');
    if (form) {
        form.addEventListener('submit', function(e) {
            console.log('폼 제출 시도...');
            if (!validateForm()) {
                e.preventDefault();
                console.log('폼 검증 실패 - 제출 중단');
                alert('필수 항목을 모두 입력해주세요.');
                return false;
            }
            console.log('폼 검증 성공 - 제출 진행');
        });
    }
    
    // File input change events
    const imageInput = document.getElementById('imageFiles');
    if (imageInput) {
        imageInput.addEventListener('change', function(e) {
            console.log('이미지 파일 선택 이벤트 발생');
            previewImages(this, 'imagePreview');
        });
    }
    
    const manualInput = document.getElementById('manualFiles');
    if (manualInput) {
        manualInput.addEventListener('change', function(e) {
            console.log('문서 파일 선택 이벤트 발생');
            previewFiles(this, 'manualPreview');
        });
    }
    
    // 검증 실패 시 임시 파일 알림 표시
    const tempImageNotice = document.querySelector('#imagePreview .temp-files-notice');
    const tempManualNotice = document.querySelector('#manualPreview .temp-files-notice');
    
    if (tempImageNotice || tempManualNotice) {
        console.log('검증 실패로 인한 임시 파일 알림이 표시됩니다.');
        
        // 파일 입력 필드에 포커스 이벤트 추가
        if (imageInput && tempImageNotice) {
            imageInput.addEventListener('focus', function() {
                tempImageNotice.style.display = 'none';
            });
        }
        
        if (manualInput && tempManualNotice) {
            manualInput.addEventListener('focus', function() {
                tempManualNotice.style.display = 'none';
            });
        }
    }
}); 