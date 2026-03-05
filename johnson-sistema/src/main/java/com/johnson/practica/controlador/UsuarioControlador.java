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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioControlador(UsuarioServicio usuarioServicio, PasswordEncoder passwordEncoder) {
        this.usuarioServicio = usuarioServicio;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cambiar-password")
    public String cambiarPasswordForm(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";
        model.addAttribute("username", auth.getName());
        return "usuario/cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String newPassword, 
                                  @RequestParam String confirmPassword,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/cambiar-password";
        }

        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "redirect:/cambiar-password";
        }

        Optional<Usuario> usuarioOpt = usuarioServicio.buscarPorUsername(auth.getName());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setPassword(passwordEncoder.encode(newPassword));
            usuario.setPasswordChanged(true);
            usuarioServicio.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada exitosamente.");
            return "redirect:/";
        }

        return "redirect:/login";
    }
}
