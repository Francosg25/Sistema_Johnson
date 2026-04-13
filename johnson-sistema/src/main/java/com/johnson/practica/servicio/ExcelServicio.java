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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExcelServicio {

    public byte[] generarMasterTimelineExcel(List<Proyecto> proyectos, List<ElementoChecklist> todosLosElementos) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Launch Overview");
            sheet.setRowSumsBelow(false); 
            sheet.setDisplayGridlines(false); // Líneas de Excel nativas desactivadas
            
            sheet.createFreezePane(5, 7);

            // ================= ESTILOS (SIN BORDES) =================
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true); headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle projectStyle = workbook.createCellStyle();
            Font projectFont = workbook.createFont(); projectFont.setBold(true); projectFont.setFontHeightInPoints((short) 12);
            projectStyle.setFont(projectFont);
            projectStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            projectStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            projectStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle projectIconStyle = workbook.createCellStyle();
            projectIconStyle.setFont(projectFont);
            projectIconStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            projectIconStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            projectIconStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            projectIconStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle stageStyle = workbook.createCellStyle();
            Font stageFont = workbook.createFont(); stageFont.setBold(true);
            stageStyle.setFont(stageFont);
            stageStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            stageStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            stageStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle planStyle = workbook.createCellStyle();
            planStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            planStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            planStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle actualStyle = workbook.createCellStyle();
            actualStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex()); 
            actualStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            actualStyle.setAlignment(HorizontalAlignment.CENTER);

            // ================= LEYENDA =================
            Row legendTitle = sheet.createRow(0);
            legendTitle.createCell(0).setCellValue("MASTER TIMELINE - EXECUTIVE OVERVIEW");
            legendTitle.getCell(0).getCellStyle().setFont(projectFont);

            Row legendPlan = sheet.createRow(1);
            Cell boxPlan = legendPlan.createCell(0); boxPlan.setCellStyle(planStyle);
            legendPlan.createCell(1).setCellValue("Target Date (Plan)");

            Row legendActual = sheet.createRow(2);
            Cell boxActual = legendActual.createCell(0); boxActual.setCellStyle(actualStyle);
            legendActual.createCell(1).setCellValue("Real Date (Actual / Executed)");

            Row legendIcon1 = sheet.createRow(3);
            legendIcon1.createCell(0).setCellValue("👥 CAR Approval");
            legendIcon1.createCell(1).setCellValue("💰 Line Buy-off");

            Row legendIcon2 = sheet.createRow(4);
            legendIcon2.createCell(0).setCellValue("🚢 Equipment Transit");

            // ================= CÁLCULO DE MESES =================
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

            int minYear = todasLasFechas.stream().map(LocalDate::getYear).min(Integer::compareTo).orElse(LocalDate.now().getYear());
            int maxYear = todasLasFechas.stream().map(LocalDate::getYear).max(Integer::compareTo).orElse(LocalDate.now().getYear());

            List<YearMonth> timelineMonths = new ArrayList<>();
            for (int y = minYear; y <= maxYear; y++) {
                for (int m = 1; m <= 12; m++) { 
                    timelineMonths.add(YearMonth.of(y, m));
                }
            }

            // ================= ENCABEZADOS =================
            Row yearHeaderRow = sheet.createRow(5); yearHeaderRow.setHeightInPoints(20);
            Row monthHeaderRow = sheet.createRow(6); monthHeaderRow.setHeightInPoints(25);
            
            String[] columnasBase = {"Project / Milestones", "Champion", "Status", "Date Type", "Date Value"};
            int colIndex = 0;
            for (String col : columnasBase) {
                Cell yearCell = yearHeaderRow.createCell(colIndex);
                yearCell.setCellValue(col); yearCell.setCellStyle(headerStyle);
                Cell monthCell = monthHeaderRow.createCell(colIndex); monthCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(5, 6, colIndex, colIndex));
                colIndex++;
            }
            
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
            int currentYear = -1;
            int startYearCol = colIndex;

            for (int i = 0; i < timelineMonths.size(); i++) {
                YearMonth ym = timelineMonths.get(i);
                Cell monthCell = monthHeaderRow.createCell(colIndex);
                monthCell.setCellValue(ym.format(monthFormatter));
                monthCell.setCellStyle(headerStyle);
                sheet.setColumnWidth(colIndex, 1500); 

                if (ym.getYear() != currentYear) {
                    if (currentYear != -1) sheet.addMergedRegion(new CellRangeAddress(5, 5, startYearCol, colIndex - 1));
                    currentYear = ym.getYear();
                    startYearCol = colIndex;
                    Cell yearCell = yearHeaderRow.createCell(startYearCol);
                    yearCell.setCellValue(String.valueOf(currentYear)); yearCell.setCellStyle(headerStyle);
                } else {
                    yearHeaderRow.createCell(colIndex).setCellStyle(headerStyle);
                }
                colIndex++;
            }
            if (currentYear != -1 && startYearCol < colIndex - 1) {
                sheet.addMergedRegion(new CellRangeAddress(5, 5, startYearCol, colIndex - 1));
            }

            // ================= LLENADO DE DATOS =================
            int rowIndex = 7;

            for (Proyecto proyecto : proyectos) {
                List<ElementoChecklist> itemsProyecto = elementosFiltrados.stream()
                        .filter(e -> e.getProyecto().getId().equals(proyecto.getId()))
                        .collect(Collectors.toList());

                // FILA DEL PROYECTO
                Row projRow = sheet.createRow(rowIndex); projRow.setHeightInPoints(25);
                Cell projCell = projRow.createCell(0);
                projCell.setCellValue("📁 " + proyecto.getNombre() + " (" + proyecto.getNumeroParte() + ")");
                projCell.setCellStyle(projectStyle);
                
                for (int i = 1; i < columnasBase.length; i++) {
                    projRow.createCell(i).setCellStyle(projectStyle);
                }

                int currentTimelineCol = 5;
                for (YearMonth ym : timelineMonths) {
                    Cell cell = projRow.createCell(currentTimelineCol++);
                    cell.setCellStyle(projectIconStyle); 
                    
                    StringBuilder iconos = new StringBuilder();
                    if (proyecto.getFechaCar() != null && YearMonth.from(proyecto.getFechaCar()).equals(ym)) iconos.append("👥 ");
                    if (proyecto.getFechaBuyoff() != null && YearMonth.from(proyecto.getFechaBuyoff()).equals(ym)) iconos.append("💰 ");
                    if (proyecto.getFechaTransit() != null && YearMonth.from(proyecto.getFechaTransit()).equals(ym)) iconos.append("🚢 ");
                    
                    if (iconos.length() > 0) {
                        cell.setCellValue(iconos.toString().trim());
                    }
                }
                int startProjRow = ++rowIndex;

                if (itemsProyecto.isEmpty()) continue;

                // FASES Y ENTREGABLES
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

                        int tCol = 5;
                        for (YearMonth ym : timelineMonths) {
                            Cell cell = planRow.createCell(tCol++);
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

                        tCol = 5;
                        for (YearMonth ym : timelineMonths) {
                            Cell cell = actualRow.createCell(tCol++);
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