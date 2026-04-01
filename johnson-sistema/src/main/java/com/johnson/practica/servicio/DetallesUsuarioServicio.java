package com.johnson.practica.servicio;

import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.UsuarioRepositorio;

import jakarta.transaction.Transactional;

import com.johnson.practica.seguridad.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DetallesUsuarioServicio implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(DetallesUsuarioServicio.class);

    private final UsuarioRepositorio usuarioRepositorio;

    public DetallesUsuarioServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Intentando cargar usuario por nombre de usuario: {}", username);
        Usuario usuario = usuarioRepositorio.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado con username: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado con username: " + username);
                });

        logger.info("Usuario encontrado: {}", usuario.getUsername());
        logger.debug("Contraseña del usuario {}: {}", usuario.getUsername(), usuario.getPassword());
        logger.debug("Usuario {} habilitado: {}", usuario.getUsername(), usuario.isEnabled());

        Set<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toSet());
        logger.debug("Roles del usuario {}: {}", usuario.getUsername(), authorities);

        return new CustomUserDetails(
                usuario.getUsername(), 
                usuario.getPassword(), 
                usuario.isEnabled(), 
                true, 
                true, 
                true, 
                authorities,
                usuario.getDepartamento(),
                usuario.getNombreCompleto()
        );
    }
}
