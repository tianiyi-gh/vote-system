import zipfile
import shutil
import os
import time

def create_jar(source_jar, target_jar, start_class, old_packages, new_classes_src):
    """创建新的 jar 文件"""

    # 解压源 jar
    temp_dir = os.path.join(os.path.dirname(target_jar), "temp_v2")
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
    os.makedirs(temp_dir)

    print(f"解压 {source_jar}...")
    with zipfile.ZipFile(source_jar, 'r') as zip_ref:
        zip_ref.extractall(temp_dir)

    # 读取并修改 MANIFEST.MF
    manifest_path = os.path.join(temp_dir, "META-INF", "MANIFEST.MF")
    with open(manifest_path, 'r', encoding='utf-8') as f:
        manifest_lines = f.readlines()

    # 修改 Start-Class
    new_manifest = []
    for line in manifest_lines:
        if line.startswith("Start-Class:"):
            new_manifest.append(f"Start-Class: {start_class}\n")
        elif line.startswith("Implementation-Title:"):
            new_manifest.append(f"Implementation-Title: {start_class.split('.')[-1]}\n")
        else:
            new_manifest.append(line)

    # 写入 MANIFEST.MF（添加正确的换行符）
    with open(manifest_path, 'wb') as f:
        content = ''.join(new_manifest).encode('utf-8')
        f.write(content)

    print(f"✓ 已设置 Start-Class 为: {start_class}")

    # 删除旧包（保留正确的包）
    classes_dir = os.path.join(temp_dir, "BOOT-INF", "classes", "com", "dzvote")
    if os.path.exists(classes_dir):
        for package in os.listdir(classes_dir):
            if package not in old_packages:
                pkg_path = os.path.join(classes_dir, package)
                if os.path.isdir(pkg_path):
                    print(f"✓ 删除 {package}")
                    shutil.rmtree(pkg_path)

    # 复制新类文件
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

    # 创建新的 jar，不压缩 BOOT-INF 中的文件
    print(f"重新打包到 {target_jar}...")
    with zipfile.ZipFile(target_jar, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as zip_ref:
        for root, dirs, files in os.walk(temp_dir):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, temp_dir)

                # 不压缩 BOOT-INF 中的类文件和索引文件，以保持与原始 jar 一致
                if arcname.startswith('BOOT-INF/classes/') or arcname.endswith('.idx'):
                    with open(file_path, 'rb') as f:
                        zip_ref.writestr(arcname, f.read())
                else:
                    zip_ref.write(file_path, arcname)

    # 清理
    shutil.rmtree(temp_dir)

    # 验证
    with zipfile.ZipFile(target_jar, 'r') as z:
        manifest = z.read('META-INF/MANIFEST.MF').decode('utf-8')
        for line in manifest.split('\n'):
            if line.startswith('Start-Class:'):
                print(f"✓ 验证: {line}")
                break

        # 检查启动类是否存在
        start_class_file = start_class.replace('.', '/') + '.class'
        start_class_path = f"BOOT-INF/classes/{start_class_file}"
        if start_class_path in z.namelist():
            print(f"✓ 验证: {start_class_path} 存在")
        else:
            print(f"✗ 警告: {start_class_path} 不存在")

    print(f"✓ 成功创建 {target_jar}")
    print(f"  文件大小: {os.path.getsize(target_jar) / 1024 / 1024:.2f} MB")

# 创建 user-service
print("="*60)
print("创建 user-service")
print("="*60)
create_jar(
    source_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/activity-service/target/activity-service-2.0.0.jar",
    target_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service/target/user-service-2.0.0.jar",
    start_class="com.dzvote.user.UserApplication",
    old_packages=["user"],
    new_classes_src=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service/target/classes"
)

print("\n" + "="*60 + "\n")

# 创建 vote-service
print("="*60)
print("创建 vote-service")
print("="*60)
create_jar(
    source_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/activity-service/target/activity-service-2.0.0.jar",
    target_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/vote-service-2.0.0.jar",
    start_class="com.dzvote.vote.VoteServiceApplication",
    old_packages=["vote"],
    new_classes_src=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/classes"
)

print("\n" + "="*60)
print("所有 jar 文件创建完成！")
print("="*60)
