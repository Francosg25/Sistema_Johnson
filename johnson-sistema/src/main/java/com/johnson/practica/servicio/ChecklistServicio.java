package com.johnson.practica.servicio;

import com.johnson.practica.dto.ReporteProgreso;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays; 

@Service
public class ChecklistServicio {

    @Autowired
    private ElementoChecklistRepositorio repositorio;

    @Autowired
    private ProyectoRepositorio proyectoRepositorio; 

    // 1. Obtiene solo los HITOS (Programa APQP - Fase 0)
    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerHitosPrograma(Long proyectoId) {
        return repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(proyectoId, "0");
    }

    // 2. MÉTODO NUEVO: Sirve para Stage 2, 3, 4 y 5
    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerPorFase(Long proyectoId, String prefijoFase) {
        return repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(proyectoId, prefijoFase);
    }

    // 3. Obtiene solo Stage 2
    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerChecklistStage2(Long proyectoId) {
        return obtenerPorFase(proyectoId, "2");
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerChecklistStage3(Long proyectoId) {
        return obtenerPorFase(proyectoId, "3");
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerChecklistStage4(Long proyectoId) {
        return obtenerPorFase(proyectoId, "4");
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerChecklistStage5(Long proyectoId) {
        return obtenerPorFase(proyectoId, "5");
    }

    @Transactional
    public void guardarChecklistCompleto(Map<String, String> allParams) {
        if (allParams == null || allParams.isEmpty()) {
            return;
        }

        // 1. Agrupar parámetros por ID de item
        Map<Long, Map<String, String>> updatesById = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.contains("-")) {
                try {
                    String[] parts = key.split("-");
                    String fieldName = parts[0];
                    Long itemId = Long.parseLong(parts[1]);

                    updatesById.computeIfAbsent(itemId, k -> new HashMap<>()).put(fieldName, value);
                } catch (NumberFormatException e) {

                }
            }
        }

        // 2. Iterar y actualizar entidades
        for (Map.Entry<Long, Map<String, String>> entry : updatesById.entrySet()) {
            Long itemId = entry.getKey();
            Map<String, String> fieldsToUpdate = entry.getValue();

            repositorio.findById(itemId).ifPresent(elemento -> {
                fieldsToUpdate.forEach((fieldName, fieldValue) -> {
                    switch (fieldName) {
                        case "controlEntregable":
                            elemento.setControlEntregable(fieldValue);
                            break;
                        case "score":
                            elemento.setScore(fieldValue);
                            break;
                        case "comentario":
                            elemento.setComentario(fieldValue);
                            break;
                        case "estado":
                            elemento.setEstado(fieldValue);
                            break;
                        case "fechaReal":
                            try {
                                if (fieldValue != null && !fieldValue.isEmpty()) {
                                    elemento.setFechaReal(java.time.LocalDate.parse(fieldValue));
                                }
                            } catch (java.time.format.DateTimeParseException e) {
                            }
                            break;
                        case "fechaPlan":
                            try {
                                if (fieldValue != null && !fieldValue.isEmpty()) {
                                    elemento.setFechaPlan(java.time.LocalDate.parse(fieldValue));
                                }
                            } catch (java.time.format.DateTimeParseException e) {
                            }
                            break;
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

    public List<ReporteCascada> generarReporteCascada() {
        List<Proyecto> proyectos = proyectoRepositorio.findAll();
        List<ReporteCascada> reporte = new ArrayList<>();
        List<String> etapasVisuales = Arrays.asList("ETAPA 2", "ETAPA 3", "ETAPA 4", "ETAPA 5");

        for (Proyecto p : proyectos) {
            List<Double> porcentajes = new ArrayList<>();
            List<ElementoChecklist> elementosProgramaAPQP = repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(p.getId(), "0");

            for (String etapa : etapasVisuales) {
                porcentajes.add(calcularPorcentajeEtapaVisual(elementosProgramaAPQP, etapa));
            }
            reporte.add(new ReporteCascada(p.getNombre(), porcentajes));
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
}
