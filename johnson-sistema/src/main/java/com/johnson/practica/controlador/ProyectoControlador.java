package com.johnson.practica.controlador;

import com.johnson.practica.model.Proyecto;
import com.johnson.practica.servicio.ProyectoServicio; // Importamos el servicio en Español
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/proyectos")
public class ProyectoControlador {

    @Autowired
    private ProyectoServicio proyectoServicio; // Variable del tipo correcto

    @Autowired
    private ElementoChecklistRepositorio checklistRepositorio;

    @GetMapping("/{id}/checklist")
    public String verChecklist(@PathVariable Long id, Model model) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        
        if (proyecto == null) {
            return "redirect:/"; // Protección si no existe
        }

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("elementos", checklistRepositorio.findByProyectoId(id));
        
        return "checklist"; 
    }
    
    // Agregamos la ruta para crear nuevo proyecto que faltaba
    @GetMapping("/nuevo")
    public String nuevoProyecto(Model model) {
        model.addAttribute("proyecto", new Proyecto());
        return "nuevo-proyecto"; // Necesitarás crear este HTML o usar uno simple
    }
}