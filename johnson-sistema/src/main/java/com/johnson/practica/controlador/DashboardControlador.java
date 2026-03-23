package com.johnson.practica.controlador;

import com.johnson.practica.dto.ReporteEstadoGlobal;
import com.johnson.practica.dto.ReporteProgreso;
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Notificacion;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.modelo.Usuario;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.repositorio.UsuarioRepositorio;
import com.johnson.practica.servicio.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
    private ChecklistLogicServicio checklistLogicServicio;

    @Autowired
    private NotificacionServicio notificacionServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio; 

    @Autowired
    private BitacoraServicio bitacoraServicio;

    @GetMapping("/")
    public String index(Model modelo, HttpServletRequest solicitud, Principal principal) {
        
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

        modelo.addAttribute("proyectos", proyectos);
        modelo.addAttribute("reporteProgreso", reporteGlobal);
        modelo.addAttribute("estadoGlobal", estadoGlobal);
        modelo.addAttribute("alertas", checklistServicio.obtenerAlertasGlobales().stream()
            .filter(a -> proyectos.stream().anyMatch(p -> a.getNombre().contains(p.getNombre())))
            .toList());
        modelo.addAttribute("tendencia", checklistReporteServicio.obtenerDatosTendencia());
        modelo.addAttribute("proximosLanzamientos", checklistReporteServicio.obtenerLanzamientosProximos());
        modelo.addAttribute("avancePromedio", avancePromedio);
        
        modelo.addAttribute("notificaciones", notificaciones); 
        modelo.addAttribute("ultimosMovimientos", bitacoraServicio.obtenerUltimosMovimientos().stream().limit(5).toList());
        
        List<ElementoChecklist> todasPendientes = checklistServicio.obtenerTodasTareasPendientes().stream()
            .filter(t -> !t.getProyecto().getEsHistorico())
            .toList();

        modelo.addAttribute("todasTareasPendientes", todasPendientes);
        modelo.addAttribute("listaChampions", checklistServicio.obtenerTodosLosChampions());
        
        Map<String, Long> conteoPorChampion = todasPendientes.stream()
            .filter(t -> t.getChampion() != null)
            .collect(Collectors.groupingBy(t -> checklistLogicServicio.normalizarChampion(t.getChampion()), Collectors.counting()));
        modelo.addAttribute("conteoPorChampion", conteoPorChampion);

        List<Map<String, Object>> eventosCalendario = new ArrayList<>();
        for (Proyecto p : proyectos) {
            if (p.getSop() != null) {
                Map<String, Object> evento = new HashMap<>();
                evento.put("title", "SOP: " + p.getNombre());
                evento.put("start", p.getSop().toString());
                evento.put("description", "Inicio de Producción - PN: " + p.getNumeroParte());
                evento.put("className", "bg-warning border-0 shadow-sm"); 
                evento.put("url", "/proyectos/checklist/" + p.getId());
                eventosCalendario.add(evento);
            }
        }
        modelo.addAttribute("eventosCalendario", eventosCalendario);

        if (principal != null) {
            modelo.addAttribute("usuarioLogueado", principal.getName());
        }

        modelo.addAttribute("titulo", "Dashboard de Proyectos APQP - Johnson Electric");
        modelo.addAttribute("currentUri", solicitud.getRequestURI());
        return "index"; 
    }
}
