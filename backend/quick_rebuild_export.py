import os
import subprocess

os.chdir(r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend")

# 编译 ExportService
compile_cmd = [
    "javac",
    "-encoding", "UTF-8",
    "-cp", r"vote-service/target/vote-service-2.0.0.jar",
    "-d", r"vote-service/target/classes",
    r"vote-service/src/main/java/com/dzvote/vote/service/ExportService.java"
]

print("编译 ExportService.java...")
result = subprocess.run(compile_cmd, shell=True)
if result.returncode != 0:
    print("编译失败!")
    exit(1)
print("编译成功!")

# 更新 JAR
print("\n更新 JAR 文件...")
update_cmd = [
    "jar",
    "uf",
    r"vote-service/target/vote-service-2.0.0.jar",
    "-C", r"vote-service/target/classes",
    "com/dzvote/vote/service/ExportService.class"
]
result = subprocess.run(update_cmd, shell=True)
if result.returncode != 0:
    print("更新 JAR 失败!")
    exit(1)
print("JAR 更新成功!")
print("\n完成! 可以启动服务了。")
