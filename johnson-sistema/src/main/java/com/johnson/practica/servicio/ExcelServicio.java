package com.johnson.practica.servicio;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelServicio {

    public byte[] generarExcelMasterTimeline(List<Proyecto> proyectos, List<ElementoChecklist> todosLosElementos) throws Exception {
        try (Workbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("Vista General Lanzamientos");
            configurarHoja(hoja);

            Map<String, CellStyle> estilos = crearEstilos(libro);

            // 1. Leyenda
            crearLeyenda(hoja, estilos);

            // 2. Rango del Timeline
            List<YearMonth> mesesTimeline = calcularMesesTimeline(proyectos, todosLosElementos);

            // 3. Encabezados
            int indiceColumna = crearEncabezados(hoja, estilos, mesesTimeline);

            // 4. Datos
            renderizarProyectos(hoja, estilos, proyectos, todosLosElementos, mesesTimeline);

            // Ajustes finales
            hoja.setColumnWidth(0, 14000);
            hoja.setColumnWidth(1, 4000);
            hoja.setColumnWidth(2, 3500);
            hoja.setColumnWidth(3, 3000);
            hoja.setColumnWidth(4, 3500);

            ByteArrayOutputStream flujoSalida = new ByteArrayOutputStream();
            libro.write(flujoSalida);
            return flujoSalida.toByteArray();
        }
    }

    private void configurarHoja(Sheet hoja) {
        hoja.setRowSumsBelow(false);
        hoja.setDisplayGridlines(false);
        hoja.createFreezePane(5, 7);
    }

    private Map<String, CellStyle> crearEstilos(Workbook libro) {
        Map<String, CellStyle> estilos = new HashMap<>();

        CellStyle estiloEncabezado = libro.createCellStyle();
        Font fuenteEncabezado = libro.createFont();
        fuenteEncabezado.setBold(true); fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
        estiloEncabezado.setFont(fuenteEncabezado);
        estiloEncabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);
        estiloEncabezado.setVerticalAlignment(VerticalAlignment.CENTER);
        estilos.put("encabezado", estiloEncabezado);

        CellStyle estiloProyecto = libro.createCellStyle();
        Font fuenteProyecto = libro.createFont(); fuenteProyecto.setBold(true); fuenteProyecto.setFontHeightInPoints((short) 12);
        estiloProyecto.setFont(fuenteProyecto);
        estiloProyecto.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloProyecto.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloProyecto.setVerticalAlignment(VerticalAlignment.CENTER);
        estilos.put("proyecto", estiloProyecto);

        CellStyle estiloIconoProyecto = libro.createCellStyle();
        estiloIconoProyecto.setFont(fuenteProyecto);
        estiloIconoProyecto.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloIconoProyecto.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloIconoProyecto.setVerticalAlignment(VerticalAlignment.CENTER);
        estiloIconoProyecto.setAlignment(HorizontalAlignment.CENTER);
        estilos.put("iconoProyecto", estiloIconoProyecto);

        CellStyle estiloEtapa = libro.createCellStyle();
        Font fuenteEtapa = libro.createFont(); fuenteEtapa.setBold(true);
        estiloEtapa.setFont(fuenteEtapa);
        estiloEtapa.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        estiloEtapa.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloEtapa.setVerticalAlignment(VerticalAlignment.CENTER);
        estilos.put("etapa", estiloEtapa);

        CellStyle estiloDato = libro.createCellStyle();
        estiloDato.setVerticalAlignment(VerticalAlignment.CENTER);
        estilos.put("dato", estiloDato);

        CellStyle estiloPlan = libro.createCellStyle();
        estiloPlan.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        estiloPlan.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloPlan.setAlignment(HorizontalAlignment.CENTER);
        estilos.put("plan", estiloPlan);

        CellStyle estiloReal = libro.createCellStyle();
        estiloReal.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex()); 
        estiloReal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloReal.setAlignment(HorizontalAlignment.CENTER);
        estilos.put("real", estiloReal);

        return estilos;
    }

    private void crearLeyenda(Sheet hoja, Map<String, CellStyle> estilos) {
        Row tituloLeyenda = hoja.createRow(0);
        tituloLeyenda.createCell(0).setCellValue("MASTER TIMELINE - EXECUTIVE OVERVIEW");
        tituloLeyenda.getCell(0).setCellStyle(estilos.get("proyecto"));

        Row leyendaPlan = hoja.createRow(1);
        Cell cuadroPlan = leyendaPlan.createCell(0); cuadroPlan.setCellStyle(estilos.get("plan"));
        leyendaPlan.createCell(1).setCellValue("Fecha Objetivo (Plan)");

        Row leyendaReal = hoja.createRow(2);
        Cell cuadroReal = leyendaReal.createCell(0); cuadroReal.setCellStyle(estilos.get("real"));
        leyendaReal.createCell(1).setCellValue("Fecha Real (Actual / Ejecutado)");

        Row leyendaIcono1 = hoja.createRow(3);
        leyendaIcono1.createCell(0).setCellValue("💰 Aprobación CAR");
        leyendaIcono1.createCell(1).setCellValue("👥 Line Buy-off");

        Row leyendaIcono2 = hoja.createRow(4);
        leyendaIcono2.createCell(0).setCellValue("🚢 Equipamiento en Tránsito");
    }

    private List<YearMonth> calcularMesesTimeline(List<Proyecto> proyectos, List<ElementoChecklist> todosLosElementos) {
        List<ElementoChecklist> elementosFiltrados = todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().equals("0. Program"))
                .collect(Collectors.toList());

        List<LocalDate> todasLasFechas = new ArrayList<>();
        elementosFiltrados.forEach(e -> {
            if (e.getFechaPlan() != null) todasLasFechas.add(e.getFechaPlan());
            if (e.getFechaReal() != null) todasLasFechas.add(e.getFechaReal());
        });
        proyectos.forEach(p -> {
            if (p.getFechaCar() != null) todasLasFechas.add(p.getFechaCar());
            if (p.getFechaBuyoff() != null) todasLasFechas.add(p.getFechaBuyoff());
            if (p.getFechaTransit() != null) todasLasFechas.add(p.getFechaTransit());
        });

        if (todasLasFechas.isEmpty()) {
            todasLasFechas.add(LocalDate.now());
        }

        int anioMin = todasLasFechas.stream().map(LocalDate::getYear).min(Integer::compareTo).get();
        int anioMax = todasLasFechas.stream().map(LocalDate::getYear).max(Integer::compareTo).get();

        List<YearMonth> mesesTimeline = new ArrayList<>();
        for (int y = anioMin; y <= anioMax; y++) {
            for (int m = 1; m <= 12; m++) {
                mesesTimeline.add(YearMonth.of(y, m));
            }
        }
        return mesesTimeline;
    }

    private int crearEncabezados(Sheet hoja, Map<String, CellStyle> estilos, List<YearMonth> mesesTimeline) {
        Row filaAnio = hoja.createRow(5); filaAnio.setHeightInPoints(20);
        Row filaMes = hoja.createRow(6); filaMes.setHeightInPoints(25);
        
        String[] columnasBase = {"Proyecto / Hitos", "Champion", "Estado", "Tipo Fecha", "Valor Fecha"};
        int indiceCol = 0;
        for (String col : columnasBase) {
            Cell celdaAnio = filaAnio.createCell(indiceCol);
            celdaAnio.setCellValue(col); celdaAnio.setCellStyle(estilos.get("encabezado"));
            Cell celdaMes = filaMes.createCell(indiceCol); celdaMes.setCellStyle(estilos.get("encabezado"));
            hoja.addMergedRegion(new CellRangeAddress(5, 6, indiceCol, indiceCol));
            indiceCol++;
        }
        
        DateTimeFormatter formateadorMes = DateTimeFormatter.ofPattern("MMM");
        int anioActual = -1;
        int colInicioAnio = indiceCol;

        for (int i = 0; i < mesesTimeline.size(); i++) {
            YearMonth ym = mesesTimeline.get(i);
            Cell celdaMes = filaMes.createCell(indiceCol);
            celdaMes.setCellValue(ym.format(formateadorMes));
            celdaMes.setCellStyle(estilos.get("encabezado"));
            hoja.setColumnWidth(indiceCol, 1500); 

            if (ym.getYear() != anioActual) {
                if (anioActual != -1) hoja.addMergedRegion(new CellRangeAddress(5, 5, colInicioAnio, indiceCol - 1));
                anioActual = ym.getYear();
                colInicioAnio = indiceCol;
                Cell celdaAnio = filaAnio.createCell(colInicioAnio);
                celdaAnio.setCellValue(String.valueOf(anioActual)); celdaAnio.setCellStyle(estilos.get("encabezado"));
            } else {
                filaAnio.createCell(indiceCol).setCellStyle(estilos.get("encabezado"));
            }
            indiceCol++;
        }
        if (anioActual != -1 && colInicioAnio < indiceCol - 1) {
            hoja.addMergedRegion(new CellRangeAddress(5, 5, colInicioAnio, indiceCol - 1));
        }
        return indiceCol;
    }

    private void renderizarProyectos(Sheet hoja, Map<String, CellStyle> estilos, List<Proyecto> proyectos, List<ElementoChecklist> todosLosElementos, List<YearMonth> mesesTimeline) {
        int indiceFila = 7;
        List<ElementoChecklist> elementosProgram = todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().equals("0. Program"))
                .collect(Collectors.toList());

        for (Proyecto proyecto : proyectos) {
            List<ElementoChecklist> itemsProyecto = elementosProgram.stream()
                    .filter(e -> e.getProyecto().getId().equals(proyecto.getId()))
                    .collect(Collectors.toList());

            Row filaProj = hoja.createRow(indiceFila); filaProj.setHeightInPoints(25);
            Cell celdaProj = filaProj.createCell(0);
            celdaProj.setCellValue("📁 " + proyecto.getNombre() + " (" + proyecto.getNumeroParte() + ")");
            celdaProj.setCellStyle(estilos.get("proyecto"));
            
            for (int i = 1; i < 5; i++) filaProj.createCell(i).setCellStyle(estilos.get("proyecto"));

            int colTimelineActual = 5;
            for (YearMonth ym : mesesTimeline) {
                Cell celda = filaProj.createCell(colTimelineActual++);
                celda.setCellStyle(estilos.get("iconoProyecto")); 
                String iconos = obtenerIconosHitosProyecto(proyecto, ym);
                if (!iconos.isEmpty()) celda.setCellValue(iconos);
            }
            
            int filaInicioProj = ++indiceFila;
            if (!itemsProyecto.isEmpty()) {
                indiceFila = renderizarEtapas(hoja, estilos, itemsProyecto, mesesTimeline, indiceFila);
                if (indiceFila > filaInicioProj) {
                    hoja.groupRow(filaInicioProj, indiceFila - 1);
                    hoja.setRowGroupCollapsed(filaInicioProj, false); 
                }
            }
        }
    }

    private String obtenerIconosHitosProyecto(Proyecto p, YearMonth ym) {
        StringBuilder iconos = new StringBuilder();
        if (p.getFechaCar() != null && YearMonth.from(p.getFechaCar()).equals(ym)) iconos.append("💰 ");
        if (p.getFechaBuyoff() != null && YearMonth.from(p.getFechaBuyoff()).equals(ym)) iconos.append("👥 ");
        if (p.getFechaTransit() != null && YearMonth.from(p.getFechaTransit()).equals(ym)) iconos.append("🚢 ");
        return iconos.toString().trim();
    }

    private int renderizarEtapas(Sheet hoja, Map<String, CellStyle> estilos, List<ElementoChecklist> itemsProyecto, List<YearMonth> mesesTimeline, int indiceFila) {
        Map<String, List<ElementoChecklist>> itemsPorEtapa = itemsProyecto.stream()
                .filter(e -> e.getEtapaVisual() != null)
                .collect(Collectors.groupingBy(ElementoChecklist::getEtapaVisual));

        List<String> etapasOrdenadas = new ArrayList<>(itemsPorEtapa.keySet());
        etapasOrdenadas.sort(Comparator.naturalOrder());

        for (String etapa : etapasOrdenadas) {
            List<ElementoChecklist> items = itemsPorEtapa.get(etapa);
            Row filaEtapa = hoja.createRow(indiceFila); filaEtapa.setHeightInPoints(22);
            Cell celdaEtapa = filaEtapa.createCell(0);
            celdaEtapa.setCellValue("   ↳ 📂 " + etapa);
            celdaEtapa.setCellStyle(estilos.get("etapa"));
            for (int i = 1; i < 5 + mesesTimeline.size(); i++) filaEtapa.createCell(i).setCellStyle(estilos.get("etapa"));
            
            int filaInicioEtapa = ++indiceFila;
            items.sort(Comparator.comparing(ElementoChecklist::getFechaPlan, Comparator.nullsLast(Comparator.naturalOrder())));

            for (ElementoChecklist item : items) {
                indiceFila = renderizarFilasItem(hoja, estilos, item, mesesTimeline, indiceFila);
            }

            if (indiceFila > filaInicioEtapa) {
                hoja.groupRow(filaInicioEtapa, indiceFila - 1);
                hoja.setRowGroupCollapsed(filaInicioEtapa, true); 
            }
        }
        return indiceFila;
    }

    private int renderizarFilasItem(Sheet hoja, Map<String, CellStyle> estilos, ElementoChecklist item, List<YearMonth> mesesTimeline, int indiceFila) {
        String estado = item.getScore() != null ? item.getScore() : (item.getEstado() != null ? item.getEstado() : "PENDIENTE");
        
        // Fila Plan
        Row filaPlan = hoja.createRow(indiceFila++); filaPlan.setHeightInPoints(16);
        filaPlan.createCell(0).setCellValue("         📄 " + item.getNombre()); filaPlan.getCell(0).setCellStyle(estilos.get("dato"));
        filaPlan.createCell(1).setCellValue(item.getChampion()); filaPlan.getCell(1).setCellStyle(estilos.get("dato"));
        filaPlan.createCell(2).setCellValue(estado); filaPlan.getCell(2).setCellStyle(estilos.get("dato"));
        filaPlan.createCell(3).setCellValue("Plan"); filaPlan.getCell(3).setCellStyle(estilos.get("dato"));
        filaPlan.createCell(4).setCellValue(item.getFechaPlan() != null ? item.getFechaPlan().toString() : "N/A"); filaPlan.getCell(4).setCellStyle(estilos.get("dato"));

        int tCol = 5;
        for (YearMonth ym : mesesTimeline) {
            Cell celda = filaPlan.createCell(tCol++);
            celda.setCellStyle(estilos.get("dato"));
            if (item.getFechaPlan() != null && YearMonth.from(item.getFechaPlan()).equals(ym)) {
                celda.setCellStyle(estilos.get("plan")); 
            }
        }

        // Fila Real
        Row filaReal = hoja.createRow(indiceFila++); filaReal.setHeightInPoints(16);
        filaReal.createCell(3).setCellValue("Real"); filaReal.getCell(3).setCellStyle(estilos.get("dato"));
        filaReal.createCell(4).setCellValue(item.getFechaReal() != null ? item.getFechaReal().toString() : "N/A"); filaReal.getCell(4).setCellStyle(estilos.get("dato"));
        // Rellenar otras celdas con estilo dato
        for(int i=0; i<3; i++) filaReal.createCell(i).setCellStyle(estilos.get("dato"));

        tCol = 5;
        for (YearMonth ym : mesesTimeline) {
            Cell celda = filaReal.createCell(tCol++);
            celda.setCellStyle(estilos.get("dato"));
            if (item.getFechaReal() != null && YearMonth.from(item.getFechaReal()).equals(ym)) {
                celda.setCellStyle(estilos.get("real")); 
            }
        }
        return indiceFila;
    }
}
