#!/bin/bash

# 服务器初始化脚本
# 用途：在全新服务器上安装投票系统所需的环境

set -e

echo "========================================="
echo "   服务器环境初始化脚本"
echo "========================================="
echo ""

# 检测操作系统
if [ -f /etc/redhat-release ]; then
    OS="centos"
    echo "检测到操作系统: CentOS"
elif [ -f /etc/debian_version ]; then
    OS="ubuntu"
    echo "检测到操作系统: Ubuntu"
else
    echo "错误：不支持的操作系统"
    exit 1
fi

# 1. 安装 Java 17
echo "========================================="
echo "步骤 1/7: 安装 Java 17"
echo "========================================="

if command -v java &> /dev/null; then
    echo "Java 已安装，版本: $(java -version 2>&1 | head -n 1)"
else
    if [ "$OS" = "centos" ]; then
        sudo yum install -y java-17-openjdk-devel
    else
        sudo apt update
        sudo apt install -y openjdk-17-jdk
    fi
    echo "[OK] Java 17 安装完成"
fi

# 2. 安装 Node.js
echo ""
echo "========================================="
echo "步骤 2/7: 安装 Node.js"
echo "========================================="

if command -v node &> /dev/null; then
    echo "Node.js 已安装，版本: $(node --version)"
else
    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
    export NVM_DIR="$HOME/.nvm"
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
    nvm install 18
    nvm alias default 18
    echo "[OK] Node.js 18 安装完成"
fi

# 3. 安装 MySQL
echo ""
echo "========================================="
echo "步骤 3/7: 安装 MySQL"
echo "========================================="

if command -v mysql &> /dev/null; then
    echo "MySQL 已安装"
else
    if [ "$OS" = "centos" ]; then
        sudo yum install -y mysql-server
        sudo systemctl start mysqld
        sudo systemctl enable mysqld
    else
        sudo apt install -y mysql-server
        sudo systemctl start mysql
        sudo systemctl enable mysql
    fi
    echo "[OK] MySQL 安装完成"
fi

# 4. 安装 Redis
echo ""
echo "========================================="
echo "步骤 4/7: 安装 Redis"
echo "========================================="

if command -v redis-server &> /dev/null; then
    echo "Redis 已安装，版本: $(redis-server --version)"
else
    if [ "$OS" = "centos" ]; then
        sudo yum install -y redis
    else
        sudo apt install -y redis-server
    fi
    sudo systemctl start redis
    sudo systemctl enable redis
    echo "[OK] Redis 安装完成"
fi

# 5. 安装 Nginx
echo ""
echo "========================================="
echo "步骤 5/7: 安装 Nginx"
echo "========================================="

if command -v nginx &> /dev/null; then
    echo "Nginx 已安装，版本: $(nginx -v 2>&1)"
else
    if [ "$OS" = "centos" ]; then
        sudo yum install -y nginx
    else
        sudo apt install -y nginx
    fi
    sudo systemctl start nginx
    sudo systemctl enable nginx
    echo "[OK] Nginx 安装完成"
fi

# 6. 创建目录和配置
echo ""
echo "========================================="
echo "步骤 6/7: 创建目录和配置"
echo "========================================="

# 创建目录
sudo mkdir -p /opt/vote-service
sudo mkdir -p /opt/vote-service/uploads
sudo mkdir -p /opt/h5
sudo mkdir -p /opt/h5/dist
sudo mkdir -p /opt/backups

# 设置权限
sudo chown -R $USER:$USER /opt/vote-service
sudo chown -R $USER:$USER /opt/h5
sudo chmod 755 /opt/vote-service/uploads

echo "[OK] 目录创建完成"

# 7. 创建 Nginx 配置
echo ""
echo "========================================="
echo "步骤 7/7: 配置 Nginx"
echo "========================================="

# 读取域名
read -p "请输入域名 (例如: tp.dzwww.com): " DOMAIN

if [ -z "$DOMAIN" ]; then
    echo "警告：未设置域名，将使用默认配置"
    DOMAIN="localhost"
fi

# 创建 Nginx 配置
sudo tee /etc/nginx/conf.d/vote.conf > /dev/null <<EOF
server {
    listen 80;
    server_name $DOMAIN;

    # 前端静态文件
    location / {
        root /opt/h5/dist;
        try_files \$uri \$uri/ /index.html;
        index index.html;

        # 启用 gzip 压缩
        gzip on;
        gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
        gzip_min_length 1024;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://localhost:8082;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # 上传文件目录
    location /uploads {
        alias /opt/vote-service/uploads;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
EOF

# 测试 Nginx 配置
sudo nginx -t

if [ $? -eq 0 ]; then
    sudo systemctl restart nginx
    echo "[OK] Nginx 配置完成"
else
    echo "错误：Nginx 配置有误"
    exit 1
fi

# 配置防火墙
echo ""
echo "========================================="
echo "配置防火墙"
echo "========================================="

if [ "$OS" = "centos" ]; then
    if command -v firewall-cmd &> /dev/null; then
        sudo firewall-cmd --permanent --add-port=80/tcp
        sudo firewall-cmd --permanent --add-port=443/tcp
        sudo firewall-cmd --reload
        echo "[OK] 防火墙配置完成 (CentOS)"
    fi
else
    if command -v ufw &> /dev/null; then
        sudo ufw allow 80/tcp
        sudo ufw allow 443/tcp
        sudo ufw allow 22/tcp
        sudo ufw --force enable
        echo "[OK] 防火墙配置完成 (Ubuntu)"
    fi
fi

# 提示下一步操作
echo ""
echo "========================================="
echo "   初始化完成！"
echo "========================================="
echo ""
echo "下一步操作："
echo ""
echo "1. 创建数据库"
echo "   mysql -u root -p"
echo "   CREATE DATABASE dzvote_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo "   CREATE USER 'dzvote'@'localhost' IDENTIFIED BY 'your_password';"
echo "   GRANT ALL PRIVILEGES ON dzvote_v2.* TO 'dzvote'@'localhost';"
echo "   FLUSH PRIVILEGES;"
echo ""
echo "2. 上传后端 jar 文件"
echo "   scp target/vote-service-2.0.0.jar user@server:/opt/vote-service/"
echo ""
echo "3. 修改后端配置"
echo "   nano /opt/vote-service/application.yml"
echo "   # 修改数据库、Redis、微信等配置"
echo ""
echo "4. 创建 systemd 服务"
echo "   sudo nano /etc/systemd/system/vote-service.service"
echo "   # 粘贴服务配置（见文档）"
echo "   sudo systemctl daemon-reload"
echo "   sudo systemctl start vote-service"
echo "   sudo systemctl enable vote-service"
echo ""
echo "5. 上传前端文件"
echo "   scp -r h5/dist/* user@server:/opt/h5/dist/"
echo ""
echo "6. 配置微信 JS 接口安全域名"
echo "   在微信公众平台添加: $DOMAIN"
echo ""
echo "========================================="
echo ""
echo "详细文档请查看: DEPLOYMENT_GUIDE.md"
echo ""
