/**
 * 관리자 코드 등록 폼 JavaScript
 * 1440px 해상도 최적화 모노크롬 디자인 시스템 기반
 */

document.addEventListener('DOMContentLoaded', function() {
    // CSSManager와 NotificationManager 사용
    const cssManager = window.CSSManager;
    const notify = window.notify;

    // 관리자 코드 등록 전용 스타일 생성
    cssManager.addStyle('admin-code-add', `
        .admin-code-add-page .code-add-container {
            opacity: 0;
            transform: translateY(20px);
            transition: all var(--transition-normal);
        }

        .admin-code-add-page .code-add-container.loaded {
            opacity: 1;
            transform: translateY(0);
        }

        .admin-code-add-page .form-card {
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-xl);
            margin-bottom: var(--space-lg);
        }

        .admin-code-add-page .form-group {
            margin-bottom: var(--space-lg);
        }

        .admin-code-add-page .form-label {
            display: block;
            font-weight: 600;
            color: var(--color-text);
            margin-bottom: var(--space-xs);
        }

        .admin-code-add-page .form-input,
        .admin-code-add-page .form-select {
            width: 100%;
            padding: var(--space-sm);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            font-size: var(--font-size-base);
            transition: border-color var(--transition-fast);
        }

        .admin-code-add-page .form-input:focus,
        .admin-code-add-page .form-select:focus {
            outline: none;
            border-color: var(--color-primary);
            box-shadow: 0 0 0 2px rgba(34, 40, 49, 0.1);
        }

        .admin-code-add-page .form-input.error {
            border-color: var(--color-error);
            box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
        }

        .admin-code-add-page .form-input.success {
            border-color: var(--color-success);
            box-shadow: 0 0 0 2px rgba(34, 40, 49, 0.1);
        }

        .admin-code-add-page .duplicate-check-section {
            background: var(--color-light-gray);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-md);
            margin-bottom: var(--space-lg);
        }

        .admin-code-add-page .check-result {
            margin-top: var(--space-sm);
            font-size: var(--font-size-sm);
        }

        .admin-code-add-page .check-result .success {
            color: var(--color-success);
        }

        .admin-code-add-page .check-result .error {
            color: var(--color-error);
        }

        .admin-code-add-page .preview-section {
            background: var(--color-light-gray);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            padding: var(--space-md);
            margin-bottom: var(--space-lg);
            display: none;
        }

        .admin-code-add-page .preview-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: var(--space-xs) 0;
            border-bottom: 1px solid var(--color-border);
        }

        .admin-code-add-page .preview-item:last-child {
            border-bottom: none;
        }

        .admin-code-add-page .preview-label {
            font-weight: 600;
            color: var(--color-text);
            min-width: 100px;
        }

        .admin-code-add-page .preview-value {
            color: var(--color-text-secondary);
            flex: 1;
            text-align: right;
        }

        .admin-code-add-page .form-actions {
            display: flex;
            gap: var(--space-sm);
            justify-content: flex-end;
            margin-top: var(--space-xl);
        }

        .admin-code-add-page .btn--submit {
            background: var(--color-primary);
            color: var(--color-white);
        }

        .admin-code-add-page .btn--cancel {
            background: var(--color-secondary);
            color: var(--color-white);
        }

        .admin-code-add-page .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: var(--z-modal);
        }

        .admin-code-add-page .loading-spinner {
            width: 40px;
            height: 40px;
            border: 4px solid var(--color-border);
            border-top: 4px solid var(--color-primary);
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    `);

    // 폼 요소들
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
    
    /**
     * 초기화
     */
    function init() {
        initializeFormEvents();
        animatePageLoad();
    }

    /**
     * 폼 이벤트 초기화
     */
    function initializeFormEvents() {
        // 폼 제출 이벤트
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            if (!validateForm()) {
                return;
            }
            
            if (!isDuplicateChecked) {
                notify.warning('중복 확인을 먼저 진행해주세요.', '중복 확인 필요');
                return;
            }
            
            if (!isDuplicateAvailable) {
                notify.error('이미 존재하는 코드입니다. 다른 코드값을 입력해주세요.', '중복 오류');
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
    }

    /**
     * 중복 확인
     */
    async function checkDuplicate() {
        const gcode = gcodeField.value.trim();
        const code = codeField.value.trim();
        const resultDiv = document.getElementById('code-check-result');
        
        if (!gcode || !code) {
            notify.warning('그룹코드와 코드값을 모두 입력해주세요.', '입력 필요');
            return;
        }
        
        // 로딩 표시
        resultDiv.innerHTML = '<span style="color: var(--color-text-muted);">확인 중...</span>';
        
        try {
            const data = await ajax.get(`/api/admin/codes/check-duplicate?gcode=${encodeURIComponent(gcode)}&code=${encodeURIComponent(code)}`);
            
            if (data.code === '00') {
                if (!data.data.exists) {
                    resultDiv.innerHTML = '<span class="success">✓ 사용 가능한 코드입니다.</span>';
                    isDuplicateChecked = true;
                    isDuplicateAvailable = true;
                    submitBtn.disabled = false;
                    notify.success('사용 가능한 코드입니다.', '중복 확인');
                } else {
                    resultDiv.innerHTML = '<span class="error">✗ 이미 존재하는 코드입니다.</span>';
                    isDuplicateChecked = true;
                    isDuplicateAvailable = false;
                    submitBtn.disabled = true;
                    notify.error('이미 존재하는 코드입니다.', '중복 오류');
                }
            } else {
                resultDiv.innerHTML = '<span class="error">확인 중 오류가 발생했습니다.</span>';
                isDuplicateChecked = false;
                isDuplicateAvailable = false;
                submitBtn.disabled = true;
                notify.error('확인 중 오류가 발생했습니다.', '오류');
            }
        } catch (error) {
            console.error('Error:', error);
            resultDiv.innerHTML = '<span class="error">네트워크 오류가 발생했습니다.</span>';
            isDuplicateChecked = false;
            isDuplicateAvailable = false;
            submitBtn.disabled = true;
            notify.error('네트워크 오류가 발생했습니다.', '오류');
        }
    }

    /**
     * 중복 확인 상태 초기화
     */
    function resetDuplicateCheck() {
        isDuplicateChecked = false;
        isDuplicateAvailable = false;
        document.getElementById('code-check-result').innerHTML = '';
        submitBtn.disabled = true;
    }

    /**
     * 폼 유효성 검사
     */
    function validateForm() {
        const gcode = gcodeField.value.trim();
        const code = codeField.value.trim();
        const decode = decodeField.value.trim();
        const sortOrder = sortOrderField.value;
        const useYn = useYnField.value;
        
        // 필수 필드 검사
        if (!gcode) {
            notify.warning('그룹코드를 입력해주세요.', '입력 필요');
            gcodeField.focus();
            return false;
        }
        
        if (!code) {
            notify.warning('코드값을 입력해주세요.', '입력 필요');
            codeField.focus();
            return false;
        }
        
        if (!decode) {
            notify.warning('코드명을 입력해주세요.', '입력 필요');
            decodeField.focus();
            return false;
        }
        
        if (!sortOrder || sortOrder < 1) {
            notify.warning('정렬순서를 입력해주세요.', '입력 필요');
            sortOrderField.focus();
            return false;
        }
        
        if (!useYn) {
            notify.warning('사용여부를 선택해주세요.', '입력 필요');
            useYnField.focus();
            return false;
        }
        
        // 형식 검사
        if (!/^[A-Z_][A-Z0-9_]*$/.test(gcode)) {
            notify.warning('그룹코드는 영문 대문자, 숫자, 언더스코어만 사용 가능하며, 영문 대문자로 시작해야 합니다.', '형식 오류');
            gcodeField.focus();
            return false;
        }
        
        if (!/^[A-Z_][A-Z0-9_]*$/.test(code)) {
            notify.warning('코드값은 영문 대문자, 숫자, 언더스코어만 사용 가능하며, 영문 대문자로 시작해야 합니다.', '형식 오류');
            codeField.focus();
            return false;
        }
        
        return true;
    }

    /**
     * 폼 제출
     */
    async function submitForm() {
        const formData = new FormData(form);
        
        // 로딩 표시
        loadingOverlay.style.display = 'flex';
        
        try {
            const data = await ajax.post('/api/admin/codes', {
                gcode: formData.get('gcode'),
                code: formData.get('code'),
                decode: formData.get('decode'),
                pcode: formData.get('pcode') || null,
                sortOrder: parseInt(formData.get('sortOrder')),
                useYn: formData.get('useYn')
            });
            
            if (data.code === '00') {
                notify.success('코드가 성공적으로 등록되었습니다.', '등록 완료');
                setTimeout(() => {
                    window.location.href = '/admin/codes';
                }, 1000);
            } else {
                if (data.code === '01' && data.details && data.details.fieldErrors) {
                    const errorMessages = data.details.fieldErrors.map(error => error.defaultMessage).join('\n');
                    notify.error('입력 오류:\n' + errorMessages, '유효성 검사 오류');
                } else {
                    notify.error(data.message || '코드 등록 중 오류가 발생했습니다.', '등록 오류');
                }
            }
        } catch (error) {
            console.error('Error:', error);
            notify.error('네트워크 오류가 발생했습니다.', '오류');
        } finally {
            loadingOverlay.style.display = 'none';
        }
    }

    /**
     * 미리보기 업데이트
     */
    function updatePreview() {
        const gcode = gcodeField.value.trim();
        const code = codeField.value.trim();
        const decode = decodeField.value.trim();
        const pcode = pcodeField.value;
        const pcodeSelect = pcodeField;
        
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

    /**
     * 뒤로 가기
     */
    function goBack() {
        if (confirm('작성 중인 내용이 사라집니다. 정말 나가시겠습니까?')) {
            window.location.href = '/admin/codes';
        }
    }

    /**
     * 페이지 로드 애니메이션
     */
    function animatePageLoad() {
        const container = document.querySelector('.code-add-container');
        if (container) {
            setTimeout(() => {
                container.classList.add('loaded');
            }, 100);
        }
    }

    // 전역 함수로 노출
    window.checkDuplicate = checkDuplicate;
    window.goBack = goBack;

    // 초기화 실행
    init();
}); 