package com.johnson.practica.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServicio {

    @Autowired
    private JavaMailSender mailSender;

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
}
