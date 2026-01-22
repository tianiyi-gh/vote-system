#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复上传路径配置
"""

import os
import sys

def fix_upload_path():
    """
    检查并修复上传路径配置
    """
    print("=" * 60)
    print("检查上传路径配置")
    print("=" * 60)

    # 上传目录
    upload_dir = r"D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\uploads"

    print(f"\n上传目录: {upload_dir}")
    print(f"是否存在: {os.path.exists(upload_dir)}")

    # 创建必要的子目录
    subdirs = [
        os.path.join(upload_dir, "avatars"),
        os.path.join(upload_dir, "avatars", "2025"),
        os.path.join(upload_dir, "avatars", "2025", "01"),
        os.path.join(upload_dir, "avatars", "2025", "01", "15"),
    ]

    print("\n检查并创建子目录:")
    for subdir in subdirs:
        exists = os.path.exists(subdir)
        print(f"  {subdir} - {'✓ 存在' if exists else '✗ 不存在'}")
        if not exists:
            try:
                os.makedirs(subdir, exist_ok=True)
                print(f"    → 创建成功")
            except Exception as e:
                print(f"    → 创建失败: {e}")

    print("\n" + "=" * 60)
    print("检查完成")
    print("=" * 60)

if __name__ == "__main__":
    fix_upload_path()
