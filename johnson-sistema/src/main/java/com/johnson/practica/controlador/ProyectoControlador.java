package com.johnson.practica.controlador;

import com.johnson.practica.model.Proyecto;
import com.johnson.practica.model.ElementoChecklist; // <--- Faltaba este
import com.johnson.practica.servicio.ProyectoServicio;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/proyectos")
public class ProyectoControlador {

    @Autowired
    private ProyectoServicio proyectoServicio;

    @Autowired
    private ElementoChecklistRepositorio checklistRepositorio;

    @GetMapping("/{id}/checklist")
    public String verChecklist(@PathVariable Long id, Model model) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        
        if (proyecto == null) {
            return "redirect:/";
        }

        List<ElementoChecklist> elementos = checklistRepositorio.findByProyectoId(id);
        
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("elementos", elementos);
        
        return "checklist";
    }
    
    @GetMapping("/nuevo")
    public String nuevoProyecto(Model model) {
        model.addAttribute("proyecto", new Proyecto());
        return "nuevo-proyecto"; 
    }

    @PostMapping("/guardar") // Esta es la ruta que llama el formulario
    public String guardarProyecto(@ModelAttribute Proyecto proyecto) {
        // Guardamos el proyecto y se genera el checklist automático
        proyectoServicio.guardarProyecto(proyecto);
        
        // Redirigimos al dashboard para ver el nuevo proyecto en la tabla
        return "redirect:/";
    }


    @GetMapping("/eliminar/{id}")
    public String eliminarProyecto(@PathVariable Long id) {
        proyectoServicio.eliminarProyecto(id);
        return "redirect:/"; // Nos regresa al Dashboard limpio
    }

}