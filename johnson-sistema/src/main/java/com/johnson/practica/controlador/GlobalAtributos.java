package com.johnson.practica.controlador;

import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.UsuarioRepositorio;
import com.johnson.practica.servicio.NotificacionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice 
public class GlobalAtributos {

    @Autowired
    private NotificacionServicio notificacionServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @ModelAttribute("totalNoLeidas")
    public long inyectarNotificacionesNoLeidas(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findByUsername(auth.getName());
            
            if (usuarioOpt.isPresent()) {
                return notificacionServicio.contarNoLeidas(usuarioOpt.get());
            }
        }
        return 0;
    }
}