package com.johnson.practica.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ControladorGeneral {

    // Ruta segura que evita acceder a checklist sin proyecto: redirige al listado de proyectos
    @GetMapping("/checklist")
    public String checklistRedirect() {
        return "redirect:/proyectos";
    }
}
