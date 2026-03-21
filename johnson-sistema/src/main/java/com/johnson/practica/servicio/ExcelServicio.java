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

    public byte[] generarMasterTimelineExcel(List<Proyecto> proyectos, List<ElementoChecklist> todosLosElementos) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Launch Overview");
            setupSheet(sheet);

            Map<String, CellStyle> styles = createStyles(workbook);

            // 1. Legend
            createLegend(sheet, styles);

            // 2. Timeline range
            List<YearMonth> timelineMonths = calculateTimelineMonths(proyectos, todosLosElementos);

            // 3. Headers
            int colIndex = createHeaders(sheet, styles, timelineMonths);

            // 4. Data
            renderProjects(sheet, styles, proyectos, todosLosElementos, timelineMonths);

            // Final adjustments
            sheet.setColumnWidth(0, 14000);
            sheet.setColumnWidth(1, 4000);
            sheet.setColumnWidth(2, 3500);
            sheet.setColumnWidth(3, 3000);
            sheet.setColumnWidth(4, 3500);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void setupSheet(Sheet sheet) {
        sheet.setRowSumsBelow(false);
        sheet.setDisplayGridlines(false);
        sheet.createFreezePane(5, 7);
    }

    private Map<String, CellStyle> createStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true); headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("header", headerStyle);

        CellStyle projectStyle = workbook.createCellStyle();
        Font projectFont = workbook.createFont(); projectFont.setBold(true); projectFont.setFontHeightInPoints((short) 12);
        projectStyle.setFont(projectFont);
        projectStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        projectStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        projectStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("project", projectStyle);

        CellStyle projectIconStyle = workbook.createCellStyle();
        projectIconStyle.setFont(projectFont);
        projectIconStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        projectIconStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        projectIconStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        projectIconStyle.setAlignment(HorizontalAlignment.CENTER);
        styles.put("projectIcon", projectIconStyle);

        CellStyle stageStyle = workbook.createCellStyle();
        Font stageFont = workbook.createFont(); stageFont.setBold(true);
        stageStyle.setFont(stageFont);
        stageStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        stageStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        stageStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("stage", stageStyle);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("data", dataStyle);

        CellStyle planStyle = workbook.createCellStyle();
        planStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        planStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        planStyle.setAlignment(HorizontalAlignment.CENTER);
        styles.put("plan", planStyle);

        CellStyle actualStyle = workbook.createCellStyle();
        actualStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex()); 
        actualStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        actualStyle.setAlignment(HorizontalAlignment.CENTER);
        styles.put("actual", actualStyle);

        return styles;
    }

    private void createLegend(Sheet sheet, Map<String, CellStyle> styles) {
        Row legendTitle = sheet.createRow(0);
        legendTitle.createCell(0).setCellValue("MASTER TIMELINE - EXECUTIVE OVERVIEW");
        legendTitle.getCell(0).setCellStyle(styles.get("project"));

        Row legendPlan = sheet.createRow(1);
        Cell boxPlan = legendPlan.createCell(0); boxPlan.setCellStyle(styles.get("plan"));
        legendPlan.createCell(1).setCellValue("Target Date (Plan)");

        Row legendActual = sheet.createRow(2);
        Cell boxActual = legendActual.createCell(0); boxActual.setCellStyle(styles.get("actual"));
        legendActual.createCell(1).setCellValue("Real Date (Actual / Executed)");

        Row legendIcon1 = sheet.createRow(3);
        legendIcon1.createCell(0).setCellValue("💰 CAR Approval");
        legendIcon1.createCell(1).setCellValue("👥 Line Buy-off");

        Row legendIcon2 = sheet.createRow(4);
        legendIcon2.createCell(0).setCellValue("🚢 Equipment Transit");
    }

    private List<YearMonth> calculateTimelineMonths(List<Proyecto> proyectos, List<ElementoChecklist> todosLosElementos) {
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

        int minYear = todasLasFechas.stream().map(LocalDate::getYear).min(Integer::compareTo).get();
        int maxYear = todasLasFechas.stream().map(LocalDate::getYear).max(Integer::compareTo).get();

        List<YearMonth> timelineMonths = new ArrayList<>();
        for (int y = minYear; y <= maxYear; y++) {
            for (int m = 1; m <= 12; m++) {
                timelineMonths.add(YearMonth.of(y, m));
            }
        }
        return timelineMonths;
    }

    private int createHeaders(Sheet sheet, Map<String, CellStyle> styles, List<YearMonth> timelineMonths) {
        Row yearHeaderRow = sheet.createRow(5); yearHeaderRow.setHeightInPoints(20);
        Row monthHeaderRow = sheet.createRow(6); monthHeaderRow.setHeightInPoints(25);
        
        String[] columnasBase = {"Project / Milestones", "Champion", "Status", "Date Type", "Date Value"};
        int colIndex = 0;
        for (String col : columnasBase) {
            Cell yearCell = yearHeaderRow.createCell(colIndex);
            yearCell.setCellValue(col); yearCell.setCellStyle(styles.get("header"));
            Cell monthCell = monthHeaderRow.createCell(colIndex); monthCell.setCellStyle(styles.get("header"));
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
            monthCell.setCellStyle(styles.get("header"));
            sheet.setColumnWidth(colIndex, 1500); 

            if (ym.getYear() != currentYear) {
                if (currentYear != -1) sheet.addMergedRegion(new CellRangeAddress(5, 5, startYearCol, colIndex - 1));
                currentYear = ym.getYear();
                startYearCol = colIndex;
                Cell yearCell = yearHeaderRow.createCell(startYearCol);
                yearCell.setCellValue(String.valueOf(currentYear)); yearCell.setCellStyle(styles.get("header"));
            } else {
                yearHeaderRow.createCell(colIndex).setCellStyle(styles.get("header"));
            }
            colIndex++;
        }
        if (currentYear != -1 && startYearCol < colIndex - 1) {
            sheet.addMergedRegion(new CellRangeAddress(5, 5, startYearCol, colIndex - 1));
        }
        return colIndex;
    }

    private void renderProjects(Sheet sheet, Map<String, CellStyle> styles, List<Proyecto> proyectos, List<ElementoChecklist> todosLosElementos, List<YearMonth> timelineMonths) {
        int rowIndex = 7;
        List<ElementoChecklist> elementosProgram = todosLosElementos.stream()
                .filter(e -> e.getFase() != null && e.getFase().equals("0. Program"))
                .collect(Collectors.toList());

        for (Proyecto proyecto : proyectos) {
            List<ElementoChecklist> itemsProyecto = elementosProgram.stream()
                    .filter(e -> e.getProyecto().getId().equals(proyecto.getId()))
                    .collect(Collectors.toList());

            Row projRow = sheet.createRow(rowIndex); projRow.setHeightInPoints(25);
            Cell projCell = projRow.createCell(0);
            projCell.setCellValue("📁 " + proyecto.getNombre() + " (" + proyecto.getNumeroParte() + ")");
            projCell.setCellStyle(styles.get("project"));
            
            for (int i = 1; i < 5; i++) projRow.createCell(i).setCellStyle(styles.get("project"));

            int currentTimelineCol = 5;
            for (YearMonth ym : timelineMonths) {
                Cell cell = projRow.createCell(currentTimelineCol++);
                cell.setCellStyle(styles.get("projectIcon")); 
                String icons = getProjectMilestoneIcons(proyecto, ym);
                if (!icons.isEmpty()) cell.setCellValue(icons);
            }
            
            int startProjRow = ++rowIndex;
            if (!itemsProyecto.isEmpty()) {
                rowIndex = renderStages(sheet, styles, itemsProyecto, timelineMonths, rowIndex);
                if (rowIndex > startProjRow) {
                    sheet.groupRow(startProjRow, rowIndex - 1);
                    sheet.setRowGroupCollapsed(startProjRow, false); 
                }
            }
        }
    }

    private String getProjectMilestoneIcons(Proyecto p, YearMonth ym) {
        StringBuilder iconos = new StringBuilder();
        if (p.getFechaCar() != null && YearMonth.from(p.getFechaCar()).equals(ym)) iconos.append("💰 ");
        if (p.getFechaBuyoff() != null && YearMonth.from(p.getFechaBuyoff()).equals(ym)) iconos.append("👥 ");
        if (p.getFechaTransit() != null && YearMonth.from(p.getFechaTransit()).equals(ym)) iconos.append("🚢 ");
        return iconos.toString().trim();
    }

    private int renderStages(Sheet sheet, Map<String, CellStyle> styles, List<ElementoChecklist> itemsProyecto, List<YearMonth> timelineMonths, int rowIndex) {
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
            stageCell.setCellStyle(styles.get("stage"));
            for (int i = 1; i < 5 + timelineMonths.size(); i++) stageRow.createCell(i).setCellStyle(styles.get("stage"));
            
            int startStageRow = ++rowIndex;
            items.sort(Comparator.comparing(ElementoChecklist::getFechaPlan, Comparator.nullsLast(Comparator.naturalOrder())));

            for (ElementoChecklist item : items) {
                rowIndex = renderItemRows(sheet, styles, item, timelineMonths, rowIndex);
            }

            if (rowIndex > startStageRow) {
                sheet.groupRow(startStageRow, rowIndex - 1);
                sheet.setRowGroupCollapsed(startStageRow, true); 
            }
        }
        return rowIndex;
    }

    private int renderItemRows(Sheet sheet, Map<String, CellStyle> styles, ElementoChecklist item, List<YearMonth> timelineMonths, int rowIndex) {
        String status = item.getScore() != null ? item.getScore() : (item.getEstado() != null ? item.getEstado() : "PENDING");
        
        // Plan Row
        Row planRow = sheet.createRow(rowIndex++); planRow.setHeightInPoints(16);
        planRow.createCell(0).setCellValue("         📄 " + item.getNombre()); planRow.getCell(0).setCellStyle(styles.get("data"));
        planRow.createCell(1).setCellValue(item.getChampion()); planRow.getCell(1).setCellStyle(styles.get("data"));
        planRow.createCell(2).setCellValue(status); planRow.getCell(2).setCellStyle(styles.get("data"));
        planRow.createCell(3).setCellValue("Plan"); planRow.getCell(3).setCellStyle(styles.get("data"));
        planRow.createCell(4).setCellValue(item.getFechaPlan() != null ? item.getFechaPlan().toString() : "N/A"); planRow.getCell(4).setCellStyle(styles.get("data"));

        int tCol = 5;
        for (YearMonth ym : timelineMonths) {
            Cell cell = planRow.createCell(tCol++);
            cell.setCellStyle(styles.get("data"));
            if (item.getFechaPlan() != null && YearMonth.from(item.getFechaPlan()).equals(ym)) {
                cell.setCellStyle(styles.get("plan")); 
            }
        }

        // Actual Row
        Row actualRow = sheet.createRow(rowIndex++); actualRow.setHeightInPoints(16);
        actualRow.createCell(3).setCellValue("Actual"); actualRow.getCell(3).setCellStyle(styles.get("data"));
        actualRow.createCell(4).setCellValue(item.getFechaReal() != null ? item.getFechaReal().toString() : "N/A"); actualRow.getCell(4).setCellStyle(styles.get("data"));
        // Fill other cells with dataStyle to maintain formatting
        for(int i=0; i<3; i++) actualRow.createCell(i).setCellStyle(styles.get("data"));

        tCol = 5;
        for (YearMonth ym : timelineMonths) {
            Cell cell = actualRow.createCell(tCol++);
            cell.setCellStyle(styles.get("data"));
            if (item.getFechaReal() != null && YearMonth.from(item.getFechaReal()).equals(ym)) {
                cell.setCellStyle(styles.get("actual")); 
            }
        }
        return rowIndex;
    }
}
