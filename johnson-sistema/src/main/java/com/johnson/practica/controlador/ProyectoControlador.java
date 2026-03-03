package com.johnson.practica.controlador;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.servicio.ChecklistServicio;
import com.johnson.practica.servicio.ProyectoServicio;
import com.johnson.practica.servicio.NotificacionServicio; // <-- IMPORTACIÓN DEL SERVICIO DE NOTIFICACIONES

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional; 
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proyectos")
public class ProyectoControlador {

    @Autowired private ProyectoServicio proyectoServicio;
    @Autowired private ChecklistServicio checklistServicio;
    @Autowired private ProyectoRepositorio proyectoRepositorio;
    
    @Autowired 
    private NotificacionServicio notificacionServicio; // <-- INYECCIÓN PARA LAS ALERTAS

    @Data @AllArgsConstructor
    public static class FaseVista {
        private String id;
        private String nombre;
        private List<ElementoChecklist> items;
    }

    @GetMapping("/checklist/{id}")
    @Transactional(readOnly = true)
    public String verChecklist(@PathVariable Long id, Model model, HttpServletRequest request) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        
        if (proyecto == null) {
            return "redirect:/";
        }

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("proyecto", proyecto);

        List<FaseVista> fases = new ArrayList<>();
        
        // Programa APQP 
        fases.add(new FaseVista("prog", "Programa APQP", checklistServicio.obtenerHitosPrograma(id)));
        
        // Stage 2 
        fases.add(new FaseVista("s2", "Stage 2", checklistServicio.obtenerPorFase(id, "2. Stage 2")));
        
        // Stage 3, Stage 4, Stage 5 
        fases.add(new FaseVista("s3", "Stage 3", checklistServicio.obtenerPorFase(id, "3. Stage 3")));
        fases.add(new FaseVista("s4", "Stage 4", checklistServicio.obtenerPorFase(id, "4. Stage 4")));
        fases.add(new FaseVista("s5", "Stage 5", checklistServicio.obtenerPorFase(id, "5. Stage 5")));

        model.addAttribute("fases", fases); 

        return "checklist";
    }

    @PostMapping("/checklist/guardar-todo/{proyectoId}")
    public String guardarChecklistCompleto(@PathVariable Long proyectoId, @RequestParam Map<String, String> allParams) {
        if (allParams != null) {
            checklistServicio.guardarChecklistCompleto(allParams);
        }
        return "redirect:/proyectos/checklist/" + proyectoId;
    }

    
    @PostMapping("/guardar")
    public String guardarProyecto(@ModelAttribute Proyecto proyecto, java.security.Principal principal) {
        boolean esNuevo = (proyecto.getId() == null);
        
        Proyecto proyectoGuardado = proyectoServicio.guardarProyecto(proyecto);
        
        if (esNuevo) {
            String titulo = "Nuevo Proyecto APQP";
            String msj = "Se ha inicializado el portafolio para el proyecto: " + proyectoGuardado.getNombre();
            String url = "/proyectos/checklist/" + proyectoGuardado.getId();
            
            String autor = (principal != null) ? principal.getName() : "Sistema";
            
            notificacionServicio.alertarATodos(titulo, msj, "SUCCESS", url, autor);
        }
        
        return "redirect:/";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProyecto(@PathVariable Long id) {
        proyectoServicio.eliminarProyecto(id);
        return "redirect:/"; 
    }
    
    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        List<Proyecto> lista = proyectoRepositorio.findAll();
        model.addAttribute("proyectos", lista);
        model.addAttribute("currentUri", request.getRequestURI());
        return "index";
    }

    



}