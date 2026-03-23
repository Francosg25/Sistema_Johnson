package com.johnson.practica.controlador;

import com.johnson.practica.dto.FaseVista;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.servicio.ChecklistServicio;
import com.johnson.practica.servicio.FirmaEtapaServicio;
import com.johnson.practica.servicio.ProyectoServicio;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proyectos/checklist")
public class ChecklistControlador {

    @Autowired private ProyectoServicio proyectoServicio;
    @Autowired private ChecklistServicio checklistServicio;
    @Autowired private FirmaEtapaServicio firmaEtapaServicio;

    @GetMapping("/{id}")
    public String verChecklist(@PathVariable Long id, Model modelo, HttpServletRequest solicitud) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        if (proyecto == null) return "redirect:/";

        modelo.addAttribute("currentUri", solicitud.getRequestURI());
        modelo.addAttribute("proyecto", proyecto);

        List<FaseVista> fases = checklistServicio.construirFasesVista(id);
        modelo.addAttribute("fases", fases); 

        // Firmas utilizadas en los modales
        modelo.addAttribute("firmasGate3", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 3));
        modelo.addAttribute("firmasGate4", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 4));
        modelo.addAttribute("firmasGate5", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 5));

        return "checklist";
    }

    @PostMapping("/firmar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')")
    public String firmarEtapa(@PathVariable Long id, @RequestParam Integer etapa, @RequestParam String rol, Principal principal) {
        firmaEtapaServicio.firmar(id, etapa, rol, principal.getName());
        return "redirect:/proyectos/checklist/" + id;
    }

    @PostMapping("/firmar-ajax/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')")
    @ResponseBody
    public Map<String, Object> firmarEtapaAjax(@PathVariable Long id, @RequestParam Integer etapa, @RequestParam String rol, Principal principal) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            firmaEtapaServicio.firmar(id, etapa, rol, principal.getName());
            respuesta.put("exito", true);
            respuesta.put("mensaje", "Firma aplicada correctamente.");
        } catch (Exception e) {
            respuesta.put("exito", false);
            respuesta.put("mensaje", e.getMessage());
        }
        return respuesta;
    }

    @PostMapping("/guardar-todo/{proyectoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')") 
    public String guardarChecklistCompleto(@PathVariable Long proyectoId, @RequestParam Map<String, String> todosLosParametros) {
        if (todosLosParametros != null) {
            checklistServicio.guardarChecklistCompleto(todosLosParametros);
        }
        return "redirect:/proyectos/checklist/" + proyectoId;
    }

    @PostMapping("/guardar-ajax/{proyectoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')") 
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarChecklistAjax(@PathVariable Long proyectoId, @RequestParam Map<String, String> todosLosParametros) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            if (todosLosParametros != null) {
                checklistServicio.guardarChecklistCompleto(todosLosParametros);
            }
            respuesta.put("exito", true);
            respuesta.put("mensaje", "Checklist guardado correctamente");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("exito", false);
            respuesta.put("mensaje", "Error al guardar el checklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }
}
