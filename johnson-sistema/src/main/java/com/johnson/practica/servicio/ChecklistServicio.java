package com.johnson.practica.servicio;

import com.johnson.practica.dto.ReporteProgreso;
import com.johnson.practica.dto.TimelineGrupo;
import com.johnson.practica.dto.TimelineItem;
import com.johnson.practica.dto.ReporteCascada;
import com.johnson.practica.dto.ReporteEstadoGlobal; 
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays; 

@Service
public class ChecklistServicio {

    @Autowired
    private ElementoChecklistRepositorio repositorio;

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

    @Autowired
    private BitacoraServicio bitacoraServicio; 

    @Autowired
    private com.johnson.practica.repositorio.HitoProyectoRepositorio hitoProyectoRepositorio;

    @Autowired
    private NotificacionServicio notificacionServicio;

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerHitosPrograma(Long proyectoId) {
        return repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(proyectoId, "0");
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerPorFase(Long proyectoId, String prefijoFase) {
        return repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(proyectoId, prefijoFase);
    }

    @Transactional
    @CacheEvict(value = "reportes", allEntries = true) 
    public void guardarChecklistCompleto(Map<String, String> allParams) {
        if (allParams == null || allParams.isEmpty()) return;

        Map<Long, Map<String, String>> updatesById = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if (key.contains("-")) {
                try {
                    String[] parts = key.split("-");
                    Long itemId = Long.parseLong(parts[1]);
                    updatesById.computeIfAbsent(itemId, k -> new HashMap<>()).put(parts[0], entry.getValue());
                } catch (NumberFormatException ignored) {}
            }
        }

        List<ElementoChecklist> elementosDesdeBD = repositorio.findAllById(updatesById.keySet());
        
        List<ElementoChecklist> elementosRealmenteModificados = new ArrayList<>();

        String nombreUsuarioLogueado = "Sistema"; 
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            nombreUsuarioLogueado = auth.getName();
        }

        for (ElementoChecklist elemento : elementosDesdeBD) {
            Map<String, String> cambios = updatesById.get(elemento.getId());
            
            final boolean[] huboCambioReal = {false}; 
            
            cambios.forEach((fieldName, fieldValue) -> {
                String valorNuevo = (fieldValue == null) ? "" : fieldValue.replaceAll("[\\n\\r]+", " ").trim();

                switch (fieldName) {
                    case "controlEntregable" -> {
                        String valorActual = elemento.getControlEntregable() == null ? "" : elemento.getControlEntregable().replaceAll("[\\n\\r]+", " ").trim();
                        if (!valorNuevo.equalsIgnoreCase(valorActual) && !(valorNuevo.isEmpty() && valorActual.isEmpty())) { 
                            elemento.setControlEntregable(valorNuevo); 
                            huboCambioReal[0] = true;
                        }
                    }
                    case "score" -> {
                        String valorActual = elemento.getScore() == null ? "" : elemento.getScore().replaceAll("[\\n\\r]+", " ").trim();
                        if (!valorNuevo.equalsIgnoreCase(valorActual) && !(valorNuevo.isEmpty() && valorActual.isEmpty())) {
                            elemento.setScore(valorNuevo); 
                            huboCambioReal[0] = true;
                        }
                    }
                    case "comentario" -> {
                        String valorActual = elemento.getComentario() == null ? "" : elemento.getComentario().replaceAll("[\\n\\r]+", " ").trim();
                        if (!valorNuevo.equalsIgnoreCase(valorActual) && !(valorNuevo.isEmpty() && valorActual.isEmpty())) {
                            elemento.setComentario(valorNuevo); 
                            huboCambioReal[0] = true;
                        }
                    }
                    case "estado" -> {
                        String valorActual = elemento.getEstado() == null ? "" : elemento.getEstado().replaceAll("[\\n\\r]+", " ").trim();
                        if (!valorNuevo.equalsIgnoreCase(valorActual) && !(valorNuevo.isEmpty() && valorActual.isEmpty())) {
                            elemento.setEstado(valorNuevo); 
                            huboCambioReal[0] = true;
                        }
                    }
                    case "fechaReal" -> {
                        try { 
                            if (!valorNuevo.isEmpty()) {
                                LocalDate nuevaFecha = LocalDate.parse(valorNuevo);
                                if (elemento.getFechaReal() == null || !nuevaFecha.equals(elemento.getFechaReal())) {
                                    elemento.setFechaReal(nuevaFecha); 
                                    huboCambioReal[0] = true;
                                }
                            } else if (elemento.getFechaReal() != null) {
                                elemento.setFechaReal(null); 
                                huboCambioReal[0] = true;
                            }
                        } catch (Exception ignored) {}
                    }
                    case "fechaPlan" -> {
                        try { 
                            if (!valorNuevo.isEmpty()) {
                                LocalDate nuevaFecha = LocalDate.parse(valorNuevo);
                                if (elemento.getFechaPlan() == null || !nuevaFecha.equals(elemento.getFechaPlan())) {
                                    elemento.setFechaPlan(nuevaFecha); 
                                    huboCambioReal[0] = true;
                                }
                            } else if (elemento.getFechaPlan() != null) {
                                elemento.setFechaPlan(null); 
                                huboCambioReal[0] = true;
                            }
                        } catch (Exception ignored) {}
                    }
                }
            });

            if (huboCambioReal[0]) {
                bitacoraServicio.registrarAccion(
                    nombreUsuarioLogueado, 
                    "ACTUALIZO_ENTREGABLE", 
                    "Se actualizó el entregable: " + elemento.getCodigo() + " - " + elemento.getNombre()
                );
                elementosRealmenteModificados.add(elemento);
            }
        }
        
