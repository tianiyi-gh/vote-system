# 防刷票安全功能实施指南

## 概述

本文档介绍投票系统的防刷票安全功能，包括短信/邮箱验证、IP黑名单管理、设备指纹识别和异常投票检测。

## 功能清单

### ✅ 已实现功能

1. **短信验证码** - 通过短信发送验证码进行身份验证
2. **邮箱验证码** - 通过邮箱发送验证码进行身份验证
3. **IP黑名单管理** - 管理和封禁恶意IP地址
4. **设备指纹识别** - 生成和识别设备指纹，防止多账号刷票
5. **异常投票检测** - 自动检测异常投票行为

## 技术架构

### 实体类

#### VerificationCode（验证码）
```java
- id: 主键
- type: 类型（SMS/EMAIL）
- target: 接收地址（手机号/邮箱）
- code: 验证码
- purpose: 用途（VOTE/LOGIN/REGISTER）
- verified: 是否已验证
- used: 是否已使用
- expireTime: 过期时间
- createTime: 创建时间
- verifyTime: 验证时间
- ipAddress: IP地址
- deviceFingerprint: 设备指纹
```

#### IpBlacklist（IP黑名单）
```java
- id: 主键
- ipAddress: IP地址
- reason: 封禁原因
- type: 封禁类型（PERMANENT/TEMPORARY）
- startTime: 封禁开始时间
- endTime: 封禁结束时间
- status: 封禁状态（0-已解除 1-生效中）
- operator: 操作员
- createTime: 创建时间
- remark: 备注
```

### API接口

#### 1. 验证码相关

##### 发送短信验证码
```
POST /api/security/sms/send
参数：
  - phoneNumber: 手机号
  - purpose: 用途（可选，默认VOTE）
返回：
  - success: true/false
  - message: 提示信息
```

##### 发送邮箱验证码
```
POST /api/security/email/send
参数：
  - emailAddress: 邮箱地址
  - purpose: 用途（可选，默认VOTE）
返回：
  - success: true/false
  - message: 提示信息
```

##### 验证验证码
```
POST /api/security/code/verify
参数（JSON）：
  - codeId: 验证码ID
  - code: 验证码
  - target: 接收目标（手机号/邮箱）
  - purpose: 用途
返回：
  - success: true/false
  - data: 验证结果
```

##### 获取验证码发送统计
```
GET /api/security/code/stats
参数：
  - hours: 统计小时数（可选，默认1）
返回：
  - success: true/false
  - data: 验证码统计列表
```

#### 2. IP黑名单相关

##### 检查IP是否被封禁
```
GET /api/security/ip/check
参数：
  - ipAddress: IP地址
返回：
  - success: true/false
  - data: 是否被封禁（true/false）
```

##### 添加IP到黑名单
```
POST /api/security/ip/blacklist
参数（JSON）：
  - ipAddress: IP地址
  - reason: 封禁原因（可选）
  - type: 封禁类型（PERMANENT/TEMPORARY，可选，默认TEMPORARY）
  - hours: 封禁时长（仅临时封禁，可选，默认24小时）
  - operator: 操作员（可选，默认admin）
  - remark: 备注（可选）
返回：
  - success: true/false
  - message: 提示信息
```

##### 解封IP
```
POST /api/security/ip/unblock
参数：
  - id: 黑名单记录ID
返回：
  - success: true/false
  - message: 提示信息
```

##### 获取所有黑名单
```
GET /api/security/ip/blacklist
返回：
  - success: true/false
  - data: 黑名单列表
```

#### 3. 设备指纹相关

##### 生成设备指纹
```
POST /api/security/device/fingerprint
参数（JSON）：
  - userAgent: User-Agent
  - acceptLanguage: Accept-Language
  - screenResolution: 屏幕分辨率
  - timezone: 时区
返回：
  - success: true/false
  - data: 设备指纹字符串
```

##### 获取设备使用统计
```
GET /api/security/device/stats
返回：
  - success: true/false
  - data: 设备统计列表
```

#### 4. 异常检测相关

##### 检测异常投票
```
POST /api/security/fraud/detect
参数（JSON）：
  - activityId: 活动ID
  - ipAddress: IP地址
  - deviceFingerprint: 设备指纹
返回：
  - success: true/false
  - data: {
      suspicious: true/false,
      fraudType: "HIGH_FREQUENCY_IP" | "HIGH_FREQUENCY_DEVICE" | "RASH_VOTING" | "SUSPICIOUS_DEVICE" | "AUTOMATED_VOTING",
      riskLevel: "LOW" | "MEDIUM" | "HIGH",
      description: "异常描述"
    }
```

## 异常检测规则

### 1. IP短时间高频投票
- 规则：1分钟内同一IP投票超过10次
- 风险等级：HIGH
- 处理方式：标记异常，建议封禁IP

