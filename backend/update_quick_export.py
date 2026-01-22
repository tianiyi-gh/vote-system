#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""更新 QuickExportController 到 JAR"""

import zipfile
import shutil
import os

jar_path = r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\vote-service-2.0.0.jar"
class_file = r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\classes\com\dzvote\vote\controller\QuickExportController.class"
temp_dir = r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\target\temp_jar"

print("更新 QuickExportController 到 JAR...")

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
    print(f"已添加类文件: QuickExportController.class")
else:
    print(f"警告: 类文件不存在: {class_file}")

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
