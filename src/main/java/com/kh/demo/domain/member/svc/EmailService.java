package com.kh.demo.domain.member.svc;

public interface EmailService {
    
    /**
     * 이메일 인증 코드 발송
     * @param to 수신자 이메일
     * @param subject 제목
     * @param content 내용
     */
    void sendEmail(String to, String subject, String content);
    
    /**
     * 이메일 인증 코드 발송
     * @param to 수신자 이메일
     * @param verificationCode 인증 코드
     */
    void sendVerificationCode(String to, String verificationCode);
    
    /**
     * 비밀번호 재설정 이메일 발송
     * @param to 수신자 이메일
     * @param resetToken 비밀번호 재설정 토큰
     */
    void sendPasswordResetEmail(String to, String resetToken);
    
    /**
     * 회원가입 환영 이메일 발송
     * @param to 수신자 이메일
     * @param nickname 닉네임
     */
    void sendWelcomeEmail(String to, String nickname);
} 