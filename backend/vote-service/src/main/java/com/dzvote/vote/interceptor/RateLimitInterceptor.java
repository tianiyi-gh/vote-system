package com.dzvote.vote.interceptor;

import com.dzvote.vote.annotation.IpLimit;
import com.dzvote.vote.annotation.RateLimit;
import com.dzvote.vote.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器
 */
@Aspect
@Component
public class RateLimitInterceptor {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 环绕通知，拦截带有@RateLimit或@IpLimit注解的方法
     */
    @Around("@annotation(com.dzvote.vote.annotation.RateLimit) || " +
            "@annotation(com.dzvote.vote.annotation.IpLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取客户端IP
        String ip = getClientIp(request);

        // 检查@IpLimit注解
        IpLimit ipLimit = method.getAnnotation(IpLimit.class);
        if (ipLimit != null) {
            if (!checkIpLimit(ip, ipLimit)) {
                return Result.error(ipLimit.message());
            }
        }

        // 检查@RateLimit注解
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit != null) {
            if (!checkRateLimit(ip, rateLimit)) {
                return Result.error(rateLimit.message());
            }
        }

        return joinPoint.proceed();
    }

    /**
     * 检查IP限流
     */
    private boolean checkIpLimit(String ip, IpLimit ipLimit) {
        String key = "ip:limit:" + ipLimit.key() + ":" + ip;
        return checkLimit(key, ipLimit.time(), ipLimit.count());
    }

    /**
     * 检查通用限流
     */
    private boolean checkRateLimit(String ip, RateLimit rateLimit) {
        String key = "rate:limit:" + rateLimit.key() + ":" + ip;
        return checkLimit(key, rateLimit.time(), rateLimit.count());
    }

    /**
     * 检查限流逻辑
     */
    private boolean checkLimit(String key, int time, int count) {
        try {
            Long current = redisTemplate.opsForValue().increment(key);

            if (current == 1) {
                // 第一次访问，设置过期时间
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }

            return current <= count;
        } catch (Exception e) {
            // Redis异常时，默认放行
            return true;
        }
    }

    /**
     * 获取请求对象
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }
}
