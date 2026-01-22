#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修补 vote-service JAR 中的 CaptchaController.class
将分隔符从逗号改为竖线
"""

import os
import sys
import zipfile
import tempfile
import shutil

# Java 字节码中的字符串
# 我们需要将 String.format("%s|%s", ...) 中的逗号替换为竖线
# 由于这是一个简单的字符串替换，我们可以修改字节码中的 UTF-8 字符串

def patch_captcha_controller():
    jar_path = "d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/vote-service-2.0.0.jar"
    backup_path = jar_path + ".backup"
    
    if not os.path.exists(jar_path):
        print(f"错误: JAR 文件不存在: {jar_path}")
        return False
    
    # 创建备份
    if not os.path.exists(backup_path):
        shutil.copy2(jar_path, backup_path)
        print(f"已创建备份: {backup_path}")
    
    # 创建临时目录
    with tempfile.TemporaryDirectory() as temp_dir:
        # 解压 JAR
        print("解压 JAR 文件...")
        with zipfile.ZipFile(jar_path, 'r') as zip_ref:
            zip_ref.extractall(temp_dir)
        
        # 查找 CaptchaController.class
        class_path = os.path.join(temp_dir, "BOOT-INF", "classes", "com", "dzvote", "vote", "controller", "CaptchaController.class")
        
        if not os.path.exists(class_path):
            print(f"警告: 未找到 CaptchaController.class: {class_path}")
            print("将跳过修补")
            return True
        
        print(f"找到 CaptchaController.class: {class_path}")
        
        # 读取 class 文件
        with open(class_path, 'rb') as f:
            class_data = f.read()
        
        # 替换字节码中的字符串
        # UTF-8 编码: "," = 0x2C, "|" = 0x7C
        # 我们需要替换出现在 String.format 中的逗号
        original = class_data
        modified = class_data.replace(b'%s,%s', b'%s|%s')
        
        if modified == original:
            print("警告: 未找到需要替换的字符串")
            return True
        
        # 写回 class 文件
        with open(class_path, 'wb') as f:
            f.write(modified)
        
        print("已修补 CaptchaController.class")
        
        # 重新打包 JAR
        print("重新打包 JAR 文件...")
        with zipfile.ZipFile(jar_path, 'w', zipfile.ZIP_DEFLATED) as zip_ref:
            for root, dirs, files in os.walk(temp_dir):
                for file in files:
                    file_path = os.path.join(root, file)
                    arcname = os.path.relpath(file_path, temp_dir)
                    zip_ref.write(file_path, arcname)
        
        print("JAR 文件修补完成")
        return True

if __name__ == "__main__":
    try:
        if patch_captcha_controller():
            print("✓ 修补成功")
            sys.exit(0)
        else:
            print("✗ 修补失败")
            sys.exit(1)
    except Exception as e:
        print(f"✗ 错误: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
