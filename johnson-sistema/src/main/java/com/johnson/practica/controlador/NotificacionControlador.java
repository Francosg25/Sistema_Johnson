package com.johnson.practica.controlador;

import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.UsuarioRepositorio;
import com.johnson.practica.servicio.NotificacionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionControlador {

    @Autowired
    private NotificacionServicio notificacionServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @GetMapping("/resumen")
    public Map<String, Object> obtenerResumen(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> res = new HashMap<>();
        if (userDetails == null) return res;

        Usuario usuario = usuarioRepositorio.findByUsername(userDetails.getUsername()).orElse(null);
        if (usuario != null) {
            res.put("totalNoLeidas", notificacionServicio.contarNoLeidas(usuario));
            res.put("ultimas", notificacionServicio.obtenerNoLeidas(usuario));
        }
        return res;
    }

    @PostMapping("/marcar-leida/{id}")
    public void marcarLeida(@PathVariable Long id) {
        notificacionServicio.marcarComoLeida(id);
    }

    @PostMapping("/marcar-todas-leidas")
    public void marcarTodasLeidas(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return;
        Usuario usuario = usuarioRepositorio.findByUsername(userDetails.getUsername()).orElse(null);
        if (usuario != null) {
            notificacionServicio.marcarTodasComoLeidas(usuario);
        }
    }
}