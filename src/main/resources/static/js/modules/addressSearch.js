/**
 * 주소 검색 모듈
 * 행정안전부 도로명주소 API를 사용하여 주소 검색 기능을 제공합니다.
 */
class AddressSearch {
    constructor() {
        this.init();
    }
    
    init() {
        // 메시지 이벤트 리스너 등록
        window.addEventListener('message', this.handleMessage.bind(this));
        
        // 주소 검색 버튼 클릭 이벤트 리스너 등록
        document.addEventListener('click', this.handleButtonClick.bind(this));
    }
    
    /**
     * 주소 검색 버튼 클릭 이벤트를 처리합니다.
     */
    handleButtonClick(event) {
        if (event.target.classList.contains('address-search-btn')) {
            const addressId = event.target.getAttribute('data-address-id');
            if (addressId) {
                this.searchAddress(addressId);
            }
        }
    }
    
    /**
     * 주소 검색 팝업을 엽니다.
     * @param {string} addressId - 주소 입력 폼의 고유 ID
     */
    searchAddress(addressId) {
        // returnUrl은 사용하지 않음 (서버에서 고정 콜백 URL 사용)
        const returnUrl = encodeURIComponent(window.location.href);
        
        // 팝업 URL 생성 API 호출
        fetch(`/api/address/popup-url?returnUrl=${returnUrl}`)
            .then(response => response.json())
            .then(data => {
                if (data.popupUrl) {
                    // 팝업 창 설정
                    const popup = window.open(data.popupUrl, 'addressSearch', 
                        'width=600,height=700,scrollbars=yes,resizable=yes,location=no,status=no');
                    
                    // 팝업이 차단되었는지 확인
                    if (!popup || popup.closed || typeof popup.closed == 'undefined') {
                        alert('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.');
                        return;
                    }
                    
                    // 현재 addressId를 저장
                    this.currentAddressId = addressId;
                    console.log("주소 검색 팝업 열림, addressId:", this.currentAddressId);
                } else {
                    alert('주소 검색 팝업 URL을 생성할 수 없습니다.');
                }
            })
            .catch(error => {
                console.error('주소 검색 팝업 URL 생성 오류:', error);
                alert('주소 검색 팝업을 열 수 없습니다.');
            });
    }
    
    /**
     * 팝업에서 전달된 메시지를 처리합니다.
     */
    handleMessage(event) {
        console.log("=== 주소 검색 메시지 수신 ===");
        console.log("event", event);
        console.log("event.data", event.data);
        console.log("event.origin", event.origin);
        
        // 실제 API에서 제공되는 주소 데이터 구조에 맞춰 처리
        if (event.data && event.data.zipNo) {
            console.log("주소 데이터 처리 시작:", event.data);
            this.setAddressData(this.currentAddressId, event.data);
        } else {
            console.log("유효하지 않은 주소 데이터:", event.data);
        }
    }
    
    /**
     * 주소 검색 결과를 처리합니다.
     * @param {string} addressId - 주소 입력 폼의 고유 ID
     * @param {Object} addressData - 주소 데이터 (실제 API 구조)
     */
    setAddressData(addressId, addressData) {
        console.log("=== 주소 데이터 설정 시작 ===");
        console.log("addressId:", addressId);
        console.log("addressData:", addressData);
        
        const zipcodeInput = document.getElementById(`zipcode-${addressId}`);
        const addressInput = document.getElementById(`address-${addressId}`);
        const addressDetailInput = document.getElementById(`addressDetail-${addressId}`);
        const addressDisplay = document.getElementById(`addressDisplay-${addressId}`);
        
        console.log("찾은 요소들:", {
            zipcodeInput: zipcodeInput,
            addressInput: addressInput,
            addressDetailInput: addressDetailInput,
            addressDisplay: addressDisplay
        });
        
        if (zipcodeInput && addressInput && addressDetailInput && addressDisplay) {
            // 실제 API 데이터 구조에 맞춰 값 설정
            zipcodeInput.value = addressData.zipNo || '';
            addressInput.value = addressData.roadAddr || addressData.jibunAddr || '';
            addressDetailInput.value = addressData.addrDetail || '';
            
            console.log("입력 필드 값 설정 완료:", {
                zipcode: zipcodeInput.value,
                address: addressInput.value,
                addressDetail: addressDetailInput.value
            });
            
            // 주소 표시 영역 업데이트
            const zipcodeElement = addressDisplay.querySelector('.zipcode');
            const addressElement = addressDisplay.querySelector('.address');
            const detailElement = addressDisplay.querySelector('.detail');
            
            if (zipcodeElement) zipcodeElement.textContent = `[${addressData.zipNo}]`;
            if (addressElement) addressElement.textContent = addressData.roadAddr || addressData.jibunAddr;
            if (detailElement) detailElement.textContent = addressData.addrDetail;
            
            // 주소가 있으면 표시 영역을 보여줌
            if (addressData.roadAddr || addressData.jibunAddr) {
                addressDisplay.style.display = 'block';
            } else {
                addressDisplay.style.display = 'none';
            }
            
            // 주소 입력 완료 후 이벤트 발생 (폼 검증 등에 활용)
            const addressSelectedEvent = new CustomEvent('addressSelected', {
                detail: {
                    addressId: addressId,
                    addressData: addressData
                }
            });
            document.dispatchEvent(addressSelectedEvent);
            
            console.log('주소 정보가 성공적으로 설정되었습니다:', addressData);
        } else {
            console.error("주소 입력 필드를 찾을 수 없습니다. addressId:", addressId);
        }
    }
    
