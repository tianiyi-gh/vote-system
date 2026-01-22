# 前台H5投票功能完善说明

## 功能概述
前台H5投票页面已完成投票验证码和投票限制功能的集成，现在可以正确调用后端投票API。

## 完善内容

### 1. 验证码功能
- **验证码生成**：从后端 `/api/captcha` 接口获取验证码
- **验证码验证**：投票时携带 `captcha` 和 `captchaKey` 参数
- **验证码刷新**：投票失败时自动刷新验证码
- **错误提示**：验证码错误时显示友好提示

### 2. 投票限制检查
后端已实现完整的投票限制逻辑：

#### 2.1 活动配置限制
- **vote_limit**: 每人投票限制（默认10票）
- **ip_limit**: IP投票限制（每个IP的投票次数限制）
- **enable_captcha**: 是否启用验证码（1=启用，0=禁用）

#### 2.2 投票验证流程
```
1. 验证投票有效性
   - 检查渠道有效性（SMS/IVR/WEB/APP/WECHAT/PAY）

2. 验证验证码（如果活动启用）
   - 从Redis获取验证码并验证
   - 验证失败返回错误提示

3. 检查每人投票限制
   - 基于Redis计数器
   - Key格式: vote:person:{activityId}:{voterId}
   - 默认限制: activity.vote_limit (10票)
   - 过期时间: 1天

4. 检查IP投票限制
   - 基于Redis计数器
   - Key格式: vote:limit:ip:{activityId}:{voterIp}
   - 限制数量: activity.ip_limit
   - 过期时间: 1天

5. 创建投票记录
   - 插入vote_records表
   - 更新candidates表票数
   - 增加限制计数器
```

#### 2.3 Redis缓存设计
```
验证码缓存:
- Key: captcha:{key}
- 过期: 5分钟

每人投票限制:
- Key: vote:person:{activityId}:{voterId}
- 过期: 1天

IP投票限制:
- Key: vote:limit:ip:{activityId}:{voterIp}
- 过期: 1天
```

### 3. 前端实现细节

#### 3.1 投票请求参数
```typescript
{
  activityId: number,      // 活动ID
  candidateId: number,     // 候选人ID
  channel: 'WEB',         // 投票渠道
  voterIp: string,         // 投票人IP地址（真实IP）
  captcha: string,          // 验证码
  captchaKey: string        // 验证码Key
}
```

#### 3.2 IP地址获取
使用 `api.ipify.org` 服务获取真实IP地址：
```typescript
const getClientIP = async (): Promise<string> => {
  const response = await fetch('https://api.ipify.org?format=json')
  const data = await response.json()
  return data.ip || '127.0.0.1'
}
```

#### 3.3 投票前验证
- 检查候选人是否选中
- 检查活动状态（必须为进行中）
- 检查活动时间范围
  - 当前时间 >= 活动开始时间
  - 当前时间 <= 活动结束时间
- 刷新并显示验证码

#### 3.4 投票后处理
- 成功：
  - 显示成功提示
  - 更新候选人票数（+1）
  - 关闭对话框
  - 清空验证码输入
- 失败：
  - 显示错误消息
  - 刷新验证码
  - 保持对话框打开

### 4. 错误处理
后端返回的错误类型：
- `INVALID_VOTE`: 投票信息无效
- `ACTIVITY_NOT_FOUND`: 活动不存在
- `CAPTCHA_REQUIRED`: 请输入验证码
- `CAPTCHA_ERROR`: 验证码错误
- `PERSON_LIMIT_EXCEEDED`: 已达到投票限制
- `IP_LIMIT_EXCEEDED`: IP投票次数限制
- `SYSTEM_ERROR`: 系统错误

## API接口说明

### 获取验证码
```
GET /api/captcha

Response:
{
  "code": 200,
  "message": "success",
  "data": "key|base64_image"
}
```

### 投票
```
POST /api/votes

Request:
{
  "activityId": 1,
  "candidateId": 2,
  "channel": "WEB",
  "voterIp": "1.2.3.4",
  "captcha": "ABCD",
  "captchaKey": "uuid-key"
}

Response (Success):
{
  "code": 200,
  "message": "投票成功: 123",
  "data": null
}

Response (Error):
{
  "code": 500,
  "message": "您已达到该活动的投票限制（每人10票）",
  "data": null
}
```

## 使用说明

### 投票流程
1. 用户选择候选人
2. 点击"为XXX投票"按钮
3. 显示验证码输入对话框
4. 用户输入验证码
5. 点击"确认投票"
6. 后端验证：
   - 验证码正确性
   - 活动状态
   - 活动时间范围
   - 每人投票限制
   - IP投票限制
7. 投票成功或失败，显示相应提示

### 注意事项
1. 验证码5分钟内有效
2. 每人投票限制每天重置（Redis过期时间1天）
3. IP限制也每天重置
4. 投票失败时验证码会自动刷新
5. 活动未开始或已结束时无法投票
6. 真实IP地址使用外部服务获取，确保准确性

## 技术特点
- ✅ 完整的验证码生成和验证机制
- ✅ 基于Redis的高性能限制检查
- ✅ 支持多种投票渠道
- ✅ 可配置的投票限制
- ✅ 真实IP地址获取
- ✅ 友好的错误提示
- ✅ 自动验证码刷新机制
