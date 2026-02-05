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
import com.johnson.practica.servicio.ProyectoServicio;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
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
            // 1. Buscamos el proyecto
            Proyecto proyecto = proyectoServicio.buscarPorId(id);
            
            // 2. Si no existe, volvemos al inicio para evitar errores
            if (proyecto == null) {
                return "redirect:/";
            }

            List<ElementoChecklist> elementos = checklistRepositorio.findByProyectoId(id);
            
            // 4. Enviamos todo a la vista
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("elementos", elementos);
            
            return "checklist"; // Esto abre el archivo checklist.html
        }
    }