import os
import subprocess

# 编译命令
compile_cmd = [
    "javac",
    "-encoding", "UTF-8",
    "-cp", r"vote-service/target/vote-service-2.0.0.jar;user-service/target/user-service-2.0.0.jar",
    "-d", r"vote-service/target/classes",
    r"vote-service/src/main/java/com/dzvote/vote/controller/VoteController.java",
    r"vote-service/src/main/java/com/dzvote/vote/service/impl/VoteServiceImpl.java"
]

print("编译修改的类文件...")
result = subprocess.run(compile_cmd, shell=True)
if result.returncode != 0:
    print("编译失败!")
    exit(1)
print("编译成功!")

# 运行重新打包脚本
print("\n重新打包 jar...")
import create_jar_v2
