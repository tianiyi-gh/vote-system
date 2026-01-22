import zipfile
import shutil
import os
import io

def fix_jar(source_jar, target_jar, start_class, old_classes_dirs, old_mappers, new_classes_src):

    # 检查源文件是否存在
    if not os.path.exists(source_jar):
        print(f"源文件不存在: {source_jar}")
        return False

    # 创建目标目录
    os.makedirs(os.path.dirname(target_jar), exist_ok=True)

    # 打开源 jar
    print(f"处理 {source_jar}")

    # 临时目录
    temp_dir = os.path.join(os.path.dirname(target_jar), "temp_fix")
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
    os.makedirs(temp_dir)

    # 解压到临时目录
    with zipfile.ZipFile(source_jar, 'r') as zip_ref:
        zip_ref.extractall(temp_dir)

    # 修改 MANIFEST.MF
    manifest_path = os.path.join(temp_dir, "META-INF", "MANIFEST.MF")
    if os.path.exists(manifest_path):
        with open(manifest_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        new_lines = []
        for line in lines:
            if line.startswith("Start-Class:"):
                new_lines.append(f"Start-Class: {start_class}\n")
            else:
                new_lines.append(line)

        with open(manifest_path, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)

        print(f"✓ 已修改 MANIFEST.MF 的 Start-Class 为: {start_class}")

    # 删除旧的类目录
    for old_dir in old_classes_dirs:
        dir_path = os.path.join(temp_dir, "BOOT-INF", "classes", old_dir.replace('.', os.sep))
        if os.path.exists(dir_path):
            shutil.rmtree(dir_path)
            print(f"✓ 已删除 {old_dir}")

    # 删除旧的 mapper 文件
    mapper_dir = os.path.join(temp_dir, "BOOT-INF", "classes", "mapper")
    for old_mapper in old_mappers:
        mapper_path = os.path.join(mapper_dir, old_mapper)
        if os.path.exists(mapper_path):
            os.remove(mapper_path)
            print(f"✓ 已删除 {old_mapper}")

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

    # 删除旧的目标 jar
    if os.path.exists(target_jar):
        os.remove(target_jar)

    # 重新打包
    with zipfile.ZipFile(target_jar, 'w', zipfile.ZIP_DEFLATED) as zip_ref:
        for root, dirs, files in os.walk(temp_dir):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, temp_dir)
                zip_ref.write(file_path, arcname)

    # 清理临时目录
    shutil.rmtree(temp_dir)

    print(f"✓ 已创建 {target_jar}")
    return True

# 修复 user-service
fix_jar(
    source_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/activity-service/target/activity-service-2.0.0.jar",
    target_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service/target/user-service-2.0.0.jar",
    start_class="com.dzvote.user.UserApplication",
    old_classes_dirs=["com.dzvote.activity", "com.dzvote.candidate"],
    old_mappers=["ActivityMapper.xml", "CandidateMapper.xml"],
    new_classes_src=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service/target/classes"
)

print("\n" + "="*50)

# 修复 vote-service
fix_jar(
    source_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/activity-service/target/activity-service-2.0.0.jar",
    target_jar=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/vote-service-2.0.0.jar",
    start_class="com.dzvote.vote.VoteServiceApplication",
    old_classes_dirs=["com.dzvote.activity", "com.dzvote.candidate", "com.dzvote.user"],
    old_mappers=["ActivityMapper.xml", "CandidateMapper.xml", "UserMapper.xml"],
    new_classes_src=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/classes"
)
