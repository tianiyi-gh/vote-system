package com.dzvote.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String role;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
