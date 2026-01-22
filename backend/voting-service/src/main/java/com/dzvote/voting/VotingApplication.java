package com.dzvote.voting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 投票服务应用启动类
 */
@SpringBootApplication
@MapperScan("com.dzvote.voting.mapper")
public class VotingApplication {
    public static void main(String[] args) {
        SpringApplication.run(VotingApplication.class, args);
    }
}
