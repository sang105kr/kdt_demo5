// 회원정보 수정 폼 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('회원정보 수정 폼 로드됨');
    
    const nicknameInput = document.querySelector('input[name="nickname"]');
    const submitBtn = document.querySelector('button[type="submit"]');
    
    if (nicknameInput) {
        // 별칭 중복체크
        let nicknameMsgElem = createMessageElement(nicknameInput);
        let lastNicknameChecked = '';
        let lastNicknameResult = null;

        nicknameInput.addEventListener('blur', () => checkNickname(nicknameInput, nicknameMsgElem, lastNicknameChecked, lastNicknameResult));
        nicknameInput.addEventListener('input', () => resetMessage(nicknameMsgElem, lastNicknameResult));
    }
    
    // 폼 유효성 검사
    const form = document.querySelector('.edit-form');
    if (form) {
        form.addEventListener('submit', function(e) {
            const nickname = document.getElementById('nickname').value.trim();
            const tel = document.getElementById('tel').value.trim();
            
            if (!nickname) {
                alert('별칭을 입력해주세요.');
                e.preventDefault();
                return;
            }
            
            if (!tel) {
                alert('연락처를 입력해주세요.');
                e.preventDefault();
                return;
            }
            
            // 연락처 형식 검사
            const telPattern = /^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$/;
            if (!telPattern.test(tel)) {
                alert('올바른 연락처 형식이 아닙니다. (예: 010-1234-5678)');
                e.preventDefault();
                return;
            }
        });
    }
    
    // 연락처 자동 포맷팅
    const telInput = document.getElementById('tel');
    if (telInput) {
        telInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/[^0-9]/g, '');
            
            if (value.length <= 3) {
                value = value;
            } else if (value.length <= 7) {
                value = value.slice(0, 3) + '-' + value.slice(3);
            } else {
                value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11);
            }
            
            e.target.value = value;
        });
    }

    function createMessageElement(input) {
        let msgElem = document.createElement('div');
        msgElem.className = 'check-msg';
        msgElem.style.fontSize = '0.97em';
        msgElem.style.marginTop = '4px';
        input.parentNode.insertBefore(msgElem, input.nextSibling);
        return msgElem;
    }

    function resetMessage(msgElem, lastResult) {
        msgElem.textContent = '';
        msgElem.style.color = '#444';
        lastResult = null;
    }

    function checkNickname(input, msgElem, lastChecked, lastResult) {
        const nickname = input.value.trim();
        if (!nickname || nickname === lastChecked) return;
        lastChecked = nickname;
        msgElem.textContent = '중복 확인 중...';
        msgElem.style.color = '#888';
        
        // 현재 사용자의 닉네임 가져오기 (이메일 필드에서 추출)
        const emailInput = document.getElementById('email');
        const currentEmail = emailInput ? emailInput.value : '';
        
        // common.js의 ajax 객체 사용
        ajax.get(`/api/member/nickname-exists?nickname=${encodeURIComponent(nickname)}&currentEmail=${encodeURIComponent(currentEmail)}`)
            .then(data => {
                if (data.code !== '00') {
                    const errorMessage = data.data?.message || data.message || '별칭 형식이 올바르지 않습니다.';
                    msgElem.textContent = errorMessage;
                    msgElem.style.color = '#d32f2f';
                    lastResult = false;
                } else if (data.data && data.data.exists) {
                    msgElem.textContent = data.data.message || '이미 사용 중인 별칭입니다.';
                    msgElem.style.color = '#d32f2f';
                    lastResult = false;
                } else {
                    msgElem.textContent = data.data?.message || '사용 가능한 별칭입니다!';
                    msgElem.style.color = '#388e3c';
                    lastResult = true;
                }
            })
            .catch(() => {
                msgElem.textContent = '중복 확인 중 오류가 발생했습니다.';
                msgElem.style.color = '#d32f2f';
                lastResult = false;
                          });
      }
    }); 

    // 주소 검색 모듈 로드
    const script = document.createElement('script');
    script.src = '/js/modules/addressSearch.js';
    document.head.appendChild(script);
    
    // 기존 주소 데이터가 있으면 표시
    script.onload = function() {
        // 기존 주소 데이터 확인 및 표시
        const zipcodeInput = document.getElementById('zipcode-mypage');
        const addressInput = document.getElementById('address-mypage');
        const addressDetailInput = document.getElementById('addressDetail-mypage');
        const addressDisplay = document.getElementById('addressDisplay-mypage');
        
        if (zipcodeInput && addressInput && addressDetailInput && addressDisplay) {
            // 기존 주소 데이터가 있으면 표시 영역을 보여줌
            if (zipcodeInput.value && addressInput.value) {
                const zipcodeElement = addressDisplay.querySelector('.zipcode');
                const addressElement = addressDisplay.querySelector('.address');
                const detailElement = addressDisplay.querySelector('.detail');
                
                if (zipcodeElement) zipcodeElement.textContent = `[${zipcodeInput.value}]`;
                if (addressElement) addressElement.textContent = addressInput.value;
                if (detailElement) detailElement.textContent = addressDetailInput.value;
                
                addressDisplay.style.display = 'block';
                console.log('기존 주소 정보가 표시되었습니다.');
            }
        }
        
        if (window.displayExistingAddress) {
            window.displayExistingAddress('mypage');
        }
    }; 