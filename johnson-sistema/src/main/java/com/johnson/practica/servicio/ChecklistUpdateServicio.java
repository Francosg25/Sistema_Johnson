package com.johnson.practica.servicio;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChecklistUpdateServicio {

    @Autowired
    private ElementoChecklistRepositorio repositorio;

    @Autowired
    private BitacoraServicio bitacoraServicio;

    @Autowired
    private NotificacionServicio notificacionServicio;

    @Autowired
    private ChecklistLogicServicio checklistLogicServicio;

    @Transactional
    @CacheEvict(value = "reportes", allEntries = true)
    public void guardarChecklistCompleto(Map<String, String> allParams) {
        if (allParams == null || allParams.isEmpty()) return;

        Map<Long, Map<String, String>> updatesById = groupingParamsById(allParams);

        List<ElementoChecklist> elementosDesdeBD = repositorio.findAllById(updatesById.keySet());
        List<ElementoChecklist> elementosRealmenteModificados = new ArrayList<>();

        String nombreUsuarioLogueado = getAuthenticatedUsername();

        for (ElementoChecklist elemento : elementosDesdeBD) {
            Map<String, String> cambios = updatesById.get(elemento.getId());
            boolean huboCambioReal = applyChangesToElement(elemento, cambios, nombreUsuarioLogueado);

            // Auto-reevaluate status
            if (reevaluateStatus(elemento)) {
                huboCambioReal = true;
            }

            if (huboCambioReal) {
                registrarYNotificarCambio(elemento, nombreUsuarioLogueado);
                elementosRealmenteModificados.add(elemento);
            }
        }
        
        if (!elementosRealmenteModificados.isEmpty()) {
            repositorio.saveAll(elementosRealmenteModificados);
        }
    }

    private Map<Long, Map<String, String>> groupingParamsById(Map<String, String> allParams) {
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
        return updatesById;
    }

    private String getAuthenticatedUsername() {
        String usuarioAudit = "Sistema";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            usuarioAudit = auth.getName();
        }
        return usuarioAudit;
    }

    private boolean applyChangesToElement(ElementoChecklist elemento, Map<String, String> cambios, String usuario) {
        final boolean[] huboCambio = {false};
        cambios.forEach((fieldName, fieldValue) -> {
            String valorNuevo = (fieldValue == null) ? "" : fieldValue.trim();
            if (updateField(elemento, fieldName, valorNuevo, usuario)) {
                huboCambio[0] = true;
            }
        });
        return huboCambio[0];
    }

    private boolean updateField(ElementoChecklist elemento, String fieldName, String valorNuevo, String usuario) {
        switch (fieldName) {
            case "controlEntregable" -> {
                if (esDiferente(elemento.getControlEntregable(), valorNuevo)) {
                    elemento.setControlEntregable(valorNuevo);
                    return true;
                }
            }
            case "score" -> {
                if (esDiferente(elemento.getScore(), valorNuevo)) {
                    if ("OK".equalsIgnoreCase(valorNuevo)) {
                        notificarScoreOk(elemento, usuario);
                        if (elemento.getFechaReal() == null) {
                            elemento.setFechaReal(LocalDate.now());
                        }
                    }
                    elemento.setScore(valorNuevo);
                    return true;
                }
            }
            case "comentario" -> {
                if (esDiferente(elemento.getComentario(), valorNuevo)) {
                    elemento.setComentario(valorNuevo);
                    checklistLogicServicio.procesarMenciones(valorNuevo, elemento, usuario);
                    return true;
                }
            }
            case "estado" -> {
                if (esDiferente(elemento.getEstado(), valorNuevo)) {
                    if ("OK".equalsIgnoreCase(valorNuevo) && elemento.getFechaReal() == null) {
                        elemento.setFechaReal(LocalDate.now());
                    }
                    notificarGateValidation(elemento, valorNuevo, usuario);
                    elemento.setEstado(valorNuevo);
                    return true;
                }
            }
            case "fechaReal" -> {
                return updateDateReal(elemento, valorNuevo);
            }
            case "fechaPlan" -> {
                return updateDatePlan(elemento, valorNuevo);
            }
        }
        return false;
    }

    private boolean updateDateReal(ElementoChecklist elemento, String valorNuevo) {
        try {
            if (!valorNuevo.isEmpty()) {
                LocalDate nuevaFecha = LocalDate.parse(valorNuevo);
                if (elemento.getFechaReal() == null || !nuevaFecha.equals(elemento.getFechaReal())) {
                    elemento.setFechaReal(nuevaFecha);
                    return true;
                }
            } else if (elemento.getFechaReal() != null) {
                elemento.setFechaReal(null);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean updateDatePlan(ElementoChecklist elemento, String valorNuevo) {
        try {
            if (!valorNuevo.isEmpty()) {
                LocalDate nuevaFecha = LocalDate.parse(valorNuevo);
                if (elemento.getFechaPlan() == null || !nuevaFecha.equals(elemento.getFechaPlan())) {
                    elemento.setFechaPlan(nuevaFecha);
                    return true;
                }
            } else if (elemento.getFechaPlan() != null) {
                elemento.setFechaPlan(null);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void notificarScoreOk(ElementoChecklist elemento, String usuario) {
        String titulo = "Deliverable OK";
        String msj = "The deliverable '" + elemento.getNombre() + "' in " + elemento.getProyecto().getNombre() + " was marked as OK.";
        String url = "/proyectos/checklist/" + elemento.getProyecto().getId();
        notificacionServicio.alertarATodos(titulo, msj, "SUCCESS", url, usuario);
    }

    private void notificarGateValidation(ElementoChecklist elemento, String valorNuevo, String usuario) {
        boolean esGate345 = elemento.getFase() != null && 
                           (elemento.getFase().startsWith("3") || 
                            elemento.getFase().startsWith("4") || 
                            elemento.getFase().startsWith("5"));
        
        if (esGate345 && "GATE".equals(elemento.getTipoInput()) && "Validation".equals(elemento.getGrupo())) {
            String gateNum = elemento.getFase().substring(0, 1);
            String respuesta = "OK".equalsIgnoreCase(valorNuevo) ? "YES" : ("NOK".equalsIgnoreCase(valorNuevo) ? "NO" : valorNuevo);
            String titulo = "Gate " + gateNum + " Validation: " + respuesta;
            String msj = "The requirement '" + elemento.getNombre() + "' in " + elemento.getProyecto().getNombre() + " was marked as " + respuesta + ".";
            String url = "/proyectos/checklist/" + elemento.getProyecto().getId();
            notificacionServicio.alertarATodos(titulo, msj, "INFO", url, usuario);
        }
    }

    private boolean reevaluateStatus(ElementoChecklist elemento) {
        if (elemento.getFechaPlan() == null) return false;

        String currentCtrl = (elemento.getControlEntregable() == null) ? "" : elemento.getControlEntregable();
        boolean changed = false;

        if (elemento.getFechaReal() != null) {
            if (elemento.getFechaReal().isAfter(elemento.getFechaPlan())) {
                if (!"Closed late".equalsIgnoreCase(currentCtrl)) {
                    elemento.setControlEntregable("Closed late");
                    changed = true;
                }
            } else {
                if (!"Closed on time".equalsIgnoreCase(currentCtrl) && 
                    !"DECISION".equalsIgnoreCase(currentCtrl) && 
                    !"NEEDS ACTION".equalsIgnoreCase(currentCtrl)) {
                    elemento.setControlEntregable("Closed on time");
                    changed = true;
                }
            }
        } else if (elemento.getFechaPlan().isBefore(LocalDate.now()) && !"OK".equalsIgnoreCase(elemento.getScore())) {
            if (!"Closed late".equalsIgnoreCase(currentCtrl) && !"Closed on time".equalsIgnoreCase(currentCtrl)) {
                elemento.setControlEntregable("Closed late");
                changed = true;
            }
        }
        return changed;
    }

    private void registrarYNotificarCambio(ElementoChecklist elemento, String usuario) {
        bitacoraServicio.registrarAccion(
            usuario, 
            "UPDATE DELIVERABLE", 
            "Deliverable: " + elemento.getNombre() + " was updated in project " + elemento.getProyecto().getNombre()
        );
        
        String lider = elemento.getProyecto().getLiderProyecto();
        if (lider != null && !lider.equalsIgnoreCase(usuario)) {
            notificacionServicio.alertarAUsuario(
                lider,
                "Deliverable Updated",
                "The item '" + elemento.getNombre() + "' has been updated by " + usuario,
                "INFO",
                "/proyectos/checklist/" + elemento.getProyecto().getId(),
                usuario
            );
        }
    }

    private boolean esDiferente(String actual, String nuevo) {
        String a = (actual == null) ? "" : actual.replaceAll("[\\n\\r]+", " ").trim();
        String n = (nuevo == null) ? "" : nuevo.replaceAll("[\\n\\r]+", " ").trim();
        if (n.isEmpty() && (a.isEmpty() || a.equalsIgnoreCase("PENDING") || a.equalsIgnoreCase("Open"))) return false;
        return !n.equalsIgnoreCase(a);
    }
}
