package com.johnson.practica.config;

import com.johnson.practica.modelo.Rol;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.RolRepositorio;
import com.johnson.practica.servicio.UsuarioServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class CargadorDatos implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CargadorDatos.class);

    private final UsuarioServicio usuarioServicio;
    private final RolRepositorio rolRepositorio;

    @Value("${app.admin.default-password}")
    private String defaultAdminPassword;

    public CargadorDatos(UsuarioServicio usuarioServicio, RolRepositorio rolRepositorio) {
        this.usuarioServicio = usuarioServicio;
        this.rolRepositorio = rolRepositorio;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Cargando datos iniciales...");
        usuarioServicio.inicializarRoles();

        Optional<Usuario> adminOpt = usuarioServicio.buscarPorUsername("admin");
        
        if (adminOpt.isEmpty()) {
            logger.info("Creando usuario 'admin' de sistema...");
            Usuario admin = new Usuario();
            
            admin.setUsername("admin");
            admin.setPassword(defaultAdminPassword); 
            
            admin.setCorreo("Eduardo.Tejeida@johnsonelectric.com"); 
            admin.setNombreCompleto("Eduardo Tejeida"); 
            admin.setDepartamento("Management"); 
            
            admin.setEnabled(true);
            admin.setPasswordChanged(false); 
            Set<Rol> roles = new HashSet<>();
            roles.add(rolRepositorio.findByNombre("ROLE_ADMIN").get());
            roles.add(rolRepositorio.findByNombre("ROLE_CHAMPION").get());
            roles.add(rolRepositorio.findByNombre("ROLE_VIEWER").get());
            admin.setRoles(roles);

            usuarioServicio.guardarUsuario(admin);
        

        System.out.println("\n=========================================================");
        System.out.println("CREDENCIALES MAESTRAS DEL SISTEMA APQP");
        System.out.println(" USUARIO: admin");
        System.out.println(" CONTRASEÑA: " + defaultAdminPassword);
        System.out.println("=========================================================\n");
    }
}
}