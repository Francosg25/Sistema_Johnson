package com.johnson.practica.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServicio {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@johnson.com}")
    private String mailFrom;

    @Value("${app.mail.display-name:Johnson APQP System}")
    private String displayName;

    @Async 
    public void enviarAlertaUrgente(String destinatario, String asunto, String mensaje) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(mailFrom, displayName);
            helper.setTo(destinatario);
            helper.setSubject("[APQP ALERT] " + asunto);
            helper.setText(mensaje + "\n\nPlease review the APQP system as soon as possible.\n\nRegards,\n" + displayName + ".");
            
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + destinatario);
        } catch (Exception e) {
            System.err.println("CRITICAL EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void enviarEnlaceRecuperacion(String destinatario, String token, String nombreUsuario) {
        String enlace = "http://localhost:8081/recuperar-password?token=" + token;
        String cuerpo = String.format(
            "Hello %s,\n\nYou have requested to reset your password in the Johnson APQP System.\n" +
            "Click on the following link to create a new password (expires in 1 hour):\n\n" +
            "%s\n\n" +
            "If you did not request this change, you can ignore this email.",
            nombreUsuario, enlace
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(mailFrom, displayName);
            helper.setTo(destinatario);
            helper.setSubject("Password Reset - APQP System");
            helper.setText(cuerpo);
            
            mailSender.send(message);
            System.out.println("Recovery email sent to: " + destinatario);
        } catch (Exception e) {
            System.err.println("Error sending recovery email: " + e.getMessage());
        }
    }
}
