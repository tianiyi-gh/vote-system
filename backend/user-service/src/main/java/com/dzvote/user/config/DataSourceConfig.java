package com.dzvote.user.config;

import org.springframework.context.annotation.Configuration;

// 自定义配置类（暂时留空，让 Spring Boot 自动配置）
@Configuration
public class DataSourceConfig {
    // 注释掉自定义 DataSource 配置，使用 Spring Boot 自动配置
    // @Primary
    // @Bean
    // public DataSource dataSource() { ... }
}
