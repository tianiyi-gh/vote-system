#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
DZVOTE数据验证工具
验证迁移后的数据完整性
"""

import pymysql
import json
import logging
from datetime import datetime
from typing import Dict, Any
import sys

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('verification.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# 数据库配置
OLD_DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'dzvote_old',
    'charset': 'latin1'
}

NEW_DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'dzvote_v2',
    'charset': 'utf8mb4'
}

class DataVerifier:
    def __init__(self):
        self.old_conn = None
        self.new_conn = None
        self.verification_result = {
            'activities': {'old': 0, 'new': 0, 'match': True},
            'candidates': {'old': 0, 'new': 0, 'match': True},
            'vote_records': {'old': 0, 'new': 0, 'match': True},
            'errors': []
        }
    
    def connect_databases(self):
        """连接数据库"""
        try:
            self.old_conn = pymysql.connect(**OLD_DB_CONFIG)
            self.new_conn = pymysql.connect(**NEW_DB_CONFIG)
            logger.info("数据库连接成功")
            return True
        except Exception as e:
            logger.error(f"连接数据库失败: {e}")
            return False
    
    def close_connections(self):
        """关闭数据库连接"""
        if self.old_conn:
            self.old_conn.close()
        if self.new_conn:
            self.new_conn.close()
        logger.info("数据库连接已关闭")
    
    def verify_activities(self):
        """验证活动数据"""
        logger.info("验证活动数据...")
        
        try:
            # 旧系统活动数量
            old_cursor = self.old_conn.cursor()
            old_cursor.execute("SELECT COUNT(*) FROM app_vote_obj WHERE Deleted = 0")
            old_count = old_cursor.fetchone()[0]
            self.verification_result['activities']['old'] = old_count
            
            # 新系统活动数量
            new_cursor = self.new_conn.cursor()
            new_cursor.execute("SELECT COUNT(*) FROM vote_activity WHERE deleted = 0")
            new_count = new_cursor.fetchone()[0]
            self.verification_result['activities']['new'] = new_count
            
            # 验证数据匹配
            if old_count != new_count:
                self.verification_result['activities']['match'] = False
                error_msg = f"活动数量不匹配: 旧={old_count}, 新={new_count}"
                logger.warning(error_msg)
                self.verification_result['errors'].append(error_msg)
            else:
                logger.info(f"活动数据验证通过: {new_count} 个活动")
            
            old_cursor.close()
            new_cursor.close()
            
        except Exception as e:
            error_msg = f"验证活动数据失败: {e}"
            logger.error(error_msg)
            self.verification_result['errors'].append(error_msg)
    
    def verify_candidates(self):
        """验证候选人数据"""
        logger.info("验证候选人数据...")
        
        try:
            # 旧系统候选人数量
            old_cursor = self.old_conn.cursor()
            old_cursor.execute("SELECT COUNT(*) FROM app_vote_user WHERE Deleted = 0")
            old_count = old_cursor.fetchone()[0]
            self.verification_result['candidates']['old'] = old_count
            
            # 新系统候选人数量
            new_cursor = self.new_conn.cursor()
            new_cursor.execute("SELECT COUNT(*) FROM vote_candidate WHERE deleted = 0")
            new_count = new_cursor.fetchone()[0]
            self.verification_result['candidates']['new'] = new_count
            
            # 验证数据匹配
            if old_count != new_count:
                self.verification_result['candidates']['match'] = False
                error_msg = f"候选人数量不匹配: 旧={old_count}, 新={new_count}"
                logger.warning(error_msg)
                self.verification_result['errors'].append(error_msg)
            else:
                logger.info(f"候选人数据验证通过: {new_count} 个候选人")
            
            old_cursor.close()
            new_cursor.close()
            
        except Exception as e:
            error_msg = f"验证候选人数据失败: {e}"
            logger.error(error_msg)
            self.verification_result['errors'].append(error_msg)
    
    def verify_vote_records(self):
        """验证投票记录数据"""
        logger.info("验证投票记录数据...")
        
        try:
            # 统计旧系统各渠道投票记录
            old_cursor = self.old_conn.cursor()
            old_total = 0
            
            channels = [
                ('SMS', 'app_vote_sms'),
                ('WEB', 'app_vote_web'),
                ('IVR', 'app_vote_ivr'),
                ('APP', 'app_vote_app'),
                ('WECHAT', 'app_vote_wechat'),
                ('PAY', 'app_vote_pay')
            ]
            
            for channel, table_name in channels:
                old_cursor.execute(f"SELECT COUNT(*) FROM {table_name} WHERE Flag = 1")
                count = old_cursor.fetchone()[0]
                old_total += count
            
            self.verification_result['vote_records']['old'] = old_total
            
            # 统计新系统投票记录
            new_cursor = self.new_conn.cursor()
            new_cursor.execute("SELECT COUNT(*) FROM vote_record WHERE status = 1")
            new_count = new_cursor.fetchone()[0]
            self.verification_result['vote_records']['new'] = new_count
            
            # 验证数据匹配
            if old_total != new_count:
                self.verification_result['vote_records']['match'] = False
                error_msg = f"投票记录数量不匹配: 旧={old_total}, 新={new_count}"
                logger.warning(error_msg)
                self.verification_result['errors'].append(error_msg)
            else:
                logger.info(f"投票记录数据验证通过: {new_count} 条记录")
            
            old_cursor.close()
            new_cursor.close()
            
        except Exception as e:
            error_msg = f"验证投票记录数据失败: {e}"
            logger.error(error_msg)
            self.verification_result['errors'].append(error_msg)
    
    def verify_data_integrity(self):
        """验证数据完整性"""
        logger.info("验证数据完整性...")
        
        try:
            cursor = self.new_conn.cursor()
            
            # 检查外键完整性
            cursor.execute("""
            SELECT COUNT(*) FROM vote_candidate c 
            LEFT JOIN vote_activity a ON c.activity_id = a.id 
            WHERE a.id IS NULL AND c.deleted = 0
            """)
            orphaned_candidates = cursor.fetchone()[0]
            
            if orphaned_candidates > 0:
                error_msg = f"发现 {orphaned_candidates} 个孤立的候选人记录"
                logger.warning(error_msg)
                self.verification_result['errors'].append(error_msg)
            
            # 检查投票记录完整性
            cursor.execute("""
            SELECT COUNT(*) FROM vote_record v 
            LEFT JOIN vote_candidate c ON v.candidate_id = c.id 
            LEFT JOIN vote_activity a ON v.activity_id = a.id 
            WHERE c.id IS NULL OR a.id IS NULL
            """)
            orphaned_records = cursor.fetchone()[0]
            
            if orphaned_records > 0:
                error_msg = f"发现 {orphaned_records} 条孤立的投票记录"
                logger.warning(error_msg)
                self.verification_result['errors'].append(error_msg)
            
            # 检查数据一致性
            cursor.execute("""
            SELECT c.id, c.total_votes, 
                   (SELECT COUNT(*) FROM vote_record r 
                    WHERE r.candidate_id = c.id AND r.status = 1) as actual_votes
            FROM vote_candidate c
            HAVING c.total_votes != actual_votes
            LIMIT 10
            """)
            inconsistent_records = cursor.fetchall()
            
            if inconsistent_records:
                error_msg = f"发现 {len(inconsistent_records)} 条票数不一致的记录"
                logger.warning(error_msg)
                self.verification_result['errors'].append(error_msg)
            
            cursor.close()
            logger.info("数据完整性验证完成")
            
        except Exception as e:
            error_msg = f"验证数据完整性失败: {e}"
            logger.error(error_msg)
            self.verification_result['errors'].append(error_msg)
    
    def verify_business_logic(self):
        """验证业务逻辑"""
        logger.info("验证业务逻辑...")
        
        try:
            cursor = self.new_conn.cursor()
            
            # 检查候选人排名
            cursor.execute("""
            SELECT activity_id, COUNT(*) - COUNT(DISTINCT ranking) as duplicate_ranks
            FROM vote_candidate 
            WHERE deleted = 0 AND ranking > 0
            GROUP BY activity_id
            HAVING duplicate_ranks > 0
            """)
            duplicate_rankings = cursor.fetchall()
            
            if duplicate_rankings:
                error_msg = f"发现 {len(duplicate_rankings)} 个活动存在重复排名"
                logger.warning(error_msg)
                self.verification_result['errors'].append(error_msg)
            
            # 检查活动时间逻辑
            cursor.execute("""
            SELECT COUNT(*) FROM vote_activity 
            WHERE deleted = 0 AND end_time < start_time
            """)
            invalid_time_activities = cursor.fetchone()[0]
            
            if invalid_time_activities > 0:
                error_msg = f"发现 {invalid_time_activities} 个活动时间设置无效"
                logger.warning(error_msg)
                self.verification_result['errors'].append(error_msg)
            
            cursor.close()
            logger.info("业务逻辑验证完成")
            
        except Exception as e:
            error_msg = f"验证业务逻辑失败: {e}"
            logger.error(error_msg)
            self.verification_result['errors'].append(error_msg)
    
    def generate_verification_report(self):
        """生成验证报告"""
        report = {
            'verification_time': datetime.now().isoformat(),
            'result': self.verification_result,
            'summary': {
                'total_errors': len(self.verification_result['errors']),
                'success': len(self.verification_result['errors']) == 0
            }
        }
        
        with open('verification_report.json', 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)
        
        logger.info("验证报告已生成: verification_report.json")
        
        return report
    
    def run_verification(self):
        """执行完整验证"""
        try:
            logger.info("="*50)
            logger.info("开始DZVOTE数据验证")
            logger.info("="*50)
            
            if not self.connect_databases():
                sys.exit(1)
            
            # 执行验证步骤
            self.verify_activities()
            self.verify_candidates()
            self.verify_vote_records()
            self.verify_data_integrity()
            self.verify_business_logic()
            
            # 生成报告
            report = self.generate_verification_report()
            
            logger.info("="*50)
            logger.info("数据验证完成")
            logger.info(f"验证结果: {'通过' if report['summary']['success'] else '失败'}")
            logger.info(f"总错误数: {report['summary']['total_errors']}")
            if report['summary']['total_errors'] > 0:
                logger.warning("发现问题，请查看详细报告")
            logger.info("="*50)
            
            return report
            
        except Exception as e:
            logger.error(f"验证过程中发生错误: {e}")
            sys.exit(1)
        finally:
            self.close_connections()

def main():
    """主函数"""
    verifier = DataVerifier()
    
    print("DZVOTE数据验证工具")
    print("="*30)
    
    report = verifier.run_verification()
    
    if report['summary']['success']:
        print("✅ 数据验证通过")
        return 0
    else:
        print("❌ 数据验证失败")
        print(f"发现 {report['summary']['total_errors']} 个问题")
        return 1

if __name__ == '__main__':
    sys.exit(main())