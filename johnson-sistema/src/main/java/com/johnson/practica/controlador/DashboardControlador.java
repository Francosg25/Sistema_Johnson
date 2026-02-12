package com.johnson.practica.controlador;

import com.johnson.practica.repositorio.ProyectoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardControlador {

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

    @GetMapping("/")
    public String index(Model model, jakarta.servlet.http.HttpServletRequest request) {
        model.addAttribute("proyectos", proyectoRepositorio.findAll());
        model.addAttribute("titulo", "Dashboard de Proyectos APQP - Johnson Electric");
        model.addAttribute("currentUri", request.getRequestURI());
        return "index"; 
    }

    

}