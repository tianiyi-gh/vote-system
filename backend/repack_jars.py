import zipfile
import shutil
import os

def create_jar_from_activity(source_jar, target_jar, start_class, old_package_prefix, new_classes_src):
    """从 activity-service 的 jar 创建新的 jar"""

    # 解压源 jar
    temp_dir = os.path.join(os.path.dirname(target_jar), "temp_repack")
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
    os.makedirs(temp_dir)

    print(f"解压 {source_jar} 到临时目录...")
    with zipfile.ZipFile(source_jar, 'r') as zip_ref:
        zip_ref.extractall(temp_dir)

    # 修改 MANIFEST.MF
    manifest_path = os.path.join(temp_dir, "META-INF", "MANIFEST.MF")
    with open(manifest_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    new_lines = []
    for line in lines:
        if line.startswith("Start-Class:"):
            new_lines.append(f"Start-Class: {start_class}\n")
        elif line.startswith("Implementation-Title:"):
            new_lines.append(f"Implementation-Title: {start_class.split('.')[-1]}\n")
        else:
            new_lines.append(line)

    with open(manifest_path, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)

    print(f"✓ 已设置 Start-Class 为: {start_class}")

    # 删除旧的包
    classes_dir = os.path.join(temp_dir, "BOOT-INF", "classes", "com", "dzvote")
    if os.path.exists(classes_dir):
        for item in os.listdir(classes_dir):
            item_path = os.path.join(classes_dir, item)
            if os.path.isdir(item_path):
                if item != old_package_prefix:
                    print(f"✓ 删除 {item}")
                    shutil.rmtree(item_path)

    # 复制新的类文件
    if os.path.exists(new_classes_src):
        new_classes_dst = os.path.join(temp_dir, "BOOT-INF", "classes")
        for root, dirs, files in os.walk(new_classes_src):
            for file in files:
                src_file = os.path.join(root, file)
                rel_path = os.path.relpath(src_file, new_classes_src)
                dst_file = os.path.join(new_classes_dst, rel_path)
                os.makedirs(os.path.dirname(dst_file), exist_ok=True)
                shutil.copy2(src_file, dst_file)
        print(f"✓ 已复制新类文件")

    # 删除旧 jar
    if os.path.exists(target_jar):
        os.remove(target_jar)

    # 重新打包
    print(f"重新打包到 {target_jar}...")
    with zipfile.ZipFile(target_jar, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as zip_ref:
        for root, dirs, files in os.walk(temp_dir):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, temp_dir)
                zip_ref.write(file_path, arcname)

    # 清理
    shutil.rmtree(temp_dir)

    print(f"✓ 成功创建 {target_jar}")

    # 验证
    with zipfile.ZipFile(target_jar, 'r') as z:
        manifest = z.read('META-INF/MANIFEST.MF').decode('utf-8')
        for line in manifest.split('\n'):
            if line.startswith('Start-Class:'):
                print(f"✓ 验证: {line}")
                break

# 创建 user-service
create_jar_from_activity(
    source_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/activity-service/target/activity-service-2.0.0.jar",
    target_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service/target/user-service-2.0.0.jar",
    start_class="com.dzvote.user.UserApplication",
    old_package_prefix="user",
    new_classes_src=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service/target/classes"
)

print("\n" + "="*60 + "\n")

# 创建 vote-service
create_jar_from_activity(
    source_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/activity-service/target/activity-service-2.0.0.jar",
    target_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/vote-service-2.0.0.jar",
    start_class="com.dzvote.vote.VoteServiceApplication",
    old_package_prefix="vote",
    new_classes_src=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/classes"
)
