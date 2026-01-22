package com.dzvote.vote.service.impl;

import com.dzvote.vote.entity.IpBlacklist;
import com.dzvote.vote.mapper.IpBlacklistMapper;
import com.dzvote.vote.service.IpBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IP黑名单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IpBlacklistServiceImpl implements IpBlacklistService {

    private final IpBlacklistMapper ipBlacklistMapper;

    /**
     * 封禁IP
     */
    @Override
    public boolean blockIp(String ipAddress, String reason, String type, Integer hours, String operator, String remark) {
        try {
            // 验证参数
            if (ipAddress == null || ipAddress.isEmpty()) {
                log.warn("IP地址为空");
                return false;
            }

            if (!"PERMANENT".equals(type) && !"TEMPORARY".equals(type)) {
                log.warn("无效的封禁类型: {}", type);
                return false;
            }

            // 检查是否已存在生效中的封禁
            IpBlacklist existing = ipBlacklistMapper.findByIpAddress(ipAddress);
            if (existing != null && existing.getStatus() == 1) {
                log.warn("IP已在黑名单中: {}", ipAddress);
                return false;
            }

            // 创建黑名单记录
            IpBlacklist ipBlacklist = new IpBlacklist();
            ipBlacklist.setIpAddress(ipAddress);
            ipBlacklist.setReason(reason);
            ipBlacklist.setType(type);
            ipBlacklist.setStartTime(LocalDateTime.now());
            ipBlacklist.setOperator(operator);
            ipBlacklist.setRemark(remark);

            if ("TEMPORARY".equals(type) && hours != null && hours > 0) {
                ipBlacklist.setEndTime(LocalDateTime.now().plusHours(hours));
            }

            int result = ipBlacklistMapper.insert(ipBlacklist);
            if (result > 0) {
                log.info("IP封禁成功: IP={}, Type={}, Reason={}, Operator={}", ipAddress, type, reason, operator);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("封禁IP异常", e);
            return false;
        }
    }

    /**
     * 解封IP
     */
    @Override
    public boolean unblockIp(Long id) {
        try {
            int result = ipBlacklistMapper.unblock(id);
            if (result > 0) {
                log.info("IP解封成功: ID={}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("解封IP异常", e);
            return false;
        }
    }

    /**
     * 检查IP是否被封禁
     */
    @Override
    public boolean isBlocked(String ipAddress) {
        try {
            int count = ipBlacklistMapper.isBlocked(ipAddress);
            return count > 0;
        } catch (Exception e) {
            log.error("检查IP封禁状态异常", e);
            return false;
        }
    }

    /**
     * 获取所有黑名单记录
     */
    @Override
    public List<IpBlacklist> getAllBlacklist() {
        try {
            return ipBlacklistMapper.findAll();
        } catch (Exception e) {
            log.error("获取黑名单列表异常", e);
            return null;
        }
    }

    /**
     * 获取生效中的黑名单
     */
    @Override
    public List<IpBlacklist> getActiveBlacklist() {
        try {
            return ipBlacklistMapper.findActive();
        } catch (Exception e) {
            log.error("获取生效中的黑名单异常", e);
            return null;
        }
    }

    /**
     * 删除黑名单记录
     */
    @Override
    public boolean deleteBlacklist(Long id) {
        try {
            int result = ipBlacklistMapper.delete(id);
            if (result > 0) {
                log.info("删除黑名单记录成功: ID={}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除黑名单记录异常", e);
            return false;
        }
    }
}
