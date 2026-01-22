import os
import subprocess
import sys

backend_dir = r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend"
os.chdir(backend_dir)

# 编译
print("编译 VoteController 和 VoteServiceImpl...")
compile_cmd = [
    "javac",
    "-encoding", "UTF-8",
    "-cp", "vote-service/target/vote-service-2.0.0.jar;user-service/target/user-service-2.0.0.jar;D:/ide/maven3.9/repository/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar",
    "-d", "vote-service/target/classes",
    r"vote-service\src\main\java\com\dzvote\vote\controller\VoteController.java",
    r"vote-service\src\main\java\com\dzvote\vote\service\impl\VoteServiceImpl.java"
]

result = subprocess.run(compile_cmd, capture_output=True)
if result.returncode != 0:
    print("编译失败:")
    print(result.stderr.decode('utf-8', errors='replace'))
    sys.exit(1)

print("编译成功!")

# 更新 jar
print("\n更新 jar 文件...")
jar_cmd = [
    "jar",
    "uf", "vote-service/target/vote-service-2.0.0.jar",
    "-C", "vote-service/target/classes",
    "com/dzvote/vote/controller/VoteController.class",
    "com/dzvote/vote/service/impl/VoteServiceImpl.class"
]

result = subprocess.run(jar_cmd, capture_output=True)
if result.returncode != 0:
    print("更新 jar 失败:")
    print(result.stderr.decode('utf-8', errors='replace'))
    sys.exit(1)

print("jar 文件更新成功!")
print("\n现在请重启 vote-service 服务")