### 2. 设备短时间高频投票
- 规则：1分钟内同一设备投票超过10次
- 风险等级：HIGH
- 处理方式：标记异常，记录设备指纹

### 3. 冲动投票
- 规则：1分钟内为5个以上不同候选人投票
- 风险等级：MEDIUM
- 处理方式：标记异常，人工审核

### 4. 设备指纹异常
- 规则：检测到机器人/爬虫User-Agent
- 风险等级：MEDIUM
- 处理方式：标记异常，拒绝投票

### 5. 自动化投票
- 规则：每秒至少1票（精确时间间隔）
- 风险等级：HIGH
- 处理方式：标记异常，封禁IP和设备

## 数据库表结构

### verification_codes（验证码表）
```sql
CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(10) NOT NULL COMMENT '接收方式：SMS/EMAIL',
    target VARCHAR(100) NOT NULL COMMENT '接收地址：手机号/邮箱',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    purpose VARCHAR(20) NOT NULL COMMENT '验证码用途：VOTE/LOGIN/REGISTER',
    verified TINYINT DEFAULT 0 COMMENT '是否已验证：0-未验证 1-已验证',
    used TINYINT DEFAULT 0 COMMENT '是否已使用：0-未使用 1-已使用',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    verify_time DATETIME COMMENT '验证时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    device_fingerprint VARCHAR(50) COMMENT '设备指纹',
    INDEX idx_target (target),
    INDEX idx_create_time (create_time),
    INDEX idx_expire_time (expire_time),
    INDEX idx_ip_device (ip_address, device_fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### ip_blacklist（IP黑名单表）
```sql
CREATE TABLE IF NOT EXISTS ip_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(50) NOT NULL COMMENT 'IP地址',
    reason VARCHAR(200) COMMENT '封禁原因',
    type VARCHAR(20) NOT NULL DEFAULT 'TEMPORARY' COMMENT '封禁类型：PERMANENT/TEMPORARY',
    start_time DATETIME NOT NULL COMMENT '封禁开始时间',
    end_time DATETIME COMMENT '封禁结束时间',
    status TINYINT DEFAULT 1 COMMENT '封禁状态：0-已解除 1-生效中',
    operator VARCHAR(50) COMMENT '操作员',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    remark VARCHAR(500) COMMENT '备注',
    UNIQUE KEY uk_ip (ip_address, start_time),
    INDEX idx_status (status),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### vote_records扩展字段
```sql
ALTER TABLE vote_records
ADD COLUMN device_fingerprint VARCHAR(50) COMMENT '设备指纹',
ADD COLUMN location VARCHAR(200) COMMENT '位置信息',
ADD INDEX idx_device_fingerprint (device_fingerprint);
```

## 部署步骤

### 1. 数据库初始化
```bash
# 执行SQL脚本
mysql -u root -p dzvote < 新架构/sql/security_tables.sql
```

### 2. 编译打包
```bash
cd 新架构/backend/vote-service
mvn clean package -DskipTests
```

### 3. 启动服务
```bash
# 查看JAR包
ls target/*.jar

# 启动服务（使用实际生成的JAR文件名）
java -jar target/vote-service.jar --server.port=8082
```

### 4. 验证接口
```bash
# 测试发送短信验证码（模拟）
curl -X POST http://localhost:8082/api/security/sms/send \
  -d "phoneNumber=13800138000&purpose=VOTE"

# 测试检查IP
curl http://localhost:8082/api/security/ip/check?ipAddress=127.0.0.1

# 测试异常检测
curl -X POST http://localhost:8082/api/security/fraud/detect \
  -H "Content-Type: application/json" \
  -d '{"activityId": 1, "ipAddress": "127.0.0.1", "deviceFingerprint": "abc123"}'
```

## 集成说明

### 短信服务集成

当前使用模拟版本，实际使用时需要接入短信服务商：

#### 阿里云短信集成
```java
// pom.xml 添加依赖
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>aliyun-java-sdk-core</artifactId>
    <version>4.5.0</version>
</dependency>
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>aliyun-java-sdk-dysmsapi</artifactId>
    <version>2.1.0</version>
</dependency>

// SmsServiceImpl.java 替换实现
DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
IAcsClient client = new DefaultAcsClient(profile);
SendSmsRequest request = new SendSmsRequest();
request.setPhoneNumbers(phoneNumber);
request.setSignName("投票系统");
request.setTemplateCode("SMS_xxxxxxxx");
request.setTemplateParam("{\"code\":\"" + code + "\"}");
SendSmsResponse response = client.getAcsResponse(request);
return "OK".equals(response.getCode());
```

#### 腾讯云短信集成
```java
// pom.xml 添加依赖
<dependency>
    <groupId>com.tencentcloudapi</groupId>
    <artifactId>tencentcloud-sdk-java</artifactId>
    <version>3.1.0</version>
</dependency>
```

