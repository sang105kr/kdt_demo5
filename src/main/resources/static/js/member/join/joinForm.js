document.addEventListener('DOMContentLoaded', function() {
  const emailInput = document.querySelector('input[name="email"]');
  const nicknameInput = document.querySelector('input[name="nickname"]');
  const submitBtn = document.getElementById('submitBtn');
  const loadingOverlay = document.getElementById('loadingOverlay');
  
  if (!emailInput || !nicknameInput) return;

  // 이메일 중복체크
  let emailMsgElem = createMessageElement(emailInput);
  let lastEmailChecked = '';
  let lastEmailResult = null;

  emailInput.addEventListener('blur', () => checkEmail(emailInput, emailMsgElem, lastEmailChecked, lastEmailResult));
  emailInput.addEventListener('input', () => resetMessage(emailMsgElem, lastEmailResult));

  // 별칭 중복체크
  let nicknameMsgElem = createMessageElement(nicknameInput);
  let lastNicknameChecked = '';
  let lastNicknameResult = null;

  nicknameInput.addEventListener('blur', () => checkNickname(nicknameInput, nicknameMsgElem, lastNicknameChecked, lastNicknameResult));
  nicknameInput.addEventListener('input', () => resetMessage(nicknameMsgElem, lastNicknameResult));

  // 폼 제출 이벤트
  const form = document.querySelector('form');
  form.addEventListener('submit', handleSubmit);

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

  function checkEmail(input, msgElem, lastChecked, lastResult) {
    const email = input.value.trim();
    if (!email || email === lastChecked) return;
    lastChecked = email;
    msgElem.textContent = '중복 확인 중...';
    msgElem.style.color = '#888';
    
    // common.js의 ajax 객체 사용
    ajax.get(`/api/member/email-exists?email=${encodeURIComponent(email)}`)
      .then(data => {
        if (data.code !== '00') {
          // 에러 메시지 처리
          const errorMessage = data.data?.message || data.message || '이메일 형식이 올바르지 않습니다.';
          msgElem.textContent = errorMessage;
          msgElem.style.color = '#d32f2f';
          lastResult = false;
        } else if (data.data && data.data.exists) {
          msgElem.textContent = data.data.message || '이미 사용 중인 이메일입니다.';
          msgElem.style.color = '#d32f2f';
          lastResult = false;
        } else {
          msgElem.textContent = data.data?.message || '사용 가능한 이메일입니다!';
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

  function checkNickname(input, msgElem, lastChecked, lastResult) {
    const nickname = input.value.trim();
    if (!nickname || nickname === lastChecked) return;
    lastChecked = nickname;
    msgElem.textContent = '중복 확인 중...';
    msgElem.style.color = '#888';
    
    // common.js의 ajax 객체 사용
    ajax.get(`/api/member/nickname-exists?nickname=${encodeURIComponent(nickname)}`)
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

  function handleSubmit(e) {
    e.preventDefault();
    
    // 로딩 상태 활성화
    submitBtn.disabled = true;
    const btnText = submitBtn.querySelector('.btn-text');
    const btnLoading = submitBtn.querySelector('.btn-loading');
    btnText.style.display = 'none';
    btnLoading.style.display = 'inline-flex';
    
    // 로딩 오버레이 표시
    if (loadingOverlay) {
      loadingOverlay.style.display = 'flex';
    }
    
    // 폼 제출
    form.submit();
  }
});
