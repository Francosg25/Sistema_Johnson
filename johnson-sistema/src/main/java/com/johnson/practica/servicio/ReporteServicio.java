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
        
        long okCount = entregablesPrograma.stream().filter(e -> "OK".equalsIgnoreCase(e.getScore())).count();
        long actionCount = entregablesPrograma.stream().filter(e -> "NEEDS ACTION".equalsIgnoreCase(e.getScore())).count();
        long pendingCount = entregablesPrograma.size() - okCount - actionCount;
        double progreso = entregablesPrograma.isEmpty() ? 0 : (okCount * 100.0) / entregablesPrograma.size();

        Map<String, Double> progresoEtapas = new java.util.LinkedHashMap<>();
        String[] etapas = {"STAGE 1", "STAGE 2", "STAGE 3", "STAGE 4", "STAGE 5"};
        for (String etapa : etapas) {
            long totalE = entregablesPrograma.stream().filter(e -> etapa.equalsIgnoreCase(e.getEtapaVisual())).count();
            long okE = entregablesPrograma.stream().filter(e -> etapa.equalsIgnoreCase(e.getEtapaVisual()) && "OK".equalsIgnoreCase(e.getScore())).count();
            progresoEtapas.put(etapa, totalE == 0 ? 0 : Math.round((okE * 100.0) / totalE * 10.0) / 10.0);
        }

        long onTime = entregablesPrograma.stream().filter(e -> e.getControlEntregable() != null && e.getControlEntregable().toUpperCase().contains("ON TIME")).count();
        long late = entregablesPrograma.stream().filter(e -> e.getControlEntregable() != null && e.getControlEntregable().toUpperCase().contains("LATE")).count();
        long others = entregablesPrograma.size() - onTime - late;

        String gEstatus = descargarGraficaBase64("{type:'doughnut',data:{labels:['OK','Action','Pend.'],datasets:[{data:["+okCount+","+actionCount+","+pendingCount+"],backgroundColor:['#10b981','#ef4444','#cbd5e1']}]},options:{plugins:{doughnutlabel:{labels:[{text:'"+Math.round(progreso)+"%',font:{size:20}},{text:'Progreso'}]}}}}");
        String gEtapas = descargarGraficaBase64("{type:'horizontalBar',data:{labels:['STAGE 1','STAGE 2','STAGE 3','STAGE 4','STAGE 5'],datasets:[{label:'% Completed',data:"+progresoEtapas.values().toString()+",backgroundColor:'#3b82f6'}]},options:{scales:{xAxes:[{ticks:{min:0,max:100}}]}}}");
        String gTiempos = descargarGraficaBase64("{type:'pie',data:{labels:['On Time','Late','Otros'],datasets:[{data:["+onTime+","+late+","+others+"],backgroundColor:['#34d399','#f87171','#94a3b8']}]}}");

        Map<String, Object> variables = new HashMap<>();
        variables.put("proyecto", proyecto);
        variables.put("entregables", entregablesPrograma);
        variables.put("progreso", Math.round(progreso));
        variables.put("gEstatus", gEstatus);
        variables.put("gEtapas", gEtapas);
        variables.put("gTiempos", gTiempos);
        variables.put("progresoEtapas", progresoEtapas);
        return variables;
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
        try {
            String url = "https://quickchart.io/chart?w=350&h=200&bkg=white&c=" + URLEncoder.encode(config, "UTF-8");
            try (InputStream is = new URL(url).openStream()) {
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(is.readAllBytes());
            }
        } catch (Exception e) {
            return "";
        }
    }
}