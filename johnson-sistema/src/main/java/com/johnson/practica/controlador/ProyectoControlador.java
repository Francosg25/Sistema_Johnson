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
    public String index(Model modelo, HttpServletRequest solicitud) {
        List<Proyecto> lista = proyectoRepositorio.findByEsHistoricoFalse(); 
        modelo.addAttribute("proyectos", lista);
        modelo.addAttribute("currentUri", solicitud.getRequestURI());

        Map<String, Integer> tendencia = checklistReporteServicio.obtenerTendenciaAprobacionesOK();
        modelo.addAttribute("tendencia", tendencia);

        return "index";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')") 
    @CacheEvict(value = "reportes", allEntries = true)
    public String guardarProyecto(@ModelAttribute Proyecto proyecto, Principal principal) {
        boolean esNuevo = (proyecto.getId() == null);
        Proyecto proyectoGuardado = proyectoServicio.guardarProyecto(proyecto);
        
        if (esNuevo) {
            String autor = (principal != null) ? principal.getName() : "System";
            notificacionServicio.alertarATodos("New APQP Project", 
                "Portfolio has been initialized for project: " + proyectoGuardado.getNombre(), 
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
    public String archivarProyecto(@PathVariable Long id, RedirectAttributes atributosRedireccion) {
        proyectoServicio.archivarProyecto(id);
        atributosRedireccion.addFlashAttribute("mensajeExito", "Project moved to Historical Vault.");
        return "redirect:/proyectos/vault";
    }

    @GetMapping("/vault")
    public String verBaulHistorico(Model modelo, HttpServletRequest solicitud) {
        List<Proyecto> proyectosHistoricos = proyectoRepositorio.findByEsHistoricoTrue();
        modelo.addAttribute("proyectos", proyectosHistoricos);
        modelo.addAttribute("currentUri", solicitud.getRequestURI()); 
        return "vault"; 
    }

    @GetMapping("/exportar-master-timeline")
    public ResponseEntity<byte[]> descargarMasterTimeline() {
        try {
            List<Proyecto> proyectos = proyectoRepositorio.findByEsHistoricoFalse();
            List<ElementoChecklist> todosLosElementos = checklistServicio.obtenerTodos(); 
            byte[] datos = excelServicio.generarExcelMasterTimeline(proyectos, todosLosElementos);

            HttpHeaders encabezados = new HttpHeaders();
            encabezados.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            encabezados.setContentDispositionFormData("attachment", "Master_Timeline_Overview.xlsx");

            return new ResponseEntity<>(datos, encabezados, HttpStatus.OK);
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

        if (esFechaDiferente(proyecto.getFechaCar(), fechaCar)) {
            cambios.append("CAR Approval: ").append(fechaCar != null ? fechaCar : "N/A").append(". ");
            proyecto.setFechaCar(fechaCar);
        }
        if (esFechaDiferente(proyecto.getFechaBuyoff(), fechaBuyoff)) {
            cambios.append("Line Buy-off: ").append(fechaBuyoff != null ? fechaBuyoff : "N/A").append(". ");
            proyecto.setFechaBuyoff(fechaBuyoff);
        }
        if (esFechaDiferente(proyecto.getFechaTransit(), fechaTransit)) {
            cambios.append("Equipment Transit: ").append(fechaTransit != null ? fechaTransit : "N/A").append(". ");
            proyecto.setFechaTransit(fechaTransit);
        }
        if (esFechaDiferente(proyecto.getFechaSop(), fechaSop)) {
            cambios.append("SOP: ").append(fechaSop != null ? fechaSop : "N/A").append(". ");
            proyecto.setFechaSop(fechaSop);
        }

        if (cambios.length() > 0) {
            proyectoRepositorio.save(proyecto);
            String mensaje = "Executive milestones updated for " + proyecto.getNombre() + ": " + cambios.toString();
            bitacoraServicio.registrarAccion(usuario, "UPDATE MILESTONES", mensaje);
            notificacionServicio.alertarATodos("Executive Milestones Updated", mensaje, "INFO", "/proyectos/checklist/" + id, usuario);
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("exito", true);
        respuesta.put("nombre", proyecto.getNombre());
        respuesta.put("numeroParte", proyecto.getNumeroParte());
        return respuesta;
    }

    private boolean esFechaDiferente(LocalDate actual, LocalDate nueva) {
        if (actual == null && nueva == null) return false;
        if (actual == null || nueva == null) return true;
        return !actual.equals(nueva);
    }

    @GetMapping("/exportar-pdf/{id}")
    public ResponseEntity<byte[]> descargarReportePdf(@PathVariable Long id) {
        try {
            byte[] pdf = reporteServicio.generarPdfProyecto(id);
            HttpHeaders encabezados = new HttpHeaders();
            encabezados.setContentType(MediaType.APPLICATION_PDF);
            encabezados.setContentDispositionFormData("attachment", "Report_APQP_Project_" + id + ".pdf");
            return new ResponseEntity<>(pdf, encabezados, HttpStatus.OK);
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
