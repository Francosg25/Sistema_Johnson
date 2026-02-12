package com.johnson.practica.servicio;

import com.johnson.practica.model.ElementoChecklist;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ChecklistServicio {

    @Autowired
    private ElementoChecklistRepositorio repositorio;

    // 1. Obtiene solo los HITOS (Programa APQP - Fase 0)
    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerHitosPrograma(Long proyectoId) {
        return repositorio.findByProyectoIdAndCatalogoFaseStartingWithOrderByCodigoAsc(proyectoId, "0");
    }

    // 2. MÉTODO NUEVO: Sirve para Stage 2, 3, 4 y 5
    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerPorFase(Long proyectoId, String prefijoFase) {
        return repositorio.findByProyectoIdAndCatalogoFaseStartingWithOrderByCodigoAsc(proyectoId, prefijoFase);
    }

    // 3. Obtiene solo Stage 2 (Lo dejamos por compatibilidad con tu código anterior)
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
                    // Ignorar claves que no sigan el formato esperado, como "_csrf"
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
                                // Ignorar si el formato de fecha no es válido
                            }
                            break;
                        case "fechaPlan":
                            try {
                                if (fieldValue != null && !fieldValue.isEmpty()) {
                                    elemento.setFechaPlan(java.time.LocalDate.parse(fieldValue));
                                }
                            } catch (java.time.format.DateTimeParseException e) {
                                // Ignorar si el formato de fecha no es válido
                            }
                            break;
                    }
                });
            });
        }
    }
}