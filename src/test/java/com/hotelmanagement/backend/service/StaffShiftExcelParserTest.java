package com.hotelmanagement.backend.service;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaffShiftExcelParserTest {
    StaffShiftExcelParser parser = new StaffShiftExcelParser();

    @Test
    void parsesRequiredColumnsAndExcelDate() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Schedules");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("Email");
            header.createCell(1).setCellValue("Work Date");
            header.createCell(2).setCellValue("Shift Code");
            var row = sheet.createRow(1);
            row.createCell(0).setCellValue("ngoc@example.com");
            var dateCell = row.createCell(1);
            dateCell.setCellValue(Date.from(LocalDate.of(2026, 8, 11)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
            dateCell.setCellStyle(workbook.createCellStyle());
            dateCell.getCellStyle().setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
            row.createCell(2).setCellValue("MORNING");
            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

        var rows = parser.parse(new MockMultipartFile(
                "file", "schedules.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                workbookBytes
        ));

        assertEquals(1, rows.size());
        assertEquals("ngoc@example.com", rows.get(0).getEmail());
        assertEquals("2026-08-11", rows.get(0).getWorkDate());
        assertEquals("MORNING", rows.get(0).getShiftCode());
    }
}
