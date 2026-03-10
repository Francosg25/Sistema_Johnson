package com.johnson.practica.servicio;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.Proyecto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelServicio {

    public byte[] generarExcelProyecto(Proyecto proyecto, List<ElementoChecklist> items) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Checklist APQP");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Información del Proyecto
            Row rowInfo1 = sheet.createRow(0);
            rowInfo1.createCell(0).setCellValue("PROYECTO:");
            rowInfo1.createCell(1).setCellValue(proyecto.getNombre());
            
            Row rowInfo2 = sheet.createRow(1);
            rowInfo2.createCell(0).setCellValue("PN:");
            rowInfo2.createCell(1).setCellValue(proyecto.getNumeroParte());

            Row rowInfo3 = sheet.createRow(2);
            rowInfo3.createCell(0).setCellValue("CLIENTE:");
            rowInfo3.createCell(1).setCellValue(proyecto.getCliente());

            // Cabeceras de la tabla
            Row headerRow = sheet.createRow(5);
            String[] columns = {"Código", "Elemento", "Fase", "Champion", "Estado", "Puntaje"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 6;
            for (ElementoChecklist item : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getCodigo());
                row.createCell(1).setCellValue(item.getNombre());
                row.createCell(2).setCellValue(item.getEtapaVisual());
                row.createCell(3).setCellValue(item.getChampion());
                row.createCell(4).setCellValue(item.getControlEntregable());
                row.createCell(5).setCellValue(item.getScore() != null ? item.getScore() : "0");
            }

            // Auto-ajustar columnas
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
