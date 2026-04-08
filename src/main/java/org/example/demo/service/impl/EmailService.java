package org.example.demo.service.impl;

import org.example.demo.service.II18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private II18nService i18nService;

    @Value("${spring.mail.from:${spring.mail.username:}}")
    private String fromAddress;

    // Phương thức tạo OTP ngẫu nhiên
    public String generateOTP() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(999999); // Tạo OTP 6 chữ số
        return String.format("%06d", code); // Đảm bảo OTP có 6 chữ số
    }

    // Phương thức gửi OTP qua email
    public void sendOTPEmail(String to, String code) {
        String subject = i18nService.getMessage("email.otp.subject");
        String text = i18nService.getMessage("email.otp.body", code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(fromAddress);

        javaMailSender.send(message);
    }

}
