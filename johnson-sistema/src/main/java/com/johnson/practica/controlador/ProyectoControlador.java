package com.johnson.practica.controlador;

import com.johnson.practica.model.Proyecto;
import com.johnson.practica.servicio.ChecklistServicio;
import com.johnson.practica.servicio.ProyectoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proyectos")
public class ProyectoControlador {

    @Autowired
    private ProyectoServicio proyectoServicio;

    @Autowired
    private ChecklistServicio checklistServicio; // Inyectamos el experto en checklists

    // 1. Ver el Checklist (Separando Hitos de Preguntas)
    @GetMapping("/{id}/checklist")
    public String verChecklist(@PathVariable Long id, Model model) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        
        // Patrón "Guard Clause": Si no existe, regresar al inicio
        if (proyecto == null) {
            return "redirect:/";
        }

        // Delegamos al servicio la lógica de obtención de datos
        model.addAttribute("proyecto", proyecto);
        
        // Aquí ocurre la magia de la separación:
        model.addAttribute("prog", checklistServicio.obtenerHitosPrograma(id));     // Fase 0
        model.addAttribute("stage2", checklistServicio.obtenerChecklistStage2(id)); // Fase 2

        return "checklist";
    }

    // 2. Endpoint AJAX para guardar cambios sin recargar
    @PostMapping("/checklist/actualizar/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarElemento(@PathVariable Long id, 
                                                @RequestParam(required = false) String estado,
                                                @RequestParam(required = false) String comentario) {
        
        var elemento = checklistServicio.actualizarElemento(id, estado, comentario);
        
        if (elemento == null) {
            return ResponseEntity.badRequest().body("Elemento no encontrado");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("nuevoEstado", elemento.getEstado());
        
        return ResponseEntity.ok(response);
    }

    // 3. Listado de Proyectos
    @GetMapping
    public String listarProyectos(Model model) {
        List<Proyecto> proyectos = proyectoServicio.obtenerTodos();
        model.addAttribute("proyectos", proyectos);
        // Si no tienes una vista 'proyectos.html', cambia esto a "index" o lo que uses de dashboard
        return "index"; 
    }
    
    // 4. Formulario de Nuevo Proyecto
    @GetMapping("/nuevo")
    public String nuevoProyecto(Model model) {
        model.addAttribute("proyecto", new Proyecto());
        return "nuevo-proyecto"; 
    }

    // 5. Guardar Proyecto (POST)
    @PostMapping("/guardar")
    public String guardarProyecto(@ModelAttribute Proyecto proyecto) {
        proyectoServicio.guardarProyecto(proyecto);
        return "redirect:/";
    }

    // 6. Eliminar Proyecto
    @GetMapping("/eliminar/{id}")
    public String eliminarProyecto(@PathVariable Long id) {
        proyectoServicio.eliminarProyecto(id);
        return "redirect:/"; 
    }
}