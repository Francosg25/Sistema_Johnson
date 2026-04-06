package com.johnson.practica.controlador;

import com.johnson.practica.servicio.UsuarioServicio;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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
    private final com.johnson.practica.servicio.BitacoraServicio bitacoraServicio;
    private final com.johnson.practica.servicio.EmailServicio emailServicio;

    public AdminControlador(UsuarioServicio usuarioServicio, 
                            com.johnson.practica.servicio.BitacoraServicio bitacoraServicio,
                            com.johnson.practica.servicio.EmailServicio emailServicio) {
        this.usuarioServicio = usuarioServicio;
        this.bitacoraServicio = bitacoraServicio;
        this.emailServicio = emailServicio;
    }

    @GetMapping("/usuarios")
    public String gestionarUsuarios(org.springframework.ui.Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("usuarios", usuarioServicio.listarTodos());
        return "admin/usuarios"; 
    }

    @PostMapping("/usuarios/crear-usuario")
    public String crearUsuario(@RequestParam String username, 
                               @RequestParam String correo, 
                               @RequestParam String nombre,
                               @RequestParam String rol, 
                               @RequestParam String departamento,
                               @RequestParam(defaultValue = "false") boolean esManager,
                               RedirectAttributes redirectAttributes,
                               java.security.Principal principal) {
        try {
            usuarioServicio.crearUsuarioConRol(username, correo, nombre, rol, departamento, esManager);
            
            String admin = (principal != null) ? principal.getName() : "System";
            bitacoraServicio.registrarAccion(admin, "CREATE USER", 
                "New user created: " + username + " (" + nombre + ") with role " + rol + (esManager ? " [MANAGER]" : ""));

            redirectAttributes.addFlashAttribute("mensaje", "User created successfully. An invitation email has been sent.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/editar")
    public String editarUsuario(@RequestParam Long id,
                                @RequestParam String nombre,
                                @RequestParam String correo,
                                @RequestParam String rol,
                                @RequestParam String departamento,
                                @RequestParam(defaultValue = "false") boolean esManager,
                                RedirectAttributes redirectAttributes,
                                java.security.Principal principal) {
        try {
            usuarioServicio.editarUsuario(id, nombre, correo, rol, departamento, esManager);

            String admin = (principal != null) ? principal.getName() : "System";
            bitacoraServicio.registrarAccion(admin, "UPDATE USER", 
                "User updated: ID " + id + " (" + nombre + ")" + (esManager ? " [MANAGER]" : ""));

            redirectAttributes.addFlashAttribute("mensaje", "User updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/toggle-estado")
    public String toggleEstado(@RequestParam Long id, RedirectAttributes redirectAttributes, java.security.Principal principal) {
        try {
            usuarioServicio.toggleEstado(id);
            
            String admin = (principal != null) ? principal.getName() : "System";
            bitacoraServicio.registrarAccion(admin, "TOGGLE USER STATUS", "Status changed for user ID: " + id);

            redirectAttributes.addFlashAttribute("mensaje", "User status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating status: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/datos")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> obtenerDatosUsuario(@RequestParam Long id) {
        return usuarioServicio.obtenerPorId(id)
                .map(u -> {
                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    data.put("id", u.getId());
                    data.put("nombre", u.getNombreCompleto());
                    data.put("username", u.getUsername());
                    data.put("correo", u.getCorreo());
                    data.put("departamento", u.getDepartamento());
                    data.put("esManager", u.isEsManager());
                    data.put("rol", u.getRoles().iterator().next().getNombre());
                    return org.springframework.http.ResponseEntity.ok(data);
                })
                .orElse(org.springframework.http.ResponseEntity.notFound().build());
    }

    @PostMapping("/usuarios/eliminar")
    public String eliminarUsuario(@RequestParam Long id, RedirectAttributes redirectAttributes, java.security.Principal principal) {
        try {
            String admin = (principal != null) ? principal.getName() : "System";
            bitacoraServicio.registrarAccion(admin, "DELETE USER", "User deleted permanently, ID: " + id);

            usuarioServicio.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/test-email")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> probarEmail(@RequestParam String email, java.security.Principal principal) {
        try {
            String admin = (principal != null) ? principal.getName() : "System";
            bitacoraServicio.registrarAccion(admin, "SMTP TEST", "Manual SMTP connection test triggered for: " + email);
            
            emailServicio.probarConexionSmtp(email);
            
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("exito", true, "mensaje", "Test email sent successfully to " + email));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("exito", false, "mensaje", "SMTP Error: " + e.getMessage()));
        }
    }
}