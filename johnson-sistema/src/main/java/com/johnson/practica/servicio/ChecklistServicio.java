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

    @Autowired
    private com.johnson.practica.repositorio.UsuarioRepositorio usuarioRepositorio;

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

  
    private boolean esDiferente(String actual, String nuevo) {
        String a = (actual == null) ? "" : actual.replaceAll("[\\n\\r]+", " ").trim();
        String n = (nuevo == null) ? "" : nuevo.replaceAll("[\\n\\r]+", " ").trim();
        
        if (n.isEmpty() && (a.isEmpty() || a.equalsIgnoreCase("PENDING") || a.equalsIgnoreCase("Open"))) {
            return false;
        }
        
        return !n.equalsIgnoreCase(a);
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

                switch (fieldName) {
                    case "controlEntregable" -> {
                        if (esDiferente(elemento.getControlEntregable(), valorNuevo)) {
                            elemento.setControlEntregable(valorNuevo); 
                            huboCambioReal[0] = true;
                        }
                    }
                    case "score" -> {
                        if (esDiferente(elemento.getScore(), valorNuevo)) {
                            if ("OK".equalsIgnoreCase(valorNuevo)) {
                                String titulo = "Deliverable OK";
                                String msj = "The deliverable '" + elemento.getNombre() + "' in " + elemento.getProyecto().getNombre() + " was marked as OK.";
                                String url = "/checklist?proyectoId=" + elemento.getProyecto().getId();
                                notificacionServicio.alertarATodos(titulo, msj, "SUCCESS", url, nombreUsuarioLogueado);
                                
                                // NUEVO: Auto-asignar fecha si está vacía
                                if (elemento.getFechaReal() == null) {
                                    elemento.setFechaReal(LocalDate.now());
                                }
                            }
                            elemento.setScore(valorNuevo); 
                            huboCambioReal[0] = true;
                        }
                    }
                    
                    case "comentario" -> {
                        if (esDiferente(elemento.getComentario(), valorNuevo)) {
                            elemento.setComentario(valorNuevo); 
                            huboCambioReal[0] = true;
                            procesarMenciones(valorNuevo, elemento, nombreUsuarioLogueado);
                        }
                    }
                    case "estado" -> {
                        if (esDiferente(elemento.getEstado(), valorNuevo)) {
                            if ("OK".equalsIgnoreCase(valorNuevo) && elemento.getFechaReal() == null) {
                                elemento.setFechaReal(LocalDate.now());
                            }
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
                    "UPDATE DELIVERABLE", 
                    "Deliverable: " + elemento.getNombre() + " was updated."
                );
                elementosRealmenteModificados.add(elemento);
            }
        }
        
        if (!elementosRealmenteModificados.isEmpty()) {
            repositorio.saveAll(elementosRealmenteModificados);
        }
    }


    public Map<String, Integer> obtenerTendenciaAprobacionesOK() {
        List<ElementoChecklist> todos = repositorio.findAll();
        Map<String, Integer> tendencia = new HashMap<>();

        for (ElementoChecklist e : todos) {
            boolean isScoreOk = "OK".equalsIgnoreCase(e.getScore());
            boolean isEstadoOk = "OK".equalsIgnoreCase(e.getEstado());

            if (isScoreOk || isEstadoOk) {
                LocalDate fecha = e.getFechaReal() != null ? e.getFechaReal() : LocalDate.now();
                
                String mesAnio = fecha.getMonthValue() + "/" + fecha.getYear();
                tendencia.put(mesAnio, tendencia.getOrDefault(mesAnio, 0) + 1);
            }
        }
        return tendencia;
    }


   @Cacheable("reportes")
   public List<ReporteProgreso> generarReporteGlobal() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
            .filter(p -> !p.isArchivado())
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
            
            reporteProgreso.setFechaCar(p.getFechaCar() != null ? p.getFechaCar().toString() : null);
            reporteProgreso.setFechaBuyoff(p.getFechaBuyoff() != null ? p.getFechaBuyoff().toString() : null);
            reporteProgreso.setFechaTransit(p.getFechaTransit() != null ? p.getFechaTransit().toString() : null);

            double riesgo = calcularRiesgoDinamico(p, todosLosItemsDelProyecto);
            
            reporteProgreso.setRiesgo(riesgo);

            reporte.add(reporteProgreso);
        }
        return reporte;
    }

    public ReporteEstadoGlobal generarReporteEstadoGlobal() {
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
            .filter(p -> !p.isArchivado())
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
            .filter(p -> !p.isArchivado())
            .toList();
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
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
                .filter(p -> !p.isArchivado())
                .toList();
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
        List<Proyecto> proyectos = proyectoRepositorio.findAllByOrderByIdAsc().stream()
            .filter(p -> !p.isArchivado())
            .toList();
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
        
        // Mapeo estricto a siglas
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

        // 1. Validar Deliverables (APQP Program / Stage 1)
        // Solo validamos que tengan Score OK (los que no son tipo GATE)
        long entregablesSinOk = todos.stream()
                .filter(e -> !"GATE".equals(e.getTipoInput())) 
                .filter(e -> e.getEtapaVisual() != null && e.getEtapaVisual().contains("APQP"))
                .filter(e -> !"OK".equalsIgnoreCase(e.getScore()))
                .count();

        if (entregablesSinOk > 0) {
            errores.add("There are " + entregablesSinOk + " deliverables in APQP Program without 'OK' status.");
        }

        // 2. Validar Gate 2 (Stage 2)
        // Debe tener algo marcado en compliance (estado != null) para todas sus preguntas
        long gate2SinResponder = todos.stream()
                .filter(e -> e.getCodigo() != null && e.getCodigo().startsWith("S2-"))
                .filter(e -> e.getEstado() == null || e.getEstado().trim().isEmpty())
                .count();

        if (gate2SinResponder > 0) {
            errores.add("Gate 2 (Design) has " + gate2SinResponder + " requirements without compliance selection (Yes/No/NA).");
        }

        // 3. Validar Gates 3, 4 y 5 (Requirements Validation)
        // Validar específicamente los 3 puntos de la sección "Conclusion" / "Requirements Validation"
        for (int i = 3; i <= 5; i++) {
            final String prefix = "S" + i + "-CONC";
            long concSinMarcar = todos.stream()
                    .filter(e -> e.getCodigo() != null && e.getCodigo().startsWith(prefix))
                    .filter(e -> e.getEstado() == null || e.getEstado().trim().isEmpty())
                    .count();
            
            if (concSinMarcar > 0) {
                errores.add("Gate " + i + " has " + concSinMarcar + " validation points not yet marked.");
            }
        }

        resultado.put("listo", errores.isEmpty());
        resultado.put("errores", errores);
        return resultado;
    }

    private String scoreFormateado(String s) {
        return (s == null) ? "" : s.trim().toUpperCase();
    }

    private void procesarMenciones(String comentario, ElementoChecklist elemento, String autor) {
        if (comentario == null || !comentario.contains("@")) return;

        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(comentario);
        while (matcher.find()) {
            String username = matcher.group(1);
            String titulo = "You were mentioned";
            String msj = autor + " mentioned you in the remarks of '" + elemento.getNombre() + "' (" + elemento.getProyecto().getNombre() + ")";
            String url = "/proyectos/checklist/" + elemento.getProyecto().getId();
            notificacionServicio.alertarAUsuario(username, titulo, msj, "INFO", url, autor);
        }
    }

}