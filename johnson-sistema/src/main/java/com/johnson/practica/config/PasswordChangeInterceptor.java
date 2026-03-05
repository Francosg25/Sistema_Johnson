package com.johnson.practica.config;

import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.servicio.UsuarioServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class PasswordChangeInterceptor implements HandlerInterceptor {

    private final UsuarioServicio usuarioServicio;

    public PasswordChangeInterceptor(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal().toString())) {
            String uri = request.getRequestURI();
            
            // Evitar bucle infinito y permitir recursos estáticos
            if (uri.equals("/cambiar-password") || uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") || uri.equals("/logout")) {
                return true;
            }

            Optional<Usuario> usuarioOpt = usuarioServicio.buscarPorUsername(auth.getName());
            if (usuarioOpt.isPresent() && !usuarioOpt.get().isPasswordChanged()) {
                response.sendRedirect("/cambiar-password");
                return false;
            }
        }
        
        return true;
    }
}
