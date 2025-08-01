// 관리자 코드 등록 폼 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('codeAddForm');
    const submitBtn = document.getElementById('submitBtn');
    const loadingOverlay = document.getElementById('loadingOverlay');
    
    // 폼 필드들
    const gcodeField = document.getElementById('gcode');
    const codeField = document.getElementById('code');
    const decodeField = document.getElementById('decode');
    const pcodeField = document.getElementById('pcode');
    const sortOrderField = document.getElementById('sortOrder');
    const useYnField = document.getElementById('useYn');
    
    // 중복 확인 상태
    let isDuplicateChecked = false;
    let isDuplicateAvailable = false;
    
    // 폼 제출 이벤트
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        if (!validateForm()) {
            return;
        }
        
        if (!isDuplicateChecked) {
            showToast('중복 확인을 먼저 진행해주세요.', 'warning');
            return;
        }
        
        if (!isDuplicateAvailable) {
            showToast('이미 존재하는 코드입니다. 다른 코드값을 입력해주세요.', 'error');
            return;
        }
        
        submitForm();
    });
    
    // 필드 변경 시 중복 확인 상태 초기화
    gcodeField.addEventListener('input', resetDuplicateCheck);
    codeField.addEventListener('input', resetDuplicateCheck);
    
    // 실시간 미리보기 업데이트
    gcodeField.addEventListener('input', updatePreview);
    codeField.addEventListener('input', updatePreview);
    decodeField.addEventListener('input', updatePreview);
    pcodeField.addEventListener('change', updatePreview);
    
    // 초기 미리보기 설정
    updatePreview();
});

