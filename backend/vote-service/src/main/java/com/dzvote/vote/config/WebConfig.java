package com.dzvote.vote.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 到实际的文件目录
        String resourceLocation = "file:" + uploadPath + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
