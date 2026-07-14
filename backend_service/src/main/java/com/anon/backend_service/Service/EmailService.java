package com.anon.backend_service.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private final JavaMailSender javaMailSender;
    
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    
    public void sendVerificationEmail(String to, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Verification Code");
        message.setText("Your 6-digit verification code is: " + verificationCode + "\n\nThis code expires in 15 minutes.");
        
        try {
            javaMailSender.send(message);
            System.out.println("Verification email sent successfully to: " + to);
            System.out.println("Verification code: " + verificationCode);
        } catch (Exception e) {
            System.err.println("Failed to send verification email to: " + to);
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}