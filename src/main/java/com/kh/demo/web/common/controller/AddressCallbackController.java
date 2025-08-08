package com.kh.demo.web.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class AddressCallbackController {

    /**
     * 주소 검색 팝업에서 선택된 결과를 처리하는 콜백 페이지 (GET)
     */
    @GetMapping("/api/address/callback")
    public String addressCallback(
            @RequestParam(required = false) String roadFullAddr,
            @RequestParam(required = false) String roadAddrPart1,
            @RequestParam(required = false) String roadAddrPart2,
            @RequestParam(required = false) String jibunAddr,
            @RequestParam(required = false) String zipNo,
            @RequestParam(required = false) String addrDetail,
            @RequestParam(required = false) String admCd,
            @RequestParam(required = false) String rnMgtSn,
            @RequestParam(required = false) String bdMgtSn,
            @RequestParam(required = false) String detBdNmList,
            @RequestParam(required = false) String bdNm,
            @RequestParam(required = false) String bdKdcd,
            @RequestParam(required = false) String siNm,
            @RequestParam(required = false) String sggNm,
            @RequestParam(required = false) String emdNm,
            @RequestParam(required = false) String liNm,
            @RequestParam(required = false) String rn,
            @RequestParam(required = false) String udrtYn,
            @RequestParam(required = false) String buldMnnm,
            @RequestParam(required = false) String buldSlno,
            @RequestParam(required = false) String mtYn,
            @RequestParam(required = false) String lnbrMnnm,
            @RequestParam(required = false) String lnbrSlno,
            @RequestParam(required = false) String emdNo,
            Model model) {
        
        log.info("주소 검색 콜백 수신 (GET): roadFullAddr={}, zipNo={}", roadFullAddr, zipNo);
        
        // 모델에 주소 정보 추가
        model.addAttribute("roadFullAddr", roadFullAddr);
        model.addAttribute("roadAddrPart1", roadAddrPart1);
        model.addAttribute("roadAddrPart2", roadAddrPart2);
        model.addAttribute("jibunAddr", jibunAddr);
        model.addAttribute("zipNo", zipNo);
        model.addAttribute("addrDetail", addrDetail);
        model.addAttribute("admCd", admCd);
        model.addAttribute("rnMgtSn", rnMgtSn);
        model.addAttribute("bdMgtSn", bdMgtSn);
        model.addAttribute("detBdNmList", detBdNmList);
        model.addAttribute("bdNm", bdNm);
        model.addAttribute("bdKdcd", bdKdcd);
        model.addAttribute("siNm", siNm);
        model.addAttribute("sggNm", sggNm);
        model.addAttribute("emdNm", emdNm);
        model.addAttribute("liNm", liNm);
        model.addAttribute("rn", rn);
        model.addAttribute("udrtYn", udrtYn);
        model.addAttribute("buldMnnm", buldMnnm);
        model.addAttribute("buldSlno", buldSlno);
        model.addAttribute("mtYn", mtYn);
        model.addAttribute("lnbrMnnm", lnbrMnnm);
        model.addAttribute("lnbrSlno", lnbrSlno);
        model.addAttribute("emdNo", emdNo);
        
        return "common/addressCallback";
    }

    /**
     * 주소 검색 팝업에서 선택된 결과를 처리하는 콜백 페이지 (POST)
     */
    @PostMapping("/api/address/callback")
    public String addressCallbackPost(
            @RequestParam(required = false) String roadFullAddr,
            @RequestParam(required = false) String roadAddrPart1,
            @RequestParam(required = false) String roadAddrPart2,
            @RequestParam(required = false) String jibunAddr,
            @RequestParam(required = false) String zipNo,
            @RequestParam(required = false) String addrDetail,
            @RequestParam(required = false) String admCd,
            @RequestParam(required = false) String rnMgtSn,
            @RequestParam(required = false) String bdMgtSn,
            @RequestParam(required = false) String detBdNmList,
            @RequestParam(required = false) String bdNm,
            @RequestParam(required = false) String bdKdcd,
            @RequestParam(required = false) String siNm,
            @RequestParam(required = false) String sggNm,
            @RequestParam(required = false) String emdNm,
            @RequestParam(required = false) String liNm,
            @RequestParam(required = false) String rn,
            @RequestParam(required = false) String udrtYn,
            @RequestParam(required = false) String buldMnnm,
            @RequestParam(required = false) String buldSlno,
            @RequestParam(required = false) String mtYn,
            @RequestParam(required = false) String lnbrMnnm,
            @RequestParam(required = false) String lnbrSlno,
            @RequestParam(required = false) String emdNo,
            Model model) {
        
        log.info("주소 검색 콜백 수신 (POST): roadFullAddr={}, zipNo={}", roadFullAddr, zipNo);
        
        // 모델에 주소 정보 추가
        model.addAttribute("roadFullAddr", roadFullAddr);
        model.addAttribute("roadAddrPart1", roadAddrPart1);
        model.addAttribute("roadAddrPart2", roadAddrPart2);
        model.addAttribute("jibunAddr", jibunAddr);
        model.addAttribute("zipNo", zipNo);
        model.addAttribute("addrDetail", addrDetail);
        model.addAttribute("admCd", admCd);
        model.addAttribute("rnMgtSn", rnMgtSn);
        model.addAttribute("bdMgtSn", bdMgtSn);
        model.addAttribute("detBdNmList", detBdNmList);
        model.addAttribute("bdNm", bdNm);
        model.addAttribute("bdKdcd", bdKdcd);
        model.addAttribute("siNm", siNm);
        model.addAttribute("sggNm", sggNm);
        model.addAttribute("emdNm", emdNm);
        model.addAttribute("liNm", liNm);
        model.addAttribute("rn", rn);
        model.addAttribute("udrtYn", udrtYn);
        model.addAttribute("buldMnnm", buldMnnm);
        model.addAttribute("buldSlno", buldSlno);
        model.addAttribute("mtYn", mtYn);
        model.addAttribute("lnbrMnnm", lnbrMnnm);
        model.addAttribute("lnbrSlno", lnbrSlno);
        model.addAttribute("emdNo", emdNo);
        
        return "common/addressCallback";
    }
} 