package com.johnson.practica.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.modelo.Usuario;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServicio {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    // Variables de configuración de correo
    @Value("${app.mail.from:noreply@johnsonelectric.com}")
    private String mailFrom;

    @Value("${app.mail.display-name:Johnson APQP System}")
    private String displayName;

    @Value("${app.base-url:http://localhost:8081}")
    private String appBaseUrl;


    @Async
    public void enviarCorreoNuevoProyecto(Usuario destinatario, Proyecto proyecto, String autor) {
        try {
            Context context = new Context();
            context.setVariable("usuario", destinatario);
            context.setVariable("proyecto", proyecto);
            context.setVariable("autor", autor);
            
            // Usando appBaseUrl en lugar de localhost
            context.setVariable("appUrl", appBaseUrl + "/proyectos/checklist/" + proyecto.getId());

            String htmlContent = templateEngine.process("email/nuevo-proyecto", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom, displayName);
            helper.setTo(destinatario.getCorreo());
            helper.setSubject("[NEW PROJECT] " + proyecto.getNombre());
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending project notification email: " + e.getMessage());
        }
    }

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
        String enlace = appBaseUrl + "/recuperar-password?token=" + token;
        
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

    @Async
    public void enviarWeeklyDigest(Usuario destinatario, java.util.List<com.johnson.practica.modelo.ElementoChecklist> tareas) {
        try {
            Context context = new Context();
            context.setVariable("usuario", destinatario);
            context.setVariable("tareas", tareas);
            
            // Usando appBaseUrl en lugar de localhost
            context.setVariable("appUrl", appBaseUrl);
            
            String html = templateEngine.process("email/weekly-digest", context);
            
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(mailFrom, displayName);
            helper.setTo(destinatario.getCorreo());
            helper.setSubject("📅 APQP Weekly Digest - Tareas Pendientes");
            helper.setText(html, true);
            
            mailSender.send(message);
            System.out.println("Weekly Digest enviado a: " + destinatario.getCorreo() + " con " + tareas.size() + " tareas.");
        } catch (Exception e) {
            System.err.println("Error enviando Weekly Digest a: " + destinatario.getCorreo());
        }
    }

    @Async
    public void enviarCorreoMencion(Usuario destinatario, String autor, String tarea, String proyecto, Long id) {
        try {
            Context context = new Context();
            context.setVariable("usuario", destinatario);
            context.setVariable("autor", autor);
            context.setVariable("tarea", tarea);
            context.setVariable("proyecto", proyecto);
            context.setVariable("appUrl", appBaseUrl + "/proyectos/checklist/" + id);
            
            String html = templateEngine.process("email/mencion", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(mailFrom, displayName);
            helper.setTo(destinatario.getCorreo());
            helper.setSubject("💬 Mention in APQP: " + tarea);
            helper.setText(html, true);
            
            mailSender.send(message);
        } catch (Exception e) { 
            System.err.println("Error enviando correo de mención: " + e.getMessage()); 
        }
    }

    private void enviarHtml(String to, String subject, String template, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(mailFrom, displayName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(templateEngine.process(template, context), true);
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error enviando correo HTML a " + to + ": " + e.getMessage());
        }
    }

    @Async
    public void enviarCorreoBienvenida(Usuario destinatario, String passwordTemporal) {
        Context context = new Context();
        context.setVariable("usuario", destinatario);
        context.setVariable("password", passwordTemporal); 
        context.setVariable("appUrl", appBaseUrl);
        
        enviarHtml(destinatario.getCorreo(), "Welcome to the APQP System", "email/nuevo-usuario", context);
    }
}
