package com.johnson.practica.servicio;

import com.johnson.practica.modelo.Rol;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.RolRepositorio;
import com.johnson.practica.repositorio.UsuarioRepositorio;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario guardarUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        // Asumiendo que los roles ya son entidades gestionadas (ej. desde CargadorDatos)
        // No es necesario volver a buscar/guardar roles aquí si ya están en la DB
        return usuarioRepositorio.save(usuario);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepositorio.findByUsername(username);
    }

    @Transactional
    public void inicializarRoles() {
        if (rolRepositorio.findByNombre("ROLE_ADMIN").isEmpty()) {
            Rol adminRole = new Rol();
            adminRole.setNombre("ROLE_ADMIN");
            rolRepositorio.save(adminRole);
        }
        if (rolRepositorio.findByNombre("ROLE_CHAMPION").isEmpty()) {
            Rol championRole = new Rol();
            championRole.setNombre("ROLE_CHAMPION");
            rolRepositorio.save(championRole);
        }
        if (rolRepositorio.findByNombre("ROLE_VIEWER").isEmpty()) {
            Rol viewerRole = new Rol();
            viewerRole.setNombre("ROLE_VIEWER");
            rolRepositorio.save(viewerRole);
        }
    }
}
