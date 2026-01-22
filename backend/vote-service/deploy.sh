#!/bin/bash

# 投票系统部署脚本
# 用途：将本地构建的文件上传到服务器并重启服务

# ==================== 配置区域 ====================

# 服务器信息
SERVER_USER="root"  # 服务器用户名
SERVER_IP=""         # 服务器IP地址，请填写
SERVER_PORT="22"     # SSH端口

# 部署路径
BACKEND_DIR="/opt/vote-service"
FRONTEND_DIR="/opt/h5"

# 本地文件路径
BACKEND_JAR="target/vote-service-2.0.0.jar"
FRONTEND_DIST="新架构/frontend/h5/dist"

# ==========================================================

if [ -z "$SERVER_IP" ]; then
    echo "错误：请先设置 SERVER_IP 变量"
    echo "编辑此文件，将 SERVER_IP=\"\" 改为你的服务器IP"
    exit 1
fi

echo "========================================="
echo "   投票系统部署脚本"
echo "========================================="
echo "服务器: $SERVER_USER@$SERVER_IP"
echo "后端目录: $BACKEND_DIR"
echo "前端目录: $FRONTEND_DIR"
echo "========================================="

# 检查本地文件
echo ""
echo "检查本地文件..."

if [ ! -f "$BACKEND_JAR" ]; then
    echo "错误：后端 jar 文件不存在: $BACKEND_JAR"
    echo "请先运行: mvn clean package -DskipTests"
    exit 1
fi

if [ ! -d "$FRONTEND_DIST" ]; then
    echo "错误：前端构建目录不存在: $FRONTEND_DIST"
    echo "请先运行: cd 新架构/frontend/h5 && npm run build"
    exit 1
fi

echo "✓ 本地文件检查通过"

# 构建前端（如果需要）
echo ""
read -p "是否重新构建前端？(y/n): " rebuild_frontend
if [ "$rebuild_frontend" = "y" ]; then
    echo "构建前端..."
    cd "新架构/frontend/h5"
    npm install
    npm run build
    cd ../..

    if [ $? -ne 0 ]; then
        echo "错误：前端构建失败"
        exit 1
    fi
    echo "✓ 前端构建完成"
fi

# 1. 部署后端
echo ""
echo "========================================="
echo "步骤 1/3: 部署后端"
echo "========================================="

echo "上传后端 jar 文件..."
scp -P $SERVER_PORT "$BACKEND_JAR" $SERVER_USER@$SERVER_IP:$BACKEND_DIR/

if [ $? -ne 0 ]; then
    echo "错误：上传后端失败"
    exit 1
fi
echo "✓ 后端上传完成"

# 2. 部署前端
echo ""
echo "========================================="
echo "步骤 2/3: 部署前端"
echo "========================================="

echo "上传前端文件..."
scp -P $SERVER_PORT -r "$FRONTEND_DIST/"* $SERVER_USER@$SERVER_IP:$FRONTEND_DIR/dist/

if [ $? -ne 0 ]; then
    echo "错误：上传前端失败"
    exit 1
fi
echo "✓ 前端上传完成"

# 3. 重启服务
echo ""
echo "========================================="
echo "步骤 3/3: 重启服务"
echo "========================================="

echo "在服务器上执行重启命令..."
ssh -p $SERVER_PORT $SERVER_USER@$SERVER_IP << 'ENDSSH'
# 停止后端服务
systemctl stop vote-service

# 备份旧 jar
if [ -f /opt/vote-service/vote-service-2.0.0.jar.bak ]; then
    rm -f /opt/vote-service/vote-service-2.0.0.jar.bak
fi
if [ -f /opt/vote-service/vote-service-2.0.0.jar ]; then
    mv /opt/vote-service/vote-service-2.0.0.jar /opt/vote-service/vote-service-2.0.0.jar.bak
fi

# 启动后端服务
systemctl start vote-service

# 重启 Nginx
systemctl restart nginx

# 检查服务状态
echo ""
echo "========================================="
echo "服务状态"
echo "========================================="
systemctl status vote-service --no-pager -l
systemctl status nginx --no-pager -l
ENDSSH

echo ""
echo "========================================="
echo "   部署完成！"
echo "========================================="
echo "访问地址: http://$SERVER_IP"
echo "后端 API: http://$SERVER_IP/api"
echo ""
echo "查看日志:"
echo "  后端: journalctl -u vote-service -f"
echo "  Nginx: tail -f /var/log/nginx/error.log"
echo "========================================="
