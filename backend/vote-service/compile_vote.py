import os
import subprocess
import shutil

# 编译 vote-service
vote_service_dir = r"d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/backend/vote-service"
src_dir = os.path.join(vote_service_dir, "src", "main", "java")
target_dir = os.path.join(vote_service_dir, "target", "classes")

# 创建目标目录
os.makedirs(target_dir, exist_ok=True)

# 添加资源文件
resources_dir = os.path.join(vote_service_dir, "src", "main", "resources")
if os.path.exists(resources_dir):
    for root, dirs, files in os.walk(resources_dir):
        for file in files:
            src_file = os.path.join(root, file)
            rel_path = os.path.relpath(src_file, resources_dir)
            dst_file = os.path.join(target_dir, rel_path)
            os.makedirs(os.path.dirname(dst_file), exist_ok=True)
            shutil.copy2(src_file, dst_file)

print("vote-service 的资源文件已复制")

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
]

classpath_str = ";".join(classpath)

# 收集所有 Java 文件
java_files = []
for root, dirs, files in os.walk(src_dir):
    for file in files:
        if file.endswith(".java"):
            java_files.append(os.path.join(root, file))

print(f"找到 {len(java_files)} 个 Java 文件")

# 编译所有 Java 文件
if java_files:
    javac_cmd = [
        r"D:/ide/Java/java17/bin/javac.exe",
        "-encoding", "UTF-8",
        "-d", target_dir,
        "-cp", classpath_str
    ] + java_files

    print("开始编译...")
    result = subprocess.run(javac_cmd, capture_output=True)

    if result.returncode != 0:
        print("编译失败:")
        # 写入文件以避免编码问题
        error_file = os.path.join(vote_service_dir, "compile_error.log")
        with open(error_file, 'w', encoding='utf-8') as f:
            f.write("STDERR:\n")
            if result.stderr:
                f.write(result.stderr.decode('utf-8', errors='replace'))
            f.write("\nSTDOUT:\n")
            if result.stdout:
                f.write(result.stdout.decode('utf-8', errors='replace'))
        print(f"详细错误已写入: {error_file}")
    else:
        print("编译成功!")
        print(f"生成的类文件位于: {target_dir}")
else:
    print("没有找到 Java 文件")
