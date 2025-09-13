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

    @Value("${MAIL_FROM:Tempo <no-reply@tempo-app.dev>}")
    private String mailFrom;

    public void sendPasswordResetCode(String toEmail, String code, int ttlMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(mailFrom);
        message.setSubject("🔐 Code de réinitialisation - Tempo");
        message.setText(
                "Bonjour,\n\n" +
                        "Vous avez demandé la réinitialisation de votre mot de passe sur Tempo.\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "   VOTRE CODE DE RÉINITIALISATION : " + code + "\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "⏰ Ce code expire dans " + ttlMinutes + " minutes.\n\n" +
                        "Pour des raisons de sécurité :\n" +
                        "• Utilisez-le uniquement sur l'application Tempo\n" +
                        "• Si vous n'êtes pas à l'origine de cette demande, ignorez cet email\n\n" +
                        "Besoin d'aide ? Contactez notre support à cette adresse : pierre.guillard.dev@gmail.com\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Tempo\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        mailSender.send(message);
    }
}
