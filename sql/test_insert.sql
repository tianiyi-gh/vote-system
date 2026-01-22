-- 测试activities表插入功能
USE dzvote_v2;

INSERT INTO activities (
    title, subtitle, region, description, cover_image,
    start_time, end_time, status, vote_limit, group_count,
    sms_weight, ivr_weight, web_weight, app_weight,
    wechat_weight, pay_weight, ip_limit, enable_captcha,
    show_votes, domain, template, rules, service_id,
    candidate_count, total_votes, created_by, deleted
) VALUES (
    '测试活动',
    '测试副标题',
    '测试区域',
    '这是测试活动描述',
    NULL,
    '2026-01-01 00:00:00',
    '2026-12-31 23:59:59',
    1,
    1,
    1,
    100,
    100,
    100,
    100,
    100,
    100,
    0,
    1,
    1,
    NULL,
    NULL,
    NULL,
    NULL,
    0,
    0,
    'admin',
    0
);

SELECT * FROM activities WHERE title = '测试活动';
