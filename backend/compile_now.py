import subprocess
import os

os.chdir(r"d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service")

cmd = [
    "python", "compile_vote.py"
]

print("执行编译脚本...")
result = subprocess.run(cmd, shell=True)
print(f"退出码: {result.returncode}")
