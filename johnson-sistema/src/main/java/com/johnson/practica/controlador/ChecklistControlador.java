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
    public String verChecklist(@PathVariable Long id, Model model, HttpServletRequest request) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        if (proyecto == null) return "redirect:/";

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("proyecto", proyecto);

        List<FaseVista> fases = checklistServicio.construirFasesVista(id);
        model.addAttribute("fases", fases); 

        // Firmas used in modals
        model.addAttribute("firmasGate3", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 3));
        model.addAttribute("firmasGate4", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 4));
        model.addAttribute("firmasGate5", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 5));

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
        Map<String, Object> response = new HashMap<>();
        try {
            firmaEtapaServicio.firmar(id, etapa, rol, principal.getName());
            response.put("exito", true);
            response.put("mensaje", "Signature applied successfully.");
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", e.getMessage());
        }
        return response;
    }

    @PostMapping("/guardar-todo/{proyectoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')") 
    public String guardarChecklistCompleto(@PathVariable Long proyectoId, @RequestParam Map<String, String> allParams) {
        if (allParams != null) {
            checklistServicio.guardarChecklistCompleto(allParams);
        }
        return "redirect:/proyectos/checklist/" + proyectoId;
    }

    @PostMapping("/guardar-ajax/{proyectoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')") 
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarChecklistAjax(@PathVariable Long proyectoId, @RequestParam Map<String, String> allParams) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (allParams != null) {
                checklistServicio.guardarChecklistCompleto(allParams);
            }
            response.put("exito", true);
            response.put("mensaje", "Checklist saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error saving checklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
