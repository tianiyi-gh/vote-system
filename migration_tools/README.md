# 数据迁移工具

## 工具说明

本目录包含DZVOTE投票系统从旧架构迁移到新架构的工具和脚本。

## 文件说明

- `migrate_database.py` - 数据库迁移工具
- `verify_migration.py` - 数据验证工具
- `requirements.txt` - Python依赖包列表
- `migration_template.sql` - 数据库模板（如果需要）

## 使用步骤

### 1. 环境准备

```bash
# 安装Python依赖
pip install -r requirements.txt

# 确保MySQL服务运行
# 确保新旧数据库都可访问
```

### 2. 配置数据库连接

编辑脚本中的数据库配置：

```python
# 旧数据库配置
OLD_DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'dzvote_old',
    'charset': 'latin1'
}

# 新数据库配置
NEW_DB_CONFIG = {
    'host': 'localhost', 
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'dzvote_v2',
    'charset': 'utf8mb4'
}
```

### 3. 执行迁移

```bash
# 运行迁移工具
python migrate_database.py

# 查看迁移日志
tail -f migration.log
```

### 4. 验证数据

```bash
# 运行验证工具
python verify_migration.py

# 查看验证报告
cat verification_report.json
```

## 迁移范围

### 活动数据 (app_vote_obj → vote_activity)
- 基本信息：标题、副标题、区域、描述
- 时间设置：开始时间、结束时间
- 配置信息：各渠道权重、IP限制、验证码设置
- 状态信息：活动状态、创建信息

### 候选人数据 (app_vote_user → vote_candidate)
- 基本信息：姓名、编号、分组、照片、简介
- 投票统计：各渠道票数、总票数、排名
- 状态信息：候选人状态

### 投票记录 (多渠道表 → vote_record)
- SMS投票：app_vote_sms
- WEB投票：app_vote_web  
- IVR投票：app_vote_ivr
- APP投票：app_vote_app
- 微信投票：app_vote_wechat
- 付费投票：app_vote_pay

## 数据转换规则

### 时间格式转换
- 旧格式：VARCHAR (YYYY-MM-DD HH:MM:SS)
- 新格式：DATETIME

### 字符编码转换
- 旧编码：latin1
- 新编码：utf8mb4

### 权重设置
- 默认权重：100（各渠道）
- 原权重值：保持原设置

## 注意事项

### 迁移前
1. **备份旧数据库**：确保数据安全
2. **创建新数据库**：执行schema.sql
3. **测试连接**：验证数据库连接配置

### 迁移中
1. **监控日志**：实时查看迁移进度
2. **错误处理**：记录并处理错误数据
3. **性能监控**：长时间迁移需要监控服务器资源

### 迁移后
1. **数据验证**：确保数据完整性
2. **业务测试**：验证投票功能正常
3. **性能测试**：验证系统性能

## 故障排除

### 常见问题

#### 连接失败
```
错误: Access denied for user
解决: 检查用户名密码和权限
```

#### 编码问题
```
错误: Incorrect string value
解决: 检查数据库字符集设置
```

#### 时间格式错误
```
错误: Incorrect datetime value
解决: 检查时间字段格式转换
```

#### 内存不足
```
错误: Memory error
解决: 分批处理大量数据
```

### 性能优化

#### 批量插入
```python
# 使用批量插入减少数据库连接
cursor.executemany(insert_sql, values_list)
```

#### 分页处理
```python
# 大数据量分页处理
OFFSET page_size * page_number LIMIT page_size
```

## 监控指标

### 迁移进度
- 活动数量：873个
- 候选人数量：85,514个
- 投票记录：~6亿条（分渠道统计）

### 预估时间
- 活动迁移：< 1分钟
- 候选人迁移：< 5分钟
- 投票记录迁移：2-4小时（取决于数据量）

### 资源需求
- 内存：建议8GB+
- 磁盘：临时空间需要10GB+
- 网络：数据库连接稳定性

## 回滚方案

如果迁移失败，可以：

1. **恢复备份**：从数据库备份恢复
2. **重新迁移**：修复问题后重新执行
3. **部分迁移**：只迁移关键数据

## 验证清单

- [ ] 活动数据完整性
- [ ] 候选人数据完整性  
- [ ] 投票记录数量匹配
- [ ] 外键关系正确
- [ ] 业务逻辑一致
- [ ] 权重设置正确
- [ ] 时间格式正确
- [ ] 编码转换正确
- [ ] 排名计算正确

## 支持与联系

如果在迁移过程中遇到问题：

1. 查看详细日志文件
2. 检查验证报告
3. 联系技术支持

## 更新日志

- v1.0.0 - 初始版本
- v1.1.0 - 增加数据验证功能
- v1.2.0 - 优化大数据量处理