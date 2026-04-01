package com.johnson.practica.servicio;

import com.johnson.practica.dto.*;
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import com.johnson.practica.repositorio.HitoProyectoRepositorio;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    @Cacheable("reportes")
    public List<ReporteProgreso> generarReporteGlobal() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
            .filter(p -> !p.getEsHistorico()) 
            .collect(Collectors.toList());
        List<ReporteProgreso> reporte = new ArrayList<>();

        for (Proyecto p : proyectos) {
            List<ElementoChecklist> todosLosItemsDelProyecto = repositorio.findByProyecto_Id(p.getId());
            List<ElementoChecklist> itemsFase0 = repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(p.getId(), "0");
            
            int total = itemsFase0.size(); 
            int ok = 0;

            for (ElementoChecklist item : itemsFase0) {
                if ("OK".equalsIgnoreCase(item.getScore())){
                    ok++;
                }
            }

            double porcentaje = (total > 0) ? ((double) ok / total) * 100 : 0.0;
            porcentaje = Math.round(porcentaje * 10.0) / 10.0;

            String sopStr = (p.getSop() != null) ? p.getSop().toString() : "N/A";
            ReporteProgreso reporteProgreso = new ReporteProgreso(
                p.getId(), p.getNombre(), p.getCliente(), p.getNumeroParte(), p.getLiderProyecto(), sopStr, total, ok, porcentaje
            );
            
            reporteProgreso.setFechaCar(p.getFechaCar() != null ? p.getFechaCar().toString() : null);
            reporteProgreso.setFechaBuyoff(p.getFechaBuyoff() != null ? p.getFechaBuyoff().toString() : null);
            reporteProgreso.setFechaTransit(p.getFechaTransit() != null ? p.getFechaTransit().toString() : null);

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

            double riesgo = calcularRiesgoDinamico(p, todosLosItemsDelProyecto);
            reporteProgreso.setRiesgo(riesgo);

            reporte.add(reporteProgreso);
        }
        return reporte;
    }

    public ReporteEstadoGlobal generarReporteEstadoGlobal() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
            .filter(p -> !p.getEsHistorico())
            .collect(Collectors.toList());
        
        int total = 0, onTime = 0, late = 0, needsAction = 0, decision = 0, fulfilled = 0;

        for (Proyecto p : proyectos) {
            List<ElementoChecklist> items = repositorio.findByProyecto_Id(p.getId());
            for (ElementoChecklist item : items) {
                total++;
                if ("OK".equalsIgnoreCase(item.getScore())) fulfilled++;

                if (item.getControlEntregable() != null) {
                    String c = item.getControlEntregable().toUpperCase();
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
            .filter(p -> !p.getEsHistorico()).collect(Collectors.toList());
        List<ReporteCascada> reporte = new ArrayList<>();
        List<String> etapas = Arrays.asList("STAGE 1", "STAGE 2", "STAGE 3", "STAGE 4", "STAGE 5");

        for (Proyecto p : proyectos) {
            List<Double> porcentajes = new ArrayList<>();
            List<ElementoChecklist> items = repositorio.findByProyecto_Id(p.getId());
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
        return repositorio.findAll().stream()
                .filter(e -> e.getProyecto() != null && !e.getProyecto().getEsHistorico()) 
                .filter(e -> e.getControlEntregable() != null && e.getControlEntregable().equalsIgnoreCase("NEEDS ACTION"))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> obtenerDatosTendencia() {
        Map<String, Long> tendencia = new HashMap<>();
        for (ElementoChecklist item : repositorio.findAll()) {
            if (item.getProyecto() != null && !item.getProyecto().getEsHistorico() && "OK".equalsIgnoreCase(item.getScore()) && item.getFechaReal() != null) {
                String mesAnio = item.getFechaReal().getMonthValue() + "/" + item.getFechaReal().getYear();
                tendencia.put(mesAnio, tendencia.getOrDefault(mesAnio, 0L) + 1);
            }
        }
        return tendencia;
    }

    @Transactional(readOnly = true)
    public long obtenerLanzamientosProximos() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusMonths(6);
        return proyectoRepositorio.findAllByOrderByIdAsc().stream()
                .filter(p -> !p.getEsHistorico())
                .filter(p -> p.getSop() != null && (p.getSop().isAfter(hoy) || p.getSop().isEqual(hoy)) && p.getSop().isBefore(limite))
                .count();
    }

    private double calcularPorcentajeEtapaVisual(List<ElementoChecklist> elementos, String etapaVisual) {
        List<ElementoChecklist> itemsEnEtapa = new ArrayList<>();
        int ok = 0;

        for (ElementoChecklist item : elementos) {
            if (item.getEtapaVisual() != null && item.getEtapaVisual().equalsIgnoreCase(etapaVisual)) {
                itemsEnEtapa.add(item);
                if (item.getScore() != null && "OK".equalsIgnoreCase(item.getScore().trim())) {
                    ok++;
                }
            }
        }

        if (itemsEnEtapa.isEmpty()) return 0.0;
        double pct = ((double) ok / itemsEnEtapa.size()) * 100.0;
        return Math.round(Math.min(pct, 100.0) * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDatosTimeline() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
            .filter(p -> !p.getEsHistorico()).collect(Collectors.toList());
        
        List<TimelineGrupo> groups = new ArrayList<>();
        List<TimelineItem> items = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (Proyecto p : proyectos) {
            groups.add(new TimelineGrupo(p.getId(), p.getNombre()));
            List<ElementoChecklist> todosElementos = repositorio.findByProyecto_Id(p.getId());
            List<com.johnson.practica.modelo.HitoProyecto> hitosManuales = hitoProyectoRepositorio.findByProyecto_Id(p.getId());

            for (com.johnson.practica.modelo.HitoProyecto hito : hitosManuales) {
                double progresoActual = (hito.getEtapaAsociada() != null && !hito.getEtapaAsociada().isEmpty()) 
                        ? calcularPorcentajeEtapaVisual(todosElementos, hito.getEtapaAsociada()) : 0.0;

                int objetivo = (hito.getPorcentajeObjetivo() != null) ? hito.getPorcentajeObjetivo() : 100;
                boolean completado = progresoActual >= objetivo;
                boolean isLate = hito.getFecha() != null && hito.getFecha().isBefore(hoy) && !completado;
                
                String colorTexto = completado ? "#28a745" : (isLate ? "#dc3545" : "#3f6ad8");
                String claseCSS = completado ? "hito-completado" : (isLate ? "hito-atrasado" : "hito-pendiente");
                
                String htmlContent = "<div class='milestone-text'><strong>" + hito.getEtapaAsociada() + "</strong>: " +                                     
                                     "<span style='color: " + colorTexto + "; font-weight: 900;'>" + (int)progresoActual + "% / " + objetivo + "%</span></div>";

                items.add(new TimelineItem(hito.getId() * -1, p.getId(), htmlContent, hito.getFecha().toString(), "point", claseCSS));
            }

           int contadorMainEvent = 1; 
            for (ElementoChecklist item : todosElementos) {
                if (!item.isEsMainEvent()) continue;

                LocalDate fecha = (item.getFechaPlan() != null) ? item.getFechaPlan() : item.getFechaReal();
                if (fecha != null) {
                    boolean esExterno = item.getChampion() != null && (item.getChampion().contains("SCS") || item.getChampion().contains("Cliente") || item.getChampion().contains("Proveedor"));
                    
                    String claseCSS = esExterno ? "vis-event vis-event-external " : "vis-event vis-event-internal "; 
                    
                   
                  
                    String scoreActual = item.getScore() != null ? item.getScore().trim().toUpperCase() : "";
                    String controlActual = item.getControlEntregable() != null ? item.getControlEntregable().trim().toUpperCase() : "";

                    boolean estaCompletado = scoreActual.equals("OK") || scoreActual.equals("NA") || scoreActual.equals("N/A");

                    boolean estaRetrasado = false;
                    
                    if (item.getFechaPlan() != null && item.getFechaPlan().isBefore(hoy)) {
                        estaRetrasado = true;
                    }
                    
                    if (item.getFechaReal() != null && item.getFechaPlan() != null && item.getFechaReal().isAfter(item.getFechaPlan())) {
                        estaRetrasado = true;
                    }
                    
                    if (controlActual.contains("LATE") || controlActual.contains("ACTION")) {
                        estaRetrasado = true;
                    }

                    if (estaCompletado) {
                        claseCSS += "event-completed"; // Verde (Prioridad 1: Ya tiene el OK oficial)
                    } else if (estaRetrasado) {
                        claseCSS += "event-delayed";   // Rojo (Prioridad 2: Vencido matemáticamente o por HOY)
                    } else {
                        claseCSS += "event-pending";   // Azul (Prioridad 3: Pendiente pero a tiempo)
                    }
                    // --------------------------------------------
                    
                    String alertIcon = claseCSS.contains("event-delayed") ? "<i class='bi bi-exclamation-triangle-fill text-white me-1'></i>" : "";
                    String htmlBox = "<div class='event-content' data-realname='" + item.getNombre() + "' title='" + item.getNombre() + "'>" + 
                                     alertIcon + "Main event " + contadorMainEvent + "</div>";

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
                .collect(Collectors.toList());

        long totalTareas = preSop.size();
        if (totalTareas == 0) return 0.0;

        long needsAction = preSop.stream().filter(e -> "NEEDS ACTION".equalsIgnoreCase(e.getControlEntregable())).count();
        long pendientesNormales = preSop.stream().filter(e -> !"OK".equalsIgnoreCase(e.getScore()) && !"NEEDS ACTION".equalsIgnoreCase(e.getControlEntregable())).count();

        if (needsAction == 0 && pendientesNormales == 0) return 0.0;

        LocalDate hoy = LocalDate.now();
        LocalDate sop = p.getSop();

        if (sop == null) {
            return Math.min(100.0, ((needsAction * 2.0 + pendientesNormales) * 100.0) / totalTareas);
        }

        long diasParaSop = ChronoUnit.DAYS.between(hoy, sop);
        if (diasParaSop <= 0) return 100.0;

        double multiplicadorTiempo = (diasParaSop <= 7) ? 3.0 : (diasParaSop <= 15 ? 1.8 : (diasParaSop <= 30 ? 1.0 : 0.4));
        double riesgoTotal = ((pendientesNormales * 100.0) / totalTareas * multiplicadorTiempo) + ((needsAction * 100.0 / totalTareas) * 2.5);

        return Math.min(100.0, Math.round(riesgoTotal * 10.0) / 10.0);
    }
}