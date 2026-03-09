package com.johnson.practica.controlador;

import com.johnson.practica.servicio.UsuarioServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RecuperacionControlador {

    private final UsuarioServicio usuarioServicio;

    public RecuperacionControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping("/olvido-password")
    public String mostrarFormOlvido() {
        return "olvido-password";
    }

    @PostMapping("/olvido-password")
    public String procesarOlvido(@RequestParam("correo") String correo, RedirectAttributes redirectAttributes) {
        try {
            usuarioServicio.solicitarRecuperacion(correo);
            redirectAttributes.addFlashAttribute("mensaje", "Se ha enviado un enlace de recuperación a tu correo electrónico.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/olvido-password";
    }

    @GetMapping("/recuperar-password")
    public String mostrarFormRecuperar(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "recuperar-password";
    }

    @PostMapping("/recuperar-password")
    public String procesarRecuperar(@RequestParam("token") String token, 
                                    @RequestParam("password") String nuevaContrasena, 
                                    RedirectAttributes redirectAttributes) {
        try {
            usuarioServicio.completarRecuperacion(token, nuevaContrasena);
            redirectAttributes.addFlashAttribute("mensaje", "Tu contraseña ha sido restablecida exitosamente. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recuperar-password?token=" + token;
        }
    }
}
