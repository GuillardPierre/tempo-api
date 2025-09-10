package com.tempo.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${MAIL_FROM:Tempo <no-reply@tempo.app>}")
    private String mailFrom;

    public void sendPasswordResetCode(String toEmail, String code, int ttlMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(mailFrom);
        message.setSubject("Votre code de réinitialisation");
        message.setText("Voici votre code: " + code + "\nIl expire dans " + ttlMinutes
                + " minutes.\nSi vous n'êtes pas à l'origine de cette demande, ignorez cet email.");
        mailSender.send(message);
    }
}
