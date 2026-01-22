package com.dzvote.candidate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 候选人服务应用启动类
 */
@SpringBootApplication
@MapperScan("com.dzvote.candidate.mapper")
public class CandidateApplication {
    public static void main(String[] args) {
        SpringApplication.run(CandidateApplication.class, args);
    }
}
