import pymysql
import sys

# 数据库连接配置
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
    conn = pymysql.connect(**config)
    cursor = conn.cursor()

    # 插入测试用户
    user_sql = """
    INSERT INTO `users` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`, `created_at`)
    VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFD', '系统管理员', '13800138000', 'admin@dzvote.com', 'ADMIN', 1, NOW())
    ON DUPLICATE KEY UPDATE `real_name`=VALUES(`real_name`);
    """
    cursor.execute(user_sql)
    print("✅ 插入测试用户: admin / admin123")

    # 插入示例活动
    activity_sql = """
    INSERT INTO `activities` (`title`, `description`, `start_time`, `end_time`, `status`, `vote_limit`, `created_by`, `created_at`)
    VALUES ('2024年度优秀员工评选', '评选公司年度优秀员工，每人可投3票', '2024-01-01 09:00:00', '2025-12-31 18:00:00', 1, 3, 'admin', NOW())
    ON DUPLICATE KEY UPDATE `title`=VALUES(`title`);
    """
    cursor.execute(activity_sql)
    print("✅ 插入测试活动: 2024年度优秀员工评选")

    # 插入候选人
    candidates = [
        (1, '张三', '技术部高级工程师，工作认真负责', 'https://via.placeholder.com/150/4CAF50/ffffff?text=张三', 15, 1, 1),
        (1, '李四', '销售部销售经理，业绩突出', 'https://via.placeholder.com/150/2196F3/ffffff?text=李四', 12, 2, 1),
        (1, '王五', '市场部产品经理，创新能力突出', 'https://via.placeholder.com/150/FF9800/ffffff?text=王五', 8, 3, 1),
        (1, '赵六', '人事部主管，管理能力强', 'https://via.placeholder.com/150/E91E63/ffffff?text=赵六', 6, 4, 1),
    ]

    for activity_id, name, desc, avatar, votes, order_num, status in candidates:
        candidate_sql = """
        INSERT INTO `candidates` (`activity_id`, `name`, `description`, `avatar`, `votes`, `order_num`, `status`, `created_at`)
        VALUES (%s, %s, %s, %s, %s, %s, %s, NOW())
        ON DUPLICATE KEY UPDATE `votes`=VALUES(`votes`);
        """
        cursor.execute(candidate_sql, (activity_id, name, desc, avatar, votes, order_num, status))

    print(f"✅ 插入 {len(candidates)} 个候选人")

    # 更新活动统计
    update_stats_sql = """
    UPDATE `activities` SET
        `candidate_count` = (SELECT COUNT(*) FROM `candidates` WHERE `activity_id` = 1),
        `total_votes` = (SELECT SUM(`votes`) FROM `candidates` WHERE `activity_id` = 1)
    WHERE `id` = 1;
    """
    cursor.execute(update_stats_sql)
    print("✅ 更新活动统计信息")

    conn.commit()
    print("\n🎉 测试数据初始化完成！")

    # 查询并显示数据
    print("\n📊 当前数据:")
    cursor.execute("SELECT id, title, status, candidate_count, total_votes FROM activities")
    activities = cursor.fetchall()
    for act in activities:
        print(f"  活动: ID={act[0]}, 标题={act[1]}, 状态={act[2]}, 候选人={act[3]}, 总票数={act[4]}")

    cursor.execute("SELECT id, activity_id, name, votes FROM candidates")
    candidates = cursor.fetchall()
    print(f"  候选人数量: {len(candidates)}")
    for cand in candidates:
        print(f"    ID={cand[0]}, 活动={cand[1]}, 姓名={cand[2]}, 票数={cand[3]}")

except Exception as e:
    print(f"❌ 错误: {e}")
    conn.rollback()
    sys.exit(1)
finally:
    cursor.close()
    conn.close()
