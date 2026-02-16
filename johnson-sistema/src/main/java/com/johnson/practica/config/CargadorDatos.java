package com.johnson.practica.config;

import com.johnson.practica.modelo.Rol;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.RolRepositorio;
import com.johnson.practica.servicio.UsuarioServicio;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
public class CargadorDatos implements CommandLineRunner {

    private final UsuarioServicio usuarioServicio;
    private final RolRepositorio rolRepositorio;

    public CargadorDatos(UsuarioServicio usuarioServicio, RolRepositorio rolRepositorio) {
        this.usuarioServicio = usuarioServicio;
        this.rolRepositorio = rolRepositorio;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        usuarioServicio.inicializarRoles();

        if (usuarioServicio.buscarPorUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword("adminpass"); // Contraseña en texto plano
            admin.setCorreo("admin@example.com");
            admin.setEnabled(true);

            Set<Rol> roles = new HashSet<>();
            roles.add(rolRepositorio.findByNombre("ROLE_ADMIN").get());
            roles.add(rolRepositorio.findByNombre("ROLE_CHAMPION").get());
            roles.add(rolRepositorio.findByNombre("ROLE_VIEWER").get());
            admin.setRoles(roles);

            usuarioServicio.guardarUsuario(admin); // Usar la capa de servicio
            System.out.println("Usuario admin creado.");
        }
    }
}
