package com.johnson.practica.config;

import com.johnson.practica.modelo.Rol;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.RolRepositorio;
import com.johnson.practica.servicio.UsuarioServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors; 

@Component
public class CargadorDatos implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CargadorDatos.class);

    private final UsuarioServicio usuarioServicio;
    private final RolRepositorio rolRepositorio;

    public CargadorDatos(UsuarioServicio usuarioServicio, RolRepositorio rolRepositorio) {
        this.usuarioServicio = usuarioServicio;
        this.rolRepositorio = rolRepositorio;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Cargando datos iniciales...");
        usuarioServicio.inicializarRoles();

        if (usuarioServicio.buscarPorUsername("admin").isEmpty()) {
            logger.info("Creando usuario 'admin'...");
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword("adminpass"); // Contraseña en texto plano
            admin.setCorreo("admin@example.com");
            admin.setEnabled(true);
            logger.debug("Detalles del usuario 'admin': username={}, password={}, correo={}, enabled={}",
                         admin.getUsername(), admin.getPassword(), admin.getCorreo(), admin.isEnabled());

            Set<Rol> roles = new HashSet<>();
            roles.add(rolRepositorio.findByNombre("ROLE_ADMIN").get());
            roles.add(rolRepositorio.findByNombre("ROLE_CHAMPION").get());
            roles.add(rolRepositorio.findByNombre("ROLE_VIEWER").get());
            admin.setRoles(roles);
            logger.debug("Roles del usuario 'admin': {}", roles.stream().map(Rol::getNombre).collect(Collectors.joining(", ")));

            usuarioServicio.guardarUsuario(admin); // Usar la capa de servicio
            logger.info("Usuario 'admin' creado y guardado.");
        } else {
            logger.info("El usuario 'admin' ya existe. Omitiendo la creación.");
        }
    }
}
