package com.johnson.practica.servicio;

import com.johnson.practica.dto.ReporteProgreso;
import com.johnson.practica.dto.ReporteCascada;
import com.johnson.practica.dto.ReporteEstadoGlobal;
import com.johnson.practica.dto.TimelineGrupo;
import com.johnson.practica.dto.TimelineItem;
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.modelo.HitoProyecto;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.repositorio.HitoProyectoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChecklistReporteServicio {

    @Autowired
    private ElementoChecklistRepositorio repositorio;

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

    @Autowired
    private HitoProyectoRepositorio hitoProyectoRepositorio;

    @Autowired
    private ChecklistLogicServicio checklistLogicServicio;

    @Cacheable("reportes")
    public List<ReporteProgreso> generarReporteGlobal() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
            .filter(p -> !p.getEsHistorico()) 
            .toList();
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
                p.getId(), p.getNombre(), p.getCliente(), p.getNumeroParte(), p.getLiderProyecto(), sopStr, total, ok, porcentaje
            );
            
            reporteProgreso.setFechaCar(p.getFechaCar() != null ? p.getFechaCar().toString() : null);
            reporteProgreso.setFechaBuyoff(p.getFechaBuyoff() != null ? p.getFechaBuyoff().toString() : null);
            reporteProgreso.setFechaTransit(p.getFechaTransit() != null ? p.getFechaTransit().toString() : null);

            // Health task counts for individual project
            int pOnTime = 0, pLate = 0, pNeedsAction = 0, pDecision = 0;
            for (ElementoChecklist item : todosLosItemsDelProyecto) {
                String control = item.getControlEntregable();
                if (control != null) {
                    String c = control.toUpperCase();
                    if (c.contains("ON TIME")) pOnTime++;
                    else if (c.contains("LATE")) pLate++;
                    else if (c.contains("NEEDS ACTION")) pNeedsAction++;
                    else if (c.contains("DECISION")) pDecision++;
                }
            }
            reporteProgreso.setOnTimeCount(pOnTime);
            reporteProgreso.setLateCount(pLate);
            reporteProgreso.setNeedsActionCount(pNeedsAction);
            reporteProgreso.setDecisionCount(pDecision);

            double riesgo = checklistLogicServicio.calcularRiesgoDinamico(p, todosLosItemsDelProyecto);
            reporteProgreso.setRiesgo(riesgo);

            reporte.add(reporteProgreso);
        }
        return reporte;
    }

    public ReporteEstadoGlobal generarReporteEstadoGlobal() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
            .filter(p -> !p.getEsHistorico())
            .toList();
        
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
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
            .filter(p -> !p.getEsHistorico())
            .toList();
        List<ReporteCascada> reporte = new ArrayList<>();
        List<String> etapas = Arrays.asList("STAGE 1", "STAGE 2", "STAGE 3", "STAGE 4", "STAGE 5");

        for (Proyecto p : proyectos) {
            List<Double> porcentajes = new ArrayList<>();
            List<ElementoChecklist> items = repositorio.findByProyecto_Id(p.getId());
            
            for (String etapa : etapas) {
                porcentajes.add(checklistLogicServicio.calcularPorcentajeEtapaVisual(items, etapa));
            }
            
            String fechaSop = (p.getSop() != null) ? p.getSop().toString() : "Sin SOP";
            reporte.add(new ReporteCascada(p.getNombre(), porcentajes, fechaSop));
        }
        return reporte;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> obtenerDatosTendencia() {
        List<ElementoChecklist> todos = repositorio.findAll();
        Map<String, Long> tendencia = new HashMap<>();
        
        for (ElementoChecklist item : todos) {
            if (item.getProyecto() != null && !item.getProyecto().getEsHistorico() && "OK".equalsIgnoreCase(item.getScore()) && item.getFechaReal() != null) {
                String mesAnio = item.getFechaReal().getMonthValue() + "/" + item.getFechaReal().getYear();
                tendencia.put(mesAnio, tendencia.getOrDefault(mesAnio, 0L) + 1);
            }
        }
        return tendencia;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDatosTimeline() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
            .filter(p -> !p.getEsHistorico())
            .toList();
        List<TimelineGrupo> groups = new ArrayList<>();
        List<TimelineItem> items = new ArrayList<>();

        for (Proyecto p : proyectos) {
            groups.add(new TimelineGrupo(p.getId(), p.getNombre()));

            List<ElementoChecklist> todosElementos = repositorio.findByProyecto_Id(p.getId());
            List<HitoProyecto> hitosManuales = hitoProyectoRepositorio.findByProyecto_Id(p.getId());

            for (HitoProyecto hito : hitosManuales) {
                double progresoActual = 0.0;
                if (hito.getEtapaAsociada() != null && !hito.getEtapaAsociada().isEmpty()) {
                    progresoActual = checklistLogicServicio.calcularPorcentajeEtapaVisual(todosElementos, hito.getEtapaAsociada());
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

    @Transactional(readOnly = true)
    public long obtenerLanzamientosProximos() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
                .filter(p -> !p.getEsHistorico())
                .toList();
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusMonths(6);
        
        return proyectos.stream()
                .filter(p -> p.getSop() != null && (p.getSop().isAfter(hoy) || p.getSop().isEqual(hoy)) && p.getSop().isBefore(limite))
                .count();
    }

    public Map<String, Integer> obtenerTendenciaAprobacionesOK() {
        List<ElementoChecklist> todos = repositorio.findAll();
        Map<String, Integer> tendencia = new HashMap<>();

        for (ElementoChecklist e : todos) {
            if (e.getProyecto() != null && !e.getProyecto().getEsHistorico()) {
                boolean isScoreOk = "OK".equalsIgnoreCase(e.getScore());
                boolean isEstadoOk = "OK".equalsIgnoreCase(e.getEstado());

                if (isScoreOk || isEstadoOk) {
                    LocalDate fecha = e.getFechaReal() != null ? e.getFechaReal() : LocalDate.now();
                    String mesAnio = fecha.getMonthValue() + "/" + fecha.getYear();
                    tendencia.put(mesAnio, tendencia.getOrDefault(mesAnio, 0) + 1);
                }
            }
        }
        return tendencia;
    }
}
