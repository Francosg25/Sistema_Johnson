package com.johnson.practica.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.servicio.ChecklistServicio;
import com.johnson.practica.servicio.NotificacionServicio; 
import com.johnson.practica.repositorio.UsuarioRepositorio; // <-- NUEVO
import com.johnson.practica.dto.ReporteProgreso;
import com.johnson.practica.dto.ReporteEstadoGlobal;
import com.johnson.practica.modelo.Notificacion; 
import com.johnson.practica.modelo.Usuario; // <-- NUEVO

import com.johnson.practica.modelo.ElementoChecklist; // <-- NUEVO
import com.johnson.practica.modelo.Proyecto; // <-- NUEVO
import java.security.Principal; // <-- NUEVO
import java.util.ArrayList;
import java.util.HashMap; // <-- NUEVO
import java.util.List;
import java.util.Map; // <-- NUEVO
import java.util.stream.Collectors; // <-- NUEVO

@Controller
public class DashboardControlador {

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

    @Autowired
    private ChecklistServicio checklistServicio;

    @Autowired
    private NotificacionServicio notificacionServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio; // Inyectamos el repo de usuarios

    @Autowired
    private com.johnson.practica.servicio.BitacoraServicio bitacoraServicio;

    @GetMapping("/")
    public String index(Model model, jakarta.servlet.http.HttpServletRequest request, Principal principal) {
        List<Proyecto> proyectos = proyectoRepositorio.findAll();
        List<ReporteProgreso> reporteGlobal = checklistServicio.generarReporteGlobal();
        ReporteEstadoGlobal estadoGlobal = checklistServicio.generarReporteEstadoGlobal();
        
        double avancePromedio = 0;
        if (!reporteGlobal.isEmpty()) {
            avancePromedio = reporteGlobal.stream().mapToDouble(ReporteProgreso::getPorcentaje).average().orElse(0.0);
        }
        avancePromedio = Math.round(avancePromedio * 10.0) / 10.0;

        // --- SOLUCIÓN DEL ERROR: Buscamos al usuario logueado antes de pedir sus alertas ---
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
        model.addAttribute("alertas", checklistServicio.obtenerAlertasGlobales());
        model.addAttribute("tendencia", checklistServicio.obtenerDatosTendencia());
        model.addAttribute("proximosLanzamientos", checklistServicio.obtenerLanzamientosProximos());
        model.addAttribute("avancePromedio", avancePromedio);
        
        model.addAttribute("notificaciones", notificaciones); 
        model.addAttribute("ultimosMovimientos", bitacoraServicio.obtenerUltimosMovimientos().stream().limit(5).toList());
        
        // --- SECCIÓN: MIS TAREAS / FILTRO POR CHAMPION ---
        List<ElementoChecklist> todasPendientes = checklistServicio.obtenerTodasTareasPendientes();
        model.addAttribute("todasTareasPendientes", todasPendientes);
        model.addAttribute("listaChampions", checklistServicio.obtenerTodosLosChampions());
        
        // Agregamos conteo por champion para el filtro moderno
        Map<String, Long> conteoPorChampion = todasPendientes.stream()
            .filter(t -> t.getChampion() != null)
            .collect(Collectors.groupingBy(t -> checklistServicio.normalizarChampion(t.getChampion()), Collectors.counting()));
        model.addAttribute("conteoPorChampion", conteoPorChampion);

        // --- SECCIÓN: CALENDARIO DE EVENTOS ---
        List<Map<String, Object>> eventosCalendario = new ArrayList<>();
        for (Proyecto p : proyectos) {
            // Evento SOP (Usamos el campo 'sop' que es el principal)
            if (p.getSop() != null) {
                Map<String, Object> event = new HashMap<>();
                event.put("title", "SOP: " + p.getNombre());
                event.put("start", p.getSop().toString());
                event.put("description", "Start of Production - PN: " + p.getNumeroParte());
                event.put("className", "bg-warning border-0 shadow-sm text-dark fw-bold"); 
                event.put("url", "/proyectos/checklist/" + p.getId());
                event.put("tipo", "SOP");
                eventosCalendario.add(event);
            }

            // Evento CAR
            if (p.getFechaCar() != null) {
                Map<String, Object> event = new HashMap<>();
                event.put("title", "CAR: " + p.getNombre());
                event.put("start", p.getFechaCar().toString());
                event.put("className", "bg-primary border-0 shadow-sm text-white");
                event.put("url", "/proyectos/checklist/" + p.getId());
                event.put("tipo", "CAR");
                eventosCalendario.add(event);
            }

            // Evento Buyoff
            if (p.getFechaBuyoff() != null) {
                Map<String, Object> event = new HashMap<>();
                event.put("title", "Buyoff: " + p.getNombre());
                event.put("start", p.getFechaBuyoff().toString());
                event.put("className", "bg-success border-0 shadow-sm text-white");
                event.put("url", "/proyectos/checklist/" + p.getId());
                event.put("tipo", "BUYOFF");
                eventosCalendario.add(event);
            }

            // Evento Ship (Transit)
            if (p.getFechaTransit() != null) {
                Map<String, Object> event = new HashMap<>();
                event.put("title", "Ship: " + p.getNombre());
                event.put("start", p.getFechaTransit().toString());
                event.put("className", "bg-info border-0 shadow-sm text-white");
                event.put("url", "/proyectos/checklist/" + p.getId());
                event.put("tipo", "SHIP");
                eventosCalendario.add(event);
            }
        }
        model.addAttribute("eventosCalendario", eventosCalendario);

        // --- FILTRO PARA "UPCOMING EVENTS" (Solo de hoy en adelante y ordenados) ---
        java.time.LocalDate hoy = java.time.LocalDate.now();
        List<Map<String, Object>> eventosProximos = eventosCalendario.stream()
            .filter(e -> {
                java.time.LocalDate fecha = java.time.LocalDate.parse((String)e.get("start"));
                return !fecha.isBefore(hoy);
            })
            .sorted((a, b) -> {
                java.time.LocalDate fA = java.time.LocalDate.parse((String)a.get("start"));
                java.time.LocalDate fB = java.time.LocalDate.parse((String)b.get("start"));
                return fA.compareTo(fB);
            })
            .collect(Collectors.toList());
        model.addAttribute("eventosProximos", eventosProximos);

        if (principal != null) {
            model.addAttribute("usuarioLogueado", principal.getName());
        }

        model.addAttribute("titulo", "Dashboard de Proyectos APQP - Johnson Electric");
        model.addAttribute("currentUri", request.getRequestURI());
        return "index"; 
    }
}