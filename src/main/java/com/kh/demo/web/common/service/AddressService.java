package com.kh.demo.web.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    @Value("${juso.api.key}")
    private String apiKey;

    /**
     * 주소 검색 팝업 URL 생성
     * 파라미터 정보 타입 필수여부 설명 
     * 
     * 1. confmKey String Y 신청 시 부여받은 승인키 
     * 2. returnUrl String Y 주소 검색 결과를 리턴받을 URL (통합검색창을 호출한 페이지) 
     * 3. resultType String N  도로명주소 검색결과 화면 출력유형 
     *    1 : 도로명, 2 : 도로명+지번+상세보기(관련지번, 관할주민센터), 3 : 도로명+상세보기(상세건물명), 
     *    4 : 도로명+지번+상세보기(관련지번, 관할주민센터, 상세건물명) 
     * 4. useDetailAddr  String N  상세주소 동/층/호정보 제공여부 Y : 제공, N : 미제공(직접입력) 
     *  
     */
    public String createAddressPopupUrl(String returnUrl) {
        log.info("주소 검색 팝업 URL 생성: returnUrl={}", returnUrl);
        
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("https://business.juso.go.kr/addrlink/addrLinkUrl.do");
        urlBuilder.append("?confmKey=").append(apiKey);
        // returnUrl을 우리 콜백 페이지로 설정
        String callbackUrl = "http://localhost:9082/api/address/callback";
        urlBuilder.append("&returnUrl=").append(callbackUrl);
        urlBuilder.append("&resultType=4");  // 도로명+지번+상세보기(관련지번, 관할주민센터, 상세건물명)
        urlBuilder.append("&useDetailAddr=Y");  // 상세주소 제공
        
        return urlBuilder.toString();
    }
} 