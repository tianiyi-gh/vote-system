package com.dzvote.vote.annotation;

import java.lang.annotation.*;

/**
 * IP限流注解
 * 基于IP的限流，防止同一IP频繁投票
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IpLimit {

    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 限流时间窗口（秒）
     */
    int time() default 3600;

    /**
     * 时间窗口内最大请求数
     */
    int count() default 100;

    /**
     * 限流提示信息
     */
    String message() default "您已经投过票了";
}
