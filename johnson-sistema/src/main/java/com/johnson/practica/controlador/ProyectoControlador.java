package com.johnson.practica.controlador;

import com.johnson.practica.model.ElementoChecklist;
import com.johnson.practica.model.Proyecto;
import com.johnson.practica.servicio.ProyectoServicio;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/proyectos")
public class ProyectoControlador {

    @Autowired
    private ProyectoServicio proyectoServicio;

    @Autowired
    private ElementoChecklistRepositorio checklistRepositorio;

    // Listado de proyectos (dashboard/list)
    @GetMapping
    public String listarProyectos(Model model) {
        model.addAttribute("proyectos", proyectoServicio.obtenerTodos());
        model.addAttribute("titulo", "Dashboard de Proyectos");
        return "index";
    }

    // Vista para crear nuevo proyecto (formulario)
    @GetMapping("/nuevo")
    public String nuevoProyecto(Model model) {
        model.addAttribute("proyecto", new Proyecto());
        return "nuevo-proyecto";
    }

    // Ver checklist de un proyecto
    @GetMapping("/{id}/checklist")
    public String verChecklist(@PathVariable Long id, Model model) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        if (proyecto == null) {
            return "redirect:/proyectos";
        }

        List<ElementoChecklist> checklist = checklistRepositorio.findByProyectoId(id);
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("elementos", checklist);

        return "checklist";
    }

}