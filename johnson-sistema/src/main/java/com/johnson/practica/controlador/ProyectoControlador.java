package com.johnson.practica.controlador;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.servicio.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional; 
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/proyectos")
public class ProyectoControlador {

    @Autowired private ProyectoServicio proyectoServicio;
    @Autowired private ChecklistServicio checklistServicio;
    @Autowired private ChecklistReporteServicio checklistReporteServicio;
    @Autowired private ProyectoRepositorio proyectoRepositorio;
    @Autowired private NotificacionServicio notificacionServicio;
    @Autowired private ReporteServicio reporteServicio;
    @Autowired private ExcelServicio excelServicio;
    @Autowired private BitacoraServicio bitacoraServicio;

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        List<Proyecto> lista = proyectoRepositorio.findByEsHistoricoFalse(); 
        model.addAttribute("proyectos", lista);
        model.addAttribute("currentUri", request.getRequestURI());

        Map<String, Integer> tendencia = checklistReporteServicio.obtenerTendenciaAprobacionesOK();
        model.addAttribute("tendencia", tendencia);

        return "index";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')") 
    @CacheEvict(value = "reportes", allEntries = true)
    public String guardarProyecto(@ModelAttribute Proyecto proyecto, Principal principal) {
        boolean esNuevo = (proyecto.getId() == null);
        Proyecto proyectoGuardado = proyectoServicio.guardarProyecto(proyecto);
        
        if (esNuevo) {
            String autor = (principal != null) ? principal.getName() : "Sistema";
            notificacionServicio.alertarATodos("New Project APQP", 
                "The portfolio has been initialized for the project: " + proyectoGuardado.getNombre(), 
                "SUCCESS", "/proyectos/checklist/" + proyectoGuardado.getId(), autor);
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
    public String archivarProyecto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        proyectoServicio.archivarProyecto(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Project moved to Historical Vault.");
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
            notificacionServicio.alertarATodos("Executive Milestones Updated", msg, "INFO", "/proyectos/checklist/" + id, usuario);
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
