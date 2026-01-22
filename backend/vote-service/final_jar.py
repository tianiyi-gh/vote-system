import zipfile
import shutil
import os

# 源文件和目标文件
source_jar = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/activity-service/target/activity-service-2.0.0.jar"
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
    content = content.replace("Start-Class: com.dzvote.activity.ActivityApplication", "Start-Class: com.dzvote.vote.VoteServiceApplication")
    with open(manifest_path, 'w') as f:
        f.write(content)
    print(f"已修改 MANIFEST.MF")
else:
    print("MANIFEST.MF 不存在")

# 删除旧的 com/dzvote/activity 目录
activity_dir = os.path.join(temp_dir, "BOOT-INF", "classes", "com", "dzvote", "activity")
if os.path.exists(activity_dir):
    shutil.rmtree(activity_dir)
    print(f"已删除旧的 activity 类")

# 删除旧的 mapper XML 文件
old_mapper = os.path.join(temp_dir, "BOOT-INF", "classes", "mapper", "ActivityMapper.xml")
if os.path.exists(old_mapper):
    os.remove(old_mapper)
    print(f"已删除旧的 ActivityMapper.xml")

# 删除旧的 user 目录（如果存在）
user_dir = os.path.join(temp_dir, "BOOT-INF", "classes", "com", "dzvote", "user")
if os.path.exists(user_dir):
    shutil.rmtree(user_dir)
    print(f"已删除旧的 user 类")

# 删除旧的 candidate 目录（如果存在）
candidate_dir = os.path.join(temp_dir, "BOOT-INF", "classes", "com", "dzvote", "candidate")
if os.path.exists(candidate_dir):
    shutil.rmtree(candidate_dir)
    print(f"已删除旧的 candidate 类")

# 复制 vote-service 的类文件到 jar
vote_classes_src = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/classes"
vote_classes_dst = os.path.join(temp_dir, "BOOT-INF", "classes")

# 递归复制 vote-service 的类文件
for root, dirs, files in os.walk(vote_classes_src):
    for file in files:
        src_file = os.path.join(root, file)
        rel_path = os.path.relpath(src_file, vote_classes_src)
        dst_file = os.path.join(vote_classes_dst, rel_path)
        os.makedirs(os.path.dirname(dst_file), exist_ok=True)
        shutil.copy2(src_file, dst_file)

print(f"已复制 vote-service 类文件")

# 重新打包为 jar
with zipfile.ZipFile(target_jar, 'w') as zip_ref:
    for root, dirs, files in os.walk(temp_dir):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, temp_dir)
            zip_ref.write(file_path, arcname)

# 删除临时目录
shutil.rmtree(temp_dir)

print(f"✓ 成功创建 {target_jar}")
