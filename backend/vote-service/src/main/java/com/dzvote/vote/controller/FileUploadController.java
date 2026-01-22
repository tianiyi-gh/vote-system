package com.dzvote.vote.controller;

import com.dzvote.vote.util.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    /**
     * 初始化时转换路径为绝对路径
     */
    @PostConstruct
    public void init() {
        File path = new File(uploadPath);
        if (!path.isAbsolute()) {
            // 如果是相对路径，转换为相对于项目根目录的绝对路径
            String projectRoot = System.getProperty("user.dir");
            uploadPath = new File(projectRoot, uploadPath).getAbsolutePath();
        }
        log.info("文件上传根目录: {}", uploadPath);
    }

    /**
     * 上传候选人头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("========== 开始上传头像 ==========");
        log.info("文件名: {}", file.getOriginalFilename());
        log.info("文件大小: {} bytes", file.getSize());
        log.info("文件类型: {}", file.getContentType());

        try {
            // 验证文件
            if (file.isEmpty()) {
                log.warn("上传文件为空");
                return Result.error("文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("不支持的文件类型: {}", contentType);
                return Result.error("只支持图片文件");
            }

            // 验证文件大小（限制为5MB）
            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                log.warn("文件大小超过限制: {} bytes", file.getSize());
                return Result.error("图片大小不能超过5MB");
            }

            // 生成文件名：日期/UUID.扩展名
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // 创建目录
            File baseUploadDir = new File(uploadPath);
            log.info("基础上传路径: {}", baseUploadDir.getAbsolutePath());
            log.info("基础上传路径是否存在: {}", baseUploadDir.exists());

            if (!baseUploadDir.exists()) {
                boolean created = baseUploadDir.mkdirs();
                log.info("创建基础上传目录: {}, 结果: {}", baseUploadDir.getAbsolutePath(), created);
            }

            // 构建完整路径
            String fullPath = uploadPath + File.separator + "avatars" + File.separator + datePath.replace("/", File.separator);
            Path uploadDir = Paths.get(fullPath);
            log.info("上传目录: {}", uploadDir.toAbsolutePath());

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建目录成功: {}", uploadDir.toAbsolutePath());
            }

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            log.info("准备保存文件到: {}", filePath.toAbsolutePath());
            log.info("父目录是否存在: {}", filePath.toFile().getParentFile().exists());

            file.transferTo(filePath.toFile());

            log.info("文件上传成功: {}", filePath.toAbsolutePath());

            // 返回文件访问路径
            String fileUrl = "/uploads/avatars/" + datePath + "/" + filename;
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("path", filePath.toString());

            log.info("返回URL: {}", fileUrl);
            log.info("========== 上传完成 ==========");

            return Result.success(result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("上传过程中发生未预期错误", e);
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public Result<String> deleteFile(@RequestParam("path") String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", filePath);
                    return Result.success("删除成功");
                } else {
                    return Result.error("删除失败");
                }
            } else {
                return Result.error("文件不存在");
            }
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return Result.error("删除文件失败: " + e.getMessage());
        }
    }
}
