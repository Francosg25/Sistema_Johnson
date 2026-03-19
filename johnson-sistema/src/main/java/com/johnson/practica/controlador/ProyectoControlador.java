package com.johnson.practica.controlador;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.servicio.ChecklistServicio;
import com.johnson.practica.servicio.ExcelServicio;
import com.johnson.practica.servicio.ProyectoServicio;
import com.johnson.practica.servicio.ReporteServicio;
import com.johnson.practica.servicio.NotificacionServicio; 
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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

import com.johnson.practica.servicio.FirmaEtapaServicio;
import com.johnson.practica.modelo.FirmaEtapa;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/proyectos")
public class ProyectoControlador {

    @Autowired private ProyectoServicio proyectoServicio;
    @Autowired private ChecklistServicio checklistServicio;
    @Autowired private ProyectoRepositorio proyectoRepositorio;
    @Autowired private FirmaEtapaServicio firmaEtapaServicio;
    @Autowired private NotificacionServicio notificacionServicio;
    @Autowired private ReporteServicio reporteServicio;
    @Autowired private ExcelServicio excelServicio;
    @Autowired private com.johnson.practica.servicio.BitacoraServicio bitacoraServicio;


    @Data @AllArgsConstructor
    public static class FaseVista {
        private String id;
        private String nombre;
        private List<ElementoChecklist> items;
        private Map<String, FirmaEtapa> firmas;
    }

    
    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        // Esto trae SOLO los proyectos que NO están en la bóveda
        List<Proyecto> lista = proyectoRepositorio.findByEsHistoricoFalse(); 
        
        model.addAttribute("proyectos", lista);
        model.addAttribute("currentUri", request.getRequestURI());

        Map<String, Integer> tendencia = checklistServicio.obtenerTendenciaAprobacionesOK();
        model.addAttribute("tendencia", tendencia);

        

