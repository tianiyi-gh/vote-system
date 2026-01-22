# ⚠️ Java 8 兼容性说明

## 检测到您的环境

- **JDK版本**: Java 1.8.0_152
- **已自动调整**: 项目已适配Java 8

## 🔄 已完成的调整

### 1. Spring Boot版本降级
- Spring Boot: 3.1.0 → **2.7.14**
- Spring Cloud: 2022.0.3 → **2021.0.8**
- Spring Cloud Alibaba: 2022.0.0.0 → **2021.0.5.0**

### 2. API调整
- `jakarta.*` → `javax.*`
- SpringDoc OpenAPI → Springfox Swagger 3.0

### 3. 编译配置
```xml
<java.version>1.8</java.version>
<maven.compiler.source>1.8</maven.compiler.source>
<maven.compiler.target>1.8</maven.compiler.target>
```

## 🚀 快速启动（针对Java 8）

### 步骤1：构建项目

```powershell
# 方法1：使用构建脚本
cd d:\ide\toupiao\ROOT_CodeBuddy\新架构
.\build.bat

# 方法2：手动构建
cd d:\ide\toupiao\ROOT_CodeBuddy\新架构\backend
mvn clean install -DskipTests
```

### 步骤2：启动后端

```powershell
# 方法1：使用Maven
cd d:\ide\toupiao\ROOT_CodeBuddy\新架构\backend\activity-service
mvn spring-boot:run

# 方法2：直接运行JAR
cd d:\ide\toupiao\ROOT_CodeBuddy\新架构\backend\activity-service\target
java -jar activity-service-2.0.0.jar
```

### 步骤3：启动前端

```powershell
cd d:\ide\toupiao\ROOT_CodeBuddy\新架构\frontend\admin
npm install
npm run dev
```

## 📱 访问地址

- **管理后台**: http://localhost:3000
- **Swagger文档**: http://localhost:8081/swagger-ui/
- **API测试**: http://localhost:8081/api/activities

## ⚠️ 注意事项

### Swagger访问地址变化

**Java 8版本（Springfox）**:
- http://localhost:8081/swagger-ui/
- http://localhost:8081/swagger-ui/index.html

**Java 17版本（SpringDoc）**:
- http://localhost:8081/swagger-ui.html

### 兼容性说明

| 功能 | Java 8 | Java 17 |
|------|--------|---------|
| Spring Boot | 2.7.x | 3.x |
| jakarta.* | ❌ | ✅ |
| javax.* | ✅ | ❌ |
| Record类型 | ❌ | ✅ |
| Text Blocks | ❌ | ✅ |
| var关键字 | ❌ | ✅ |

## 🔧 故障排查

### Q: 构建失败提示"无效的目标发行版: 17"

**A:** 已修复。项目已调整为Java 8兼容。重新执行：
```bash
cd d:\ide\toupiao\ROOT_CodeBuddy\新架构\backend
mvn clean install -DskipTests
```

### Q: Swagger页面404

**A:** 使用正确的URL:
- ✅ http://localhost:8081/swagger-ui/
- ❌ http://localhost:8081/swagger-ui.html

### Q: 启动报错"Unsupported class file major version"

**A:** 清理并重新编译：
```bash
mvn clean
mvn install -DskipTests
```

## 🆙 升级建议

### 推荐升级JDK（可选）

虽然已适配Java 8，但建议升级到Java 11或17以获得更好的性能和特性：

**下载地址**:
- OpenJDK 11: https://adoptium.net/
- OpenJDK 17: https://adoptium.net/

**升级后的优势**:
- 更好的性能（GC优化）
- 更多语言特性（Record、Text Blocks等）
- 更新的Spring Boot 3.x支持
- 更长的LTS支持周期

## 📝 当前配置

### pom.xml 关键配置

```xml
<properties>
    <java.version>1.8</java.version>
    <spring-boot.version>2.7.14</spring-boot.version>
</properties>
```

### Swagger配置

```java
@Configuration
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.dzvote.activity.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
```

## ✅ 验证安装

```bash
# 检查Java版本
java -version
# 应显示: java version "1.8.0_xxx"

# 检查Maven版本
mvn -version

# 验证构建
mvn clean compile
```

---

**所有调整已完成，可以正常使用！** 🎉
