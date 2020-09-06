package org.noear.solon.annotation;

import java.lang.annotation.*;

/**
 * 缓存注解器
 * */
@Inherited //要可继承
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XCache {
    /**
     * 缓存服务
     * */
    @XNote("缓存服务")
    String service() default "";

    /**
     * 0表示采用cache service的默认是境
     * */
    @XNote("缓存时间")
    int seconds() default 0;

    /**
     * 例：user_${user_id} ，user_id 为参数
     * */
    @XNote("缓存标签")
    String tags() default "";

    /**
     * 例：user_${user_id} ，user_id 为参数
     * */
    @XNote("清除缓存标签")
    String clearTags() default "";
}