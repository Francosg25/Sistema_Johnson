package com.johnson.practica.controlador;

import com.johnson.practica.model.Proyecto;
import com.johnson.practica.model.ElementoChecklist; 
import com.johnson.practica.servicio.ProyectoServicio;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import com.johnson.practica.repositorio.ProyectoRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/proyectos")
public class ProyectoControlador {

    @Autowired
    private ProyectoServicio proyectoServicio;

    @Autowired
    private ElementoChecklistRepositorio checklistRepositorio;

    @Autowired
    private ProyectoRepositorio proyectoRepositorio; 


    @GetMapping("") 
    public String listarProyectos() {
        return "redirect:/"; // Redirige al Dashboard donde está la tabla
    }

    // En ProyectoControlador.java

    @GetMapping("/{id}/checklist")
    public String verChecklist(@PathVariable Long id, Model model) {
        Proyecto proyecto = proyectoRepositorio.findById(id).orElse(null);
        if (proyecto == null) {
            return "redirect:/";
        }

        // 1. Obtener todos los elementos del checklist de este proyecto
        List<ElementoChecklist> todosLosItems = checklistRepositorio.findByProyectoId(id);

        // 2. FILTRAR: Separar los Hitos (Programa) de las Preguntas (Stage 2)
        // Usamos el campo 'fase' del catálogo para saber a dónde pertenece cada uno
    
        // En ProyectoControlador.java, dentro de verChecklist:

        List<ElementoChecklist> programa = todosLosItems.stream()
            .filter(item -> item.getCatalogo() != null && 
                    item.getCatalogo().getFase() != null && 
                    item.getCatalogo().getFase().startsWith("0"))
            .collect(Collectors.toList());

        List<ElementoChecklist> stage2 = todosLosItems.stream()
            .filter(item -> item.getCatalogo() != null && 
                    item.getCatalogo().getFase() != null && 
                    item.getCatalogo().getFase().startsWith("2"))
            .collect(Collectors.toList());

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("prog", programa);   // <--- Esta es la variable que espera tu HTML nuevo
        model.addAttribute("stage2", stage2);   // <--- Esta es para la otra pestaña

        return "checklist";
    }

    @GetMapping("/{id}/checklist/stage2")
    public String cargarStage2(@PathVariable Long id, Model model) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        if (proyecto == null) return "";

        List<ElementoChecklist> elementosStage2 = checklistRepositorio.findByProyectoIdAndFase(id, "2. Stage 2");
        model.addAttribute("elementos_stage2", elementosStage2);
        return "fragments/checklist-stage2 :: stage2Content";
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
        
        return "redirect:/";
    }


    @GetMapping("/eliminar/{id}")
    public String eliminarProyecto(@PathVariable Long id) {
        proyectoServicio.eliminarProyecto(id);
        return "redirect:/"; 
    }

    // --- NUEVO: API para guardar cambios sin recargar (AJAX) ---
    @PostMapping("/checklist/actualizar/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarElemento(@PathVariable Long id, 
                                                @RequestParam(required = false) String estado,
                                                @RequestParam(required = false) String comentario) {
        
        ElementoChecklist elemento = checklistRepositorio.findById(id).orElse(null);
        if (elemento == null) {
            return ResponseEntity.badRequest().body("Elemento no encontrado");
        }

        // Solo actualizamos lo que nos llegue
        if (estado != null) elemento.setEstado(estado);
        if (comentario != null) elemento.setComentario(comentario);

        checklistRepositorio.save(elemento);

        // Devolvemos un JSON simple confirmando éxito
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", "success");
        respuesta.put("nuevoEstado", elemento.getEstado());
        return ResponseEntity.ok(respuesta);
    }

}