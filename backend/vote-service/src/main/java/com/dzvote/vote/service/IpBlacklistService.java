package com.dzvote.vote.service;

import com.dzvote.vote.entity.IpBlacklist;

import java.util.List;

/**
 * IP黑名单服务
 */
public interface IpBlacklistService {

    /**
     * 封禁IP
     * @param ipAddress IP地址
     * @param reason 封禁原因
     * @param type 封禁类型：PERMANENT/TEMPORARY
     * @param hours 封禁时长（仅临时封禁有效，单位：小时）
     * @param operator 操作员
     * @param remark 备注
     * @return 操作结果
     */
    boolean blockIp(String ipAddress, String reason, String type, Integer hours, String operator, String remark);

    /**
     * 解封IP
     * @param id 黑名单记录ID
     * @return 操作结果
     */
    boolean unblockIp(Long id);

    /**
     * 检查IP是否被封禁
     * @param ipAddress IP地址
     * @return true-已封禁 false-未封禁
     */
    boolean isBlocked(String ipAddress);

    /**
     * 获取所有黑名单记录
     * @return 黑名单列表
     */
    List<IpBlacklist> getAllBlacklist();

    /**
     * 获取生效中的黑名单
     * @return 生效中的黑名单列表
     */
    List<IpBlacklist> getActiveBlacklist();

    /**
     * 删除黑名单记录
     * @param id 黑名单记录ID
     * @return 操作结果
     */
    boolean deleteBlacklist(Long id);
}
