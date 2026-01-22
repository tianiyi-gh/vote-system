#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
DZVOTE数据库迁移工具
从旧系统数据迁移到新系统
"""

import pymysql
import json
import logging
from datetime import datetime
from typing import Dict, List, Any
import sys

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('migration.log', encoding='utf-8'),
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

class DatabaseMigrator:
    def __init__(self):
        self.old_conn = None
        self.new_conn = None
        self.migration_stats = {
            'activities': 0,
            'candidates': 0,
            'vote_records': 0,
            'errors': []
        }
        
    def connect_databases(self):
        """连接数据库"""
        try:
            # 连接旧数据库
            self.old_conn = pymysql.connect(**OLD_DB_CONFIG)
            self.old_conn.cursor().execute("SET NAMES 'latin1'")
            logger.info("连接旧数据库成功")
            
            # 连接新数据库
            self.new_conn = pymysql.connect(**NEW_DB_CONFIG)
            self.new_conn.cursor().execute("SET NAMES 'utf8mb4'")
            logger.info("连接新数据库成功")
            
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
    
    def migrate_activities(self):
        """迁移活动数据"""
        logger.info("开始迁移活动数据...")
        
        try:
            old_cursor = self.old_conn.cursor(pymysql.cursors.DictCursor)
            new_cursor = self.new_conn.cursor()
            
            # 查询旧系统活动数据
            sql = """
            SELECT ServiceId, Title, SubTitle, Region, Description, CoverImage,
                   StartTime, EndTime, Flag, GroupNum, Specific_SMS, Specific_WEB, 
                   Specific_IVR, Specific_APP, Specific_WECHAT, Specific_PAY,
                   IPLimit, EnableCaptcha, ShowVotes, Domain, Template, Rules,
                   CreateBy, CreateTime, UpdateBy, UpdateTime
            FROM app_vote_obj 
            WHERE Deleted = 0
            ORDER BY CreateTime DESC
            """
            old_cursor.execute(sql)
            activities = old_cursor.fetchall()
            
            # 插入新系统
            for activity in activities:
                try:
                    # 转换时间格式
                    start_time = self.convert_datetime(activity['StartTime'])
                    end_time = self.convert_datetime(activity['EndTime'])
                    create_time = self.convert_datetime(activity['CreateTime'])
                    update_time = self.convert_datetime(activity['UpdateTime'])
                    
                    insert_sql = """
                    INSERT INTO vote_activity (
                        service_id, title, subtitle, region, description, cover_image,
                        start_time, end_time, status, group_count, sms_weight, 
                        web_weight, ivr_weight, app_weight, wechat_weight, 
                        pay_weight, ip_limit, enable_captcha, show_votes, 
                        domain, template, rules, create_by, create_time, 
                        update_by, update_time, deleted
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 
                            %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 0)
                    """
                    
                    values = (
                        activity['ServiceId'], activity['Title'], activity['SubTitle'], 
                        activity['Region'], activity['Description'], activity['CoverImage'],
                        start_time, end_time, activity['Flag'], activity['GroupNum'],
                        activity['Specific_SMS'] or 100, activity['Specific_WEB'] or 100,
                        activity['Specific_IVR'] or 100, activity['Specific_APP'] or 100,
                        activity['Specific_WECHAT'] or 100, activity['Specific_PAY'] or 100,
                        activity['IPLimit'], activity['EnableCaptcha'], activity['ShowVotes'],
                        activity['Domain'], activity['Template'], activity['Rules'],
                        activity['CreateBy'], create_time, activity['UpdateBy'], update_time
                    )
                    
                    new_cursor.execute(insert_sql, values)
                    self.migration_stats['activities'] += 1
                    
                except Exception as e:
                    error_msg = f"迁移活动失败: ServiceId={activity['ServiceId']}, error={e}"
                    logger.error(error_msg)
                    self.migration_stats['errors'].append(error_msg)
            
            self.new_conn.commit()
            old_cursor.close()
            new_cursor.close()
            
            logger.info(f"活动迁移完成: 迁移了 {self.migration_stats['activities']} 个活动")
            
        except Exception as e:
            logger.error(f"迁移活动数据失败: {e}")
            raise
    
    def migrate_candidates(self):
        """迁移候选人数据"""
        logger.info("开始迁移候选人数据...")
        
        try:
            old_cursor = self.old_conn.cursor(pymysql.cursors.DictCursor)
            new_cursor = self.new_conn.cursor()
            
            # 查询旧系统候选人数据
            sql = """
            SELECT ServiceId, CandidateNo, Name, GroupName, Description, Photo,
                   Votes, WebVotes, IVRVote, APPVote, WeChatVote, PayVote,
                   Flag, CreateTime, UpdateTime
            FROM app_vote_user 
            WHERE Deleted = 0
            ORDER BY ServiceId, CreateTime
            """
            old_cursor.execute(sql)
            candidates = old_cursor.fetchall()
            
            # 插入新系统
            for candidate in candidates:
                try:
                    # 获取活动ID
                    activity_id = self.get_activity_id(candidate['ServiceId'])
                    if not activity_id:
                        continue
                    
                    create_time = self.convert_datetime(candidate['CreateTime'])
                    update_time = self.convert_datetime(candidate['UpdateTime'])
                    
                    insert_sql = """
                    INSERT INTO vote_candidate (
                        activity_id, service_id, candidate_no, name, group_name, 
                        description, photo, sms_votes, ivr_votes, web_votes, 
                        app_votes, wechat_votes, pay_votes, total_votes, score, 
                        ranking, status, create_time, update_time, deleted
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 
                            %s, %s, %s, %s, %s, 0, 0, 0, %s, %s, 0)
                    """
                    
                    values = (
                        activity_id, candidate['ServiceId'], candidate['CandidateNo'],
                        candidate['Name'], candidate['GroupName'], candidate['Description'],
                        candidate['Photo'], 0, candidate['IVRVote'] or 0,
                        candidate['WebVotes'] or 0, candidate['APPVote'] or 0,
                        candidate['WeChatVote'] or 0, candidate['PayVote'] or 0,
                        candidate['Votes'] or 0, 0, 0, candidate['Flag'],
                        create_time, update_time
                    )
                    
                    new_cursor.execute(insert_sql, values)
                    self.migration_stats['candidates'] += 1
                    
                except Exception as e:
                    error_msg = f"迁移候选人失败: ServiceId={candidate['ServiceId']}, error={e}"
                    logger.error(error_msg)
                    self.migration_stats['errors'].append(error_msg)
            
            self.new_conn.commit()
            old_cursor.close()
            new_cursor.close()
            
            logger.info(f"候选人迁移完成: 迁移了 {self.migration_stats['candidates']} 个候选人")
            
        except Exception as e:
            logger.error(f"迁移候选人数据失败: {e}")
            raise
    
    def migrate_vote_records(self):
        """迁移投票记录数据"""
        logger.info("开始迁移投票记录数据...")
        
        try:
            old_cursor = self.old_conn.cursor(pymysql.cursors.DictCursor)
            new_cursor = self.new_conn.cursor()
            
            # 迁移各渠道投票记录
            channels = [
                ('SMS', 'app_vote_sms'),
                ('WEB', 'app_vote_web'),
                ('IVR', 'app_vote_ivr'),
                ('APP', 'app_vote_app'),
                ('WECHAT', 'app_vote_wechat'),
                ('PAY', 'app_vote_pay')
            ]
            
            for channel, table_name in channels:
                logger.info(f"迁移{channel}渠道投票记录...")
                
                sql = f"""
                SELECT ServiceId, CandidateNo, VoterPhone, VoterIP, VoteTime, Flag
                FROM {table_name} 
                WHERE Flag = 1
                ORDER BY VoteTime
                """
                old_cursor.execute(sql)
                records = old_cursor.fetchall()
                
                for record in records:
                    try:
                        # 获取活动ID和候选人ID
                        activity_id = self.get_activity_id(record['ServiceId'])
                        candidate_id = self.get_candidate_id(record['ServiceId'], record['CandidateNo'])
                        
                        if not activity_id or not candidate_id:
                            continue
                        
                        vote_time = self.convert_datetime(record['VoteTime'])
                        
                        insert_sql = """
                        INSERT INTO vote_record (
                            activity_id, candidate_id, channel, voter_phone, 
                            voter_ip, vote_time, status
                        ) VALUES (%s, %s, %s, %s, %s, %s, 1)
                        """
                        
                        values = (
                            activity_id, candidate_id, channel, record['VoterPhone'],
                            record['VoterIP'], vote_time
                        )
                        
                        new_cursor.execute(insert_sql, values)
                        self.migration_stats['vote_records'] += 1
                        
                    except Exception as e:
                        error_msg = f"迁移投票记录失败: channel={channel}, error={e}"
                        logger.error(error_msg)
                        self.migration_stats['errors'].append(error_msg)
            
            self.new_conn.commit()
            old_cursor.close()
            new_cursor.close()
            
            logger.info(f"投票记录迁移完成: 迁移了 {self.migration_stats['vote_records']} 条记录")
            
        except Exception as e:
            logger.error(f"迁移投票记录数据失败: {e}")
            raise
    
    def get_activity_id(self, service_id):
        """获取活动ID"""
        try:
            cursor = self.new_conn.cursor()
            sql = "SELECT id FROM vote_activity WHERE service_id = %s"
            cursor.execute(sql, (service_id,))
            result = cursor.fetchone()
            cursor.close()
            return result[0] if result else None
        except Exception:
            return None
    
    def get_candidate_id(self, service_id, candidate_no):
        """获取候选人ID"""
        try:
            cursor = self.new_conn.cursor()
            sql = """
            SELECT c.id FROM vote_candidate c 
            JOIN vote_activity a ON c.activity_id = a.id 
            WHERE a.service_id = %s AND c.candidate_no = %s
            """
            cursor.execute(sql, (service_id, candidate_no))
            result = cursor.fetchone()
            cursor.close()
            return result[0] if result else None
        except Exception:
            return None
    
    def convert_datetime(self, date_str):
        """转换时间格式"""
        if not date_str or date_str == '0000-00-00 00:00:00':
            return None
        
        try:
            # 处理不同的时间格式
            if len(date_str) == 10:  # YYYY-MM-DD
                return datetime.strptime(date_str, '%Y-%m-%d')
            elif len(date_str) == 19:  # YYYY-MM-DD HH:MM:SS
                return datetime.strptime(date_str, '%Y-%m-%d %H:%M:%S')
            else:
                return datetime.now()
        except Exception:
            return datetime.now()
    
    def update_candidate_statistics(self):
        """更新候选人统计数据"""
        logger.info("开始更新候选人统计...")
        
        try:
            cursor = self.new_conn.cursor()
            
            # 更新候选人票数统计
            sql = """
            UPDATE vote_candidate c 
            SET total_votes = (
                SELECT COUNT(*) FROM vote_record v 
                WHERE v.candidate_id = c.id AND v.status = 1
            )
            """
            cursor.execute(sql)
            
            # 计算排名
            sql = """
            SET @rank = 0;
            UPDATE vote_candidate 
            SET ranking = (@rank := @rank + 1)
            WHERE activity_id IN (SELECT id FROM vote_activity)
            ORDER BY activity_id, total_votes DESC, name;
            """
            cursor.execute(sql)
            
            self.new_conn.commit()
            cursor.close()
            
            logger.info("候选人统计更新完成")
            
        except Exception as e:
            logger.error(f"更新候选人统计失败: {e}")
            raise
    
    def generate_migration_report(self):
        """生成迁移报告"""
        report = {
            'migration_time': datetime.now().isoformat(),
            'statistics': self.migration_stats,
            'errors': self.migration_stats['errors']
        }
        
        with open('migration_report.json', 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)
        
        logger.info("迁移报告已生成: migration_report.json")
    
    def run_migration(self):
        """执行完整迁移"""
        try:
            logger.info("="*50)
            logger.info("开始DZVOTE数据库迁移")
            logger.info("="*50)
            
            if not self.connect_databases():
                sys.exit(1)
            
            # 执行迁移步骤
            self.migrate_activities()
            self.migrate_candidates()
            self.migrate_vote_records()
            self.update_candidate_statistics()
            
            # 生成报告
            self.generate_migration_report()
            
            logger.info("="*50)
            logger.info("数据库迁移完成")
            logger.info(f"迁移统计: {json.dumps(self.migration_stats, ensure_ascii=False)}")
            logger.info("="*50)
            
        except Exception as e:
            logger.error(f"迁移过程中发生错误: {e}")
            sys.exit(1)
        finally:
            self.close_connections()

def main():
    """主函数"""
    migrator = DatabaseMigrator()
    
    print("DZVOTE数据库迁移工具")
    print("="*30)
    print("确认要开始迁移吗？(y/N): ", end='')
    
    if input().lower() != 'y':
        print("迁移已取消")
        return
    
    migrator.run_migration()

if __name__ == '__main__':
    main()