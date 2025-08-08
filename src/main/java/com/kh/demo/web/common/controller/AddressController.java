package com.kh.demo.web.common.controller;

import com.kh.demo.web.common.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 주소 검색 팝업 URL 생성 API
     */
    @GetMapping("/popup-url")
    public ResponseEntity<Map<String, String>> getPopupUrl(@RequestParam String returnUrl) {
        try {
            log.info("주소 검색 팝업 URL 요청: returnUrl={}", returnUrl);//http://localhost:9082/member/mypage/edit
            String popupUrl = addressService.createAddressPopupUrl(returnUrl);
            log.info("주소 검색 팝업 URL 생성 완료: popupUrl={}", popupUrl); 
            Map<String, String> response = new HashMap<>();
            response.put("popupUrl", popupUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("주소 검색 팝업 URL 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 