package com.johnson.practica.servicio;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ExcelServicio {

 
    public byte[] generarMasterTimelineExcel(List<Proyecto> proyectos, List<ElementoChecklist> todosLosElementos) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Master Timeline");
            sheet.setRowSumsBelow(false); 
            sheet.setDisplayGridlines(false); 
            
            // Congelar las primeras 5 filas (Leyenda + Header) y las primeras 5 columnas
            sheet.createFreezePane(5, 5);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true); headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN); headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle projectStyle = workbook.createCellStyle();
            Font projectFont = workbook.createFont(); projectFont.setBold(true); projectFont.setFontHeightInPoints((short) 12);
            projectStyle.setFont(projectFont);
            projectStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            projectStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            projectStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            projectStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle stageStyle = workbook.createCellStyle();
            Font stageFont = workbook.createFont(); stageFont.setBold(true);
            stageStyle.setFont(stageFont);
            stageStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            stageStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            stageStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            stageStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.HAIR); dataStyle.setBorderRight(BorderStyle.HAIR);

            CellStyle planStyle = workbook.createCellStyle();
            planStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            planStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            planStyle.setBorderTop(BorderStyle.THIN);
            planStyle.setBorderBottom(BorderStyle.THIN);
            planStyle.setBorderLeft(BorderStyle.THIN);
            planStyle.setBorderRight(BorderStyle.THIN);

            CellStyle actualStyle = workbook.createCellStyle();
            actualStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex()); 
            actualStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            actualStyle.setBorderTop(BorderStyle.THIN);
            actualStyle.setBorderBottom(BorderStyle.THIN);
            actualStyle.setBorderLeft(BorderStyle.THIN);
            actualStyle.setBorderRight(BorderStyle.THIN);

            Row legendTitle = sheet.createRow(0);
            legendTitle.createCell(0).setCellValue("MASTER TIMELINE - COLOR LEGEND");
            legendTitle.getCell(0).getCellStyle().setFont(projectFont);

            Row legendPlan = sheet.createRow(1);
            Cell boxPlan = legendPlan.createCell(0); boxPlan.setCellStyle(planStyle);
            legendPlan.createCell(1).setCellValue("Target Date (Plan)");

            Row legendActual = sheet.createRow(2);
            Cell boxActual = legendActual.createCell(0); boxActual.setCellStyle(actualStyle);
            legendActual.createCell(1).setCellValue("Real Date (Actual / Executed)");

            List<ElementoChecklist> elementosFiltrados = todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().equals("0. Program"))
                .collect(Collectors.toList());

            List<LocalDate> todasLasFechas = new ArrayList<>();
            elementosFiltrados.forEach(e -> {
                if (e.getFechaPlan() != null) todasLasFechas.add(e.getFechaPlan());
                if (e.getFechaReal() != null) todasLasFechas.add(e.getFechaReal());
            });

            int minYear = todasLasFechas.stream().map(LocalDate::getYear).min(Integer::compareTo).orElse(LocalDate.now().getYear());
            int maxYear = todasLasFechas.stream().map(LocalDate::getYear).max(Integer::compareTo).orElse(LocalDate.now().getYear());

            List<YearMonth> timelineMonths = new ArrayList<>();
            for (int y = minYear; y <= maxYear; y++) {
                for (int m = 1; m <= 12; m++) { 
                    timelineMonths.add(YearMonth.of(y, m));
                }
            }

            Row headerRow = sheet.createRow(4);
            headerRow.setHeightInPoints(30);
            
            String[] columnasBase = {"Project", "Champion", "Score", "Date Type", "Date Value"};
            int colIndex = 0;
            for (String col : columnasBase) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(col); cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM-yy");
            for (YearMonth ym : timelineMonths) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(ym.format(monthFormatter));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(colIndex - 1, 2000); 
            }

            int rowIndex = 5;

            for (Proyecto proyecto : proyectos) {
                List<ElementoChecklist> itemsProyecto = elementosFiltrados.stream()
                        .filter(e -> e.getProyecto().getId().equals(proyecto.getId()))
                        .collect(Collectors.toList());

                if (itemsProyecto.isEmpty()) continue;

                Row projRow = sheet.createRow(rowIndex); projRow.setHeightInPoints(25);
                Cell projCell = projRow.createCell(0);
                projCell.setCellValue("📁 " + proyecto.getNombre() + " (" + proyecto.getNumeroParte() + ")");
                projCell.setCellStyle(projectStyle);
                for (int i = 1; i < columnasBase.length + timelineMonths.size(); i++) projRow.createCell(i).setCellStyle(projectStyle);
                int startProjRow = ++rowIndex;

                Map<String, List<ElementoChecklist>> itemsPorEtapa = itemsProyecto.stream()
                        .filter(e -> e.getEtapaVisual() != null)
                        .collect(Collectors.groupingBy(ElementoChecklist::getEtapaVisual));

                List<String> etapasOrdenadas = new ArrayList<>(itemsPorEtapa.keySet());
                etapasOrdenadas.sort(Comparator.naturalOrder());

                for (String etapa : etapasOrdenadas) {
                    List<ElementoChecklist> items = itemsPorEtapa.get(etapa);
                    Row stageRow = sheet.createRow(rowIndex); stageRow.setHeightInPoints(22);
                    Cell stageCell = stageRow.createCell(0);
                    stageCell.setCellValue("   ↳ 📂 " + etapa);
                    stageCell.setCellStyle(stageStyle);
                    for (int i = 1; i < columnasBase.length + timelineMonths.size(); i++) stageRow.createCell(i).setCellStyle(stageStyle);
                    int startStageRow = ++rowIndex;
                    items.sort((item1, item2) -> {
                        if (item1.getFechaPlan() == null && item2.getFechaPlan() == null) return 0;
                        if (item1.getFechaPlan() == null) return 1;
                        if (item2.getFechaPlan() == null) return -1;
                        return item1.getFechaPlan().compareTo(item2.getFechaPlan());
                    });

                    for (ElementoChecklist item : items) {
                        String status = item.getScore() != null ? item.getScore() : item.getEstado();
                        
                        Row planRow = sheet.createRow(rowIndex++); planRow.setHeightInPoints(16);
                        planRow.createCell(0).setCellValue("         📄 " + item.getNombre()); planRow.getCell(0).setCellStyle(dataStyle);
                        planRow.createCell(1).setCellValue(item.getChampion()); planRow.getCell(1).setCellStyle(dataStyle);
                        planRow.createCell(2).setCellValue(status != null ? status : "PENDING"); planRow.getCell(2).setCellStyle(dataStyle);
                        planRow.createCell(3).setCellValue("Plan"); planRow.getCell(3).setCellStyle(dataStyle);
                        planRow.createCell(4).setCellValue(item.getFechaPlan() != null ? item.getFechaPlan().toString() : "N/A"); planRow.getCell(4).setCellStyle(dataStyle);

                        int currentTimelineCol = 5;
                        for (YearMonth ym : timelineMonths) {
                            Cell cell = planRow.createCell(currentTimelineCol++);
                            cell.setCellStyle(dataStyle);
                            if (item.getFechaPlan() != null && YearMonth.from(item.getFechaPlan()).equals(ym)) {
                                cell.setCellStyle(planStyle); 
                            }
                        }

                        Row actualRow = sheet.createRow(rowIndex++); actualRow.setHeightInPoints(16);
                        actualRow.createCell(0).setCellValue(""); actualRow.getCell(0).setCellStyle(dataStyle);
                        actualRow.createCell(1).setCellValue(""); actualRow.getCell(1).setCellStyle(dataStyle);
                        actualRow.createCell(2).setCellValue(""); actualRow.getCell(2).setCellStyle(dataStyle);
                        actualRow.createCell(3).setCellValue("Actual"); actualRow.getCell(3).setCellStyle(dataStyle);
                        actualRow.createCell(4).setCellValue(item.getFechaReal() != null ? item.getFechaReal().toString() : "N/A"); actualRow.getCell(4).setCellStyle(dataStyle);

                        currentTimelineCol = 5;
                        for (YearMonth ym : timelineMonths) {
                            Cell cell = actualRow.createCell(currentTimelineCol++);
                            cell.setCellStyle(dataStyle);
                            if (item.getFechaReal() != null && YearMonth.from(item.getFechaReal()).equals(ym)) {
                                cell.setCellStyle(actualStyle); 
                            }
                        }
                    }

                    if (rowIndex > startStageRow) {
                        sheet.groupRow(startStageRow, rowIndex - 1);
                        sheet.setRowGroupCollapsed(startStageRow, true); 
                    }
                }

                if (rowIndex > startProjRow) {
                    sheet.groupRow(startProjRow, rowIndex - 1);
                    sheet.setRowGroupCollapsed(startProjRow, false); 
                }
            }

            sheet.setColumnWidth(0, 14000); sheet.setColumnWidth(1, 4000);
            sheet.setColumnWidth(2, 3500); sheet.setColumnWidth(3, 3000); sheet.setColumnWidth(4, 3500);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
