package com.dzvote.activity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 活动服务应用启动类
 */
@SpringBootApplication
@MapperScan("com.dzvote.activity.mapper")
public class ActivityTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActivityTestApplication.class, args);
    }
}