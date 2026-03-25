package com.johnson.practica.eventos;

import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.UsuarioRepositorio;
import com.johnson.practica.servicio.EmailServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProyectoEventListener {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private EmailServicio emailServicio;

    @Async
    @EventListener
    public void notificarNuevoProyecto(ProyectoCreadoEvent evento) {
        List<Usuario> todosLosUsuarios = usuarioRepositorio.findAll();
        
        for (Usuario usuario : todosLosUsuarios) {
            if (usuario.isEnabled() && usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()
            && !usuario.getUsername().equals(evento.getAutor())) { 
                emailServicio.enviarCorreoNuevoProyecto(usuario, evento.getProyecto(), evento.getAutor());
            }
        }
    }
}