#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""编译并更新 QuickExportController 到 JAR"""

import zipfile
import shutil
import os
import subprocess

jar_path = r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\vote-service-2.0.0.jar"
src_file = r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\src\main\java\com\dzvote\vote\controller\QuickExportController.java"
class_output_dir = r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\classes"
class_file = os.path.join(class_output_dir, r"com\dzvote\vote\controller\QuickExportController.class")
temp_dir = r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\temp_jar"

print("1. 编译 QuickExportController...")

# 编译
javac_cmd = [
    r"D:\ide\Java\java17\bin\javac.exe",
    "-encoding", "UTF-8",
    "-cp", jar_path,
    "-d", class_output_dir,
    src_file
]

result = subprocess.run(javac_cmd, capture_output=True, text=True)
if result.returncode != 0:
    print(f"编译失败: {result.stderr}")
    exit(1)

print("编译成功!")

print("2. 更新 JAR...")

# 提取 JAR
if os.path.exists(temp_dir):
    shutil.rmtree(temp_dir)
os.makedirs(temp_dir)

print(f"提取 JAR: {jar_path}")
with zipfile.ZipFile(jar_path, 'r') as zip_ref:
    zip_ref.extractall(temp_dir)

# 添加新类文件
target_dir = os.path.join(temp_dir, "BOOT-INF/classes/com/dzvote/vote/controller")
os.makedirs(target_dir, exist_ok=True)

if os.path.exists(class_file):
    shutil.copy(class_file, target_dir)
    print(f"已添加类文件")
else:
    print(f"错误: 类文件不存在: {class_file}")
    exit(1)

# 重新打包
print("重新打包 JAR...")
with zipfile.ZipFile(jar_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
    for root, dirs, files in os.walk(temp_dir):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, temp_dir)
            zipf.write(file_path, arcname)

# 清理
shutil.rmtree(temp_dir)

print("更新完成！")
