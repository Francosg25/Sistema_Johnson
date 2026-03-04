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

    @Autowired
    private com.johnson.practica.servicio.ReporteServicio reporteServicio;

    @GetMapping
    public String verReportes(Model model) {
        var datos = checklistServicio.generarReporteGlobal();
        model.addAttribute("datosGrafica", datos);
        
        ReporteEstadoGlobal datosEstadoGlobal = checklistServicio.generarReporteEstadoGlobal();
        model.addAttribute("datosEstadoGlobal", datosEstadoGlobal);

        var datosCascada = checklistServicio.generarReporteCascada();
        model.addAttribute("datosCascada", datosCascada);
        
        return "reportes/reportes";
    }

    @GetMapping("/proyecto/ver/{id}")
    public String verReporteProyecto(@org.springframework.web.bind.annotation.PathVariable("id") Long id, Model model) {
        java.util.Map<String, Object> datos = reporteServicio.obtenerDatosReporte(id);
        
        model.addAllAttributes(datos);
        
        return "reportes/proyecto_pdf";
    }
}
    