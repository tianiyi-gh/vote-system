import zipfile
import shutil
import os

# 源文件和目标文件
source_jar = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service/target/user-service-2.0.0.jar"
target_jar = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/vote-service-2.0.0.jar"

# 检查源文件是否存在
if not os.path.exists(source_jar):
    print(f"源文件不存在: {source_jar}")
    exit(1)

# 创建目标目录
os.makedirs(os.path.dirname(target_jar), exist_ok=True)

# 复制 jar 文件
shutil.copy2(source_jar, target_jar)
print(f"已复制 {source_jar} 到 {target_jar}")

# 打开 jar 文件并修改 MANIFEST.MF
with zipfile.ZipFile(target_jar, 'r') as zip_ref:
    # 提取所有文件到临时目录
    temp_dir = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/temp_jar"
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
    os.makedirs(temp_dir)
    zip_ref.extractall(temp_dir)

# 修改 MANIFEST.MF
manifest_path = os.path.join(temp_dir, "META-INF", "MANIFEST.MF")
if os.path.exists(manifest_path):
    with open(manifest_path, 'r') as f:
        content = f.read()
    # 替换 Start-Class
    content = content.replace("Start-Class: com.dzvote.user.UserApplication", "Start-Class: com.dzvote.vote.VoteServiceApplication")
    with open(manifest_path, 'w') as f:
        f.write(content)
    print(f"已修改 MANIFEST.MF")
else:
    print("MANIFEST.MF 不存在")

# 删除旧的 com/dzvote/user 目录
user_dir = os.path.join(temp_dir, "BOOT-INF", "classes", "com", "dzvote", "user")
if os.path.exists(user_dir):
    shutil.rmtree(user_dir)
    print(f"已删除旧的 user 类")

# 创建 vote-service 的简单类文件结构
vote_dir = os.path.join(temp_dir, "BOOT-INF", "classes", "com", "dzvote", "vote")
if os.path.exists(vote_dir):
    shutil.rmtree(vote_dir)
os.makedirs(vote_dir)

# 创建简单的启动类
app_class_content = """package com.dzvote.vote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VoteServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoteServiceApplication.class, args);
    }
}
"""
app_file = os.path.join(vote_dir, "VoteServiceApplication.class")
# 注意：这只是占位，实际需要编译的类文件

print("注意: vote-service 的类文件需要手动编译")
print("已创建基础 jar 文件，但需要添加 vote-service 的类文件")

# 重新打包为 jar
with zipfile.ZipFile(target_jar, 'w') as zip_ref:
    for root, dirs, files in os.walk(temp_dir):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, temp_dir)
            zip_ref.write(file_path, arcname)

# 删除临时目录
shutil.rmtree(temp_dir)

print(f"成功创建基础 {target_jar}")
print("注意: 这是一个基础 jar 文件，需要添加 vote-service 的实际类文件")
