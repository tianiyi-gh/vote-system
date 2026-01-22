import os
import subprocess

# 设置环境
vote_service_dir = r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service"

# 运行 Maven 编译
cmd = [
    r"D:\ide\maven3.9\bin\mvn.cmd",
    "clean",
    "package",
    "-DskipTests"
]

print("开始编译 vote-service...")
print(f"目录: {vote_service_dir}")

result = subprocess.run(cmd, cwd=vote_service_dir, capture_output=True, text=True)

print(result.stdout)
if result.stderr:
    print("STDERR:")
    print(result.stderr)

if result.returncode == 0:
    print("\n✓ 编译成功!")
else:
    print(f"\n✗ 编译失败，返回码: {result.returncode}")
