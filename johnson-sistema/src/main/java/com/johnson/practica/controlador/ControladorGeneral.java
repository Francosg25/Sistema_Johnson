package com.johnson.practica.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorGeneral {

    @GetMapping("/checklist")
    public String checklistRedirect() {
        return "redirect:/proyectos";
    }
}
