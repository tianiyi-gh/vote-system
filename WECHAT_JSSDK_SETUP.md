# 微信 JSSDK 配置指南

## 前置条件

### 1. 注册公众号
- 访问 [微信公众平台](https://mp.weixin.qq.com/)
- 注册一个公众号（服务号或订阅号均可）
- 完成认证（个人订阅号不支持 JSSDK）

### 2. 获取 AppID 和 AppSecret
1. 登录微信公众平台
2. 进入"开发" → "基本配置"
3. 获取 `AppID` 和 `AppSecret`
4. 保存好 `AppSecret`（只能查看一次）

### 3. 设置 JS 接口安全域名
1. 进入"设置" → "功能设置"
2. 找到"JS接口安全域名"
3. 填写你的域名（例如：`www.yourdomain.com`）
4. 上传验证文件到服务器根目录
5. 验证成功后即可使用

## 后端配置

### 1. 添加配置文件
将 `application-wechat.yml` 的内容合并到你的 `application.yml` 中：

```yaml
wechat:
  app-id: wx1234567890abcdef  # 替换为你的 AppID
  app-secret: abcdef1234567890abcdef1234567890abcdef  # 替换为你的 AppSecret
  js-api-domain: www.yourdomain.com  # 替换为你的域名
```

### 2. 环境变量配置（推荐）
在生产环境中，使用环境变量：

```bash
export WECHAT_APP_ID=wx1234567890abcdef
export WECHAT_APP_SECRET=abcdef1234567890abcdef1234567890abcdef
export WECHAT_JS_API_DOMAIN=www.yourdomain.com
```

### 3. 已创建的文件
- `WeChatConfig.java` - 微信配置类
- `WeChatService.java` - 微信服务类（处理 access_token、jsapi_ticket 和签名）
- `WeChatController.java` - 提供 JSSDK 配置接口

### 4. 确保依赖
确保项目中包含以下依赖：

```xml
<!-- Jackson -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Spring Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

## 前端使用

### 1. 已创建的工具类
`wechat.ts` 包含以下方法：
- `loadWeChatSDK()` - 加载微信 JSSDK
- `getWeChatConfig()` - 获取后端配置
- `configWeChatSDK()` - 配置 JSSDK
- `setShareData()` - 设置分享内容
- `isWeChat()` - 检测是否在微信环境
- `initWeChatShare()` - 初始化分享（推荐使用）

### 2. 在候选人详情页使用

修改 `Candidate.vue`，在 `onMounted` 中初始化微信分享：

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { initWeChatShare } from '@/utils/wechat'

const candidate = ref<Candidate>()

onMounted(async () => {
  // 加载候选人数据...

  // 初始化微信分享
  if (candidate.value) {
    await initWeChatShare({
      title: `为 ${candidate.value.name} 投票！`,
      desc: `TA目前有 ${candidate.value.totalVotes || 0} 票，快去支持一下吧！`,
      link: window.location.href,
      imgUrl: candidate.value.avatar || '/default-avatar.png'
    })
  }
})
</script>
```

### 3. 在其他页面使用

首页、投票页等也可以设置分享内容：

```vue
<script setup lang="ts">
import { onMounted } from 'vue'
import { initWeChatShare } from '@/utils/wechat'

onMounted(async () => {
  await initWeChatShare({
    title: '投票活动',
    desc: '快来参与投票吧！',
    link: window.location.href,
    imgUrl: '/logo.png'
  })
})
</script>
```

## 测试

### 1. 真机测试
微信 JSSDK 必须在微信客户端中测试，无法在浏览器中模拟。

### 2. 测试步骤
1. 部署到已配置安全域名的服务器
2. 在微信中打开页面
3. 点击右上角"..."
4. 选择"发送给朋友"或"分享到朋友圈"
5. 查看分享内容是否正确

### 3. 调试
在 `configWeChatSDK()` 中设置 `debug: true`：

```typescript
;(window as any).wx.config({
  debug: true,  // 开启调试模式
  // ...
})
```

会在控制台输出详细的调试信息。

## 常见问题

### 1. "invalid signature" 错误
- 检查 URL 是否正确（必须与当前页面的 URL 完全一致，包括协议、域名、路径）
- 检查 AppID 和 AppSecret 是否正确
- 检查安全域名是否正确配置

### 2. "access_token is invalid" 错误
- access_token 已过期，但缓存未及时更新
- 检查 Redis 是否正常运行
- 清除 Redis 缓存重试

### 3. 分享不生效
- 确保在 `wx.ready()` 回调中设置分享内容
- 检查 `jsApiList` 是否包含所需的 API
- 图片 URL 必须是完整的 URL，不能是相对路径

### 4. 个人订阅号不支持
个人订阅号不支持 JSSDK，需要升级为服务号或企业号。

## 注意事项

1. **access_token 有效期**：7200 秒（2 小时），需要缓存并定时刷新
2. **jsapi_ticket 有效期**：7200 秒（2 小时），需要缓存并定时刷新
3. **签名必须由后端生成**：不能在前端生成，否则会暴露 AppSecret
4. **URL 必须完整**：包括协议、域名、路径，但不包括 # 及其后面的部分
5. **图片 URL**：建议使用绝对路径，大小不超过 32KB

## 参考文档

- [微信 JS-SDK 说明文档](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html)
- [获取 access_token](https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Get_access_token.html)
- [获取 jsapi_ticket](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html#62)
