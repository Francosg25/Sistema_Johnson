package com.johnson.practica.controlador;

import com.johnson.practica.servicio.UsuarioServicio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminControlador {

    private final UsuarioServicio usuarioServicio;

    public AdminControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping("/usuarios")
    public String gestionarUsuarios() {
        return "admin/usuarios"; // Necesitaremos esta vista
    }

    @PostMapping("/usuarios/crear-champion")
    public String crearChampion(@RequestParam String username, 
                                @RequestParam String correo, 
                                @RequestParam String nombre,
                                RedirectAttributes redirectAttributes) {
        try {
            usuarioServicio.crearChampion(username, correo, nombre);
            redirectAttributes.addFlashAttribute("mensaje", "Champion creado exitosamente. Se ha enviado un correo con las credenciales.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el champion: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