### 邮箱服务集成

当前使用模拟版本，实际使用时需要配置JavaMail：

```properties
# application.properties
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

```java
// EmailServiceImpl.java 替换实现
@Autowired
private JavaMailSender mailSender;

public boolean sendVerificationCode(String emailAddress, String code) {
    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailAddress);
        message.setSubject("验证码");
        message.setText("您的验证码是：" + code + "，有效时间5分钟");
        mailSender.send(message);
        return true;
    } catch (Exception e) {
        log.error("发送邮件失败", e);
        return false;
    }
}
```

## 安全建议

### 1. 验证码策略
- 验证码长度：6位数字
- 有效时间：5分钟
- 发送频率限制：1小时最多10次
- 验证次数限制：每个验证码最多验证3次

### 2. IP黑名单策略
- 自动封禁：检测到HIGH风险等级时自动封禁24小时
- 手动封禁：管理员可手动封禁IP，选择永久或临时
- 定期清理：定期清理已过期的临时封禁记录

### 3. 设备指纹策略
- 指纹有效期：30天
- 设备限制：同一设备每天最多投票50次
- 指纹更新：检测到设备特征变化时更新指纹

### 4. 异常检测策略
- 实时检测：每次投票前进行异常检测
- 人工审核：MEDIUM风险等级需要人工审核
- 自动拒绝：HIGH风险等级自动拒绝投票

## 监控告警

### 关键指标
- 验证码发送成功率
- 验证码验证通过率
- IP黑名单新增数量
- 异常投票检测次数
- 异常投票拦截率

### 告警规则
- 验证码发送失败率超过10% → 立即告警
- 异常投票检测次数超过100次/小时 → 告警
- IP黑名单新增超过10个/小时 → 告警
- 设备指纹异常超过50次/小时 → 告警

## 文件清单

### 新增文件
```
新架构/backend/vote-service/src/main/java/com/dzvote/vote/
├── entity/
│   ├── VerificationCode.java          # 验证码实体
│   └── IpBlacklist.java             # IP黑名单实体
├── mapper/
│   ├── VerificationCodeMapper.java    # 验证码Mapper
│   └── IpBlacklistMapper.java       # IP黑名单Mapper
├── service/
│   ├── SmsService.java              # 短信服务接口
│   ├── EmailService.java            # 邮箱服务接口
│   ├── VerificationCodeService.java  # 验证码服务接口
│   ├── IpBlacklistService.java      # IP黑名单服务接口
│   ├── DeviceFingerprintService.java # 设备指纹服务接口
│   ├── FraudDetectionService.java    # 异常检测服务接口
│   └── impl/
│       ├── SmsServiceImpl.java       # 短信服务实现
│       ├── EmailServiceImpl.java     # 邮箱服务实现
│       ├── VerificationCodeServiceImpl.java # 验证码服务实现
│       ├── IpBlacklistServiceImpl.java # IP黑名单服务实现
│       ├── DeviceFingerprintServiceImpl.java # 设备指纹服务实现
│       └── FraudDetectionServiceImpl.java # 异常检测服务实现
├── controller/
│   └── SecurityController.java      # 安全防护控制器
├── dto/
│   └── VerifyCodeRequest.java       # 验证码请求DTO
新架构/sql/
└── security_tables.sql              # 安全相关表结构SQL
```

### 修改文件
```
新架构/backend/vote-service/src/main/java/com/dzvote/vote/
└── mapper/
    └── VoteRecordMapper.java       # 新增异常检测相关查询方法
```

## 测试建议

### 单元测试
- 验证码生成和验证
- IP黑名单添加和检查
- 设备指纹生成和验证
- 异常投票检测逻辑

### 集成测试
- 短信验证码发送流程
- 邮箱验证码发送流程
- IP封禁和解封流程
- 异常投票检测和拦截

### 性能测试
- 高并发验证码发送
- 高并发投票场景
- 异常检测响应时间

## 常见问题

### Q1: 验证码发送失败怎么办？
A: 检查短信/邮箱服务配置，确认API密钥正确，检查网络连接。

### Q2: IP黑名单不生效？
A: 确认IP地址格式正确，检查status字段是否为1（生效中），检查时间是否在有效期内。

### Q3: 设备指纹不唯一？
A: 确保收集足够的设备特征信息（User-Agent、屏幕分辨率、时区等），避免使用隐私模式浏览器。

### Q4: 异常检测误报怎么办？
A: 可以调整检测阈值，针对不同场景设置不同的检测规则，增加白名单机制。

## 下一步计划

- [ ] 实现验证码图片（CAPTCHA）
- [ ] 实现地理位置识别
- [ ] 实现行为分析（鼠标轨迹、键盘节奏）
- [ ] 实现机器学习模型进行异常检测
- [ ] 实现实时风控看板
- [ ] 实现风险评分系统
