package com.johnson.practica.controlador;

import com.johnson.practica.dto.ReporteEstadoGlobal; // Importar el nuevo DTO
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
        // Obtenemos los datos calculados para el reporte global por proyecto
        var datos = checklistServicio.generarReporteGlobal();
        model.addAttribute("datosGrafica", datos);
        
        // Obtenemos los datos calculados para el reporte global por estado
        ReporteEstadoGlobal datosEstadoGlobal = checklistServicio.generarReporteEstadoGlobal();
        model.addAttribute("datosEstadoGlobal", datosEstadoGlobal);

        var datosCascada = checklistServicio.generarReporteCascada();
        model.addAttribute("datosCascada", datosCascada);
        
        return "reportes";
    }
}
    