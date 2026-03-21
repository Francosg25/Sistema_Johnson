package com.johnson.practica.controlador;

import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.servicio.ChecklistReporteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/timeline")
public class TimelineControlador {

    @Autowired
    private ChecklistReporteServicio checklistReporteServicio;

    @Autowired
    private ProyectoRepositorio proyectoRepo;

    @GetMapping
    public String verTimeline(Model model) {
        Map<String, Object> datos = checklistReporteServicio.obtenerDatosTimeline();
        
        model.addAttribute("timelineGroups", datos.get("groups"));
        model.addAttribute("timelineItems", datos.get("items"));
        
        model.addAttribute("proyectos", proyectoRepo.findByEsHistoricoFalse()); 
        model.addAttribute("statusGlobal", checklistReporteServicio.generarReporteEstadoGlobal());
        
        return "timeline";
    }
}
