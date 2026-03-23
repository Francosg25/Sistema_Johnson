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
    public void guardarChecklistCompleto(Map<String, String> todosLosParametros) {
        if (todosLosParametros == null || todosLosParametros.isEmpty()) return;

        Map<Long, Map<String, String>> actualizacionesPorId = agruparParametrosPorId(todosLosParametros);

        List<ElementoChecklist> elementosDesdeBD = repositorio.findAllById(actualizacionesPorId.keySet());
        List<ElementoChecklist> elementosModificados = new ArrayList<>();

        String usuarioLogueado = obtenerUsuarioAutenticado();

        for (ElementoChecklist elemento : elementosDesdeBD) {
            Map<String, String> cambios = actualizacionesPorId.get(elemento.getId());
            boolean huboCambioReal = aplicarCambiosAElemento(elemento, cambios, usuarioLogueado);

            if (reevaluarEstadoControl(elemento)) {
                huboCambioReal = true;
            }

            if (huboCambioReal) {
                registrarYNotificarCambio(elemento, usuarioLogueado);
                elementosModificados.add(elemento);
            }
        }
        
        if (!elementosModificados.isEmpty()) {
            repositorio.saveAll(elementosModificados);
        }
    }

    private Map<Long, Map<String, String>> agruparParametrosPorId(Map<String, String> todosLosParametros) {
        Map<Long, Map<String, String>> mapa = new HashMap<>();
        for (Map.Entry<String, String> entrada : todosLosParametros.entrySet()) {
            String clave = entrada.getKey();
            if (clave.contains("-")) {
                try {
                    String[] partes = clave.split("-");
                    Long idItem = Long.parseLong(partes[1]);
                    mapa.computeIfAbsent(idItem, k -> new HashMap<>()).put(partes[0], entrada.getValue());
                } catch (NumberFormatException ignorado) {}
            }
        }
        return mapa;
    }

    private String obtenerUsuarioAutenticado() {
        String usuarioAudit = "System";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            usuarioAudit = auth.getName();
        }
        return usuarioAudit;
    }

    private boolean aplicarCambiosAElemento(ElementoChecklist elemento, Map<String, String> cambios, String usuario) {
        final boolean[] huboCambio = {false};
        cambios.forEach((nombreCampo, valorCampo) -> {
            String valorNuevo = (valorCampo == null) ? "" : valorCampo.trim();
            if (actualizarCampo(elemento, nombreCampo, valorNuevo, usuario)) {
                huboCambio[0] = true;
            }
        });
        return huboCambio[0];
    }

    private boolean actualizarCampo(ElementoChecklist elemento, String nombreCampo, String valorNuevo, String usuario) {
        switch (nombreCampo) {
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
                    notificarValidacionGate(elemento, valorNuevo, usuario);
                    elemento.setEstado(valorNuevo);
                    return true;
                }
            }
            case "fechaReal" -> {
                return actualizarFechaReal(elemento, valorNuevo);
            }
            case "fechaPlan" -> {
                return actualizarFechaPlan(elemento, valorNuevo);
            }
        }
        return false;
    }

    private boolean actualizarFechaReal(ElementoChecklist elemento, String valorNuevo) {
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
        } catch (Exception ignorado) {}
        return false;
    }

    private boolean actualizarFechaPlan(ElementoChecklist elemento, String valorNuevo) {
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
        } catch (Exception ignorado) {}
        return false;
    }

    private void notificarScoreOk(ElementoChecklist elemento, String usuario) {
        String titulo = "Deliverable OK";
        String msj = "The deliverable '" + elemento.getNombre() + "' in " + elemento.getProyecto().getNombre() + " was marked as OK.";
        String url = "/proyectos/checklist/" + elemento.getProyecto().getId();
        notificacionServicio.alertarATodos(titulo, msj, "SUCCESS", url, usuario);
    }

    private void notificarValidacionGate(ElementoChecklist elemento, String valorNuevo, String usuario) {
        boolean esGate345 = elemento.getFase() != null && 
                           (elemento.getFase().startsWith("3") || 
                            elemento.getFase().startsWith("4") || 
                            elemento.getFase().startsWith("5"));
        
        if (esGate345 && "GATE".equals(elemento.getTipoInput()) && "Validation".equals(elemento.getGrupo())) {
            String numGate = elemento.getFase().substring(0, 1);
            String respuesta = "OK".equalsIgnoreCase(valorNuevo) ? "YES" : ("NOK".equalsIgnoreCase(valorNuevo) ? "NO" : valorNuevo);
            String titulo = "Gate " + numGate + " Validation: " + respuesta;
            String msj = "The requirement '" + elemento.getNombre() + "' in " + elemento.getProyecto().getNombre() + " was marked as " + respuesta + ".";
            String url = "/proyectos/checklist/" + elemento.getProyecto().getId();
            notificacionServicio.alertarATodos(titulo, msj, "INFO", url, usuario);
        }
    }

    private boolean reevaluarEstadoControl(ElementoChecklist elemento) {
        if (elemento.getFechaPlan() == null) return false;

        String ctrlActual = (elemento.getControlEntregable() == null) ? "" : elemento.getControlEntregable();
        boolean cambio = false;

        if (elemento.getFechaReal() != null) {
            if (elemento.getFechaReal().isAfter(elemento.getFechaPlan())) {
                if (!"Closed late".equalsIgnoreCase(ctrlActual)) {
                    elemento.setControlEntregable("Closed late");
                    cambio = true;
                }
            } else {
                if (!"Closed on time".equalsIgnoreCase(ctrlActual) && 
                    !"DECISION".equalsIgnoreCase(ctrlActual) && 
                    !"NEEDS ACTION".equalsIgnoreCase(ctrlActual)) {
                    elemento.setControlEntregable("Closed on time");
                    cambio = true;
                }
            }
        } else if (elemento.getFechaPlan().isBefore(LocalDate.now()) && !"OK".equalsIgnoreCase(elemento.getScore())) {
            if (!"Closed late".equalsIgnoreCase(ctrlActual) && !"Closed on time".equalsIgnoreCase(ctrlActual)) {
                elemento.setControlEntregable("Closed late");
                cambio = true;
            }
        }
        return cambio;
    }

    private void registrarYNotificarCambio(ElementoChecklist elemento, String usuario) {
        bitacoraServicio.registrarAccion(
            usuario, 
            "UPDATE DELIVERABLE", 
            "Deliverable: " + elemento.getNombre() + " updated in project " + elemento.getProyecto().getNombre()
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
