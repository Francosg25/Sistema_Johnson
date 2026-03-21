package com.johnson.practica.servicio;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.ElementoChecklistRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChecklistLogicServicio {

    @Autowired
    private ElementoChecklistRepositorio repositorio;

    @Autowired
    private NotificacionServicio notificacionServicio;

    public double calcularRiesgoDinamico(Proyecto p, List<ElementoChecklist> elementos) {
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

    public double calcularPorcentajeEtapaVisual(List<ElementoChecklist> elementos, String etapaVisual) {
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
    public Map<String, Object> estaListoParaFinalizar(Long proyectoId) {
        List<ElementoChecklist> todos = repositorio.findByProyecto_Id(proyectoId);
        Map<String, Object> resultado = new HashMap<>();
        List<String> errores = new ArrayList<>();

        long entregablesSinOk = todos.stream()
                .filter(e -> !"GATE".equals(e.getTipoInput())) 
                .filter(e -> e.getEtapaVisual() != null && e.getEtapaVisual().contains("APQP"))
                .filter(e -> !"OK".equalsIgnoreCase(e.getScore()))
                .count();

        if (entregablesSinOk > 0) {
            errores.add("There are " + entregablesSinOk + " deliverables in APQP Program without 'OK' status.");
        }

        long gate2SinResponder = todos.stream()
                .filter(e -> e.getCodigo() != null && e.getCodigo().startsWith("S2-"))
                .filter(e -> e.getEstado() == null || e.getEstado().trim().isEmpty())
                .count();

        if (gate2SinResponder > 0) {
            errores.add("Gate 2 (Design) has " + gate2SinResponder + " requirements without compliance selection (Yes/No/NA).");
        }

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

    public void procesarMenciones(String comentario, ElementoChecklist elemento, String autor) {
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
