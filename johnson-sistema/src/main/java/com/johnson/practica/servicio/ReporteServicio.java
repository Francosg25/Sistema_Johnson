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
        
        // --- 1. DATOS PARA ESTATUS GLOBAL ---
        long okCount = entregablesPrograma.stream().filter(e -> "OK".equalsIgnoreCase(e.getScore())).count();
        long actionCount = entregablesPrograma.stream().filter(e -> "NEEDS ACTION".equalsIgnoreCase(e.getScore())).count();
        long pendingCount = entregablesPrograma.size() - okCount - actionCount;
        double progreso = entregablesPrograma.isEmpty() ? 0 : (okCount * 100.0) / entregablesPrograma.size();

        // --- 2. DATOS PARA PROGRESO POR ETAPA (CASCADA) ---
        Map<String, Double> progresoEtapas = new java.util.LinkedHashMap<>();
        String[] etapas = {"STAGE 1", "STAGE 2", "STAGE 3", "STAGE 4", "STAGE 5"};
        for (String etapa : etapas) {
            long totalE = entregablesPrograma.stream().filter(e -> etapa.equalsIgnoreCase(e.getEtapaVisual())).count();
            long okE = entregablesPrograma.stream().filter(e -> etapa.equalsIgnoreCase(e.getEtapaVisual()) && "OK".equalsIgnoreCase(e.getScore())).count();
            progresoEtapas.put(etapa, totalE == 0 ? 0 : Math.round((okE * 100.0) / totalE * 10.0) / 10.0);
        }

        // DATOS PARA HEALTH TASK (DONA)
        long onTimeCount = entregablesPrograma.stream().filter(e -> "Closed on time".equalsIgnoreCase(e.getControlEntregable())).count();
        long lateCount = entregablesPrograma.stream().filter(e -> "Closed late".equalsIgnoreCase(e.getControlEntregable())).count();
        long needsActionCount = entregablesPrograma.stream().filter(e -> "NEEDS ACTION".equalsIgnoreCase(e.getControlEntregable())).count();
        long decisionCount = entregablesPrograma.stream().filter(e -> "DECISION".equalsIgnoreCase(e.getControlEntregable())).count();
        long unassignedCount = entregablesPrograma.size() - onTimeCount - lateCount - needsActionCount - decisionCount;

        // --- 4. DATOS PARA RIESGO (LÓGICA ORIGINAL DE CHECKLIST SERVICIO) ---
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

        
        // 1. GENERAL APQP STATUS (Leyenda mucho más grande)
        String gEstatus = descargarGraficaBase64("{"
            + "type:'doughnut',"
            + "data:{"
                + "labels:['OK','Action','Pend.'],"
                + "datasets:[{"
                    + "data:["+okCount+","+actionCount+","+pendingCount+"],"
                    + "backgroundColor:['#10b981','#ef4444','#cbd5e1'],"
                    + "borderWidth: 2,"
                    + "borderColor: '#ffffff'"
                + "}]"
            + "},"
            + "options:{"
                + "layout:{padding: {top: 20, bottom: 20, left: 30, right: 30} },"
                + "cutoutPercentage: 65,"
                + "legend:{"
                    + "position:'top',"
                    + "labels:{boxWidth:30, fontSize:22, padding:15, fontStyle:'bold'}"
                + "},"
                + "plugins:{"
                    + "datalabels:{display:false}"
                + "}"
            + "}"
        + "}", 500, 400);       
        // PROGRESO POR ETAPA
        String labelsEtapas = "['STAGE 1','STAGE 2','STAGE 3','STAGE 4','STAGE 5']";
        String dataEtapas = progresoEtapas.values().toString();
        String gEtapas = descargarGraficaBase64("{"
            + "type:'horizontalBar',"
            + "data:{"
                + "labels:" + labelsEtapas + ","
                + "datasets:[{"
                    + "label:'% Completed',"
                    + "data:" + dataEtapas + ","
                    + "backgroundColor:['#3b82f6','#0ea5e9','#10b981','#f59e0b','#8b5cf6'],"
                    + "borderWidth:1"
                + "}]"
            + "},"
            + "options:{"
                + "layout:{padding: {top: 10, bottom: 10, left: 10, right: 35} },"
                + "legend:{display:false},"
                + "scales:{"
                    + "xAxes:[{ticks:{min:0, max:100, fontSize:14}}],"
                    + "yAxes:[{ticks:{fontSize:15, fontStyle:'bold', fontColor:'#475569'}}]"
                + "},"
                + "plugins:{"
                    + "datalabels:{"
                        + "display:true,"
                        + "color:'#fff',"
                        + "font:{weight:'bold', size:16}," 
                        + "anchor:'center',"
                        + "align:'center',"
                        + "formatter: function(val) { return val + '%'; }"
                    + "}"
                + "}"
            + "}"
        + "}", 1000, 600); 
               
       // HEALTH TASK DISTRIBUTION 
        String gHealth = descargarGraficaBase64("{"
            + "type:'doughnut',"
            + "data:{"
                + "labels:['ON TIME','LATE','ACTION','DECISION','TBD'],"
                + "datasets:[{"
                    + "data:["+onTimeCount+","+lateCount+","+needsActionCount+","+decisionCount+","+unassignedCount+"],"
                    + "backgroundColor:['#10b981','#ef4444','#f59e0b','#3b82f6','#cbd5e1'],"
                    + "borderWidth: 2,"
                    + "borderColor: '#ffffff'"
                + "}]"
            + "},"
            + "options:{"
                + "layout:{padding: {top: 55, bottom: 15, left: 35, right: 35} },"
                + "cutoutPercentage: 55,"
                + "legend:{"                
                    + "position:'bottom',"
                    + "labels:{boxWidth:25,fontSize:18,padding:10}"
                + "},"
                + "plugins:{"
                    + "datalabels:{"
                        + "display: function(ctx) { return ctx.dataset.data[ctx.dataIndex] > 0; },"
                        + "align: function(ctx) { return ctx.dataset.data[ctx.dataIndex] <= 5 ? 'end' : 'center'; },"
                        + "anchor: function(ctx) { return ctx.dataset.data[ctx.dataIndex] <= 5 ? 'end' : 'center'; },"
                        + "color: function(ctx) { "
                            + "const val = ctx.dataset.data[ctx.dataIndex];"
                            + "return (ctx.dataIndex === 4 || val <= 5) ? '#000000' : '#ffffff';"
                        + "},"
                        + "font:{weight:'bold',size:16},"
                        + "offset: 4" 
                    + "}"
                + "}"
            + "}"
        + "}", 500, 400);

        String riskColor = roundedRisk > 66 ? "#ef4444" : (roundedRisk > 33 ? "#f59e0b" : "#10b981");

        // RISK METER 
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
                + "cutoutPercentage:80,"
                + "plugins:{"
                    + "datalabels:{display:false}"
                + "}"
            + "}"
        + "}");
        
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