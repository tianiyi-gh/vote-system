package com.dzvote.vote.controller;

import com.dzvote.vote.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 简化导出控制器 - 使用 JDBC 直接查询
 */
@RestController
@RequestMapping("/api/export/simple")
@RequiredArgsConstructor
public class SimpleExportController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 导出候选人统计 (CSV)
     */
    @GetMapping("/candidates/{activityId}")
    public void exportCandidates(@PathVariable Long activityId,
                                 HttpServletResponse response) {
        try {
            // 使用 JDBC 原生查询避免 MyBatis 类型处理器问题
            String sql = "SELECT id, activity_id, name, description, avatar, votes, " +
                         "order_num, status, created_at, updated_at " +
                         "FROM candidates WHERE activity_id = ? AND status = 1 ORDER BY votes DESC";

            List<Map<String, Object>> candidates = jdbcTemplate.queryForList(sql, activityId);

            if (candidates.isEmpty()) {
                sendError(response, "该活动没有候选人数据");
                return;
            }

            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "candidates_" + timestamp + ".csv";
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            // UTF-8 BOM
            response.getOutputStream().write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});

            PrintWriter writer = new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8);

            writer.println("ID,候选人姓名,头像URL,得票数,排序号,状态,创建时间");
            for (Map<String, Object> c : candidates) {
                String line = String.join(",",
                    String.valueOf(c.get("id")),
                    escapeCsv(String.valueOf(c.get("name"))),
                    escapeCsv(String.valueOf(c.get("avatar"))),
                    String.valueOf(c.get("votes")),
                    String.valueOf(c.get("order_num")),
                    Integer.parseInt(String.valueOf(c.get("status"))) == 1 ? "正常" : "禁用",
                    formatDate(String.valueOf(c.get("created_at")))
                );
                writer.println(line);
            }
            writer.flush();

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出活动汇总 (CSV)
     */
    @GetMapping("/activity/{activityId}")
    public void exportActivity(@PathVariable Long activityId,
                               HttpServletResponse response) {
        try {
            // 使用 JDBC 原生查询避免 MyBatis 类型处理器问题
            String sql = "SELECT id, title, description, start_time, end_time, status, " +
                         "vote_limit, candidate_count, total_votes, created_by, created_at " +
                         "FROM activities WHERE id = ? AND deleted = 0";

            List<Map<String, Object>> activities = jdbcTemplate.queryForList(sql, activityId);

            if (activities.isEmpty()) {
                sendError(response, "活动不存在");
                return;
            }

            Map<String, Object> activity = activities.get(0);

            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "activity_" + timestamp + ".csv";
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            response.getOutputStream().write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});

            PrintWriter writer = new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8);

            writer.println("ID,活动标题,活动描述,开始时间,结束时间,状态,投票限制,候选人数量,总票数,创建人,创建时间");
            String line = String.join(",",
                String.valueOf(activity.get("id")),
                escapeCsv(String.valueOf(activity.get("title"))),
                escapeCsv(String.valueOf(activity.get("description"))),
                formatDate(String.valueOf(activity.get("start_time"))),
                formatDate(String.valueOf(activity.get("end_time"))),
                formatStatus(Integer.parseInt(String.valueOf(activity.get("status")))),
                String.valueOf(activity.get("vote_limit")),
                String.valueOf(activity.get("candidate_count")),
                String.valueOf(activity.get("total_votes")),
                escapeCsv(String.valueOf(activity.get("created_by"))),
                formatDate(String.valueOf(activity.get("created_at")))
            );
            writer.println(line);
            writer.flush();

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "导出失败: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.equals("null")) {
            return "";
        }
        // 处理数据库返回的时间格式
        if (dateTime.contains("T")) {
            return dateTime.replace("T", " ").substring(0, 19);
        }
        if (dateTime.contains(" ")) {
            return dateTime.length() > 19 ? dateTime.substring(0, 19) : dateTime;
        }
        return dateTime;
    }

    private String formatStatus(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 1 -> "进行中";
            case 2 -> "已结束";
            case 3 -> "已取消";
            default -> String.valueOf(status);
        };
    }

    private void sendError(HttpServletResponse response, String message) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
