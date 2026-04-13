package com.johnson.practica.controlador;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.repositorio.AdjuntoRepositorio;
import com.johnson.practica.servicio.ChecklistReporteServicio;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.johnson.practica.servicio.FirmaEtapaServicio;
import com.johnson.practica.modelo.FirmaEtapa;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;


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
    @Autowired private ChecklistReporteServicio checklistReporteServicio;
    @Autowired private org.springframework.context.ApplicationEventPublisher eventPublisher;
    @Autowired private UsuarioRepositorio usuarioRepositorio;
    @Autowired private PasswordEncoder passwordEncoder;

    @Data
    @AllArgsConstructor
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
        model.addAttribute("firmasGate3", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 3));
        model.addAttribute("firmasGate4", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 4));
        model.addAttribute("firmasGate5", firmaEtapaServicio.obtenerFirmasPorEtapa(id, 5));

        return "proyectos/checklist";
    }

    @PostMapping("/checklist/firmar-ajax/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAMPION')")
    @ResponseBody
    public Map<String, Object> firmarEtapaAjax(@PathVariable Long id, 
                                             @RequestParam Integer etapa, 
                                             @RequestParam String rol, 
                                             @RequestParam String password,
                                             @RequestParam(required = false) String nombreAsignado,
                                             Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioRepositorio.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 1. Validar Contraseña
            if (!passwordEncoder.matches(password, usuario.getPassword())) {
                response.put("exito", false);
                response.put("tipo", "PASSWORD_ERROR");
                response.put("mensaje", "Contraseña incorrecta. Tu identidad no ha podido ser validada.");
                return response;
            }

            // 2. Validar Identidad (Admin bypass)
            boolean esAdmin = usuario.getRoles().stream()
                    .anyMatch(r -> r.getNombre().equals("ROLE_ADMIN"));

            if (!esAdmin) {
                // 2a. Check if it's a manager-only gate (3, 4, 5)
                if (etapa >= 3 && etapa <= 5) {
                    if (!usuario.isEsManager()) {
                        response.put("exito", false);
                        response.put("mensaje", "❌ Only Managers can sign off on Gates 3, 4, and 5. Please contact your department manager.");
                        return response;
                    }
                }

                // 2b. Mapeo simple de Puesto -> Departamento
                String deptoRequerido = "";
                String r = rol.toUpperCase();
                
                if (r.contains("QUALITY")) deptoRequerido = "QE";
                else if (r.contains("PROCESS") || r.contains("PE")) deptoRequerido = "PE";
                else if (r.contains("PROGRAM") || r.contains("PM")) deptoRequerido = "PM";
                else if (r.contains("PROJECT") || r.contains("PROJ")) deptoRequerido = "PROJ";
                else if (r.contains("OPERATIONS") || r.contains("OPS")) deptoRequerido = "OPS";
                else if (r.contains("FINANCE") || r.contains("FIN")) deptoRequerido = "FIN";
                else if (r.contains("HR") || r.contains("HUMAN")) deptoRequerido = "HR";
                else if (r.contains("MATERIALS") || r.contains("MAT")) deptoRequerido = "MAT";
                else if (r.contains("SCS") || r.contains("SUPPLY")) deptoRequerido = "SCS";
                else if (r.contains("DESIGN") || r.contains("DE")) deptoRequerido = "DE";

                // Validar si el departamento del usuario coincide
                String deptoUsuario = (usuario.getDepartamento() != null) ? usuario.getDepartamento().toUpperCase() : "";
                
                // Flexible project department check
                boolean deptoValido = deptoUsuario.equals(deptoRequerido) || deptoUsuario.equals("ALL");
                if (!deptoValido && deptoRequerido.equals("PROJ")) {
                    deptoValido = deptoUsuario.equals("PROJ_LEAD") || deptoUsuario.equals("PM");
                }
                
                if (!deptoValido) {
                    response.put("exito", false);
                    response.put("mensaje", "❌ No perteneces al departamento de " + deptoRequerido + ". Solo personal de esta área puede firmar como " + rol + ".");
                    return response;
                }
            }

            // 3. Proceder con la firma
            firmaEtapaServicio.firmar(id, etapa, rol, principal.getName());
            response.put("exito", true);
            response.put("mensaje", "Signature applied successfully.");

        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", e.getMessage());
        }
        return response;
    }

    @PostMapping("/checklist/eliminar-firma-ajax/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public Map<String, Object> eliminarFirmaAjax(@PathVariable Long id, 
                                               @RequestParam Integer etapa, 
                                               @RequestParam String rol, 
                                               Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            firmaEtapaServicio.eliminarFirma(id, etapa, rol);
            
            String admin = (principal != null) ? principal.getName() : "System";
            bitacoraServicio.registrarAccion(admin, "DELETE_SIGNATURE", 
                "Signature removed from Gate " + etapa + " for role: " + rol + " in project ID: " + id);
            
            response.put("exito", true);
            response.put("mensaje", "Firma eliminada correctamente.");
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al eliminar firma: " + e.getMessage());
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

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProyecto(Model model) {
        model.addAttribute("proyecto", new Proyecto());
        model.addAttribute("currentUri", "/proyectos");
        
        return "proyectos/formulario"; 
    }

    

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')") 
    @CacheEvict(value = "reportes", allEntries = true)
    @Transactional
    public String guardarProyecto(@ModelAttribute Proyecto proyecto, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            String usuarioLogueado = (auth != null) ? auth.getName() : "System";
            boolean esNuevo = (proyecto.getId() == null);
            
            if (!esNuevo) {
                Proyecto anterior = proyectoServicio.buscarPorId(proyecto.getId());
                if (anterior != null) {
                    StringBuilder cambios = new StringBuilder();
                    if (anterior.getNombre() != null && !anterior.getNombre().equals(proyecto.getNombre())) 
                        cambios.append("Name (").append(anterior.getNombre()).append(" -> ").append(proyecto.getNombre()).append("). ");
                    if (anterior.getNumeroParte() != null && !anterior.getNumeroParte().equals(proyecto.getNumeroParte()))
                        cambios.append("P/N (").append(anterior.getNumeroParte()).append(" -> ").append(proyecto.getNumeroParte()).append("). ");
                    if (anterior.getCliente() != null && !anterior.getCliente().equals(proyecto.getCliente()))
                        cambios.append("Customer (").append(anterior.getCliente()).append(" -> ").append(proyecto.getCliente()).append("). ");
                    
                    if (cambios.length() > 0) {
                        bitacoraServicio.registrarAccion(usuarioLogueado, "UPDATE PROJECT", 
                            "Project modified: " + anterior.getNombre() + ". Changes: " + cambios.toString());
                    }
                }
            }

            Proyecto proyectoGuardado = proyectoServicio.guardarProyecto(proyecto);
            
            if (esNuevo) {
                String titulo = "New Project APQP";
                String msj = "The portfolio has been initialized for the project: " + proyectoGuardado.getNombre();
                String url = "/proyectos/checklist/" + proyectoGuardado.getId();
                notificacionServicio.alertarATodos(titulo, msj, "SUCCESS", url, usuarioLogueado);
                redirectAttributes.addFlashAttribute("mensaje", "Project created successfully.");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Project updated successfully.");
            }
            
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving project: " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        // Buscamos el proyecto real en la base de datos
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
    
        if (proyecto == null) {
            return "redirect:/"; // Si no existe, regresamos al Dashboard
        }

        // Pasamos el proyecto encontrado al modelo
        model.addAttribute("proyecto", proyecto);
    
        // Pasamos la URI para que el sidebar no explote
        model.addAttribute("currentUri", "/proyectos");
    
        return "proyectos/formulario";
}

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "reportes", allEntries = true) 
    public String eliminarProyecto(@PathVariable Long id) {
        proyectoServicio.eliminarProyecto(id);
        return "redirect:/"; 
    }

    @Autowired private AdjuntoRepositorio adjuntoRepositorio;

    @PostMapping("/archivar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "reportes", allEntries = true) 
    @Transactional
    public String archivarProyecto(@PathVariable Long id, RedirectAttributes redirectAttributes, Principal principal) {
        Proyecto proyecto = proyectoServicio.buscarPorId(id);
        if (proyecto != null) {
            String usuario = (principal != null) ? principal.getName() : "System";
            
            proyecto.setEsHistorico(true); 
            proyectoRepositorio.save(proyecto);
            
            bitacoraServicio.registrarAccion(usuario, "ARCHIVE PROJECT", 
                "Project moved to historical vault: " + proyecto.getNombre());
            
            notificacionServicio.alertarATodos("Project Archived", 
                "The project " + proyecto.getNombre() + " has been moved to the historical vault.", 
                "SUCCESS", "/proyectos/vault", usuario);
                
            redirectAttributes.addFlashAttribute("mensaje", "Project moved to Historical Vault. Evidence remains accessible there.");
        }
        return "redirect:/proyectos/vault";
    }

    @GetMapping("/vista-general")
    public String verVistaGeneral(Model model, HttpServletRequest request) {
        List<Proyecto> proyectos = proyectoRepositorio.findByEsHistoricoFalse();
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("currentUri", request.getRequestURI());
        return "proyectos/vista-general";
    }

    @GetMapping("/vault")
    public String verHistoricalVault(Model model, HttpServletRequest request) {
        List<Proyecto> historicos = proyectoRepositorio.findByEsHistoricoTrue();
        Map<Integer, Map<String, List<Proyecto>>> agrupados = historicos.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                p -> p.getFechaSop() != null ? p.getFechaSop().getYear() : LocalDate.now().getYear(),
                java.util.stream.Collectors.groupingBy(
                    p -> {
                        LocalDate fecha = p.getFechaSop() != null ? p.getFechaSop() : LocalDate.now();
                        return fecha.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
                    }
                )
            ));
        model.addAttribute("agrupados", agrupados);
        model.addAttribute("currentUri", request.getRequestURI());
        return "proyectos/vault";
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
            bitacoraServicio.registrarAccion(usuario, "UPDATE MILESTONES", "Project Executive Milestones updated: " + cambios.toString());
            notificacionServicio.alertarATodos("Executive Milestones Updated", 
                "Milestones for " + proyecto.getNombre() + " were modified.", 
                "INFO", "/proyectos/checklist/" + id, usuario);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("exito", true);
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
