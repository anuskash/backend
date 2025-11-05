package com.uon.marketplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean startTls;

    @Value("${spring.mail.properties.mail.smtp.starttls.required:true}")
    private boolean startTlsRequired;

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        // Build a JavaMailSender that uses configured properties when provided;
        // otherwise behaves as a harmless dev fallback.
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        if (host != null && !host.isBlank()) {
            mailSender.setHost(host);
            mailSender.setPort(port);
            if (username != null && !username.isBlank()) {
                mailSender.setUsername(username);
            }
            if (password != null && !password.isBlank()) {
                mailSender.setPassword(password);
            }

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", String.valueOf(smtpAuth));
            props.put("mail.smtp.starttls.enable", String.valueOf(startTls));
            props.put("mail.smtp.starttls.required", String.valueOf(startTlsRequired));
            // Reasonable timeouts
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");
        }

        return mailSender;
    }
}
