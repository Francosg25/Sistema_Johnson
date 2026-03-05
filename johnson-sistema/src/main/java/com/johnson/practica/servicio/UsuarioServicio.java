package com.johnson.practica.servicio;

import com.johnson.practica.modelo.Rol;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.RolRepositorio;
import com.johnson.practica.repositorio.UsuarioRepositorio;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final EmailServicio emailServicio;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio, PasswordEncoder passwordEncoder, EmailServicio emailServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.emailServicio = emailServicio;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    @Transactional
    public Usuario guardarUsuario(Usuario usuario) {
        if (!usuario.getPassword().startsWith("$2a$")) { // Solo encriptar si no está ya encriptada
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public Usuario crearChampion(String username, String correo, String nombreCompleto) {
        String tempPass = generarPasswordAleatoria();
        
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setCorreo(correo);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setPassword(passwordEncoder.encode(tempPass));
        usuario.setPasswordChanged(false);
        usuario.setEnabled(true);

        Set<Rol> roles = new HashSet<>();
        rolRepositorio.findByNombre("ROLE_CHAMPION").ifPresent(roles::add);
        usuario.setRoles(roles);

        Usuario guardado = usuarioRepositorio.save(usuario);
        
        String mensaje = String.format(
            "Hola %s,\n\nHas sido registrado como CHAMPION en el Sistema Johnson.\n" +
            "Tus credenciales de acceso son:\n\n" +
            "Usuario: %s\n" +
            "Contraseña temporal: %s\n\n" +
            "Por seguridad, el sistema te pedirá cambiar tu contraseña al primer ingreso.",
            nombreCompleto, username, tempPass
        );
        emailServicio.enviarAlertaUrgente(correo, "Bienvenida al Sistema APQP", mensaje);
        
        return guardado;
    }

    private String generarPasswordAleatoria() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
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