        return "index";
    }

    
    @GetMapping("/checklist/{id}")
    @Transactional(readOnly = true)
    public String verChecklist(@PathVariable Long id, Model model, HttpServletRequest request) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        if (proyecto == null) return "redirect:/";

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("proyecto", proyecto);

        List<ElementoChecklist> todosLosElementos = checklistServicio.obtenerPorProyectoId(id);
        List<FaseVista> fases = new ArrayList<>();
        
        fases.add(new FaseVista("prog", "APQP Program", todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("0")).toList(), new HashMap<>()));
                
        fases.add(new FaseVista("s2", "Stage 2", todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("2")).toList(), new HashMap<>()));
                
        fases.add(new FaseVista("s3", "Stage 3", todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("3")).toList(), 
                firmaEtapaServicio.obtenerFirmasPorEtapa(id, 3)));
                
        fases.add(new FaseVista("s4", "Stage 4", todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("4")).toList(), 
                firmaEtapaServicio.obtenerFirmasPorEtapa(id, 4)));
                
        fases.add(new FaseVista("s5", "Stage 5", todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("5")).toList(), 
                firmaEtapaServicio.obtenerFirmasPorEtapa(id, 5)));

        model.addAttribute("fases", fases); 
        model.addAttribute("firmasGate3", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 3));
        model.addAttribute("firmasGate4", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 4));
        model.addAttribute("firmasGate5", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 5));

       
        return "checklist";
    }

   
    @PostMapping("/checklist/firmar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')")
    public String firmarEtapa(@PathVariable Long id, @RequestParam Integer etapa, @RequestParam String rol, Principal principal) {
        firmaEtapaServicio.firmar(id, etapa, rol, principal.getName());
        return "redirect:/proyectos/checklist/" + id;
    }

    @PostMapping("/checklist/firmar-ajax/{id}")
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
    @PostMapping("/checklist/guardar-todo/{proyectoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')") 
    public String guardarChecklistCompleto(@PathVariable Long proyectoId, @RequestParam Map<String, String> allParams) {
        if (allParams != null) {
            checklistServicio.guardarChecklistCompleto(allParams);
        }
        return "redirect:/proyectos/checklist/" + proyectoId;
    }

    @PostMapping("/checklist/guardar-ajax/{proyectoId}")
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

   
    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')") 
    @CacheEvict(value = "reportes", allEntries = true)
    public String guardarProyecto(@ModelAttribute Proyecto proyecto, java.security.Principal principal) {
        boolean esNuevo = (proyecto.getId() == null);
        Proyecto proyectoGuardado = proyectoServicio.guardarProyecto(proyecto);
        
        if (esNuevo) {
            String titulo = "New Project APQP";
            String msj = "The portfolio has been initialized for the project: " + proyectoGuardado.getNombre();
            String url = "/proyectos/checklist/" + proyectoGuardado.getId();
            String autor = (principal != null) ? principal.getName() : "Sistema";
            
            notificacionServicio.alertarATodos(titulo, msj, "SUCCESS", url, autor);
        }
        
        return "redirect:/";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "reportes", allEntries = true) 
    public String eliminarProyecto(@PathVariable Long id) {
        proyectoServicio.eliminarProyecto(id);
        return "redirect:/"; 
    }

    
    @PostMapping("/archivar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "reportes", allEntries = true) 
    public String archivarProyecto(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        if (proyecto != null) {
            
            proyecto.setEsHistorico(true); 
            proyectoRepositorio.save(proyecto);
            
            notificacionServicio.alertarATodos("Project Archived", 
                "The project " + proyecto.getNombre() + " has been successfully archived.", 
                "SUCCESS", "/proyectos/vault", "System");
                
            redirectAttributes.addFlashAttribute("mensajeExito", "Project moved to Historical Vault.");
        }
        
        return "redirect:/proyectos/vault";
    }


    @GetMapping("/vault")
    public String verHistoricalVault(Model model, HttpServletRequest request) {
        List<Proyecto> proyectosHistoricos = proyectoRepositorio.findByEsHistoricoTrue();
        model.addAttribute("proyectos", proyectosHistoricos);
        model.addAttribute("currentUri", request.getRequestURI()); 
        
        return "vault"; 
    }

 
    

    @GetMapping("/exportar-master-timeline")
    public ResponseEntity<byte[]> descargarMasterTimeline() {
        try {
            List<Proyecto> proyectos = proyectoRepositorio.findByEsHistoricoFalse();
            List<ElementoChecklist> todosLosElementos = checklistServicio.obtenerTodos(); 

            byte[] data = excelServicio.generarMasterTimelineExcel(proyectos, todosLosElementos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "Master_Timeline_Overview.xlsx");

            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/guardar-hitos/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Map<String, Object> guardarHitosProyecto(@PathVariable Long id,
                                                    @RequestParam(required = false) LocalDate fechaCar,
                                                    @RequestParam(required = false) LocalDate fechaBuyoff,
                                                    @RequestParam(required = false) LocalDate fechaTransit,
                                                    @RequestParam(required = false) LocalDate fechaSop,
                                                    Principal principal) {
        Proyecto proyecto = proyectoRepositorio.findById(id).orElseThrow();
        String usuario = (principal != null) ? principal.getName() : "System";
        StringBuilder cambios = new StringBuilder();

        if (esDiferenteFecha(proyecto.getFechaCar(), fechaCar)) {
            cambios.append("CAR Approval: ").append(fechaCar != null ? fechaCar : "N/A").append(". ");
            proyecto.setFechaCar(fechaCar);
        }
        if (esDiferenteFecha(proyecto.getFechaBuyoff(), fechaBuyoff)) {
            cambios.append("Line Buy-off: ").append(fechaBuyoff != null ? fechaBuyoff : "N/A").append(". ");
            proyecto.setFechaBuyoff(fechaBuyoff);
        }
        if (esDiferenteFecha(proyecto.getFechaTransit(), fechaTransit)) {
            cambios.append("Equipment Ship: ").append(fechaTransit != null ? fechaTransit : "N/A").append(". ");
            proyecto.setFechaTransit(fechaTransit);
        }
        if (esDiferenteFecha(proyecto.getFechaSop(), fechaSop)) {
            cambios.append("SOP: ").append(fechaSop != null ? fechaSop : "N/A").append(". ");
            proyecto.setFechaSop(fechaSop);
        }

        if (cambios.length() > 0) {
            proyectoRepositorio.save(proyecto);

            String msg = "Project Executive Milestones updated for " + proyecto.getNombre() + ": " + cambios.toString();
            bitacoraServicio.registrarAccion(usuario, "UPDATE MILESTONES", msg);

            notificacionServicio.alertarATodos("Executive Milestones Updated", 
                "Milestones for " + proyecto.getNombre() + " were modified by " + usuario + ": " + cambios.toString(), 
                "INFO", "/proyectos/checklist/" + id, usuario);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("exito", true);
        response.put("nombre", proyecto.getNombre());
        response.put("numeroParte", proyecto.getNumeroParte());
        return response;
        }

    private boolean esDiferenteFecha(LocalDate actual, LocalDate nueva) {
        if (actual == null && nueva == null) return false;
        if (actual == null || nueva == null) return true;
        return !actual.equals(nueva);
    }
    @GetMapping("/exportar-pdf/{id}")
    public ResponseEntity<byte[]> descargarReportePdf(@PathVariable Long id) {
        try {
            byte[] pdf = reporteServicio.generarPdfProyecto(id);
        
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Reporte_APQP_Proyecto_" + id + ".pdf");
        
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Proyecto obtenerProyectoApi(@PathVariable Long id) {
        return proyectoServicio.buscarPorId(id);
    }
}