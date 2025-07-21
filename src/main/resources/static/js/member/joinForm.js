document.addEventListener('DOMContentLoaded', function() {
  const emailInput = document.querySelector('input[name="email"]');
  if (!emailInput) return;

  // 메시지 표시용 엘리먼트 생성/삽입
  let msgElem = document.createElement('div');
  msgElem.className = 'email-check-msg';
  msgElem.style.fontSize = '0.97em';
  msgElem.style.marginTop = '4px';
  emailInput.parentNode.insertBefore(msgElem, emailInput.nextSibling);

  let lastChecked = '';
  let lastResult = null;

  emailInput.addEventListener('blur', checkEmail);
  emailInput.addEventListener('input', function() {
    msgElem.textContent = '';
    msgElem.style.color = '#444';
    lastResult = null;
  });

  function checkEmail() {
    const email = emailInput.value.trim();
    if (!email || email === lastChecked) return;
    lastChecked = email;
    msgElem.textContent = '중복 확인 중...';
    msgElem.style.color = '#888';
    fetch(`/api/member/email-exists?email=${encodeURIComponent(email)}`)
      .then(res => res.json())
      .then(data => {
        if (data.code !== '00') {
          msgElem.textContent = data.message || '이메일 형식이 올바르지 않습니다.';
          msgElem.style.color = '#d32f2f';
          lastResult = false;
        } else if (data.data && data.data.exists) {
          msgElem.textContent = '이미 사용 중인 이메일입니다.';
          msgElem.style.color = '#d32f2f';
          lastResult = false;
        } else {
          msgElem.textContent = '사용 가능한 이메일입니다!';
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
