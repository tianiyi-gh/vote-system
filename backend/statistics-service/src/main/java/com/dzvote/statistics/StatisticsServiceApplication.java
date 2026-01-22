package com.dzvote.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 统计服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.dzvote")
@EnableFeignClients
@EnableScheduling
public class StatisticsServiceApplication {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("统计分析服务启动中...");
        System.out.println("========================================");
        
        SpringApplication.run(StatisticsServiceApplication.class, args);
        
        System.out.println("========================================");
        System.out.println("统计分析服务启动成功！");
        System.out.println("Swagger文档地址: http://localhost:8083/swagger-ui.html");
        System.out.println("========================================");
    }
}