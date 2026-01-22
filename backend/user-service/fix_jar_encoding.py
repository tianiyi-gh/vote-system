import zipfile
import shutil
import os

jar_path = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service/target/user-service-2.0.0.jar"
temp_dir = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service/target/fix_jar"

# 清理临时目录
if os.path.exists(temp_dir):
    shutil.rmtree(temp_dir)
os.makedirs(temp_dir)

# 解压jar
print("正在解压jar...")
with zipfile.ZipFile(jar_path, 'r') as z:
    z.extractall(temp_dir)

# 读取并修改 application-standalone.yml
standalone_yml = os.path.join(temp_dir, "BOOT-INF", "classes", "application-standalone.yml")
if os.path.exists(standalone_yml):
    with open(standalone_yml, 'r', encoding='utf-8') as f:
        content = f.read()

    # 替换错误的编码参数
    content = content.replace("characterEncoding=utf8mb4", "characterEncoding=UTF-8")
    # 同时修改用户名密码为正确的
    content = content.replace("username: dzvote", "username: root")
    content = content.replace("password: dzvote123", "password: mysql8123")

    with open(standalone_yml, 'w', encoding='utf-8') as f:
        f.write(content)
    print("已修改 application-standalone.yml")

# 修改 application-dev.yml
dev_yml = os.path.join(temp_dir, "BOOT-INF", "classes", "application-dev.yml")
if os.path.exists(dev_yml):
    with open(dev_yml, 'r', encoding='utf-8') as f:
        content = f.read()
    content = content.replace("characterEncoding=utf8mb4", "characterEncoding=UTF-8")
    with open(dev_yml, 'w', encoding='utf-8') as f:
        f.write(content)
    print("已修改 application-dev.yml")

# 重新打包
print("正在重新打包...")
backup_jar = jar_path + ".bak"
if os.path.exists(backup_jar):
    os.remove(backup_jar)
os.rename(jar_path, backup_jar)

with zipfile.ZipFile(jar_path, 'w', zipfile.ZIP_DEFLATED) as z:
    for root, dirs, files in os.walk(temp_dir):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, temp_dir)
            z.write(file_path, arcname)

print(f"修改完成！备份文件: {backup_jar}")
