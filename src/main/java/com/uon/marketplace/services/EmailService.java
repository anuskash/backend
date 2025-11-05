package com.uon.marketplace.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String body) {
        try {
            if (fromAddress == null || fromAddress.isBlank()) {
                // No SMTP configured; log to console as a fallback for dev
                System.out.println("[DEV EMAIL] To: " + to);
                System.out.println("[DEV EMAIL] Subject: " + subject);
                System.out.println("[DEV EMAIL] Body:\n" + body);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Don't fail business flow if email fails in dev
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
