package com.dzvote.user.mapper;

import com.dzvote.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据用户名查询
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据ID查询
     */
    User selectById(@Param("id") Long id);
    
    /**
     * 更新用户
     */
    int updateById(User user);
}
