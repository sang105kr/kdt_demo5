// 관리자 코드 상세 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 관련 코드 목록 로드
    loadRelatedCodes();
});

// 관련 코드 목록 로드
async function loadRelatedCodes() {
    const gcode = document.querySelector('.info-item span').textContent.trim();
    const relatedCodesList = document.getElementById('relatedCodesList');
    
    try {
        // common.js의 ajax 객체 사용
        const data = await ajax.get(`/api/admin/codes/gcode/${gcode}`);
        
        // 새로운 ApiResponse 형식 처리
        if (data.code === '00') { // SUCCESS
            const codes = data.data;
            if (codes && codes.length > 0) {
                const html = codes.map(code => `
                    <div class="related-code-item">
                        <span class="code-value">${code.code}</span>
                        <span class="code-name">${code.decode}</span>
                        <span class="code-status ${code.useYn === 'Y' ? 'active' : 'inactive'}">
                            ${code.useYn === 'Y' ? '사용' : '미사용'}
                        </span>
                        <div class="code-actions">
                            <button type="button" class="btn btn-sm btn-primary" onclick="viewDetail(${code.codeId})">상세</button>
                            <button type="button" class="btn btn-sm btn-warning" onclick="editCode(${code.codeId})">수정</button>
                        </div>
                    </div>
                `).join('');
                relatedCodesList.innerHTML = html;
            } else {
                relatedCodesList.innerHTML = '<p>같은 그룹의 다른 코드가 없습니다.</p>';
            }
        } else {
            relatedCodesList.innerHTML = '<p>관련 코드 목록을 불러오는데 실패했습니다.</p>';
            showToast('관련 코드 목록을 불러오는데 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        relatedCodesList.innerHTML = '<p>네트워크 오류가 발생했습니다.</p>';
        showToast('네트워크 오류가 발생했습니다.', 'error');
    }
}

// 코드 상세보기
function viewDetail(codeId) {
    window.location.href = `/admin/codes/${codeId}`;
}

// 코드 수정
function editCode(codeId) {
    window.location.href = `/admin/codes/${codeId}/edit`;
}

// 코드 삭제
async function deleteCode(codeId) {
    if (!confirm('정말로 이 코드를 삭제하시겠습니까?\n\n삭제된 코드는 복구할 수 없습니다.')) {
        return;
    }
    
    try {
        // common.js의 ajax 객체 사용
        const data = await ajax.delete(`/api/admin/codes/${codeId}`);
        
        // 새로운 ApiResponse 형식 처리
        if (data.code === '00') { // SUCCESS
            showToast('코드가 성공적으로 삭제되었습니다.', 'success');
            window.location.href = '/admin/codes';
        } else {
            showToast(data.message || '코드 삭제 중 오류가 발생했습니다.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('네트워크 오류가 발생했습니다.', 'error');
    }
}

// 페이지 새로고침
function refreshPage() {
    window.location.reload();
}

// 뒤로 가기
function goBack() {
    window.history.back();
}

// 목록으로 이동
function goToList() {
    window.location.href = '/admin/codes';
} 