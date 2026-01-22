import zipfile
import shutil
import os
import tempfile

def fix_jar_manifest(jar_path, start_class, temp_class_path):
    """修复 jar 文件的 MANIFEST.MF"""

    # 创建临时工作目录
    with tempfile.TemporaryDirectory() as temp_dir:
        print(f"解压 {jar_path}...")

        # 解压 jar
        with zipfile.ZipFile(jar_path, 'r') as z:
            z.extractall(temp_dir)

        # 修改 MANIFEST.MF
        manifest_path = os.path.join(temp_dir, "META-INF", "MANIFEST.MF")
        with open(manifest_path, 'rb') as f:
            manifest_bytes = f.read()

        # 转换为字符串
        manifest_str = manifest_bytes.decode('utf-8')

        # 替换 Start-Class
        lines = manifest_str.split('\n')
        new_lines = []
        for line in lines:
            if line.startswith('Start-Class:'):
                new_lines.append(f'Start-Class: {start_class}\r\n')
            elif line.startswith('Implementation-Title:'):
                new_lines.append(f'Implementation-Title: {start_class.split(".")[-1]}\r\n')
            else:
                new_lines.append(line + '\r\n' if not line.endswith('\r\n') else line)

        # 写回 MANIFEST.MF
        with open(manifest_path, 'wb') as f:
            f.write(''.join(new_lines).encode('utf-8'))

        print(f"✓ 已设置 Start-Class 为: {start_class}")

        # 删除旧 jar
        os.remove(jar_path)

        # 重新打包（使用 DEFLATED 压缩）
        print(f"重新打包 {jar_path}...")
        with zipfile.ZipFile(jar_path, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as z:
            for root, dirs, files in os.walk(temp_dir):
                for file in files:
                    file_path = os.path.join(root, file)
                    arcname = os.path.relpath(file_path, temp_dir)
                    z.write(file_path, arcname)

    # 验证
    with zipfile.ZipFile(jar_path, 'r') as z:
        manifest = z.read('META-INF/MANIFEST.MF').decode('utf-8')
        for line in manifest.split('\n'):
            if line.startswith('Start-Class:'):
                print(f"✓ 验证: {line.strip()}")
                break

    print(f"✓ 成功修复 {jar_path}")

# 修复 user-service
print("="*60)
print("修复 user-service")
print("="*60)
fix_jar_manifest(
    jar_path=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/user-service/target/user-service-2.0.0.jar",
    start_class="com.dzvote.user.UserApplication",
    temp_class_path=None
)

print("\n" + "="*60)
print("修复 vote-service")
print("="*60)
fix_jar_manifest(
    jar_path=r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/vote-service-2.0.0.jar",
    start_class="com.dzvote.vote.VoteServiceApplication",
    temp_class_path=None
)

print("\n" + "="*60)
print("所有 jar 文件已修复！")
print("="*60)
