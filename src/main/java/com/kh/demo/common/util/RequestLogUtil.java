package com.kh.demo.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * 요청 정보 로깅 유틸리티
 */
@Slf4j
public class RequestLogUtil {

    /**
     * 현재 요청의 URL 정보를 가져옵니다.
     */
    public static String getRequestUrl() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getMethod() + " " + request.getRequestURL().toString();
            }
        } catch (Exception e) {
            log.warn("Failed to get request URL", e);
        }
        return "Unknown";
    }

    /**
     * 현재 요청의 상세 정보를 가져옵니다.
     */
    public static String getRequestDetails() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return String.format("Method: %s, URL: %s, RemoteAddr: %s, UserAgent: %s",
                        request.getMethod(),
                        request.getRequestURL().toString(),
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            log.warn("Failed to get request details", e);
        }
        return "Unknown";
    }

    /**
     * 현재 요청의 IP 주소를 가져옵니다.
     */
    public static String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Failed to get client IP", e);
        }
        return "Unknown";
    }

    /**
     * 현재 요청의 User-Agent를 가져옵니다.
     */
    public static String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Failed to get User-Agent", e);
        }
        return "Unknown";
    }
} 