        if (!elementosRealmenteModificados.isEmpty()) {
            repositorio.saveAll(elementosRealmenteModificados);
        }
    }

   @Cacheable("reportes")
   public List<ReporteProgreso> generarReporteGlobal() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc();
        List<ReporteProgreso> reporte = new ArrayList<>();

        for (Proyecto p : proyectos) {
           
            List<ElementoChecklist> todosLosItemsDelProyecto = repositorio.findByProyecto_Id(p.getId());
            
            List<ElementoChecklist> itemsFase0 = repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(p.getId(), "0");
            
            int total = itemsFase0.size(); 
            int ok = 0;

            for (ElementoChecklist item : itemsFase0) {
                String score = item.getScore();
                if ("OK".equalsIgnoreCase(score)){
                    ok++;
                }
            }

            double porcentaje = 0.0;
            if (total > 0) {
                porcentaje = ((double) ok / total) * 100;
            }
            porcentaje = Math.round(porcentaje * 10.0) / 10.0;

            String sopStr = (p.getSop() != null) ? p.getSop().toString() : "N/A";
            ReporteProgreso reporteProgreso = new ReporteProgreso(
                p.getId(), 
                p.getNombre(), 
                p.getCliente(), 
                p.getNumeroParte(), 
                p.getLiderProyecto(), 
                sopStr, 
                total, 
                ok, 
                porcentaje
            );
            
            double riesgo = calcularRiesgoDinamico(p, todosLosItemsDelProyecto);
            
            reporteProgreso.setRiesgo(riesgo);

            reporte.add(reporteProgreso);
        }
        return reporte;
    }

    public ReporteEstadoGlobal generarReporteEstadoGlobal() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc();
        
        int total = 0;
        int onTime = 0;
        int late = 0;
        int needsAction = 0;
        int decision = 0;
        int fulfilled = 0;

        for (Proyecto p : proyectos) {
            List<ElementoChecklist> items = repositorio.findByProyecto_Id(p.getId());
            for (ElementoChecklist item : items) {
                total++;
                String score = item.getScore();
                String control = item.getControlEntregable();

                if ("OK".equalsIgnoreCase(score)) {
                    fulfilled++;
                }

                if (control != null) {
                    String c = control.toUpperCase();
                    if (c.contains("ON TIME")) onTime++;
                    else if (c.contains("LATE")) late++;
                    else if (c.contains("NEEDS ACTION")) needsAction++;
                    else if (c.contains("DECISION")) decision++;
                }
            }
        }

        ReporteEstadoGlobal r = new ReporteEstadoGlobal();
        r.setTotalDeliverables(total);
        r.setOnTimeCount(onTime);
        r.setDelayedCount(late);
        r.setFulfilledCount(fulfilled);
        r.setEscalationCount(needsAction);

        if (total > 0) {
            r.setOnTimePercentage(Math.round((double) onTime / total * 100));
            r.setLatePercentage(Math.round((double) late / total * 100));
            r.setNeedsActionPercentage(Math.round((double) needsAction / total * 100));
            r.setDecisionPercentage(Math.round((double) decision / total * 100));
        }

        r.setRiskHigh(late);
        r.setRiskLow(onTime);
        
        return r;
    }

    @Transactional(readOnly = true)
    public List<ReporteCascada> generarReporteCascada() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc();
        List<ReporteCascada> reporte = new ArrayList<>();
        List<String> etapas = Arrays.asList("STAGE 1", "STAGE 2", "STAGE 3", "STAGE 4", "STAGE 5");

        for (Proyecto p : proyectos) {
            List<Double> porcentajes = new ArrayList<>();
            List<ElementoChecklist> items = repositorio.findByProyecto_Id(p.getId()); // Cambio: usar findByProyecto_Id
            
            for (String etapa : etapas) {
                porcentajes.add(calcularPorcentajeEtapaVisual(items, etapa));
            }
            
            String fechaSop = (p.getSop() != null) ? p.getSop().toString() : "Sin SOP";
            reporte.add(new ReporteCascada(p.getNombre(), porcentajes, fechaSop));
        }
        return reporte;
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerAlertasGlobales() {
        List<ElementoChecklist> todos = repositorio.findAll();
        return todos.stream()
                .filter(e -> e.getControlEntregable() != null && e.getControlEntregable().equalsIgnoreCase("NEEDS ACTION"))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> obtenerDatosTendencia() {
        List<ElementoChecklist> todos = repositorio.findAll();
        Map<String, Long> tendencia = new HashMap<>();
        
        for (ElementoChecklist item : todos) {
            if ("OK".equalsIgnoreCase(item.getScore()) && item.getFechaReal() != null) {
                String mesAnio = item.getFechaReal().getMonthValue() + "/" + item.getFechaReal().getYear();
                tendencia.put(mesAnio, tendencia.getOrDefault(mesAnio, 0L) + 1);
            }
        }
        return tendencia;
    }

    @Transactional(readOnly = true)
    public long obtenerLanzamientosProximos() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc();
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusMonths(6);
        
        return proyectos.stream()
                .filter(p -> p.getSop() != null && (p.getSop().isAfter(hoy) || p.getSop().isEqual(hoy)) && p.getSop().isBefore(limite))
                .count();
    }

    private double calcularPorcentajeEtapaVisual(List<ElementoChecklist> elementos, String etapaVisual) {
        List<ElementoChecklist> itemsEnEtapa = new ArrayList<>();
        for (ElementoChecklist item : elementos) {
            if (item.getEtapaVisual() != null && item.getEtapaVisual().equalsIgnoreCase(etapaVisual)) {
                itemsEnEtapa.add(item);
            }
        }

        int total = itemsEnEtapa.size();
        int ok = 0;

        for (ElementoChecklist item : itemsEnEtapa) {
            String score = item.getScore();
            if (score != null && "OK".equalsIgnoreCase(score.trim())) {
                ok++;
            }
        }

        if (total == 0) return 0.0;
        double pct = ((double) ok / total) * 100.0;
        if (pct > 100.0) {
            pct = 100.0;
        }
        return Math.round(pct * 10.0) / 10.0;
    }

   @Transactional(readOnly = true)
    public Map<String, Object> obtenerDatosTimeline() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc();
        List<TimelineGrupo> groups = new ArrayList<>();
        List<TimelineItem> items = new ArrayList<>();

        for (Proyecto p : proyectos) {
            groups.add(new TimelineGrupo(p.getId(), p.getNombre()));

            List<ElementoChecklist> todosElementos = repositorio.findByProyecto_Id(p.getId());
            List<com.johnson.practica.modelo.HitoProyecto> hitosManuales = hitoProyectoRepositorio.findByProyecto_Id(p.getId());

            for (com.johnson.practica.modelo.HitoProyecto hito : hitosManuales) {
                double progresoActual = 0.0;
                if (hito.getEtapaAsociada() != null && !hito.getEtapaAsociada().isEmpty()) {
                    progresoActual = calcularPorcentajeEtapaVisual(todosElementos, hito.getEtapaAsociada());
                }

                int objetivo = (hito.getPorcentajeObjetivo() != null) ? hito.getPorcentajeObjetivo() : 100;
                boolean completado = progresoActual >= objetivo;
                boolean isLate = hito.getFecha() != null && hito.getFecha().isBefore(LocalDate.now()) && !completado;
                
                String colorTexto = completado ? "#28a745" : (isLate ? "#dc3545" : "#3f6ad8");
                String claseCSS = completado ? "hito-completado" : (isLate ? "hito-atrasado" : "hito-pendiente");
                
                String labelPrincipal = hito.getEtapaAsociada();
                
                String htmlContent = "<div class='milestone-text'>" + 
                                      "<strong>" + labelPrincipal + "</strong>: " +                                     
                                      "<span style='color: " + colorTexto + "; font-weight: 900;'>" + (int)progresoActual + "% / " + objetivo + "%</span>" +                                                                                              
                                      "</div>";

                items.add(new TimelineItem(hito.getId() * -1, p.getId(), htmlContent, hito.getFecha().toString(), "point", claseCSS));
            }

            int contadorMainEvent = 1; 
            for (ElementoChecklist item : todosElementos) {
                if (!item.isEsMainEvent()) continue;

                LocalDate fecha = (item.getFechaPlan() != null) ? item.getFechaPlan() : item.getFechaReal();
                if (fecha != null) {
                    
                    boolean esExterno = item.getChampion() != null && (item.getChampion().contains("SCS") || item.getChampion().contains("Cliente") || item.getChampion().contains("Proveedor"));
                    boolean isDelayed = item.getFechaPlan() != null && item.getFechaPlan().isBefore(LocalDate.now()) && !"OK".equalsIgnoreCase(item.getScore());
                    
                    String claseCSS = esExterno ? "vis-event-external" : "vis-event-internal"; 
                    if (isDelayed) claseCSS += " event-delayed"; 

                    String alertIcon = isDelayed ? "<i class='bi bi-exclamation-triangle-fill text-white me-1'></i>" : "";
                    
                    String htmlBox = "<div class='event-content' data-realname='" + item.getNombre() + "' title='" + item.getNombre() + "'>" + 
                                     alertIcon + "Main event " + contadorMainEvent + 
                                     "</div>";

                    items.add(new TimelineItem(item.getId(), p.getId(), htmlBox, fecha.toString(), "box", claseCSS));
                    
                    contadorMainEvent++; 
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("groups", groups);
        data.put("items", items);
        return data;
    }

    private double calcularRiesgoDinamico(Proyecto p, List<ElementoChecklist> elementos) {
        if (elementos == null || elementos.isEmpty()) return 0.0;

        List<ElementoChecklist> preSop = elementos.stream()
                .filter(e -> e.getEtapaVisual() != null && !e.getEtapaVisual().toUpperCase().contains("STAGE 5"))
                .toList();

        long totalTareas = preSop.size();
        if (totalTareas == 0) return 0.0;

        long needsAction = preSop.stream()
                .filter(e -> "NEEDS ACTION".equalsIgnoreCase(e.getControlEntregable()))
                .count();

        long pendientesNormales = preSop.stream()
                .filter(e -> !"OK".equalsIgnoreCase(e.getScore()) && !"NEEDS ACTION".equalsIgnoreCase(e.getControlEntregable()))
                .count();

        if (needsAction == 0 && pendientesNormales == 0) return 0.0;

        LocalDate hoy = LocalDate.now();
        LocalDate sop = p.getSop();

        if (sop == null) {
            return Math.min(100.0, ((needsAction * 2.0 + pendientesNormales) * 100.0) / totalTareas);
        }

        long diasParaSop = ChronoUnit.DAYS.between(hoy, sop);

        if (diasParaSop <= 0) return 100.0;

        double multiplicadorTiempo = 1.0;

        if (diasParaSop <= 7) {
            multiplicadorTiempo = 3.0;  
        } else if (diasParaSop <= 15) {
            multiplicadorTiempo = 1.8;  
        } else if (diasParaSop <= 30) {
            multiplicadorTiempo = 1.0;  
        } else {
            multiplicadorTiempo = 0.4; 
        }

        double riesgoTotal = 0.0;

        double porcentajeFaltante = (pendientesNormales * 100.0) / totalTareas;
        riesgoTotal += (porcentajeFaltante * multiplicadorTiempo);

        double castigoNeedsAction = (needsAction * 100.0 / totalTareas) * 2.5; 
        riesgoTotal += castigoNeedsAction;

        return Math.min(100.0, Math.round(riesgoTotal * 10.0) / 10.0);
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerTareasPendientesUsuario(String username) {
        List<ElementoChecklist> todos = repositorio.findAll();
        return todos.stream()
                .filter(e -> e.getChampion() != null && 
                            e.getChampion().equalsIgnoreCase(username) && 
                            !"OK".equalsIgnoreCase(scoreFormateado(e.getScore())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerTodasTareasPendientes() {
        return repositorio.findByScoreNotIgnoreCase("OK").stream()
                .filter(e -> e.getScore() == null || !e.getScore().equalsIgnoreCase("OK"))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> obtenerTodosLosChampions() {
        return repositorio.findDistinctChampions().stream()
                .filter(c -> c != null && !c.trim().isEmpty() && !c.equalsIgnoreCase("N/A"))
                .sorted()
                .toList();
    }

    private String scoreFormateado(String s) {
        return (s == null) ? "" : s.trim().toUpperCase();
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerPorProyectoId(Long id) {
        return repositorio.findByProyecto_IdOrderByCodigoAsc(id);
    }
    


}