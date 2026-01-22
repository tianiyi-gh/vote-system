#!/usr/bin/env python
# -*- coding: utf-8 -*-

import mysql.connector

print("正在连接数据库...")

conn = mysql.connector.connect(
    host='localhost',
    port=3307,
    user='root',
    password='mysql8123',
    database='dzvote_v2'
)

cursor = conn.cursor()

# 查看当前数据
print("\n=== 清空前数据 ===")
cursor.execute("SELECT COUNT(*) FROM vote_records")
print(f"投票记录数: {cursor.fetchone()[0]}")

cursor.execute("SELECT id, name, votes FROM candidates LIMIT 5")
print("\n候选人票数:")
for row in cursor.fetchall():
    print(f"  ID={row[0]}, 姓名={row[1]}, 票数={row[2]}")

# 清空数据
print("\n=== 开始清空 ===")
cursor.execute("TRUNCATE TABLE vote_records")
print("✓ 投票记录表已清空")

cursor.execute("UPDATE candidates SET votes = 0")
print(f"✓ 候选人票数已重置 (影响 {cursor.rowcount} 行)")

cursor.execute("UPDATE activities SET total_votes = 0")
print(f"✓ 活动总票数已重置 (影响 {cursor.rowcount} 行)")

cursor.execute("TRUNCATE TABLE vote_limits")
print("✓ 投票限制表已清空")

conn.commit()

# 查看清空后数据
print("\n=== 清空后数据 ===")
cursor.execute("SELECT COUNT(*) FROM vote_records")
print(f"投票记录数: {cursor.fetchone()[0]}")

cursor.execute("SELECT id, name, votes FROM candidates LIMIT 5")
print("\n候选人票数:")
for row in cursor.fetchall():
    print(f"  ID={row[0]}, 姓名={row[1]}, 票数={row[2]}")

cursor.close()
conn.close()

print("\n✓ 数据库清空完成！")
print("请刷新浏览器页面并清除localStorage来重新测试投票")
