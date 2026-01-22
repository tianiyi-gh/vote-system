import subprocess
import sys

sql = """
INSERT INTO candidates (id, activity_id, name, description, avatar, votes, status) VALUES
(1, 1, '候选人1', '第一个候选人', 'http://example.com/photo1.jpg', 0, 1),
(2, 1, '候选人2', '第二个候选人', 'http://example.com/photo2.jpg', 0, 1);
"""

# 使用 docker exec 执行 SQL
cmd = [
    'docker', 'exec', '-i', 'dzvote-mysql8',
    'mysql', '-u', 'root', '-pmysql8123', 'dzvote_v2'
]

proc = subprocess.run(cmd, input=sql.encode('utf-8'), capture_output=True)
print(proc.stdout.decode('utf-8'))
if proc.stderr:
    print("Error:", proc.stderr.decode('utf-8'))
