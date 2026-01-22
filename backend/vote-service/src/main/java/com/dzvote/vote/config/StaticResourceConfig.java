package com.dzvote.vote.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源配置
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceConfig.class);

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 如果是相对路径，转换为绝对路径
        File path = new File(uploadPath);
        if (!path.isAbsolute()) {
            String projectRoot = System.getProperty("user.dir");
            uploadPath = new File(projectRoot, uploadPath).getAbsolutePath();
        }

        // 配置上传文件的外部访问路径
        // Windows 路径需要添加前缀 file:///
        String uploadDir = uploadPath.replace("\\", "/");
        String resourceLocation = uploadDir.startsWith("/") ? "file:" + uploadDir + "/" : "file:///" + uploadDir + "/";

        log.info("Configuring resource handler: /uploads/** -> {}", resourceLocation);
        log.info("Upload directory exists: {}", new File(uploadPath).exists());
        log.info("Upload directory absolute path: {}", uploadPath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}
