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

 

    @Async 
    @EventListener
    public void procesarCambioEntregable(EntregableActualizadoEvent evento) {
        String campo = evento.getNombreCampo();
        String valor = evento.getValorNuevo();
        
        // FILTRO ANTI-SPAM: Ignoramos valores en blanco
        if (valor == null || valor.trim().isEmpty()) {
            return;
        }

        String autor = evento.getUsuarioAfectado();
        ElementoChecklist elemento = evento.getElemento();
        Proyecto proyecto = elemento.getProyecto();
        String urlProyecto = "/proyectos/checklist/" + proyecto.getId();

        // 1. BITÁCORA Y NOTIFICACIONES DE SCORE
        if ("score".equalsIgnoreCase(campo)) {
            String detalleCambio = "Updated SCORE to '" + valor + "' in deliverable: " + elemento.getNombre();
            bitacoraServicio.registrarAccion(autor, "UPDATE DELIVERABLE", detalleCambio);

            String msj = autor + " updated the score of '" + elemento.getNombre() + "' to: " + valor;
            notificacionServicio.alertarATodos("Deliverable Score Updated", msj, "SUCCESS", urlProyecto, autor);
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
                
                notificacionServicio.alertarAUsuario(usernameMencionado, "You were mentioned", msj, "INFO", urlProyecto, autor);
            }
        }
    }
}