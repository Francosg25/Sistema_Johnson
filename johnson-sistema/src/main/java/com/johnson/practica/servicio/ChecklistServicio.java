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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays; 

@Service
public class ChecklistServicio {

    @Autowired
    private ElementoChecklistRepositorio repositorio;

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerHitosPrograma(Long proyectoId) {
        return repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(proyectoId, "0");
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerPorFase(Long proyectoId, String prefijoFase) {
        return repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(proyectoId, prefijoFase);
    }

    @Transactional
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

        for (Map.Entry<Long, Map<String, String>> entry : updatesById.entrySet()) {
            repositorio.findById(entry.getKey()).ifPresent(elemento -> {
                entry.getValue().forEach((fieldName, fieldValue) -> {
                    switch (fieldName) {
                        case "controlEntregable" -> elemento.setControlEntregable(fieldValue);
                        case "score" -> elemento.setScore(fieldValue);
                        case "comentario" -> elemento.setComentario(fieldValue);
                        case "estado" -> elemento.setEstado(fieldValue);
                        case "fechaReal" -> {
                            try { if (fieldValue != null && !fieldValue.isEmpty()) elemento.setFechaReal(LocalDate.parse(fieldValue)); } 
                            catch (Exception ignored) {}
                        }
                        case "fechaPlan" -> {
                            try { if (fieldValue != null && !fieldValue.isEmpty()) elemento.setFechaPlan(LocalDate.parse(fieldValue)); } 
                            catch (Exception ignored) {}
                        }
                    }
                });
            });
        }
    }

    // --- 3. MÉTODOS DE REPORTES ---

    public List<ReporteProgreso> generarReporteGlobal() {
        List<Proyecto> proyectos = proyectoRepositorio.findAll();
        List<ReporteProgreso> reporte = new ArrayList<>();

        for (Proyecto p : proyectos) {
            List<ElementoChecklist> items = repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(p.getId(), "0");
            int total = items.size(); 
            int ok = 0;

            for (ElementoChecklist item : items) {
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

            reporte.add(new ReporteProgreso(p.getNombre(), total, ok, porcentaje));
        }
        return reporte;
    }

    public ReporteEstadoGlobal generarReporteEstadoGlobal() {
        List<Proyecto> proyectos = proyectoRepositorio.findAll();
        
        int totalRelevantDeliverables = 0;
        int onTimeCount = 0;
        int needsActionCount = 0;
        int lateCount = 0;
        int decisionCount = 0;

        for (Proyecto p : proyectos) {
            List<ElementoChecklist> items = repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(p.getId(), "0");
            
            for (ElementoChecklist item : items) {
                totalRelevantDeliverables++;
                String controlEntregable = item.getControlEntregable();
                
                if (controlEntregable != null && !controlEntregable.trim().isEmpty()) {
                    String estadoControl = controlEntregable.toUpperCase().trim();
                    
                    if (estadoControl.contains("ON TIME")) {
                        onTimeCount++;
                    } else if (estadoControl.contains("NEEDS ACTION")) {
                        needsActionCount++;
                    } else if (estadoControl.contains("LATE")) {
                        lateCount++;
                    } else if (estadoControl.contains("DECISION")) {
                        decisionCount++;
                    }
                }
            }
        }

        ReporteEstadoGlobal reporte = new ReporteEstadoGlobal();

        if (totalRelevantDeliverables > 0) {
            reporte.setOnTimePercentage(Math.round(((double) onTimeCount / totalRelevantDeliverables) * 10000.0) / 100.0);
            reporte.setLatePercentage(Math.round(((double) lateCount / totalRelevantDeliverables) * 10000.0) / 100.0);
            reporte.setNeedsActionPercentage(Math.round(((double) needsActionCount / totalRelevantDeliverables) * 10000.0) / 100.0);
            reporte.setDecisionPercentage(Math.round(((double) decisionCount / totalRelevantDeliverables) * 10000.0) / 100.0);
        } else {
            reporte.setOnTimePercentage(0.0);
            reporte.setLatePercentage(0.0);
            reporte.setNeedsActionPercentage(0.0);
            reporte.setDecisionPercentage(0.0);
        }
        
        return reporte;
    }

    @Transactional(readOnly = true)
    public List<ReporteCascada> generarReporteCascada() {
        List<Proyecto> proyectos = proyectoRepositorio.findAll();
        List<ReporteCascada> reporte = new ArrayList<>();
        List<String> etapas = Arrays.asList("ETAPA 1", "ETAPA 2", "ETAPA 3", "ETAPA 4", "ETAPA 5");

        for (Proyecto p : proyectos) {
            List<Double> porcentajes = new ArrayList<>();
            List<ElementoChecklist> items = repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(p.getId(), "0");
            
            for (String etapa : etapas) {
                porcentajes.add(calcularPorcentajeEtapaVisual(items, etapa));
            }
            
            // SOLUCIÓN: Convertir a String antes de instanciar el DTO
            String fechaSop = (p.getSop() != null) ? p.getSop().toString() : "Sin SOP";
            reporte.add(new ReporteCascada(p.getNombre(), porcentajes, fechaSop));
        }
        return reporte;
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
        // Ensure percentage does not exceed 100.0
        if (pct > 100.0) {
            pct = 100.0;
        }
        return Math.round(pct * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDatosTimeline() {
        List<Proyecto> proyectos = proyectoRepositorio.findAll();
        List<TimelineGrupo> groups = new ArrayList<>();
        List<TimelineItem> items = new ArrayList<>();

        for (Proyecto p : proyectos) {
            groups.add(new TimelineGrupo(p.getId(), p.getNombre()));

            List<ElementoChecklist> todosElementos = repositorio.findByProyecto_Id(p.getId());

            for (ElementoChecklist item : todosElementos) {
                LocalDate fecha = (item.getFechaPlan() != null) ? item.getFechaPlan() : item.getFechaReal();

                if (fecha != null) {
                    boolean esMilestone = "HITO".equals(item.getTipoInput()) || 
                                          (item.getFase() != null && item.getFase().contains("Programa"));

                    boolean isDelayed = false;
                    if ("LATE".equalsIgnoreCase(item.getControlEntregable())) {
                        isDelayed = true;
                    } else if (item.getFechaPlan() != null && item.getFechaPlan().isBefore(LocalDate.now()) && !"OK".equalsIgnoreCase(item.getScore())) {
                        // Si la fecha plan ya pasó y no tiene score "OK", está atrasado
                        isDelayed = true;
                    }

                    String contenido = esMilestone ? item.getNombre() : item.getCodigo();
                    String claseCSS = "";

                    if (esMilestone) {
                        claseCSS = "vis-milestone";
                    } else {
                        boolean esExt = item.getChampion() != null && (item.getChampion().contains("Compras") || item.getChampion().contains("Cliente"));
                        claseCSS = esExt ? "vis-event-external" : "vis-event-internal";
                    }

                    if (isDelayed) {
                        claseCSS += " delayed-item"; // Nueva clase CSS para el borde rojo
                        contenido = "<i class='bi bi-exclamation-circle-fill text-danger me-1 fs-6'></i> " + contenido;
                    }

                    // 5. Agregar al Timeline
                    String tipoForma = esMilestone ? "point" : "box";
                    items.add(new TimelineItem(item.getId(), p.getId(), contenido, fecha.toString(), tipoForma, claseCSS));
                }
            }

        }

        Map<String, Object> data = new HashMap<>();
        data.put("groups", groups);
        data.put("items", items);
        return data;
    }
}




