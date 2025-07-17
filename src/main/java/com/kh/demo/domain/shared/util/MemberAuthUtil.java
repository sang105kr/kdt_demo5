package com.kh.demo.domain.shared.util;

import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.domain.shared.enums.MemberType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 회원 권한 체크 유틸리티
 * 컨트롤러나 서비스에서 권한 체크를 쉽게 할 수 있도록 도와줍니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberAuthUtil {

    private final MemberSVC memberSVC;

    /**
     * 관리자 권한 체크
     */
    public boolean isAdmin(Long memberId) {
        return memberSVC.isAdmin(memberId);
    }

    /**
     * VIP 회원 권한 체크
     */
    public boolean isVip(Long memberId) {
        return memberSVC.isVip(memberId);
    }

    /**
     * 특정 권한 보유 여부 체크
     */
    public boolean hasPermission(Long memberId, String permission) {
        return memberSVC.hasPermission(memberId, permission);
    }

    /**
     * 관리자 권한 체크 (예외 발생)
     */
    public void requireAdmin(Long memberId) {
        if (!isAdmin(memberId)) {
            throw new SecurityException("관리자 권한이 필요합니다.");
        }
    }

    /**
     * VIP 회원 권한 체크 (예외 발생)
     */
    public void requireVip(Long memberId) {
        if (!isVip(memberId)) {
            throw new SecurityException("VIP 회원 권한이 필요합니다.");
        }
    }

    /**
     * 특정 권한 체크 (예외 발생)
     */
    public void requirePermission(Long memberId, String permission) {
        if (!hasPermission(memberId, permission)) {
            throw new SecurityException("권한이 부족합니다: " + permission);
        }
    }

    /**
     * 회원 타입에 따른 권한 체크
     */
    public boolean hasMemberTypePermission(Long memberId, MemberType requiredType) {
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
} 