package com.kh.demo.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 스택 트레이스 분석 유틸리티
 */
@Slf4j
public class StackTraceUtil {

    /**
     * 스택 트레이스에서 실제 비즈니스 로직 클래스와 메소드를 찾습니다.
     * Spring Framework 클래스들을 제외하고 실제 애플리케이션 코드를 찾습니다.
     */
    public static StackTraceInfo findBusinessMethod(StackTraceElement[] stackTrace) {
        if (stackTrace == null || stackTrace.length == 0) {
            return new StackTraceInfo("Unknown", "Unknown", "Unknown");
        }

        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            
            // Spring Framework 클래스들 제외
            if (isSpringFrameworkClass(className)) {
                continue;
            }
            
            // 실제 애플리케이션 코드인 경우
            if (className.startsWith("com.kh.demo")) {
                return new StackTraceInfo(
                    className,
                    element.getMethodName(),
                    element.toString()
                );
            }
        }

        // 애플리케이션 코드를 찾지 못한 경우 첫 번째 요소 반환
        StackTraceElement first = stackTrace[0];
        return new StackTraceInfo(
            first.getClassName(),
            first.getMethodName(),
            first.toString()
        );
    }

    /**
     * Spring Framework 클래스인지 확인
     */
    private static boolean isSpringFrameworkClass(String className) {
        return className.startsWith("org.springframework.") ||
               className.startsWith("org.springframework.web.") ||
               className.startsWith("org.springframework.boot.") ||
               className.startsWith("org.springframework.context.") ||
               className.startsWith("org.springframework.core.") ||
               className.startsWith("org.springframework.util.") ||
               className.startsWith("org.springframework.beans.") ||
               className.startsWith("org.springframework.aop.") ||
               className.startsWith("java.") ||
               className.startsWith("sun.") ||
               className.startsWith("com.sun.") ||
               className.startsWith("jdk.");
    }

    /**
     * 스택 트레이스 정보를 담는 클래스
     */
    public static class StackTraceInfo {
        private final String className;
        private final String methodName;
        private final String fullStackTrace;

        public StackTraceInfo(String className, String methodName, String fullStackTrace) {
            this.className = className;
            this.methodName = methodName;
            this.fullStackTrace = fullStackTrace;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getFullStackTrace() {
            return fullStackTrace;
        }

        public String getSimpleClassName() {
            if (className.contains(".")) {
                return className.substring(className.lastIndexOf(".") + 1);
            }
            return className;
        }
    }
} 