package com.dts.restro.auth.service;

import com.dts.restro.restaurant.entity.Restaurant;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@desirestro.com}")
    private String fromAddress;

    @Value("${app.name:DesiRestro}")
    private String appName;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendPasswordResetEmail(String toEmail, String userName,
                                       String resetLink, Restaurant restaurant) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", userName);
            ctx.setVariable("resetLink", resetLink);
            ctx.setVariable("restaurantName", restaurant.getName());
            ctx.setVariable("appName", appName);
            ctx.setVariable("currentYear", LocalDateTime.now().getYear());
            ctx.setVariable("supportEmail", "support@desirestro.com");

            String htmlBody = templateEngine.process("reset-password-email", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your " + restaurant.getName() + " Account Password");
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Password reset email sent to {} for restaurant {}", toEmail, restaurant.getName());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendPasswordResetConfirmationEmail(String toEmail, String userName,
                                                    Restaurant restaurant) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", userName);
            ctx.setVariable("restaurantName", restaurant.getName());
            ctx.setVariable("appName", appName);
            ctx.setVariable("resetDateTime",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
            ctx.setVariable("currentYear", LocalDateTime.now().getYear());
            ctx.setVariable("supportEmail", "support@desirestro.com");

            String htmlBody = templateEngine.process("reset-password-confirmation", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Successful — " + restaurant.getName());
            helper.setText(htmlBody, true);

            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send password-reset confirmation email to {}: {}", toEmail, e.getMessage());
        }
    }
}
