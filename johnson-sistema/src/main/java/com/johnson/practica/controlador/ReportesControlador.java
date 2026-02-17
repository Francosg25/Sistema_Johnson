package com.johnson.practica.controlador;

import com.johnson.practica.servicio.ChecklistServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reportes")
public class ReportesControlador {

    @Autowired
    private ChecklistServicio checklistServicio;

    @GetMapping
    public String verReportes(Model model) {
        // Obtenemos los datos calculados
        var datos = checklistServicio.generarReporteGlobal();
        
        // Los mandamos a la vista
        model.addAttribute("datosGrafica", datos);
        
        return "reportes"; // Nombre del archivo HTML
    }
}