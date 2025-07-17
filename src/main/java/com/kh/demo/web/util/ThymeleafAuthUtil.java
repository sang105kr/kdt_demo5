package com.kh.demo.web.util;

import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.domain.shared.enums.MemberType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Thymeleaf에서 사용할 권한 체크 유틸리티
 * View단에서 권한 체크를 쉽게 할 수 있도록 도와줍니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThymeleafAuthUtil {

    private final MemberSVC memberSVC;

    /**
     * 관리자 권한 체크
     */
    public boolean isAdmin(Long memberId) {
        if (memberId == null) return false;
        return memberSVC.isAdmin(memberId);
    }

    /**
     * VIP 회원 권한 체크
     */
    public boolean isVip(Long memberId) {
        if (memberId == null) return false;
        return memberSVC.isVip(memberId);
    }

    /**
     * 특정 권한 보유 여부 체크
     */
    public boolean hasPermission(Long memberId, String permission) {
        if (memberId == null) return false;
        return memberSVC.hasPermission(memberId, permission);
    }

    /**
     * 회원 타입에 따른 권한 체크
     */
    public boolean hasMemberTypePermission(Long memberId, MemberType requiredType) {
        if (memberId == null) return false;
        
        try {
            // 관리자는 모든 권한을 가짐
            if (isAdmin(memberId)) {
                return true;
            }

            // VIP 회원 체크
            if (requiredType == MemberType.VIP && isVip(memberId)) {
                return true;
            }

            // 일반 회원 체크
            return requiredType == MemberType.NORMAL;
        } catch (Exception e) {
            log.warn("권한 체크 중 오류 발생: memberId={}, requiredType={}", memberId, requiredType, e);
            return false;
        }
    }

    /**
     * 회원 구분 코드로 권한 체크 (기존 방식과의 호환성)
     */
    public boolean hasGubunPermission(Long memberId, Long gubunCodeId) {
        if (memberId == null || gubunCodeId == null) return false;
        
        try {
            // 관리자 권한 체크 (ADMIN1, ADMIN2)
            if (gubunCodeId == 4L || gubunCodeId == 5L) {
                return isAdmin(memberId);
            }
            
            // VIP 권한 체크
            if (gubunCodeId == 3L) {
                return isVip(memberId);
            }
            
            // 일반 회원 권한 체크
            return gubunCodeId == 2L;
        } catch (Exception e) {
            log.warn("권한 체크 중 오류 발생: memberId={}, gubunCodeId={}", memberId, gubunCodeId, e);
            return false;
        }
    }
} 