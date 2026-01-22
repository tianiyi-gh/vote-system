import zipfile
import os
import shutil

# 编译修改的类
vote_service_dir = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service"
target_jar = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service/target/vote-service-2.0.0.jar"

# 设置类路径
classpath = [
    r"D:/ide/maven3.9/repository/org/springframework/boot/spring-boot/3.2.0/spring-boot-3.2.0.jar",
    r"D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-autoconfigure/3.2.0/spring-boot-autoconfigure-3.2.0.jar",
    r"D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-web/3.2.0/spring-boot-starter-web-3.2.0.jar",
    r"D:/ide/maven3.9/repository/org/springframework/spring-web/6.1.1/spring-web-6.1.1.jar",
    r"D:/ide/maven3.9/repository/org/springframework/spring-context/6.1.1/spring-context-6.1.1.jar",
    r"D:/ide/maven3.9/repository/org/springframework/spring-core/6.1.1/spring-core-6.1.1.jar",
    r"D:/ide/maven3.9/repository/org/springframework/spring-beans/6.1.1/spring-beans-6.1.1.jar",
    r"D:/ide/maven3.9/repository/org/springframework/spring-webmvc/6.1.1/spring-webmvc-6.1.1.jar",
    r"D:/ide/maven3.9/repository/com/fasterxml/jackson/core/jackson-databind/2.15.3/jackson-databind-2.15.3.jar",
    r"D:/ide/maven3.9/repository/org/mybatis/spring/boot/mybatis-spring-boot-starter/3.0.3/mybatis-spring-boot-starter-3.0.3.jar",
    r"D:/ide/maven3.9/repository/org/mybatis/mybatis-spring/3.0.3/mybatis-spring-3.0.3.jar",
    r"D:/ide/maven3.9/repository/org/mybatis/mybatis/3.5.13/mybatis-3.5.13.jar",
    r"D:/ide/maven3.9/repository/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar",
    r"D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-data-redis/3.2.0/spring-boot-starter-data-redis-3.2.0.jar",
    r"D:/ide/maven3.9/repository/org/springframework/data/spring-data-redis/3.2.0/spring-data-redis-3.2.0.jar",
    r"D:/ide/maven3.9/repository/io/lettuce/lettuce-core/6.2.6.RELEASE/lettuce-core-6.2.6.RELEASE.jar",
    r"D:/ide/maven3.9/repository/jakarta/validation/jakarta.validation-api/3.0.2/jakarta.validation-api-3.0.2.jar",
    r"D:/ide/maven3.9/repository/org/springframework/boot/spring-boot-starter-validation/3.2.0/spring-boot-starter-validation-3.2.0.jar",
    r"D:/ide/maven3.9/repository/org/springdoc/springdoc-openapi-starter-webmvc-ui/2.2.0/springdoc-openapi-starter-webmvc-ui-2.2.0.jar",
    r"D:/ide/maven3.9/repository/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar",
    target_jar
]

classpath_str = ";".join(classpath)
src_dir = os.path.join(vote_service_dir, "src", "main", "java")
target_classes_dir = os.path.join(vote_service_dir, "target", "classes")

# 需要编译的文件
files_to_compile = [
    os.path.join(src_dir, "com", "dzvote", "vote", "controller", "AdminController.java"),
    os.path.join(src_dir, "com", "dzvote", "vote", "mapper", "VoteRecordMapper.java"),
]

print("开始编译修改的类...")

for java_file in files_to_compile:
    if os.path.exists(java_file):
        javac_cmd = [
            r"D:/ide/Java/java17/bin/javac.exe",
            "-encoding", "UTF-8",
            "-d", target_classes_dir,
            "-cp", classpath_str,
            java_file
        ]
        result = os.system(' '.join(f'"{p}"' if ' ' in p else p for p in javac_cmd))
        if result == 0:
            print(f"✓ 编译成功: {os.path.basename(java_file)}")
        else:
            print(f"✗ 编译失败: {os.path.basename(java_file)}")

print("\n更新jar文件...")

# 删除jar中的旧类
with zipfile.ZipFile(target_jar, 'r') as zip_ref:
    files_in_jar = zip_ref.namelist()

# 提取并更新jar
temp_dir = os.path.join(vote_service_dir, "target", "temp_update")
if os.path.exists(temp_dir):
    shutil.rmtree(temp_dir)
os.makedirs(temp_dir)

# 解压jar到临时目录
with zipfile.ZipFile(target_jar, 'r') as zip_ref:
    zip_ref.extractall(temp_dir)

# 替换类文件
updated_classes = [
    "BOOT-INF/classes/com/dzvote/vote/controller/AdminController.class",
    "BOOT-INF/classes/com/dzvote/vote/mapper/VoteRecordMapper.class",
]

for class_file in updated_classes:
    src_class = os.path.join(target_classes_dir, *class_file.split('/')[3:])
    if os.path.exists(src_class):
        dst_class = os.path.join(temp_dir, *class_file.split('/'))
        shutil.copy2(src_class, dst_class)
        print(f"✓ 更新: {class_file}")

# 重新打包jar
new_jar = target_jar + ".new"
with zipfile.ZipFile(new_jar, 'w') as zip_ref:
    for root, dirs, files in os.walk(temp_dir):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, temp_dir)
            zip_ref.write(file_path, arcname)

# 删除旧jar并重命名新jar
if os.path.exists(target_jar):
    os.remove(target_jar)
os.rename(new_jar, target_jar)

# 删除临时目录
shutil.rmtree(temp_dir)

print(f"\n✓ 成功更新 {target_jar}")
