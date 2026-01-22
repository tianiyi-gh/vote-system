-- 为现有活动生成serviceId
UPDATE activities
SET service_id = CONCAT('ACT', LPAD(id, 6, '0'))
WHERE service_id IS NULL OR service_id = '';

-- 为新创建的活动添加触发器自动生成serviceId（可选）
-- CREATE TRIGGER generate_service_id_before_insert
-- BEFORE INSERT ON activities
-- FOR EACH ROW
-- BEGIN
--     IF NEW.service_id IS NULL OR NEW.service_id = '' THEN
--         SET NEW.service_id = CONCAT('ACT', LPAD(NEW.id, 6, '0'));
--     END IF;
-- END;
