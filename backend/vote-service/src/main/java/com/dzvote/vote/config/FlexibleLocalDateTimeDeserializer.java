package com.dzvote.vote.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 灵活的 LocalDateTime 反序列化器
 * 支持多种日期时间格式
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),  // 2024-01-01 09:00:00
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"), // 2024-01-01T09:00:00
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"), // 2024-01-01 09:00:00.000
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"), // 2024-01-01T09:00:00.000
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
    };

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getValueAsString();
        
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        dateStr = dateStr.trim();

        // 尝试用每种格式解析
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateStr, formatter);
            } catch (Exception e) {
                // 尝试下一种格式
            }
        }

        throw JsonMappingException.from(p, "无法解析日期时间: " + dateStr + "，支持的格式: yyyy-MM-dd HH:mm:ss, yyyy-MM-dd'T'HH:mm:ss");
    }
}

