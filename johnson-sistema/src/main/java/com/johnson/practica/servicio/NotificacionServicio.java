package com.johnson.practica.servicio;

import com.johnson.practica.modelo.Notificacion;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.NotificacionRepositorio;
import com.johnson.practica.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        
        // Buscamos departamento del autor si es champion
        String departamento = null;
        if (autor != null && !"System".equalsIgnoreCase(autor)) {
            Optional<Usuario> uOpt = usuarioRepositorio.findByUsername(autor);
            if (uOpt.isPresent()) {
                Usuario u = uOpt.get();
                boolean esChamp = u.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_CHAMPION"));
                if (esChamp) {
                    departamento = u.getDepartamento();
                }
            }
        }

        for (Usuario usuario : todosLosUsuarios) {
            Notificacion notif = new Notificacion(titulo, mensaje, tipo, usuario, autor);
            notif.setLink(link);
            notif.setAutorDepartamento(departamento);
            repositorio.save(notif);
        }

        // Enviar por WebSocket después del commit para evitar race conditions
        if (messagingTemplate != null) {
            enviarNotificacionWebSocketATodos();
        }
    }

    private void enviarNotificacionWebSocketATodos() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        List<Usuario> todos = usuarioRepositorio.findAll();
                        for (Usuario u : todos) {
                            messagingTemplate.convertAndSendToUser(u.getUsername(), "/queue/notificaciones", "NUEVA_NOTIF");
                        }
                    }
                }
            );
        } else {
            List<Usuario> todos = usuarioRepositorio.findAll();
            for (Usuario u : todos) {
                messagingTemplate.convertAndSendToUser(u.getUsername(), "/queue/notificaciones", "NUEVA_NOTIF");
            }
        }
    }

    @Transactional
    public void alertarAUsuario(String usernameDestino, String titulo, String mensaje, String tipo, String link, String autor) {
        // Buscamos departamento del autor si es champion
        String deptoFinal = null;
        if (autor != null && !"System".equalsIgnoreCase(autor)) {
            Optional<Usuario> uOpt = usuarioRepositorio.findByUsername(autor);
            if (uOpt.isPresent()) {
                Usuario u = uOpt.get();
                boolean esChamp = u.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_CHAMPION"));
                if (esChamp) deptoFinal = u.getDepartamento();
            }
        }
        final String departamento = deptoFinal;

        usuarioRepositorio.findByUsername(usernameDestino).ifPresent(usuario -> {
            Notificacion notif = new Notificacion(titulo, mensaje, tipo, usuario, autor);
            notif.setLink(link);
            notif.setAutorDepartamento(departamento);
            repositorio.save(notif);

            if (messagingTemplate != null) {
                enviarNotificacionWebSocketAUsuario(usuario.getUsername());
            }
        });
    }

    private void enviarNotificacionWebSocketAUsuario(String username) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSendToUser(username, "/queue/notificaciones", "NUEVA_NOTIF");
                    }
                }
            );
        } else {
            messagingTemplate.convertAndSendToUser(username, "/queue/notificaciones", "NUEVA_NOTIF");
        }
    }

    

    @Transactional
    public void alertarAAdministradores(String titulo, String mensaje, String tipo, String link, String autor) {
        // Buscamos departamento del autor si es champion
        String deptoFinal = null;
        if (autor != null && !"System".equalsIgnoreCase(autor)) {
            Optional<Usuario> uOpt = usuarioRepositorio.findByUsername(autor);
            if (uOpt.isPresent()) {
                Usuario u = uOpt.get();
                boolean esChamp = u.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_CHAMPION"));
                if (esChamp) deptoFinal = u.getDepartamento();
            }
        }
        final String departamento = deptoFinal;

        List<Usuario> usuarios = usuarioRepositorio.findAll();
        List<String> admins = new java.util.ArrayList<>();
        for (Usuario u : usuarios) {
            boolean esAdmin = u.getRoles().stream().anyMatch(r -> r.getNombre().contains("ADMIN"));
            if (esAdmin) {
                Notificacion notif = new Notificacion(titulo, mensaje, tipo, u, autor);
                notif.setLink(link);
                notif.setAutorDepartamento(departamento);
                repositorio.save(notif);
                admins.add(u.getUsername());
            }
        }
        
        if (messagingTemplate != null && !admins.isEmpty()) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            for (String username : admins) {
                                messagingTemplate.convertAndSendToUser(username, "/queue/notificaciones", "NUEVA_NOTIF");
                            }
                        }
                    }
                );
            } else {
                for (String username : admins) {
                    messagingTemplate.convertAndSendToUser(username, "/queue/notificaciones", "NUEVA_NOTIF");
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