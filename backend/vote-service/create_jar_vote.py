import zipfile
import shutil
import os

# 使用voting-service作为基础
source_jar = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/voting-service/target/voting-service-2.0.0.jar"
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
temp_dir = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/temp_jar"
if os.path.exists(temp_dir):
    shutil.rmtree(temp_dir)
os.makedirs(temp_dir)

with zipfile.ZipFile(target_jar, 'r') as zip_ref:
    zip_ref.extractall(temp_dir)

# 修改 MANIFEST.MF
manifest_path = os.path.join(temp_dir, "META-INF", "MANIFEST.MF")
if os.path.exists(manifest_path):
    with open(manifest_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 替换 Start-Class
    content = content.replace("Start-Class: com.dzvote.voting.VotingServiceApplication", "Start-Class: com.dzvote.vote.VoteServiceApplication")

    with open(manifest_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("✓ 已更新 MANIFEST.MF")

# 删除旧的 voting 类
voting_dir = os.path.join(temp_dir, "BOOT-INF", "classes", "com", "dzvote", "voting")
if os.path.exists(voting_dir):
    shutil.rmtree(voting_dir)
    print("✓ 已删除旧的 voting 类")

# 复制 vote-service 的类文件
vote_classes_src = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/classes"
vote_classes_dst = os.path.join(temp_dir, "BOOT-INF", "classes")

for root, dirs, files in os.walk(vote_classes_src):
    for file in files:
        src_file = os.path.join(root, file)
        rel_path = os.path.relpath(src_file, vote_classes_src)
        dst_file = os.path.join(vote_classes_dst, rel_path)
        os.makedirs(os.path.dirname(dst_file), exist_ok=True)
        shutil.copy2(src_file, dst_file)

print("✓ 已复制 vote-service 类文件")

# 重新打包
new_jar = target_jar + ".new"
with zipfile.ZipFile(new_jar, 'w') as zip_ref:
    for root, dirs, files in os.walk(temp_dir):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, temp_dir)
            zip_ref.write(file_path, arcname)

if os.path.exists(target_jar):
    os.remove(target_jar)
os.rename(new_jar, target_jar)

# 删除临时目录
shutil.rmtree(temp_dir)

print(f"\n✓ 成功创建 {target_jar}")
