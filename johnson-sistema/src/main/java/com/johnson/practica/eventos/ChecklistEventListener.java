package com.johnson.practica.eventos;

import com.johnson.practica.servicio.NotificacionServicio;
import com.johnson.practica.servicio.BitacoraServicio;
import com.johnson.practica.servicio.EmailServicio;
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.UsuarioRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChecklistEventListener {

    @Autowired
    private NotificacionServicio notificacionServicio;

    @Autowired
    private BitacoraServicio bitacoraServicio;

    @Autowired
    private EmailServicio emailServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Async 
    @EventListener
    public void procesarCambioEntregable(EntregableActualizadoEvent evento) {
        String campo = evento.getNombreCampo();
        String valor = evento.getValorNuevo();
        
        // FILTRO ANTI-SPAM: Ignoramos valores en blanco (excepto si es un borrado intencional, pero usualmente no)
        if (valor == null || valor.trim().isEmpty()) {
            return;
        }

        String autor = evento.getUsuarioAfectado();
        ElementoChecklist elemento = evento.getElemento();
        Proyecto proyecto = elemento.getProyecto();
        String urlProyecto = "/proyectos/checklist/" + proyecto.getId();

        // Identificar si es Gate 2 (Stage 2)
        boolean esGate2 = elemento.getFase() != null && elemento.getFase().startsWith("2");

        // 1. AUDITORÍA GENERAL DE SCORE (Para todas las etapas)
        if ("score".equalsIgnoreCase(campo)) {
            String detalleCambio = "Updated SCORE to '" + valor + "' in deliverable: " + elemento.getNombre();
            bitacoraServicio.registrarAccion(autor, "UPDATE DELIVERABLE", detalleCambio);

            String msj = autor + " updated the score of '" + elemento.getNombre() + "' to: " + valor;
            notificacionServicio.alertarATodos("Deliverable Score Updated", msj, "SUCCESS", urlProyecto, autor);
        }

        // 2. AUDITORÍA ESPECÍFICA PARA GATE 2 (Loguear fechas y estado)
        if (esGate2) {
            String detalleGate2 = null;
            if ("fechaPlan".equalsIgnoreCase(campo)) {
                detalleGate2 = "Updated PLAN DATE to " + valor + " in Gate 2 item: " + elemento.getNombre();
            } else if ("fechaReal".equalsIgnoreCase(campo)) {
                detalleGate2 = "Updated REAL DATE to " + valor + " in Gate 2 item: " + elemento.getNombre();
            } else if ("estado".equalsIgnoreCase(campo)) {
                String valorAmigable = valor;
                if ("OK".equalsIgnoreCase(valor)) valorAmigable = "YES";
                else if ("NOK".equalsIgnoreCase(valor)) valorAmigable = "NO";
                
                detalleGate2 = "Updated COMPLIANCE to '" + valorAmigable + "' in Gate 2 item: " + elemento.getNombre();
            }

            if (detalleGate2 != null) {
                bitacoraServicio.registrarAccion(autor, "GATE 2 UPDATE", detalleGate2);
            }
        }

        boolean esCampoComentario = "comentarios".equalsIgnoreCase(campo) || 
                                    "comentario".equalsIgnoreCase(campo) || 
                                    "remarks".equalsIgnoreCase(campo) || 
                                    "remark".equalsIgnoreCase(campo);

        if (esCampoComentario && valor.contains("@")) {
            Pattern pattern = Pattern.compile("@(\\w+)");
            Matcher matcher = pattern.matcher(valor);
            
            while (matcher.find()) {
                String usernameMencionado = matcher.group(1);
                String msj = autor + " mentioned you in: " + elemento.getNombre();
                
                // Notificación en pantalla 
                notificacionServicio.alertarAUsuario(usernameMencionado, "You were mentioned", msj, "INFO", urlProyecto, autor);
                
                // Buscar al usuario y enviarle el correo
                usuarioRepositorio.findByUsername(usernameMencionado).ifPresent(user -> {
                    if (user.isEnabled() && user.getCorreo() != null) {
                        emailServicio.enviarCorreoMencion(
                            user, 
                            autor, 
                            elemento.getNombre(), 
                            proyecto.getNombre(), 
                            proyecto.getId()
                        );
                    }
                });
            }
        }
    }
}