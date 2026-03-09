package com.johnson.practica.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service

public class EmailServicio {

    @Autowired
    private JavaMailSender mailSender;

    @Async 
    public void enviarAlertaUrgente(String destinatario, String asunto, String mensaje) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(destinatario);
            
            mail.setSubject("[ALERTA APQP] " + asunto);
            mail.setText(mensaje + "\n\nPor favor, revise el sistema APQP lo antes posible.\n\nAtte: Johnson Electric APQP System.");
            
            mailSender.send(mail);
            System.out.println("Correo enviado con éxito a: " + destinatario);
        } catch (Exception e) {
            System.err.println("Error enviando correo: " + e.getMessage());
        }
    }

    public void enviarEnlaceRecuperacion(String destinatario, String token, String nombreUsuario) {
        String enlace = "http://localhost:8081/recuperar-password?token=" + token;
        String cuerpo = String.format(
            "Hola %s,\n\nHas solicitado restablecer tu contraseña en el Sistema Johnson.\n" +
            "Haz clic en el siguiente enlace para crear una nueva contraseña (vence en 1 hora):\n\n" +
            "%s\n\n" +
            "Si no solicitaste este cambio, puedes ignorar este correo.",
            nombreUsuario, enlace
        );

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(destinatario);
            mail.setSubject("Restablecer Contraseña - Sistema APQP");
            mail.setText(cuerpo);
            mailSender.send(mail);
        } catch (Exception e) {
            System.err.println("Error enviando correo de recuperación: " + e.getMessage());
        }
    }
}
