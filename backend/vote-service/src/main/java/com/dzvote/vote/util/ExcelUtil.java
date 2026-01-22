package com.dzvote.vote.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Excel工具类
 */
public class ExcelUtil {

    /**
     * 生成候选人导入模板
     */
    public static ResponseEntity<byte[]> generateCandidateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("候选人模板");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 创建数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"候选人姓名*", "编号", "所属分组", "简介", "头像URL", "状态"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // 创建示例数据
            Row dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("张三");
            dataRow1.createCell(1).setCellValue(1);
            dataRow1.createCell(2).setCellValue("第一组");
            dataRow1.createCell(3).setCellValue("张三简介");
            dataRow1.createCell(4).setCellValue("http://example.com/avatar1.jpg");
            dataRow1.createCell(5).setCellValue(1);

            Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("李四");
            dataRow2.createCell(1).setCellValue(2);
            dataRow2.createCell(2).setCellValue("第一组");
            dataRow2.createCell(3).setCellValue("李四简介");
            dataRow2.createCell(4).setCellValue("");
            dataRow2.createCell(5).setCellValue(1);

            // 应用数据样式
            for (Row row : sheet) {
                if (row.getRowNum() > 0) {
                    for (Cell cell : row) {
                        if (cell != null) {
                            cell.setCellStyle(dataStyle);
                        }
                    }
                }
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            // 设置响应头
            HttpHeaders headers1 = new HttpHeaders();
            headers1.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers1.setContentDispositionFormData("attachment", "candidates_template.xlsx");

            return ResponseEntity.ok()
                    .headers(headers1)
                    .body(bytes);
        }
    }
}
