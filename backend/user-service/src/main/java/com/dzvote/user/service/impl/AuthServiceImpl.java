package com.dzvote.user.service.impl;

import com.dzvote.user.dto.LoginRequest;
import com.dzvote.user.dto.LoginResponse;
import com.dzvote.user.entity.User;
import com.dzvote.user.mapper.UserMapper;
import com.dzvote.user.service.AuthService;
import com.dzvote.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 3. 生成Token
        String token = generateToken(user);
        
        // 4. 缓存Token
        redisTemplate.opsForValue().set("token:" + token, user.getId(), 7, TimeUnit.DAYS);
        
        // 5. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 6. 构建响应
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
            user.getId(), user.getUsername(), user.getRealName(), user.getRole()
        );
        
        log.info("用户登录成功: username={}", request.getUsername());
        return new LoginResponse(token, userInfo);
    }
    
    @Override
    public void logout(String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 删除Redis中的Token
        redisTemplate.delete("token:" + token);
        log.info("用户登出成功");
    }
    
    /**
     * 生成JWT Token
     */
    private String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        return JwtUtil.createToken(claims);
    }
}
