/**
 * 상품 등록 폼 JavaScript
 * 파일 업로드 미리보기 및 폼 검증 기능
 */

// Star Rating System
function initializeStarRating() {
    const starRating = document.getElementById('starRating');
    const ratingInput = document.getElementById('rating');
    const ratingDisplay = document.getElementById('ratingDisplay');
    
    if (!starRating || !ratingInput || !ratingDisplay) return;
    
    const stars = starRating.querySelectorAll('.star');
    let currentRating = 0;
    
    // Initialize stars
    updateStarDisplay(currentRating);
    
    // Add click event listeners
    stars.forEach((star, index) => {
        star.addEventListener('click', function(e) {
            const rect = star.getBoundingClientRect();
            const clickX = e.clientX - rect.left;
            const starWidth = rect.width;
            
            // 클릭 위치에 따라 0.5 또는 1.0 점수 결정
            const baseRating = index + 1;
            const isHalfStar = clickX < starWidth / 2;
            const rating = isHalfStar ? baseRating - 0.5 : baseRating;
            
            currentRating = rating;
            ratingInput.value = rating;
            updateStarDisplay(rating);
        });
        
        // Hover effects
        star.addEventListener('mouseenter', function(e) {
            const rect = star.getBoundingClientRect();
            const mouseX = e.clientX - rect.left;
            const starWidth = rect.width;
            
            const baseRating = index + 1;
            const isHalfStar = mouseX < starWidth / 2;
            const rating = isHalfStar ? baseRating - 0.5 : baseRating;
            
            highlightStars(rating);
        });
        
        star.addEventListener('mouseleave', function() {
            updateStarDisplay(currentRating);
        });
    });
    
    function updateStarDisplay(rating) {
        stars.forEach((star, index) => {
            const starRating = index + 1;
            const halfStarRating = starRating - 0.5;
            
            // 별의 상태 결정
            if (rating >= starRating) {
                // 완전히 채워진 별
                star.classList.add('filled');
                star.classList.remove('half-filled', 'empty');
            } else if (rating >= halfStarRating) {
                // 절반 채워진 별
                star.classList.add('half-filled');
                star.classList.remove('filled', 'empty');
            } else {
                // 빈 별
                star.classList.add('empty');
                star.classList.remove('filled', 'half-filled');
            }
        });
        ratingDisplay.textContent = `${rating.toFixed(1)} / 5.0`;
    }
    
    function highlightStars(rating) {
        stars.forEach((star, index) => {
            const starRating = index + 1;
            const halfStarRating = starRating - 0.5;
            
            if (rating >= starRating) {
                star.classList.add('active', 'filled');
                star.classList.remove('half-filled', 'empty');
            } else if (rating >= halfStarRating) {
                star.classList.add('active', 'half-filled');
                star.classList.remove('filled', 'empty');
            } else {
                star.classList.remove('active', 'filled', 'half-filled');
                star.classList.add('empty');
            }
        });
    }
}

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
                    const previewItem = document.createElement('div');
                    previewItem.className = 'preview-item';
                    previewItem.innerHTML = `
                        <img src="${e.target.result}" alt="${file.name}">
                        <div class="file-name">${file.name}</div>
                    `;
                    preview.appendChild(previewItem);
                    console.log(`이미지 미리보기 추가: ${file.name}`);
                };
                reader.readAsDataURL(file);
            } else {
                console.warn(`지원하지 않는 파일 형식: ${file.name} (${file.type})`);
            }
        });
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
            previewItem.className = 'preview-item';
            previewItem.innerHTML = `
                <div class="file-icon">📄</div>
                <div class="file-name">${file.name}</div>
                <div class="file-meta">
                    <span class="file-size">${formatFileSize(file.size)}</span>
                    <span class="file-type">${file.type || 'Unknown'}</span>
                </div>
            `;
            preview.appendChild(previewItem);
            console.log(`문서 미리보기 추가: ${file.name}`);
        });
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
        if (!field.value.trim()) {
            field.classList.add('error');
            isValid = false;
        } else {
            field.classList.remove('error');
        }
    });
    
    return isValid;
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeStarRating();
    
    // Form submission
    const form = document.querySelector('.product-form');
    if (form) {
        form.addEventListener('submit', function(e) {
            if (!validateForm()) {
                e.preventDefault();
                alert('필수 항목을 모두 입력해주세요.');
                return false;
            }
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