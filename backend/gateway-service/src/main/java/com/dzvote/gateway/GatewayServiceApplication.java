package com.dzvote.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.dzvote")
public class GatewayServiceApplication {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("网关服务启动中...");
        System.out.println("========================================");
        
        SpringApplication.run(GatewayServiceApplication.class, args);
        
        System.out.println("========================================");
        System.out.println("网关服务启动成功！");
        System.out.println("Swagger文档地址: http://localhost:8080/swagger-ui.html");
        System.out.println("========================================");
    }
}