    /**
     * 주소 데이터를 초기화합니다.
     * @param {string} addressId - 주소 입력 폼의 고유 ID
     */
    clearAddressData(addressId) {
        const zipcodeInput = document.getElementById(`zipcode-${addressId}`);
        const addressInput = document.getElementById(`address-${addressId}`);
        const addressDetailInput = document.getElementById(`addressDetail-${addressId}`);
        const addressDisplay = document.getElementById(`addressDisplay-${addressId}`);
        
        if (zipcodeInput) zipcodeInput.value = '';
        if (addressInput) addressInput.value = '';
        if (addressDetailInput) addressDetailInput.value = '';
        if (addressDisplay) addressDisplay.style.display = 'none';
    }
    
    /**
     * 기존 주소 데이터를 표시합니다.
     * @param {string} addressId - 주소 입력 폼의 고유 ID
     */
    displayExistingAddress(addressId) {
        const zipcodeInput = document.getElementById(`zipcode-${addressId}`);
        const addressInput = document.getElementById(`address-${addressId}`);
        const addressDetailInput = document.getElementById(`addressDetail-${addressId}`);
        const addressDisplay = document.getElementById(`addressDisplay-${addressId}`);
        
        if (zipcodeInput && addressInput && addressDetailInput && addressDisplay) {
            // 기존 값이 있으면 주소 표시 영역 업데이트
            if (zipcodeInput.value || addressInput.value) {
                const zipcodeElement = addressDisplay.querySelector('.zipcode');
                const addressElement = addressDisplay.querySelector('.address');
                const detailElement = addressDisplay.querySelector('.detail');
                
                if (zipcodeElement) zipcodeElement.textContent = `[${zipcodeInput.value}]`;
                if (addressElement) addressElement.textContent = addressInput.value;
                if (detailElement) detailElement.textContent = addressDetailInput.value;
                
                addressDisplay.style.display = 'block';
            }
        }
    }
}

// 전역 함수로 등록 (기존 코드와의 호환성을 위해)
window.searchAddress = function(addressId) {
    if (!window.addressSearch) {
        window.addressSearch = new AddressSearch();
    }
    window.addressSearch.searchAddress(addressId);
};

window.setAddressData = function(addressId, addressData) {
    if (!window.addressSearch) {
        window.addressSearch = new AddressSearch();
    }
    window.addressSearch.setAddressData(addressId, addressData);
};

window.clearAddressData = function(addressId) {
    if (!window.addressSearch) {
        window.addressSearch = new AddressSearch();
    }
    window.addressSearch.clearAddressData(addressId);
};

window.displayExistingAddress = function(addressId) {
    if (!window.addressSearch) {
        window.addressSearch = new AddressSearch();
    }
    window.addressSearch.displayExistingAddress(addressId);
};

// 페이지 로드 시 주소 검색 모듈 초기화
document.addEventListener('DOMContentLoaded', function() {
    if (!window.addressSearch) {
        window.addressSearch = new AddressSearch();
    }
}); 