# Java 17 安装指南（Windows）

## 📥 下载Java 17

### 方式一：Eclipse Temurin（推荐）

**官方下载地址**: https://adoptium.net/temurin/releases/

1. 访问上述网站
2. 选择配置：
   - **Version**: 17 - LTS
   - **Operating System**: Windows
   - **Architecture**: x64
   - **Package Type**: JDK
   - **File Type**: .msi (推荐) 或 .zip

3. 点击下载 `.msi` 安装包

**直接下载链接**:
- https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.msi

### 方式二：Oracle JDK 17

**官方下载地址**: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

需要登录Oracle账号（免费注册）

### 方式三：使用Chocolatey（命令行安装）

如果已安装Chocolatey，可以用命令安装：

```powershell
# 以管理员身份运行PowerShell
choco install temurin17
```

---

## 🔧 安装步骤（使用.msi安装包）

### 1. 运行安装程序

双击下载的 `.msi` 文件

### 2. 安装向导

- ✅ **欢迎界面**: 点击 `Next`
- ✅ **许可协议**: 勾选同意，点击 `Next`
- ✅ **安装路径**: 
  - 默认: `C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot\`
  - 或自定义路径（记住此路径！）
- ✅ **功能选择**: 
  - ☑️ **Set JAVA_HOME variable** （重要！）
  - ☑️ **Add to PATH**
  - ☑️ **Associate .jar**
- ✅ **安装**: 点击 `Install`
- ✅ **完成**: 点击 `Finish`

---

## ⚙️ 手动配置环境变量（如果安装时未勾选）

### 1. 设置JAVA_HOME

**步骤**:

1. **打开系统属性**
   - 按 `Win + R`
   - 输入 `sysdm.cpl`
   - 回车

2. **进入环境变量**
   - 点击 `高级` 选项卡
   - 点击 `环境变量` 按钮

3. **新建系统变量**
   - 在 "系统变量" 区域点击 `新建`
   - 变量名: `JAVA_HOME`
   - 变量值: `C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot`
   - （根据实际安装路径调整）
   - 点击 `确定`

### 2. 添加到PATH

1. **编辑PATH变量**
   - 在 "系统变量" 中找到 `Path`
   - 点击 `编辑`

2. **添加JDK路径**
   - 点击 `新建`
   - 输入: `%JAVA_HOME%\bin`
   - 点击 `确定`

3. **保存**
   - 点击所有 `确定` 按钮关闭窗口

---

## ✅ 验证安装

### 打开新的PowerShell窗口

**重要**: 必须打开新窗口，环境变量才会生效！

```powershell
# 检查Java版本
java -version

# 应该看到类似输出：
# openjdk version "17.0.9" 2023-10-17
# OpenJDK Runtime Environment Temurin-17.0.9+9 (build 17.0.9+9)
# OpenJDK 64-Bit Server VM Temurin-17.0.9+9 (build 17.0.9+9, mixed mode, sharing)

# 检查javac编译器
javac -version

# 应该看到：
# javac 17.0.9

# 检查JAVA_HOME
echo $env:JAVA_HOME

# 应该看到：
# C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot
```

---

## 🔄 管理多个Java版本

### 方式一：手动切换（推荐新手）

创建切换脚本：

**切换到Java 17**: `use-java17.bat`
```batch
@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
echo Switched to Java 17
java -version
```

**切换到Java 8**: `use-java8.bat`
```batch
@echo off
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_152
set PATH=%JAVA_HOME%\bin;%PATH%
echo Switched to Java 8
java -version
```

使用时运行对应的bat文件即可。

### 方式二：使用jEnv（类Unix工具）

安装Git Bash后可使用jEnv管理多版本。

### 方式三：使用SDKMAN（推荐高级用户）

在WSL或Git Bash中使用。

---

## 🚀 安装后配置项目

### 1. 还原Java 17配置

安装完Java 17后，修改项目POM文件：

```xml
<!-- 编辑: 新架构/backend/pom.xml -->
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <spring-boot.version>3.1.0</spring-boot.version>
    <spring-cloud.version>2022.0.3</spring-cloud.version>
    <swagger.version>2.2.0</swagger.version>