// 중복 확인
async function checkDuplicate() {
    const gcode = document.getElementById('gcode').value.trim();
    const code = document.getElementById('code').value.trim();
    const resultDiv = document.getElementById('code-check-result');
    const submitBtn = document.getElementById('submitBtn');
    
    if (!gcode || !code) {
        showToast('그룹코드와 코드값을 모두 입력해주세요.', 'warning');
        return;
    }
    
    // 로딩 표시
    resultDiv.innerHTML = '<span style="color: #666;">확인 중...</span>';
    
    try {
        // common.js의 ajax 객체 사용
        const data = await ajax.get(`/api/admin/codes/check-duplicate?gcode=${encodeURIComponent(gcode)}&code=${encodeURIComponent(code)}`);
        
        // 새로운 ApiResponse 형식 처리
        if (data.code === '00') { // SUCCESS
            if (!data.data.exists) {
                resultDiv.innerHTML = '<span class="success">✓ 사용 가능한 코드입니다.</span>';
                isDuplicateChecked = true;
                isDuplicateAvailable = true;
                submitBtn.disabled = false;
                showToast('사용 가능한 코드입니다.', 'success');
            } else {
                resultDiv.innerHTML = '<span class="error">✗ 이미 존재하는 코드입니다.</span>';
                isDuplicateChecked = true;
                isDuplicateAvailable = false;
                submitBtn.disabled = true;
                showToast('이미 존재하는 코드입니다.', 'error');
            }
        } else {
            resultDiv.innerHTML = '<span class="error">확인 중 오류가 발생했습니다.</span>';
            isDuplicateChecked = false;
            isDuplicateAvailable = false;
            submitBtn.disabled = true;
            showToast('확인 중 오류가 발생했습니다.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        resultDiv.innerHTML = '<span class="error">네트워크 오류가 발생했습니다.</span>';
        isDuplicateChecked = false;
        isDuplicateAvailable = false;
        submitBtn.disabled = true;
        showToast('네트워크 오류가 발생했습니다.', 'error');
    }
}

// 중복 확인 상태 초기화
function resetDuplicateCheck() {
    isDuplicateChecked = false;
    isDuplicateAvailable = false;
    document.getElementById('code-check-result').innerHTML = '';
    document.getElementById('submitBtn').disabled = true;
}

// 폼 유효성 검사
function validateForm() {
    const gcode = document.getElementById('gcode').value.trim();
    const code = document.getElementById('code').value.trim();
    const decode = document.getElementById('decode').value.trim();
    const sortOrder = document.getElementById('sortOrder').value;
    const useYn = document.getElementById('useYn').value;
    
    // 필수 필드 검사
    if (!gcode) {
        showToast('그룹코드를 입력해주세요.', 'warning');
        document.getElementById('gcode').focus();
        return false;
    }
    
    if (!code) {
        showToast('코드값을 입력해주세요.', 'warning');
        document.getElementById('code').focus();
        return false;
    }
    
    if (!decode) {
        showToast('코드명을 입력해주세요.', 'warning');
        document.getElementById('decode').focus();
        return false;
    }
    
    if (!sortOrder || sortOrder < 1) {
        showToast('정렬순서를 입력해주세요.', 'warning');
        document.getElementById('sortOrder').focus();
        return false;
    }
    
    if (!useYn) {
        showToast('사용여부를 선택해주세요.', 'warning');
        document.getElementById('useYn').focus();
        return false;
    }
    
    // 형식 검사
    if (!/^[A-Z_][A-Z0-9_]*$/.test(gcode)) {
        showToast('그룹코드는 영문 대문자, 숫자, 언더스코어만 사용 가능하며, 영문 대문자로 시작해야 합니다.', 'warning');
        document.getElementById('gcode').focus();
        return false;
    }
    
    if (!/^[A-Z_][A-Z0-9_]*$/.test(code)) {
        showToast('코드값은 영문 대문자, 숫자, 언더스코어만 사용 가능하며, 영문 대문자로 시작해야 합니다.', 'warning');
        document.getElementById('code').focus();
        return false;
    }
    
    return true;
}

// 폼 제출
async function submitForm() {
    const form = document.getElementById('codeAddForm');
    const formData = new FormData(form);
    const loadingOverlay = document.getElementById('loadingOverlay');
    
    // 로딩 표시
    loadingOverlay.style.display = 'flex';
    
    try {
        // common.js의 ajax 객체 사용
        const data = await ajax.post('/api/admin/codes', {
            gcode: formData.get('gcode'),
            code: formData.get('code'),
            decode: formData.get('decode'),
            pcode: formData.get('pcode') || null,
            sortOrder: parseInt(formData.get('sortOrder')),
            useYn: formData.get('useYn')
        });
        
        // 새로운 ApiResponse 형식 처리
        if (data.code === '00') { // SUCCESS
            showToast('코드가 성공적으로 등록되었습니다.', 'success');
            window.location.href = '/admin/codes';
        } else {
            if (data.code === '01' && data.details && data.details.fieldErrors) {
                // VALIDATION_ERROR 처리
                const errorMessages = data.details.fieldErrors.map(error => error.defaultMessage).join('\n');
                showToast('입력 오류:\n' + errorMessages, 'error');
            } else {
                showToast(data.message || '코드 등록 중 오류가 발생했습니다.', 'error');
            }
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('네트워크 오류가 발생했습니다.', 'error');
    } finally {
        loadingOverlay.style.display = 'none';
    }
}

// 미리보기 업데이트
function updatePreview() {
    const gcode = document.getElementById('gcode').value.trim();
    const code = document.getElementById('code').value.trim();
    const decode = document.getElementById('decode').value.trim();
    const pcode = document.getElementById('pcode').value;
    const pcodeSelect = document.getElementById('pcode');
    
    const previewSection = document.getElementById('codePreview');
    const fullCodeSpan = document.getElementById('previewFullCode');
    const displaySpan = document.getElementById('previewDisplay');
    const pathSpan = document.getElementById('previewPath');
    
    if (gcode && code) {
        fullCodeSpan.textContent = `${gcode}:${code}`;
        displaySpan.textContent = decode || '(코드명 미입력)';
        
        // 경로 생성
        let path = `/${gcode}/${code}`;
        if (pcode) {
            const selectedOption = pcodeSelect.options[pcodeSelect.selectedIndex];
            if (selectedOption && selectedOption.text !== '최상위 코드') {
                const parentText = selectedOption.text;
                const parentGcode = parentText.split(' - ')[0];
                path = `/${parentGcode}/.../${code}`;
            }
        }
        pathSpan.textContent = path;
        
        previewSection.style.display = 'block';
    } else {
        previewSection.style.display = 'none';
    }
}

// 뒤로 가기
function goBack() {
    if (confirm('작성 중인 내용이 사라집니다. 정말 나가시겠습니까?')) {
        window.location.href = '/admin/codes';
    }
}

// 전역 변수 (중복 확인 상태)
let isDuplicateChecked = false;
let isDuplicateAvailable = false; 