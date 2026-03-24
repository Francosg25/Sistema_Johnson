package com.johnson.practica.servicio;

import com.johnson.practica.estrategias.ChecklistCampoEstrategia;
import com.johnson.practica.eventos.EntregableActualizadoEvent;
import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
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
public class ChecklistServicio {

    @Autowired
    private ElementoChecklistRepositorio repositorio;

    @Autowired
    private List<ChecklistCampoEstrategia> estrategias; 

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerHitosPrograma(Long proyectoId) {
        return repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(proyectoId, "0");
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerPorFase(Long proyectoId, String prefijoFase) {
        return repositorio.findByProyecto_IdAndFaseStartingWithOrderByCodigoAsc(proyectoId, prefijoFase);
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerPorProyectoId(Long proyectoId) {
        return repositorio.findByProyecto_IdOrderByCodigoAsc(proyectoId);
    }

    public List<ElementoChecklist> obtenerTodos() {
        return repositorio.findAll();
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

        String usuarioAudit = "Sistema"; 
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            usuarioAudit = auth.getName();
        }
        final String nombreUsuarioLogueado = usuarioAudit; 

        for (ElementoChecklist elemento : elementosDesdeBD) {
            Map<String, String> cambios = updatesById.get(elemento.getId());
            final boolean[] huboCambioReal = {false}; 
            
            cambios.forEach((fieldName, fieldValue) -> {
                String valorNuevo = (fieldValue == null) ? "" : fieldValue.trim();

                for (ChecklistCampoEstrategia estrategia : estrategias) {
                    if (estrategia.aplicaPara(fieldName)) {
                        if (estrategia.actualizar(elemento, valorNuevo)) {
                            huboCambioReal[0] = true;
                            eventPublisher.publishEvent(new EntregableActualizadoEvent(elemento, nombreUsuarioLogueado, fieldName, valorNuevo));
                        }
                        break; 
                    }
                }
            });

            reEvaluarStatusFechas(elemento, huboCambioReal);

            if (huboCambioReal[0]) {
                elementosRealmenteModificados.add(elemento);
            }
        }
        
        if (!elementosRealmenteModificados.isEmpty()) {
            repositorio.saveAll(elementosRealmenteModificados);
        }
    }

    private void reEvaluarStatusFechas(ElementoChecklist elemento, boolean[] huboCambioReal) {
        if (elemento.getFechaPlan() != null) {
            String currentCtrl = (elemento.getControlEntregable() == null) ? "" : elemento.getControlEntregable();
            
            if (elemento.getFechaReal() != null) {
                if (elemento.getFechaReal().isAfter(elemento.getFechaPlan())) {
                    if (!"Closed late".equalsIgnoreCase(currentCtrl)) {
                        elemento.setControlEntregable("Closed late");
                        huboCambioReal[0] = true;
                    }
                } else {
                    if (!"Closed on time".equalsIgnoreCase(currentCtrl) && 
                        !"DECISION".equalsIgnoreCase(currentCtrl) && 
                        !"NEEDS ACTION".equalsIgnoreCase(currentCtrl)) {
                        elemento.setControlEntregable("Closed on time");
                        huboCambioReal[0] = true;
                    }
                }
            } 
            else if (elemento.getFechaPlan().isBefore(LocalDate.now()) && !"OK".equalsIgnoreCase(elemento.getScore())) {
                if (!"Closed late".equalsIgnoreCase(currentCtrl) && !"Closed on time".equalsIgnoreCase(currentCtrl)) {
                    elemento.setControlEntregable("Closed late");
                    huboCambioReal[0] = true;
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerTareasPendientesUsuario(String username) {
        return repositorio.findAll().stream()
                .filter(e -> e.getProyecto() != null && !e.getProyecto().getEsHistorico())
                .filter(e -> e.getChampion() != null && e.getChampion().equalsIgnoreCase(username) && 
                            (e.getScore() == null || !"OK".equalsIgnoreCase(e.getScore().trim())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ElementoChecklist> obtenerTodasTareasPendientes() {
        return repositorio.findByScoreNotIgnoreCase("OK").stream()
                .filter(e -> e.getProyecto() != null && !e.getProyecto().getEsHistorico())
                .filter(e -> e.getScore() == null || !e.getScore().equalsIgnoreCase("OK"))
                .filter(e -> e.getFase() != null && (e.getFase().equals("0. Program") || e.getFase().equals("2. Stage 2")))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> obtenerTodosLosChampions() {
        return repositorio.findDistinctChampions().stream()
                .filter(c -> c != null && !c.trim().isEmpty() && !c.equalsIgnoreCase("N/A"))
                .map(this::normalizarChampion)
                .distinct()
                .sorted()
                .toList();
    }

    public String normalizarChampion(String champ) {
        if (champ == null || champ.trim().isEmpty()) return "N/A";
        String c = champ.trim().toUpperCase();
        
        if (c.equals("DE") || c.contains("DESIGN ENGINEER") || c.contains("PRODUCT ENGINEER")) return "DE";
        if (c.equals("QE") || c.contains("QUALITY ENGINEER")) return "QE";
        if (c.equals("PE") || c.contains("PROCESS ENGINEER") || c.contains("MANUFACTURING")) return "PE";
        if (c.equals("PROJ") || c.contains("PROJECT ENGINEER") || c.contains("PROJECT MANAGER") || c.equals("PROJECT LEADER")) return "PROJ";
        if (c.contains("SCS") || c.contains("PROCUREMENT") || c.contains("SUPPLY CHAIN")) return "SCS";
        if (c.contains("FINANCE")) return "FIN";
        if (c.equals("OPS") || c.contains("OPERATIONS")) return "OPS";
        if (c.contains("HR") || c.contains("HUMAN RESOURCES")) return "HR";
        if (c.contains("MATERIALS")) return "MAT";
        if (c.contains("CLIENTE") || c.contains("CUSTOMER")) return "CUST";
        if (c.contains("PROVEEDOR") || c.contains("SUPPLIER")) return "SUPP";
        if (c.equals("QE/PE") || (c.contains("QE") && c.contains("PE"))) return "QE/PE";
        if (c.contains("ALL") || c.contains("CFT")) return "ALL";
        
        return champ.trim(); 
    }

    public String obtenerNombreCompletoChampion(String sigla) {
        if (sigla == null) return "Unknown";
        return switch (sigla.toUpperCase()) {
            case "DE" -> "Design / Product Engineer";
            case "QE" -> "Quality Engineer";
            case "PE" -> "Process / Manufacturing Engineer";
            case "PROJ" -> "Project Engineer / Leader";
            case "SCS" -> "Supply Chain & Procurement";
            case "FIN" -> "Finance Representative";
            case "OPS" -> "Operations / Manufacturing";
            case "HR" -> "Human Resources";
            case "MAT" -> "Materials Management";
            case "CUST" -> "Customer / Client";
            case "SUPP" -> "Supplier / Vendor";
            case "QE/PE" -> "Shared Quality & Process Responsibility";
            case "ALL" -> "Cross-Functional Team (All)";
            default -> sigla;
        };
    }

    @Transactional(readOnly = true)
    public Map<String, Object> estaListoParaFinalizar(Long proyectoId) {
        List<ElementoChecklist> todos = repositorio.findByProyecto_Id(proyectoId);
        Map<String, Object> resultado = new HashMap<>();
        List<String> errores = new ArrayList<>();

        long entregablesSinOk = todos.stream()
                .filter(e -> !"GATE".equals(e.getTipoInput()) && e.getEtapaVisual() != null && e.getEtapaVisual().contains("APQP"))
                .filter(e -> !"OK".equalsIgnoreCase(e.getScore()))
                .count();

        if (entregablesSinOk > 0) errores.add("There are " + entregablesSinOk + " deliverables in APQP Program without 'OK' status.");

        long gate2SinResponder = todos.stream()
                .filter(e -> e.getCodigo() != null && e.getCodigo().startsWith("S2-"))
                .filter(e -> e.getEstado() == null || e.getEstado().trim().isEmpty())
                .count();

        if (gate2SinResponder > 0) errores.add("Gate 2 (Design) has " + gate2SinResponder + " requirements without compliance selection (Yes/No/NA).");

        for (int i = 3; i <= 5; i++) {
            final String prefix = "S" + i + "-CONC";
            long concSinMarcar = todos.stream()
                    .filter(e -> e.getCodigo() != null && e.getCodigo().startsWith(prefix))
                    .filter(e -> e.getEstado() == null || e.getEstado().trim().isEmpty())
                    .count();
            
            if (concSinMarcar > 0) errores.add("Gate " + i + " has " + concSinMarcar + " validation points not yet marked.");
        }

        resultado.put("listo", errores.isEmpty());
        resultado.put("errores", errores);
        return resultado;
    }

    @Transactional
    public void actualizarEntregablesVencidos() {
        LocalDate hoy = LocalDate.now();
        List<ElementoChecklist> todos = repositorio.findAll();
        List<ElementoChecklist> paraActualizar = new ArrayList<>();

        for (ElementoChecklist e : todos) {
            if (e.getFechaPlan() != null && e.getFechaPlan().isBefore(hoy) 
                && !"OK".equalsIgnoreCase(e.getScore())
                && (e.getControlEntregable() == null || (!e.getControlEntregable().equalsIgnoreCase("Closed late") && !e.getControlEntregable().equalsIgnoreCase("Closed on time")))) {
                
                e.setControlEntregable("Closed late");
                paraActualizar.add(e);
            }
        }

        if (!paraActualizar.isEmpty()) repositorio.saveAll(paraActualizar);
    }
}