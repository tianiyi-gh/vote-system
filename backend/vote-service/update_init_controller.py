import zipfile
import shutil
import os

jar_path = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/vote-service-2.0.0.jar"
temp_dir = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/update_temp"
class_src = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/classes"

# 清理临时目录
if os.path.exists(temp_dir):
    shutil.rmtree(temp_dir)
os.makedirs(temp_dir)

# 解压jar
print("正在解压jar...")
with zipfile.ZipFile(jar_path, 'r') as z:
    z.extractall(temp_dir)

# 复制新编译的InitDataController
controller_src = os.path.join(class_src, "com", "dzvote", "vote", "controller", "InitDataController.class")
controller_dst = os.path.join(temp_dir, "BOOT-INF", "classes", "com", "dzvote", "vote", "controller")

if os.path.exists(controller_src):
    os.makedirs(controller_dst, exist_ok=True)
    shutil.copy2(controller_src, controller_dst)
    print("已复制 InitDataController.class")
else:
    print("InitDataController.class 不存在，需要先编译")
    # 尝试编译单个文件
    import subprocess
    print("正在编译 InitDataController.java...")
    compile_cmd = [
        "javac",
        "-cp", class_src + ";" + os.path.join(os.path.dirname(jar_path), "lib", "*"),
        "-d", class_src,
        "src/main/java/com/dzvote/vote/controller/InitDataController.java"
    ]
    subprocess.run(compile_cmd, cwd="d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service")

# 重新打包
print("正在重新打包...")
backup_jar = jar_path + ".before_update"
if os.path.exists(backup_jar):
    os.remove(backup_jar)
os.rename(jar_path, backup_jar)

with zipfile.ZipFile(jar_path, 'w', zipfile.ZIP_DEFLATED) as z:
    for root, dirs, files in os.walk(temp_dir):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, temp_dir)
            z.write(file_path, arcname)

print(f"更新完成！备份文件: {backup_jar}")
print("请重启 vote-service 以使更改生效")
