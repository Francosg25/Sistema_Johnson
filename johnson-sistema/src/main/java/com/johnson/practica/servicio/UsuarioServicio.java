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
    private final com.johnson.practica.repositorio.NotificacionRepositorio notificacionRepositorio;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, 
                          RolRepositorio rolRepositorio, 
                          PasswordEncoder passwordEncoder, 
                          EmailServicio emailServicio,
                          com.johnson.practica.repositorio.NotificacionRepositorio notificacionRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.emailServicio = emailServicio;
        this.notificacionRepositorio = notificacionRepositorio;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    @Transactional
    public void eliminarUsuario(Long id) throws Exception {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new Exception("User not found"));
        
        // Evitar que el admin se borre a sí mismo o que nos quedemos sin admins
        boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_ADMIN"));
        if (esAdmin) {
            long countAdmins = usuarioRepositorio.findAll().stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_ADMIN")))
                    .count();
            if (countAdmins <= 1) {
                throw new Exception("Cannot delete the last administrator in the system.");
            }
        }
        
        // LIMPIEZA DE NOTIFICACIONES ASOCIADAS (Evita FK Violation)
        notificacionRepositorio.deleteAll(notificacionRepositorio.findByDestinatario(usuario));
        
        usuarioRepositorio.delete(usuario);
    }

    @Transactional
    public Usuario guardarUsuario(Usuario usuario) {
        if (!usuario.getPassword().startsWith("$2a$")) { // Solo encriptar si no está ya encriptada
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public void crearUsuarioConRol(String username, String correo, String nombreCompleto, String nombreRol, String departamento, boolean esManager) throws Exception {
        if (buscarPorUsername(username).isPresent()) {
            throw new Exception("The username '" + username + "' is already in use.");
        }
        if (usuarioRepositorio.findByCorreo(correo).isPresent()) {
            throw new Exception("The email '" + correo + "' is already registered.");
        }

        String tempPass = generarPasswordAleatoria();
        
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setCorreo(correo);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setDepartamento(departamento); 
        usuario.setEsManager(esManager);
        usuario.setPassword(passwordEncoder.encode(tempPass));
        usuario.setPasswordChanged(false); 
        usuario.setEnabled(true);

        Rol rolSeleccionado = obtenerOCrearRol(nombreRol);

        Set<Rol> roles = new HashSet<>();
        roles.add(rolSeleccionado);
        usuario.setRoles(roles);

        usuarioRepositorio.save(usuario);
        
        emailServicio.enviarCorreoBienvenida(usuario, tempPass);
    }

    @Transactional
    public void editarUsuario(Long id, String nombreCompleto, String correo, String nombreRol, String departamento, boolean esManager) throws Exception {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new Exception("User not found"));

        // Verificar si el correo ya existe en otro usuario
        Optional<Usuario> userWithEmail = usuarioRepositorio.findByCorreo(correo);
        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
            throw new Exception("The email '" + correo + "' is already in use by another user.");
        }

        usuario.setNombreCompleto(nombreCompleto);
        usuario.setCorreo(correo);
        usuario.setDepartamento(departamento);
        usuario.setEsManager(esManager);

        Rol rolSeleccionado = obtenerOCrearRol(nombreRol);

        Set<Rol> roles = new HashSet<>();
        roles.add(rolSeleccionado);
        usuario.setRoles(roles);

        usuarioRepositorio.save(usuario);
    }

    @Transactional
    public Rol obtenerOCrearRol(String nombreRaw) {
        String nombreLimpio = nombreRaw.trim();
        if (!nombreLimpio.startsWith("ROLE_")) {
            nombreLimpio = "ROLE_" + nombreLimpio.replace(" ", "_").toUpperCase();
        } else {
            nombreLimpio = nombreLimpio.replace(" ", "_").toUpperCase();
        }

        final String nombreFinal = nombreLimpio;
        return rolRepositorio.findByNombre(nombreFinal)
                .orElseGet(() -> {
                    Rol nuevo = new Rol();
                    nuevo.setNombre(nombreFinal);
                    return rolRepositorio.save(nuevo);
                });
    }

    @Transactional
    public void toggleEstado(Long id) throws Exception {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new Exception("User not found"));
        
        // No permitir desactivar al último administrador
        if (usuario.isEnabled()) {
            boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_ADMIN"));
            if (esAdmin) {
                long countAdmins = usuarioRepositorio.findAll().stream()
                        .filter(u -> u.isEnabled() && u.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_ADMIN")))
                        .count();
                if (countAdmins <= 1) {
                    throw new Exception("Cannot disable the last active administrator.");
                }
            }
        }
        
        usuario.setEnabled(!usuario.isEnabled());
        usuarioRepositorio.save(usuario);
    }

    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepositorio.findById(id);
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
        
        emailServicio.enviarCorreoBienvenida(guardado, tempPass);
        
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