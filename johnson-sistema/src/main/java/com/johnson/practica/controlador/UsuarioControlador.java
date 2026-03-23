package com.johnson.practica.controlador;

import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.servicio.UsuarioServicio;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioControlador(UsuarioServicio usuarioServicio, PasswordEncoder passwordEncoder) {
        this.usuarioServicio = usuarioServicio;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/api/usuarios/menciones")
    @ResponseBody
    public List<MentionDto> obtenerUsuariosParaMenciones() {
        return usuarioServicio.listarTodos().stream()
                .map(u -> new MentionDto(u.getNombreCompleto(), u.getUsername()))
                .collect(Collectors.toList());
    }

    public static record MentionDto(String key, String value) {}

    @GetMapping("/cambiar-password")
    public String cambiarPasswordForm(Authentication auth, Model modelo) {
        if (auth == null) return "redirect:/login";
        modelo.addAttribute("username", auth.getName());
        return "usuarios/cambio-password";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String newPassword, 
                                  @RequestParam String confirmPassword,
                                  Authentication auth,
                                  RedirectAttributes atributosRedireccion) {
        if (!newPassword.equals(confirmPassword)) {
            atributosRedireccion.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/cambiar-password";
        }

        if (newPassword.length() < 8) {
            atributosRedireccion.addFlashAttribute("error", "The password must be at least 8 characters long.");
            return "redirect:/cambiar-password";
        }

        Optional<Usuario> usuarioOpt = usuarioServicio.buscarPorUsername(auth.getName());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setPassword(passwordEncoder.encode(newPassword));
            usuario.setPasswordChanged(true);
            usuarioServicio.guardarUsuario(usuario);
            atributosRedireccion.addFlashAttribute("mensaje", "Password updated successfully.");
            return "redirect:/";
        }

        return "redirect:/login";
    }
}
