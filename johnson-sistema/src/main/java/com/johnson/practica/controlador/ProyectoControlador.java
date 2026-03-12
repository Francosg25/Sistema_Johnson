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
    
    @Autowired 
    private NotificacionServicio notificacionServicio;

    @Data @AllArgsConstructor
    public static class FaseVista {
        private String id;
        private String nombre;
        private List<ElementoChecklist> items;
        private Map<String, FirmaEtapa> firmas;
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
        return "checklist";
    }

    @PostMapping("/checklist/firmar/{id}")
    public String firmarEtapa(@PathVariable Long id, @RequestParam Integer etapa, @RequestParam String rol, Principal principal) {
        firmaEtapaServicio.firmar(id, etapa, rol, principal.getName());
        return "redirect:/proyectos/checklist/" + id;
    }

    @PostMapping("/checklist/guardar-todo/{proyectoId}")
    public String guardarChecklistCompleto(@PathVariable Long proyectoId, @RequestParam Map<String, String> allParams) {
        if (allParams != null) {
            checklistServicio.guardarChecklistCompleto(allParams);
        }
        return "redirect:/proyectos/checklist/" + proyectoId;
    }

    @PostMapping("/checklist/guardar-ajax/{proyectoId}")
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

    @PostMapping("/actualizar")
    @CacheEvict(value = "reportes", allEntries = true)
    public String actualizarProyecto(@ModelAttribute Proyecto proyecto, java.security.Principal principal) {
        if (proyecto.getId() != null) {
            Proyecto proyectoExistente = proyectoServicio.buscarPorId(proyecto.getId());
            if (proyectoExistente != null) {
                proyectoExistente.setNombre(proyecto.getNombre());
                proyectoExistente.setNumeroParte(proyecto.getNumeroParte());
                proyectoExistente.setCliente(proyecto.getCliente());
                proyectoExistente.setSop(proyecto.getSop());
                
                proyectoServicio.guardarProyecto(proyectoExistente);
                
                String titulo = "Project Updated";
                String msj = "Project " + proyectoExistente.getNombre() + " has been modified.";
                String autor = (principal != null) ? principal.getName() : "Sistema";
                notificacionServicio.alertarATodos(titulo, msj, "INFO", "/proyectos/checklist/" + proyecto.getId(), autor);
            }
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

    @GetMapping("/api/{id}")
    @ResponseBody
    public Proyecto obtenerProyectoApi(@PathVariable Long id) {
        return proyectoServicio.buscarPorId(id);
    }
    
    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        List<Proyecto> lista = proyectoRepositorio.findAll();
        model.addAttribute("proyectos", lista);
        model.addAttribute("currentUri", request.getRequestURI());

        Map<String, Integer> tendencia = checklistServicio.obtenerTendenciaAprobacionesOK();
        model.addAttribute("tendencia", tendencia);

        return "index";
    }

    @Autowired
    private ReporteServicio reporteServicio;

    @Autowired
    private ExcelServicio excelServicio;


    @GetMapping("/exportar-master-timeline")
    public ResponseEntity<byte[]> descargarMasterTimeline() {
        try {
            List<Proyecto> proyectos = proyectoRepositorio.findAll();
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
    public Map<String, Object> guardarHitosProyecto(@PathVariable Long id, 
                                                    @RequestParam(required = false) LocalDate fechaCar,
                                                    @RequestParam(required = false) LocalDate fechaBuyoff,
                                                    @RequestParam(required = false) LocalDate fechaTransit,
                                                    @RequestParam(required = false) LocalDate fechaSop) {
        Proyecto proyecto = proyectoRepositorio.findById(id).orElseThrow();
        proyecto.setFechaCar(fechaCar);
        proyecto.setFechaBuyoff(fechaBuyoff);
        proyecto.setFechaTransit(fechaTransit);
        proyecto.setFechaSop(fechaSop);
        proyectoRepositorio.save(proyecto);
        
        return Map.of("exito", true);
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

    



}