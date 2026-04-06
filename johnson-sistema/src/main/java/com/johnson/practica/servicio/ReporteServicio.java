package com.johnson.practica.servicio;

import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.modelo.ElementoChecklist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine; 
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteServicio {

    @Autowired
    private SpringTemplateEngine templateEngine; 

    @Autowired
    private ProyectoServicio proyectoServicio;

    @Autowired
    private ChecklistServicio checklistServicio;

    public Map<String, Object> obtenerDatosReporte(Long proyectoId) {
        Proyecto proyecto = proyectoServicio.buscarPorId(proyectoId);
        List<ElementoChecklist> todosLosEntregables = checklistServicio.obtenerPorProyectoId(proyectoId);
        
        List<ElementoChecklist> entregablesPrograma = todosLosEntregables.stream()
                .filter(e -> e.getFase() != null && e.getFase().startsWith("0"))
                .collect(Collectors.toList());
        
        // DATOS PARA ESTATUS GLOBAL 
        long okCount = entregablesPrograma.stream().filter(e -> "OK".equalsIgnoreCase(e.getScore())).count();
        long actionCount = entregablesPrograma.stream().filter(e -> "NEEDS ACTION".equalsIgnoreCase(e.getScore())).count();
        long pendingCount = entregablesPrograma.size() - okCount - actionCount;
        double progreso = entregablesPrograma.isEmpty() ? 0 : (okCount * 100.0) / entregablesPrograma.size();

        //  DATOS PARA PROGRESO POR ETAPA 
        Map<String, Double> progresoEtapas = new java.util.LinkedHashMap<>();
        String[] etapas = {"STAGE 1", "STAGE 2", "STAGE 3", "STAGE 4", "STAGE 5"};
        for (String etapa : etapas) {
            long totalE = entregablesPrograma.stream().filter(e -> etapa.equalsIgnoreCase(e.getEtapaVisual())).count();
            long okE = entregablesPrograma.stream().filter(e -> etapa.equalsIgnoreCase(e.getEtapaVisual()) && "OK".equalsIgnoreCase(e.getScore())).count();
            progresoEtapas.put(etapa, totalE == 0 ? 0 : Math.round((okE * 100.0) / totalE * 10.0) / 10.0);
        }

        // DATOS PARA HEALTH TASK 
        long onTimeCount = entregablesPrograma.stream().filter(e -> "Closed on time".equalsIgnoreCase(e.getControlEntregable())).count();
        long lateCount = entregablesPrograma.stream().filter(e -> "Closed late".equalsIgnoreCase(e.getControlEntregable())).count();
        long needsActionCount = entregablesPrograma.stream().filter(e -> "NEEDS ACTION".equalsIgnoreCase(e.getControlEntregable())).count();
        long decisionCount = entregablesPrograma.stream().filter(e -> "DECISION".equalsIgnoreCase(e.getControlEntregable())).count();
        long unassignedCount = entregablesPrograma.size() - onTimeCount - lateCount - needsActionCount - decisionCount;

        // DATOS PARA RIESGO 
        double riesgoScore = 0.0;
        List<ElementoChecklist> preSop = todosLosEntregables.stream()
                .filter(e -> e.getEtapaVisual() != null && !e.getEtapaVisual().toUpperCase().contains("STAGE 5"))
                .collect(Collectors.toList());

        if (!preSop.isEmpty()) {
            long totalPreSop = preSop.size();
            long needsActionRisk = preSop.stream().filter(e -> "NEEDS ACTION".equalsIgnoreCase(e.getControlEntregable())).count();
            long pendientesNormales = preSop.stream()
                    .filter(e -> !"OK".equalsIgnoreCase(e.getScore()) && !"NEEDS ACTION".equalsIgnoreCase(e.getControlEntregable()))
                    .count();

            if (needsActionRisk > 0 || pendientesNormales > 0) {
                java.time.LocalDate hoy = java.time.LocalDate.now();
                java.time.LocalDate sop = proyecto.getSop();

                if (sop == null) {
                    riesgoScore = Math.min(100.0, ((needsActionRisk * 2.0 + pendientesNormales) * 100.0) / totalPreSop);
                } else {
                    long diasParaSop = java.time.temporal.ChronoUnit.DAYS.between(hoy, sop);
                    if (diasParaSop <= 0) {
                        riesgoScore = 100.0;
                    } else {
                        double multiplicadorTiempo = (diasParaSop <= 7) ? 3.0 : (diasParaSop <= 15 ? 1.8 : (diasParaSop <= 30 ? 1.0 : 0.4));
                        double porcentajeFaltante = (pendientesNormales * 100.0) / totalPreSop;
                        double castigoNeedsAction = (needsActionRisk * 100.0 / totalPreSop) * 2.5; 
                        riesgoScore = Math.min(100.0, (porcentajeFaltante * multiplicadorTiempo) + castigoNeedsAction);
                    }
                }
            }
        }
        
        long roundedRisk = Math.round(riesgoScore);

        List<Map<String, Object>> roadmapHitos = new java.util.ArrayList<>();
        int yearActual = 2026; 

        agregarHito(roadmapHitos, "CAR Approval", "💰", proyecto.getFechaCar(), yearActual);
        agregarHito(roadmapHitos, "Line Buy-off", "👥", proyecto.getFechaBuyoff(), yearActual);
        agregarHito(roadmapHitos, "Equipment Ship", "🚢", proyecto.getFechaTransit(), yearActual);
        
        if (proyecto.getFechaTransit() != null) {
            agregarHito(roadmapHitos, "Factory Arrival", "🏭", proyecto.getFechaTransit().plusDays(60), yearActual);
        }
        agregarHito(roadmapHitos, "SOP", "🏁", proyecto.getFechaSop(), yearActual);

        
        // GENERAL APQP STATUS 
        String gEstatus = descargarGraficaBase64("{"
            + "type:'doughnut',"
            + "data:{"
                + "labels:['OK','Action','Pend.'],"
                + "datasets:[{"
                    + "data:["+okCount+","+actionCount+","+pendingCount+"],"
                    + "backgroundColor:['#10b981','#ef4444','#cbd5e1'],"
                    + "borderWidth:0"
                + "}]"
            + "},"
            + "options:{"
                + "cutoutPercentage: 75,"
                + "legend:{position:'bottom', labels:{boxWidth:10, fontSize:11}},"
                + "plugins:{"
                    + "datalabels:{display:false},"
                    + "doughnutlabel:{"
                        + "labels:["
                            + "{text:'"+Math.round(progreso)+"%', font:{size:35, weight:'bold'}, color:'#0d6efd'},"
                            + "{text:'COMPLETION', font:{size:10, weight:'bold'}, color:'#64748b'}"
                        + "]"
                    + "}"
                + "}"
            + "}"
        + "}", 400, 250);       

        // PROGRESO POR ETAPA 
        String labelsEtapas = "['STAGE 1','STAGE 2','STAGE 3','STAGE 4','STAGE 5']";
        String dataEtapas = progresoEtapas.values().toString();
        String gEtapas = descargarGraficaBase64("{"
            + "type:'horizontalBar',"
            + "data:{"
                + "labels:" + labelsEtapas + ","
                + "datasets:[{"
                    + "data:" + dataEtapas + ","
                    + "backgroundColor:['#3b82f6','#0ea5e9','#10b981','#f59e0b','#8b5cf6'],"
                    + "borderWidth:0"
                + "}]"
            + "},"
            + "options:{"
                + "legend:{display:false},"
                + "scales:{"
                    + "xAxes:[{display:false, ticks:{min:0, max:100}}],"
                    + "yAxes:[{gridLines:{display:false}, ticks:{fontSize:11, fontStyle:'bold', fontColor:'#475569'}}]"
                + "},"
                + "plugins:{"
                    + "datalabels:{"
                        + "color:'#fff',"
                        + "font:{weight:'bold', size:12}," 
                        + "formatter: function(val) { return val + '%'; }"
                    + "}"
                + "}"
            + "}"
        + "}", 400, 250); 
               
        // HEALTH TASK DISTRIBUTION 
        String gHealth = descargarGraficaBase64("{"
            + "type:'doughnut',"
            + "data:{"
                + "labels:['ON TIME','LATE','ACTION','DECISION','TBD'],"
                + "datasets:[{"
                    + "data:["+onTimeCount+","+lateCount+","+needsActionCount+","+decisionCount+","+unassignedCount+"],"
                    + "backgroundColor:['#10b981','#ef4444','#f59e0b','#3b82f6','#cbd5e1'],"
                    + "borderWidth:0"
                + "}]"
            + "},"
            + "options:{"
                + "cutoutPercentage: 70,"
                + "legend:{position:'right', labels:{boxWidth:10, fontSize:10}},"
                + "plugins:{"
                    + "datalabels:{display:false},"
                    + "doughnutlabel:{"
                        + "labels:[{"
                            + "text:'" + entregablesPrograma.size() + "', font:{size:30, weight:'bold'}, color:'#64748b'"
                        + "}]"
                    + "}"
                + "}"
            + "}"
        + "}", 400, 250);

        // RISK METER
        String riskColor = roundedRisk >= 50 ? "#ef4444" : (roundedRisk >= 20 ? "#f59e0b" : "#10b981");
        String gRisk = descargarGraficaBase64("{"
            + "type:'doughnut',"
            + "data:{"
                + "datasets:[{"
                    + "data:["+roundedRisk+","+(100-roundedRisk)+"],"
                    + "backgroundColor:['"+riskColor+"','#f1f5f9'],"
                    + "borderWidth:0"
                + "}]"
            + "},"
            + "options:{"
                + "circumference: Math.PI,"
                + "rotation: Math.PI,"
                + "cutoutPercentage: 80,"
                + "legend:{display:false},"
                + "plugins:{"
                    + "datalabels:{display:false},"
                    + "doughnutlabel:{"
                        + "labels:["
                            + "{text:'"+roundedRisk+"%', font:{size:40, weight:'bold'}, color:'"+riskColor+"'},"
                            + "{text:'RISK', font:{size:12, weight:'bold'}, color:'#64748b'}"
                        + "]"
                    + "}"
                + "}"
            + "}"
        + "}", 400, 250);

        Map<String, Object> variables = new HashMap<>();
        variables.put("proyecto", proyecto);
        variables.put("entregables", entregablesPrograma);
        variables.put("progreso", Math.round(progreso));
        variables.put("gEstatus", gEstatus);
        variables.put("gEtapas", gEtapas);
        variables.put("gHealth", gHealth);
        variables.put("gRisk", gRisk);
        variables.put("riesgoScore", roundedRisk);
        variables.put("progresoEtapas", progresoEtapas);
        variables.put("roadmapHitos", roadmapHitos);
        
        variables.put("totalTareas", entregablesPrograma.size());
        variables.put("tareasOk", okCount);
        variables.put("tareasLate", lateCount);
        variables.put("tareasAction", needsActionCount);

        return variables;
    }

    private void agregarHito(List<Map<String, Object>> lista, String nombre, String icono, java.time.LocalDate fecha, int year) {
        if (fecha == null || fecha.getYear() != year) return;
        
        Map<String, Object> hito = new HashMap<>();
        hito.put("nombre", nombre);
        hito.put("icono", icono);
        hito.put("fecha", fecha);
        
        // Calcular posición porcentual en el año
        int diaAnio = fecha.getDayOfYear();
        double porcentaje = (diaAnio * 100.0) / 365.0;
        hito.put("posicion", Math.round(porcentaje * 10.0) / 10.0);
        
        lista.add(hito);
    }

    public byte[] generarPdfProyecto(Long proyectoId) throws Exception {
        Map<String, Object> datos = obtenerDatosReporte(proyectoId);
        Context context = new Context();
        context.setVariables(datos);
        
        String htmlContent = templateEngine.process("reportes/proyecto_pdf", context);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al renderizar el PDF");
        }
        return outputStream.toByteArray();
    }

    private String descargarGraficaBase64(String config) {
        return descargarGraficaBase64(config, 350, 200);
    }

    private String descargarGraficaBase64(String config, int width, int height) {
        try {
            String url = "https://quickchart.io/chart?w=" + width + "&h=" + height + "&bkg=white&c=" + URLEncoder.encode(config, "UTF-8");
            try (InputStream is = new URL(url).openStream()) {
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(is.readAllBytes());
            }
        } catch (Exception e) {
            return "";
        }
    }
}