package com.johnson.practica.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.servicio.ChecklistServicio;
import com.johnson.practica.servicio.ChecklistReporteServicio; 
import com.johnson.practica.servicio.NotificacionServicio; 
import com.johnson.practica.repositorio.UsuarioRepositorio; 
import com.johnson.practica.dto.ReporteProgreso;
import com.johnson.practica.dto.ReporteEstadoGlobal;
import com.johnson.practica.modelo.Notificacion; 
import com.johnson.practica.modelo.Usuario; 
import com.johnson.practica.modelo.ElementoChecklist; 
import com.johnson.practica.modelo.Proyecto; 

import java.security.Principal; 
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.List;
import java.util.Map; 
import java.util.stream.Collectors; 

@Controller
public class DashboardControlador {

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

    @Autowired
    private ChecklistServicio checklistServicio;

    @Autowired
    private ChecklistReporteServicio checklistReporteServicio; 

    @Autowired
    private NotificacionServicio notificacionServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio; 

    @Autowired
    private com.johnson.practica.servicio.BitacoraServicio bitacoraServicio;

    @GetMapping("/")
    public String index(Model model, jakarta.servlet.http.HttpServletRequest request, Principal principal) {
        
        List<Proyecto> proyectos = proyectoRepositorio.findByEsHistoricoFalse();
        
        List<ReporteProgreso> reporteGlobal = checklistReporteServicio.generarReporteGlobal().stream() 
            .filter(r -> {
                Proyecto p = proyectoRepositorio.findById(r.getId()).orElse(null);
                return p != null && !p.getEsHistorico(); 
            }).toList();
            
        ReporteEstadoGlobal estadoGlobal = checklistReporteServicio.generarReporteEstadoGlobal(); 
        
        double avancePromedio = 0;
        if (!reporteGlobal.isEmpty()) {
            avancePromedio = reporteGlobal.stream().mapToDouble(ReporteProgreso::getPorcentaje).average().orElse(0.0);
        }
        avancePromedio = Math.round(avancePromedio * 10.0) / 10.0;

        List<Notificacion> notificaciones = new ArrayList<>();
        if (principal != null) {
            Usuario usuarioActual = usuarioRepositorio.findByUsername(principal.getName()).orElse(null);
            if (usuarioActual != null) {
                notificaciones = notificacionServicio.obtenerNoLeidas(usuarioActual);
            }
        }

        model.addAttribute("proyectos", proyectos);
        model.addAttribute("reporteProgreso", reporteGlobal);
        model.addAttribute("estadoGlobal", estadoGlobal);
        model.addAttribute("alertas", checklistReporteServicio.obtenerAlertasGlobales().stream() 
            .filter(a -> proyectos.stream().anyMatch(p -> a.getNombre().contains(p.getNombre())))
            .toList());
        model.addAttribute("tendencia", checklistReporteServicio.obtenerDatosTendencia()); 
        model.addAttribute("proximosLanzamientos", checklistReporteServicio.obtenerLanzamientosProximos()); 
        model.addAttribute("avancePromedio", avancePromedio);
        
        model.addAttribute("notificaciones", notificaciones); 
        model.addAttribute("ultimosMovimientos", bitacoraServicio.obtenerUltimosMovimientos().stream().limit(5).toList());
        
        List<ElementoChecklist> todasPendientes = checklistServicio.obtenerTodasTareasPendientes().stream()
            .filter(t -> !t.getProyecto().getEsHistorico())
            .filter(t -> {
                String fase = t.getFase() != null ? t.getFase() : "";
                String codigo = t.getCodigo() != null ? t.getCodigo().toUpperCase() : "";
                
                // Excluimos si la Fase se llama explícitamente "2", "3", "4", o "5"
                if (fase.contains("2") || fase.contains("3") || fase.contains("4") || fase.contains("5")) {
                    return false; 
                }
                
                // Por máxima seguridad, excluimos por el Código del entregable 
                if (codigo.startsWith("S2") || codigo.startsWith("S3") || codigo.startsWith("G3") || 
                    codigo.startsWith("S4") || codigo.startsWith("G4") || 
                    codigo.startsWith("S5") || codigo.startsWith("G5")) {
                    return false;
                }
                
                return true; 
            })
            .toList();

        model.addAttribute("todasTareasPendientes", todasPendientes);
        
        Map<String, List<ElementoChecklist>> tareasAgrupadas = todasPendientes.stream()
            .collect(Collectors.groupingBy(t -> t.getProyecto().getNombre()));
        model.addAttribute("tareasAgrupadas", tareasAgrupadas);

        model.addAttribute("listaChampions", checklistServicio.obtenerTodosLosChampions());
        
        Map<String, Long> conteoPorChampion = todasPendientes.stream()
            .filter(t -> t.getChampion() != null)
            .collect(Collectors.groupingBy(t -> checklistServicio.normalizarChampion(t.getChampion()), Collectors.counting()));
        model.addAttribute("conteoPorChampion", conteoPorChampion);

        List<Map<String, Object>> eventosCalendario = new ArrayList<>();
        for (Proyecto p : proyectos) {
            if (p.getSop() != null) {
                Map<String, Object> event = new HashMap<>();
                event.put("title", "SOP: " + p.getNombre());
                event.put("start", p.getSop().toString());
                event.put("description", "Start of Production - PN: " + p.getNumeroParte());
                event.put("className", "bg-warning border-0 shadow-sm"); 
                event.put("url", "/proyectos/checklist/" + p.getId());
                eventosCalendario.add(event);
            }
        }
        model.addAttribute("eventosCalendario", eventosCalendario);

        if (principal != null) {
            model.addAttribute("usuarioLogueado", principal.getName());
        }

        model.addAttribute("titulo", "Dashboard de Proyectos APQP - Johnson Electric");
        model.addAttribute("currentUri", request.getRequestURI());
        return "index"; 
    }
}