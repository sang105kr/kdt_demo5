document.addEventListener('DOMContentLoaded', function() {
    const paymentMethodRadios = document.querySelectorAll('input[name="paymentMethod"]');
    const cardFields = document.querySelector('.card-fields');
    const bankFields = document.querySelector('.bank-fields');
    
    // 결제 방법 변경 시 필드 표시/숨김
    paymentMethodRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.value === 'CARD') {
                cardFields.style.display = 'block';
                bankFields.style.display = 'none';
            } else if (this.value === 'BANK_TRANSFER') {
                cardFields.style.display = 'none';
                bankFields.style.display = 'block';
            } else {
                cardFields.style.display = 'none';
                bankFields.style.display = 'none';
            }
        });
    });
    
    // 카드번호 자동 포맷팅
    const cardNumberInput = document.querySelector('input[name="cardNumber"]');
    if (cardNumberInput) {
        cardNumberInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 16) {
                value = value.substring(0, 16);
            }
            const formatted = value.replace(/(\d{4})(?=\d)/g, '$1-');
            e.target.value = formatted;
        });
    }
    
    // 만료월 입력 제한 (01-12)
    const expiryMonthInput = document.querySelector('input[name="cardExpiryMonth"]');
    if (expiryMonthInput) {
        expiryMonthInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 2) {
                value = value.substring(0, 2);
            }
            if (value > 12) {
                value = '12';
            }
            e.target.value = value;
        });
    }
    
    // 만료년 입력 제한 (현재년도 이후)
    const expiryYearInput = document.querySelector('input[name="cardExpiryYear"]');
    if (expiryYearInput) {
        expiryYearInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 2) {
                value = value.substring(0, 2);
            }
            e.target.value = value;
        });
    }
    
    // CVC 입력 제한 (3-4자리)
    const cvcInput = document.querySelector('input[name="cardCvc"]');
    if (cvcInput) {
        cvcInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 4) {
                value = value.substring(0, 4);
            }
            e.target.value = value;
        });
    }
    
    // 연락처 자동 포맷팅
    const phoneInput = document.querySelector('input[name="payerPhone"]');
    if (phoneInput) {
        phoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 11) {
                value = value.substring(0, 11);
            }
            if (value.length >= 7) {
                value = value.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
            } else if (value.length >= 3) {
                value = value.replace(/(\d{3})(\d{0,4})/, '$1-$2');
            }
            e.target.value = value;
        });
    }
    
    // 폼 제출 시 유효성 검사
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            const selectedMethod = document.querySelector('input[name="paymentMethod"]:checked');
            if (!selectedMethod) {
                e.preventDefault();
                alert('결제 방법을 선택해주세요.');
                return;
            }
            
            if (selectedMethod.value === 'CARD') {
                const cardNumber = document.querySelector('input[name="cardNumber"]').value;
                const expiryMonth = document.querySelector('input[name="cardExpiryMonth"]').value;
                const expiryYear = document.querySelector('input[name="cardExpiryYear"]').value;
                const cvc = document.querySelector('input[name="cardCvc"]').value;
                
                if (!cardNumber || cardNumber.replace(/\D/g, '').length < 13) {
                    e.preventDefault();
                    alert('올바른 카드번호를 입력해주세요.');
                    return;
                }
                
                if (!expiryMonth || !expiryYear) {
                    e.preventDefault();
                    alert('카드 만료일을 입력해주세요.');
                    return;
                }
                
                if (!cvc || cvc.length < 3) {
                    e.preventDefault();
                    alert('CVC를 입력해주세요.');
                    return;
                }
            } else if (selectedMethod.value === 'BANK_TRANSFER') {
                const bankCode = document.querySelector('input[name="bankCode"]').value;
                const accountNumber = document.querySelector('input[name="accountNumber"]').value;
                
                if (!bankCode) {
                    e.preventDefault();
                    alert('은행코드를 입력해주세요.');
                    return;
                }
                
                if (!accountNumber || accountNumber.length < 10) {
                    e.preventDefault();
                    alert('올바른 계좌번호를 입력해주세요.');
                    return;
                }
            }
        });
    }
}); 