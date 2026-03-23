package com.johnson.practica.controlador;

import com.johnson.practica.servicio.UsuarioServicio;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminControlador {

    private final UsuarioServicio usuarioServicio;

    public AdminControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping("/usuarios")
    public String gestionarUsuarios(Model modelo, HttpServletRequest solicitud) {
        modelo.addAttribute("currentUri", solicitud.getRequestURI());
        modelo.addAttribute("usuarios", usuarioServicio.listarTodos());
        return "usuarios/gestion"; 
    }

    @PostMapping("/usuarios/crear-usuario")
    public String crearUsuario(@RequestParam String username, 
                               @RequestParam String correo, 
                               @RequestParam String nombre,
                               @RequestParam String rol, 
                               @RequestParam String departamento, 
                               RedirectAttributes atributosRedireccion) {
        try {
            usuarioServicio.crearUsuarioConRol(username, correo, nombre, rol, departamento);
            atributosRedireccion.addFlashAttribute("mensaje", "User created successfully. A verification email has been sent.");
        } catch (Exception e) {
            atributosRedireccion.addFlashAttribute("error", "Error creating user: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/eliminar")
    public String eliminarUsuario(@RequestParam Long id, RedirectAttributes atributosRedireccion) {
        try {
            usuarioServicio.eliminarUsuario(id);
            atributosRedireccion.addFlashAttribute("mensaje", "User deleted successfully.");
        } catch (Exception e) {
            atributosRedireccion.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
