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
    public void crearUsuarioConRol(String username, String correo, String nombreCompleto, String nombreRol, String departamento) throws Exception {
        if (buscarPorUsername(username).isPresent()) {
            throw new Exception("The username '" + username + "' is already in use.");
        }

        String tempPass = generarPasswordAleatoria();
        
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setCorreo(correo);
        usuario.setNombreCompleto(nombreCompleto);
        
        usuario.setDepartamento(departamento); 
        
        usuario.setPassword(passwordEncoder.encode(tempPass));
        usuario.setPasswordChanged(false); 
        usuario.setEnabled(true);

        // Asignamos el Rol dinámicamente (CHAMPION o VIEWER)
        Rol rolSeleccionado = rolRepositorio.findByNombre(nombreRol)
                .orElseThrow(() -> new Exception("Error: El rol " + nombreRol + " no existe en la base de datos."));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolSeleccionado);
        usuario.setRoles(roles);

        // Guardamos en Base de Datos
        usuarioRepositorio.save(usuario);
        
        // Enviamos el correo dinámico 
        String nombreRolLimpio = nombreRol.replace("ROLE_", ""); 
        String mensaje = String.format(
            "Hello %s,\n\nYou have been registered as a %s for the %s department in the Johnson System.\n" +
            "Your access credentials are:\n\n" +
            "Username: %s\n" +
            "Temporary Password: %s\n\n" +
            "For security reasons, the system will ask you to change your password upon your first login.",
            nombreCompleto, nombreRolLimpio, departamento, username, tempPass
        );
        
        emailServicio.enviarAlertaUrgente(correo, "Welcome to the APQP System", mensaje);
    }

    @Transactional
    public Usuario crearChampion(String username, String correo, String nombreCompleto, String departamento) { 
        String tempPass = generarPasswordAleatoria();
        
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setCorreo(correo);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setDepartamento(departamento); 
        usuario.setPassword(passwordEncoder.encode(tempPass));
        usuario.setPasswordChanged(false);
        usuario.setEnabled(true);

        Set<Rol> roles = new HashSet<>();
        rolRepositorio.findByNombre("ROLE_CHAMPION").ifPresent(roles::add);
        usuario.setRoles(roles);

        Usuario guardado = usuarioRepositorio.save(usuario);
        
        String mensaje = String.format(
            "Hello %s,\n\nYou have been registered as a CHAMPION in the Johnson System.\n" +
            "Your access credentials are:\n\n" +
            "Username: %s\n" +
            "Temporary Password: %s\n\n" +
            "For security reasons, the system will ask you to change your password upon your first login.",
            nombreCompleto, username, tempPass
        );
        emailServicio.enviarAlertaUrgente(correo, "Welcome to the APQP System", mensaje);
        
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
    public void solicitarRecuperacion(String correo) {
        Usuario usuario = usuarioRepositorio.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo electrónico"));

        String token = java.util.UUID.randomUUID().toString();
        usuario.setResetToken(token);
        usuario.setResetTokenExpiration(java.time.LocalDateTime.now().plusHours(1));
        usuarioRepositorio.save(usuario);

        emailServicio.enviarEnlaceRecuperacion(usuario.getCorreo(), token, usuario.getNombreCompleto());
    }

    @Transactional
    public void completarRecuperacion(String token, String nuevaContrasena) {
        Usuario usuario = usuarioRepositorio.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Enlace de recuperación no válido"));

        if (usuario.getResetTokenExpiration().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("El enlace de recuperación ha expirado");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaContrasena));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiration(null);
        usuario.setPasswordChanged(true);
        usuarioRepositorio.save(usuario);
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