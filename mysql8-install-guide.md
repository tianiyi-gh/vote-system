# MySQL 8 安装配置指南

## 安装策略：双版本并行运行

### 当前配置
- MySQL 5.7: 端口 3306 (保留)
- MySQL 8: 端口 3307 (新增)
- 数据无缝迁移

### 第一步：下载MySQL 8
```bash
# 官方下载地址
https://dev.mysql.com/downloads/mysql/8.0.html
# 选择 Windows (x86, 64-bit), ZIP Archive
```

### 第二步：解压配置
```bash
# 解压到
D:\mysql80

# 创建配置文件 my.cnf
[mysqld]
port=3307
basedir=D:/mysql80
datadir=D:/mysql80/data
socket=mysql8.sock

# 并发优化配置
max_connections=2000
innodb_thread_concurrency=0
innodb_read_io_threads=8
innodb_write_io_threads=8
innodb_buffer_pool_size=2G
innodb_log_file_size=512M
```

### 第三步：初始化安装
```bash
# 管理员权限CMD
cd D:\mysql80\bin
mysqld --initialize --console
# 记录生成的临时密码

# 安装服务
mysqld --install MySQL80 --defaults-file=D:/mysql80/my.cnf

# 启动服务
net start MySQL80
```

### 第四步：配置密码和数据库
```bash
# 连接MySQL 8
mysql -h localhost -P 3307 -u root -p
# 输入临时密码

# 修改密码
ALTER USER 'root'@'localhost' IDENTIFIED BY 'mysql8123';

# 创建数据库
CREATE DATABASE dzvote_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'dzvote'@'localhost' IDENTIFIED BY 'dzvote123';
GRANT ALL PRIVILEGES ON dzvote_v2.* TO 'dzvote'@'localhost';
FLUSH PRIVILEGES;
```

### 第五步：数据迁移（可选）
```bash
# 从MySQL 5.7导出
mysqldump -h localhost -P 3306 -u root -p123456 dzvote_v2 > dzvote_backup.sql

# 导入到MySQL 8
mysql -h localhost -P 3307 -u root -pmysql8123 dzvote_v2 < dzvote_backup.sql
```

### 第六步：应用配置更新
更新应用连接配置：
- 端口：3307
- 用户：dzvote
- 密码：dzvote123
- 数据库：dzvote_v2

### 验证安装
```bash
# MySQL 5.7
mysql -h localhost -P 3306 -u root -p

# MySQL 8
mysql -h localhost -P 3307 -u root -p
```