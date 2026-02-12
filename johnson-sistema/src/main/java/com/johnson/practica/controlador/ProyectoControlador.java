package com.johnson.practica.controlador;

import com.johnson.practica.model.ElementoChecklist;
import com.johnson.practica.model.Proyecto;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.servicio.ChecklistServicio;
import com.johnson.practica.servicio.ProyectoServicio;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proyectos")
public class ProyectoControlador {

    @Autowired private ProyectoServicio proyectoServicio;
    @Autowired private ChecklistServicio checklistServicio;
    @Autowired private ProyectoRepositorio proyectoRepositorio;

    // --- CLASE AUXILIAR PARA ENVIAR DATOS AL HTML ---
    @Data @AllArgsConstructor
    public static class FaseVista {
        private String id;
        private String nombre;
        private List<ElementoChecklist> items;
    }

    // --- 1. VER EL CHECKLIST ---
    @GetMapping("/checklist/{id}")
    public String verChecklist(@PathVariable Long id, Model model, HttpServletRequest request) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        
        if (proyecto == null) {
            return "redirect:/";
        }

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("proyecto", proyecto);

        List<FaseVista> fases = new ArrayList<>();
        
        // 1. Programa APQP 
        fases.add(new FaseVista("prog", "Programa APQP", checklistServicio.obtenerHitosPrograma(id)));
        
        // 2. Stage 2 (Checklist Detallado)
        // Asegúrate que en la BD la fase se guardó como "2. Stage 2"
        fases.add(new FaseVista("s2", "Stage 2", checklistServicio.obtenerPorFase(id, "2. Stage 2")));
        
        // 3. Gate Reviews (Validación + Conclusión)
        fases.add(new FaseVista("s3", "Stage 3", checklistServicio.obtenerPorFase(id, "3. Stage 3")));
        fases.add(new FaseVista("s4", "Stage 4", checklistServicio.obtenerPorFase(id, "4. Stage 4")));
        fases.add(new FaseVista("s5", "Stage 5", checklistServicio.obtenerPorFase(id, "5. Stage 5")));

        // ¡ESTO ES LO IMPORTANTE! Enviamos "fases" al HTML
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

    // --- 3. GESTIÓN DE PROYECTOS ---
    
    @PostMapping("/guardar")
    public String guardarProyecto(@ModelAttribute Proyecto proyecto) {
        proyectoServicio.guardarProyecto(proyecto);
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