</properties>
```

### 2. 还原依赖

```xml
<!-- Swagger改回SpringDoc -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${swagger.version}</version>
</dependency>
```

### 3. 还原代码

- `jakarta.*` 替换回 `javax.*`
- `@Api` → `@Tag`
- `@ApiOperation` → `@Operation`

### 4. 重新构建

```powershell
cd d:\ide\toupiao\ROOT_CodeBuddy\新架构\backend
mvn clean install -DskipTests
```

---

## 📋 快速安装命令总结

### 使用PowerShell（管理员）

```powershell
# 1. 使用Chocolatey安装（如果已安装choco）
choco install temurin17

# 2. 或手动下载安装
# 下载: https://adoptium.net/temurin/releases/
# 运行.msi文件，勾选 "Set JAVA_HOME" 和 "Add to PATH"

# 3. 验证
java -version
javac -version
echo $env:JAVA_HOME

# 4. 如果显示正确版本，安装成功！
```

---

## 🔧 常见问题

### Q1: java -version 仍显示Java 8

**A1**: 环境变量未生效

解决方案：
```powershell
# 1. 重启PowerShell窗口
# 2. 或手动设置当前会话
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```

### Q2: javac不是内部命令

**A2**: PATH未正确设置

解决方案：
```powershell
# 检查PATH是否包含JDK bin目录
$env:Path -split ';' | Select-String -Pattern 'java'

# 如果没有，手动添加
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

### Q3: JAVA_HOME未设置

**A3**: 
```powershell
# 临时设置（当前会话）
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot"

# 永久设置（需要管理员权限）
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot", "Machine")
```

### Q4: 安装后IDE仍使用Java 8

**A4**: 需要配置IDE

**IDEA**:
- File → Project Structure → Project SDK → Add SDK → 选择Java 17路径
- File → Settings → Build, Execution, Deployment → Build Tools → Maven → Runner → JRE → 选择Java 17

**Eclipse**:
- Window → Preferences → Java → Installed JREs → Add → Standard VM → 选择Java 17路径

**VS Code**:
- 设置 `java.home` 或 `java.configuration.runtimes`

---

## 📦 完整安装脚本

创建 `install-java17.ps1`:

```powershell
# Java 17 安装脚本
# 以管理员身份运行

Write-Host "开始安装Java 17..." -ForegroundColor Green

# 检查是否安装Chocolatey
if (Get-Command choco -ErrorAction SilentlyContinue) {
    Write-Host "使用Chocolatey安装Java 17..." -ForegroundColor Yellow
    choco install temurin17 -y
} else {
    Write-Host "未检测到Chocolatey" -ForegroundColor Yellow
    Write-Host "请手动下载安装：https://adoptium.net/temurin/releases/" -ForegroundColor Cyan
    Start-Process "https://adoptium.net/temurin/releases/"
    exit
}

# 验证安装
Write-Host "`n验证安装..." -ForegroundColor Green
java -version
javac -version

Write-Host "`nJAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan

Write-Host "`n安装完成！请重启IDE或终端" -ForegroundColor Green
```

---

## 🎯 安装后检查清单

- [ ] `java -version` 显示 17.x.x
- [ ] `javac -version` 显示 17.x.x
- [ ] `echo $env:JAVA_HOME` 显示正确路径
- [ ] Maven项目可以正常编译
- [ ] IDE识别到Java 17

---

## 📞 需要帮助？

安装过程中遇到问题，提供以下信息：

```powershell
# 收集系统信息
java -version
javac -version
echo $env:JAVA_HOME
echo $env:Path
mvn -version
```

---

**准备好安装了吗？** 🚀

1. 下载Java 17: https://adoptium.net/temurin/releases/
2. 运行安装程序
3. 勾选设置环境变量选项
4. 验证安装
5. 重新构建项目

祝安装顺利！
