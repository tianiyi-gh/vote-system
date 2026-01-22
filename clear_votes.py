import mysql.connector
from mysql.connector import Error

# 数据库配置
config = {
    'host': 'localhost',
    'port': 3307,
    'user': 'root',
    'password': 'mysql8123',
    'database': 'dzvote_v2',
    'charset': 'utf8mb4'
}

try:
    # 连接数据库
    connection = mysql.connector.connect(**config)
    
    if connection.is_connected():
        cursor = connection.cursor()
        
        # 清空投票记录表
        cursor.execute("TRUNCATE TABLE vote_records")
        print("✓ 已清空投票记录表 (vote_records)")
        
        # 重置候选人的票数为0
        cursor.execute("UPDATE candidates SET votes = 0")
        affected_rows = cursor.rowcount
        print(f"✓ 已重置 {affected_rows} 个候选人的票数为0")
        
        # 重置活动的总票数
        cursor.execute("UPDATE activities SET total_votes = 0")
        affected_rows = cursor.rowcount
        print(f"✓ 已重置 {affected_rows} 个活动的总票数为0")
        
        # 清空投票限制表
        cursor.execute("TRUNCATE TABLE vote_limits")
        print("✓ 已清空投票限制表 (vote_limits)")
        
        # 提交事务
        connection.commit()
        
        print("\n所有投票数据已成功清空！")
        
except Error as e:
    print(f"数据库错误: {e}")
    if 'connection' in locals() and connection.is_connected():
        connection.rollback()
        
finally:
    if 'connection' in locals() and connection.is_connected():
        cursor.close()
        connection.close()
        print("\n数据库连接已关闭")
