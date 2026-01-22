package com.dzvote.user.service;

import com.dzvote.user.dto.LoginRequest;
import com.dzvote.user.dto.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 用户登出
     */
    void logout(String token);
}
