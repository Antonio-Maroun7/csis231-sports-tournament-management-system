package com.antonio.sportstournament.email;

import com.antonio.sportstournament.exception.EmailDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends login verification codes, with a safe development fallback when mail is not configured. */
@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String username;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        @Value("${spring.mail.username:}") String username) {
        this.mailSenderProvider = mailSenderProvider;
        this.username = username;
    }

    public void sendLoginCode(String toEmail, String userName, String code, int validityMinutes) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (username.isBlank() || sender == null) {
            log.warn("[DEV] 2FA code for {}: {} (mail not configured)", toEmail, code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(toEmail);
        message.setSubject("Your Sports Tournament login code");
        message.setText("Hello " + userName + ",\n\nYour login code is:\n" + code
                + "\n\nThis code is valid for " + validityMinutes + " minutes.\n"
                + "If you did not try to log in, change your password.");
        try {
            sender.send(message);
        } catch (MailException ex) {
            throw new EmailDeliveryException("We could not send the verification email. Please try again.");
        }
    }
}
