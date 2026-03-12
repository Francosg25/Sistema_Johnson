package com.johnson.practica.servicio;

import com.johnson.practica.modelo.Notificacion;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.NotificacionRepositorio;
import com.johnson.practica.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionServicio {

    @Autowired
    private NotificacionRepositorio repositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired(required = false)
    private EmailServicio emailServicio;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void alertarATodos(String titulo, String mensaje, String tipo, String link, String autor) {
        List<Usuario> todosLosUsuarios = usuarioRepositorio.findAll();
        
        for (Usuario usuario : todosLosUsuarios) {
            Notificacion notif = new Notificacion(titulo, mensaje, tipo, usuario, autor);
            notif.setLink(link);
            repositorio.save(notif);

            if (messagingTemplate != null) {
                final String username = usuario.getUsername();
                org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            messagingTemplate.convertAndSendToUser(
                                username, 
                                "/queue/notificaciones", 
                                "NUEVA_NOTIF"
                            );
                        }
                    }
                );
            }
        }
    }

    @Transactional
    public void alertarAUsuario(String usernameDestino, String titulo, String mensaje, String tipo, String link, String autor) {
        
        usuarioRepositorio.findByUsername(usernameDestino).ifPresent(usuario -> {
            
            Notificacion notif = new Notificacion(titulo, mensaje, tipo, usuario, autor);
            notif.setLink(link);
            
            repositorio.save(notif);

            if (messagingTemplate != null) {
                final String username = usuario.getUsername();
                org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            messagingTemplate.convertAndSendToUser(
                                username, 
                                "/queue/notificaciones", 
                                "NUEVA_NOTIF"
                            );
                        }
                    }
                );
            }
        });
    }

    @Transactional
    public void alertarAAdministradores(String titulo, String mensaje, String tipo, String link, String autor) {
        List<Usuario> usuarios = usuarioRepositorio.findAll();
        for (Usuario u : usuarios) {
            boolean esAdmin = u.getRoles().stream().anyMatch(r -> r.getNombre().contains("ADMIN"));
            if (esAdmin) {
                Notificacion notif = new Notificacion(titulo, mensaje, tipo, u, autor);
                notif.setLink(link);
                repositorio.save(notif);
            
                if (messagingTemplate != null) {
                    final String username = u.getUsername();
                    org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                        new org.springframework.transaction.support.TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                messagingTemplate.convertAndSendToUser(username, "/queue/notificaciones", "NUEVA_NOTIF");
                            }
                        }
                    );
                }
            }
        }
    }
    public List<Notificacion> obtenerNoLeidas(Usuario usuario) {
        return repositorio.findByDestinatarioAndLeidaOrderByFechaCreacionDesc(usuario, false);
    }

    public long contarNoLeidas(Usuario usuario) {
        return repositorio.countByDestinatarioAndLeida(usuario, false);
    }

    @Transactional
    public void marcarComoLeida(Long id) {
        repositorio.findById(id).ifPresent(n -> {
            n.setLeida(true);
            repositorio.save(n);
        });
    }

    @Transactional
    public void marcarTodasComoLeidas(Usuario usuario) {
        List<Notificacion> noLeidas = repositorio.findByDestinatarioAndLeidaOrderByFechaCreacionDesc(usuario, false);
        noLeidas.forEach(n -> n.setLeida(true));
        repositorio.saveAll(noLeidas);
    }
}