package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.StaffShiftImportRowRequest;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.exception.AppException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class StaffShiftExcelParser {
    private static final int MAX_IMPORT_ROWS = 5000;
    private static final String EMAIL = "email";
    private static final String WORK_DATE = "work date";
    private static final String SHIFT_CODE = "shift code";

    public List<StaffShiftImportRowRequest> parse(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null
                || !file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new AppException(ErrorCode.INVALID_FORMAT);
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new AppException(ErrorCode.INVALID_FORMAT);
            }
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Map<String, Integer> columns = readColumns(sheet.getRow(sheet.getFirstRowNum()), formatter, evaluator);
            List<StaffShiftImportRowRequest> rows = new ArrayList<>();

            for (int index = sheet.getFirstRowNum() + 1; index <= sheet.getLastRowNum(); index++) {
                Row row = sheet.getRow(index);
                if (row == null) continue;
                String email = readText(row.getCell(columns.get(EMAIL)), formatter, evaluator);
                String workDate = readDate(row.getCell(columns.get(WORK_DATE)), formatter, evaluator);
                String shiftCode = readText(row.getCell(columns.get(SHIFT_CODE)), formatter, evaluator);
                if (email.isBlank() && workDate.isBlank() && shiftCode.isBlank()) continue;
                if (rows.size() >= MAX_IMPORT_ROWS) {
                    throw new AppException(ErrorCode.INVALID_FORMAT);
                }
                rows.add(StaffShiftImportRowRequest.builder()
                        .rowNumber(index + 1)
                        .email(email)
                        .workDate(workDate)
                        .shiftCode(shiftCode)
                        .originalEmail(email)
                        .originalWorkDate(workDate)
                        .originalShiftCode(shiftCode)
                        .build());
            }
            return rows;
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof AppException appException) throw appException;
            throw new AppException(ErrorCode.INVALID_FORMAT);
        }
    }

    private Map<String, Integer> readColumns(Row header, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (header == null) throw new AppException(ErrorCode.INVALID_FORMAT);
        Map<String, Integer> columns = new HashMap<>();
        for (Cell cell : header) {
            columns.put(readText(cell, formatter, evaluator).trim().toLowerCase(Locale.ROOT), cell.getColumnIndex());
        }
        if (!columns.keySet().containsAll(List.of(EMAIL, WORK_DATE, SHIFT_CODE))) {
            throw new AppException(ErrorCode.INVALID_FORMAT);
        }
        return columns;
    }

    private String readText(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        return cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
    }

    private String readDate(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return readText(cell, formatter, evaluator);
    }
}
