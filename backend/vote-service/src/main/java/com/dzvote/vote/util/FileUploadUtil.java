package com.dzvote.vote.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传工具类
 */
public class FileUploadUtil {

    /**
     * 上传文件
     * @param file 文件
     * @param uploadPath 上传目录
     * @return 文件访问路径
     */
    public static String uploadFile(MultipartFile file, String uploadPath) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取原始文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!isAllowedImage(extension)) {
            throw new IllegalArgumentException("只支持图片格式：jpg, jpeg, png, gif, webp");
        }

        // 生成唯一文件名
        String filename = UUID.randomUUID().toString() + "." + extension;

        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 保存文件
        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());

        // 返回访问路径
        return "/uploads/" + filename;
    }

    /**
     * 删除文件
     * @param filePath 文件路径
     */
    public static void deleteFile(String filePath, String uploadPath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        // 提取文件名
        String filename = filePath.substring(filePath.lastIndexOf('/') + 1);
        Path fullPath = Paths.get(uploadPath, filename);

        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
        }
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 检查是否为允许的图片格式
     */
    private static boolean isAllowedImage(String extension) {
        return extension.matches("jpg|jpeg|png|gif|webp");
    }
}
