package com.dzvote.vote.service;

import com.dzvote.vote.dto.ReportExportRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 报表导出服务
 */
@Service
public class ExportService {

    private static final int BATCH_SIZE = 10000;  // 每批处理的数据量
    private static final int MAX_RECORDS_LIMIT = 100000;  // 单次导出最大记录数限制

    /**
     * 导出活动报表
     * @param request 导出请求
     * @param response HTTP响应
     * @param data 报表数据
     */
    public void exportReport(ReportExportRequest request, HttpServletResponse response,
                        List<Map<String, Object>> data) {
        // 检查数据量限制
        if (data.size() > MAX_RECORDS_LIMIT) {
            throw new RuntimeException(
                "数据量超过单次导出限制（" + MAX_RECORDS_LIMIT + "条），请缩小日期范围或使用异步导出"
            );
        }

        try {
            String fileName = generateFileName(request);
            String contentType = getContentType(request.getExportType());

            response.setContentType(contentType);
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"");

            if ("EXCEL".equalsIgnoreCase(request.getExportType())) {
                exportToExcel(data, request, response.getOutputStream());
            } else if ("CSV".equalsIgnoreCase(request.getExportType())) {
                exportToCsv(data, request, response.getOutputStream());
            }

        } catch (IOException e) {
            throw new RuntimeException("导出报表失败", e);
        }
    }

    /**
     * 批量导出活动报表（支持大数据量）
     * @param request 导出请求
     * @param response HTTP响应
     * @param dataSupplier 数据提供函数（支持分页查询）
     */
    public void exportReportBatch(ReportExportRequest request, HttpServletResponse response,
                                 java.util.function.Supplier<List<Map<String, Object>>> dataSupplier) {
        try {
            String fileName = generateFileName(request);
            String contentType = getContentType(request.getExportType());

            response.setContentType(contentType);
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"");

            if ("EXCEL".equalsIgnoreCase(request.getExportType())) {
                exportToExcelBatch(request, response.getOutputStream(), dataSupplier);
            } else if ("CSV".equalsIgnoreCase(request.getExportType())) {
                exportToCsvBatch(request, response.getOutputStream(), dataSupplier);
            }

        } catch (IOException e) {
            throw new RuntimeException("导出报表失败", e);
        }
    }

    /**
     * 批量导出Excel（支持大数据量）
     */
    private void exportToExcelBatch(ReportExportRequest request, OutputStream outputStream,
                                   java.util.function.Supplier<List<Map<String, Object>>> dataSupplier) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(generateSheetName(request));

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = getHeaders(request.getReportType());
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 批量填充数据
            CellStyle dataStyle = createDataStyle(workbook);
            int totalRows = 0;
            List<Map<String, Object>> batch;

            while (!(batch = dataSupplier.get()).isEmpty() && totalRows < MAX_RECORDS_LIMIT) {
                for (Map<String, Object> record : batch) {
                    if (totalRows >= MAX_RECORDS_LIMIT) break;

                    Row row = sheet.createRow(totalRows + 1);
                    Object[] values = getValues(request.getReportType(), record);

                    for (int j = 0; j < values.length; j++) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue(getCellValue(values[j]));
                        cell.setCellStyle(dataStyle);
                    }
                    totalRows++;
                }

                // 每处理BATCH_SIZE条数据，写入一次
                if (totalRows % BATCH_SIZE == 0) {
                    workbook.write(outputStream);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * 批量导出CSV（支持大数据量）
     */
    private void exportToCsvBatch(ReportExportRequest request, OutputStream outputStream,
                                 java.util.function.Supplier<List<Map<String, Object>>> dataSupplier) throws IOException {
        String[] headers = getHeaders(request.getReportType());

        // 写入BOM头
        outputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});

        // 写入标题行
        outputStream.write((String.join(",", headers) + "\n").getBytes("UTF-8"));

        // 批量写入数据行
        int totalRows = 0;
        List<Map<String, Object>> batch;

        while (!(batch = dataSupplier.get()).isEmpty() && totalRows < MAX_RECORDS_LIMIT) {
            for (Map<String, Object> record : batch) {
                if (totalRows >= MAX_RECORDS_LIMIT) break;

                Object[] values = getValues(request.getReportType(), record);
                String[] strValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    strValues[i] = escapeCsvValue(getCellValue(values[i]));
                }
                outputStream.write((String.join(",", strValues) + "\n").getBytes("UTF-8"));

                totalRows++;

                // 每处理BATCH_SIZE条数据，刷新一次
                if (totalRows % BATCH_SIZE == 0) {
                    outputStream.flush();
                }
            }
        }
    }

    /**
     * 导出为Excel
     */
    private void exportToExcel(List<Map<String, Object>> data,
                             ReportExportRequest request,
                             OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(generateSheetName(request));

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = getHeaders(request.getReportType());
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            CellStyle dataStyle = createDataStyle(workbook);
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> record = data.get(i);
                Object[] values = getValues(request.getReportType(), record);

                for (int j = 0; j < values.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(getCellValue(values[j]));
                    cell.setCellStyle(dataStyle);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * 导出为CSV
     */
    private void exportToCsv(List<Map<String, Object>> data,
                            ReportExportRequest request,
                            OutputStream outputStream) throws IOException {
        String[] headers = getHeaders(request.getReportType());
        StringBuilder csv = new StringBuilder();

        // 写入标题行
        csv.append(String.join(",", headers)).append("\n");

        // 写入数据行
        for (Map<String, Object> record : data) {
            Object[] values = getValues(request.getReportType(), record);
            String[] strValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                strValues[i] = escapeCsvValue(getCellValue(values[i]));
            }
            csv.append(String.join(",", strValues)).append("\n");
        }

        // 写入BOM头，支持Excel正确打开UTF-8编码的CSV
        outputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
        outputStream.write(csv.toString().getBytes("UTF-8"));
    }

    /**
     * 转义CSV值
     */
    private String escapeCsvValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        // 如果包含逗号或双引号，需要用双引号包裹
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 生成文件名
     */
    private String generateFileName(ReportExportRequest request) {
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s.%s",
            request.getReportType(),
            timestamp,
            getFileExtension(request.getExportType()));
    }

    /**
     * 获取工作表名称
     */
    private String generateSheetName(ReportExportRequest request) {
        return request.getReportType();
    }

    /**
     * 获取内容类型
     */
    private String getContentType(String exportType) {
        if ("EXCEL".equalsIgnoreCase(exportType)) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if ("CSV".equalsIgnoreCase(exportType)) {
            return "text/csv; charset=UTF-8";
        }
        return "application/octet-stream";
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String exportType) {
        if ("EXCEL".equalsIgnoreCase(exportType)) {
            return "xlsx";
        } else if ("CSV".equalsIgnoreCase(exportType)) {
            return "csv";
        }
        return "txt";
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

    /**
     * 创建标题样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short)12);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short)11);
        style.setFont(font);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 获取表头
     */
    private String[] getHeaders(String reportType) {
        switch (reportType) {
            case "VOTE_RECORDS":
                return new String[]{"ID", "Activity ID", "Candidate ID", "Voter Phone",
                    "Voter IP", "Vote Time", "Channel", "Valid", "Device Fingerprint"};
            case "CANDIDATE_STATS":
                return new String[]{"ID", "Name", "Description", "Votes", "Activity ID", "Status"};
            case "ACTIVITY_SUMMARY":
                return new String[]{"Activity ID", "Title", "Total Votes", "Candidate Count",
                    "Start Time", "End Time", "Status"};
            default:
                return new String[]{"ID", "Name", "Value"};
        }
    }

    /**
     * 获取值数组
     */
    private Object[] getValues(String reportType, Map<String, Object> record) {
        switch (reportType) {
            case "VOTE_RECORDS":
                return new Object[]{
                    record.get("id"),
                    record.get("activity_id"),
                    record.get("candidate_id"),
                    record.get("voter_phone"),
                    record.get("voter_ip"),
                    record.get("vote_time"),
                    record.get("channel"),
                    record.get("valid"),
                    record.get("device_fingerprint")
                };
            case "CANDIDATE_STATS":
                return new Object[]{
                    record.get("id"),
                    record.get("name"),
                    record.get("description"),
                    record.get("votes"),
                    record.get("activity_id"),
                    record.get("status")
                };
            case "ACTIVITY_SUMMARY":
                return new Object[]{
                    record.get("id"),
                    record.get("title"),
                    record.get("total_votes"),
                    record.get("candidate_count"),
                    record.get("start_time"),
                    record.get("end_time"),
                    record.get("status")
                };
            default:
                return new Object[]{
                    record.get("id"),
                    record.get("name"),
                    record.get("value")
                };
        }
    }
}
