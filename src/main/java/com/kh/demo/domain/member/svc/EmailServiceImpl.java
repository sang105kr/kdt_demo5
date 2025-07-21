package com.kh.demo.domain.member.svc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("이메일 발송 성공: {}", to);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", to, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
    
    @Override
    public void sendVerificationCode(String to, String verificationCode) {
        String subject = "[KDT Demo] 이메일 인증 코드";
        String content = String.format("""
            안녕하세요!
            
            이메일 인증 코드는 다음과 같습니다:
            
            인증 코드: %s
            
            이 코드는 10분간 유효합니다.
            인증 코드를 입력하여 이메일 인증을 완료해주세요.
            
            감사합니다.
            KDT Demo 팀
            """, verificationCode);
        
        sendEmail(to, subject, content);
    }
    
    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "[KDT Demo] 비밀번호 재설정";
        String content = String.format("""
            안녕하세요!
            
            비밀번호 재설정 요청이 접수되었습니다.
            
            아래 링크를 클릭하여 비밀번호를 재설정하세요:
            http://localhost:9082/member/password/reset?token=%s
            
            이 링크는 30분간 유효합니다.
            
            본인이 요청하지 않았다면 이 이메일을 무시하세요.
            
            감사합니다.
            KDT Demo 팀
            """, resetToken);
        
        sendEmail(to, subject, content);
    }
    
    @Override
    public void sendWelcomeEmail(String to, String nickname) {
        String subject = "[KDT Demo] 회원가입을 환영합니다!";
        String content = String.format("""
            안녕하세요, %s님!
            
            KDT Demo에 가입해주셔서 감사합니다.
            
            이제 다양한 상품을 구매하고 리뷰를 작성할 수 있습니다.
            
            즐거운 쇼핑 되세요!
            
            감사합니다.
            KDT Demo 팀
            """, nickname);
        
        sendEmail(to, subject, content);
    }
} 