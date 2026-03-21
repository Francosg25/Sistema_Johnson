package com.johnson.practica.controlador;

import com.johnson.practica.dto.ReporteEstadoGlobal;
import com.johnson.practica.servicio.ChecklistReporteServicio;
import com.johnson.practica.servicio.ReporteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/reportes")
public class ReportesControlador {

    @Autowired
    private ChecklistReporteServicio checklistReporteServicio;

    @Autowired
    private ReporteServicio reporteServicio;

    @GetMapping
    public String verReportes(Model model) {
        var datos = checklistReporteServicio.generarReporteGlobal();
        model.addAttribute("datosGrafica", datos);
        
        ReporteEstadoGlobal datosEstadoGlobal = checklistReporteServicio.generarReporteEstadoGlobal();
        model.addAttribute("datosEstadoGlobal", datosEstadoGlobal);

        var datosCascada = checklistReporteServicio.generarReporteCascada();
        model.addAttribute("datosCascada", datosCascada);
        
        return "reportes/reportes";
    }

    @GetMapping("/proyecto/ver/{id}")
    public String verReporteProyecto(@PathVariable("id") Long id, Model model) {
        Map<String, Object> datos = reporteServicio.obtenerDatosReporte(id);
        model.addAllAttributes(datos);
        return "reportes/proyecto_pdf";
    }
}
