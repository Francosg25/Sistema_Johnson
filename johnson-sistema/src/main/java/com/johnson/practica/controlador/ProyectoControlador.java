package com.johnson.practica.controlador;

import com.johnson.practica.model.Proyecto;
import com.johnson.practica.servicio.ProyectoServicio;
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
    private ProyectoServicio proyectoServicio;  

    @Autowired
    private ElementoChecklistRepositorio checklistRepositorio; // Para buscar los items

    // Ruta para ver el Checklist de un proyecto específico (ej: /proyectos/1/checklist)
    @GetMapping("/{id}/checklist")
    public String verChecklist(@PathVariable Long id, Model model) {
        // 1. Buscamos el proyecto (Simulado si no tienes el método findById en servicio aún)
        // Idealmente: proyectoService.buscarPorId(id);
        // Por ahora usaremos el repositorio directo si es necesario o asumimos que el servicio lo tiene.
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        if (proyecto == null) {
            return "redirect:/proyectos"; // proyecto no encontrado: redirigir al listado
        }
        
        // 2. Cargamos la lista de 19 puntos
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("elementos", checklistRepositorio.findByProyectoId(id));
        
        return "checklist"; // Esto buscará checklist.html
    }

    // Listado de proyectos (GET /proyectos)
    @GetMapping
    public String listarProyectos(Model model) {
        model.addAttribute("proyectos", proyectoServicio.obtenerTodos());
        model.addAttribute("titulo", "Listado de Proyectos");
        return "index"; // reutiliza la plantilla del dashboard que muestra la lista
    }
}
