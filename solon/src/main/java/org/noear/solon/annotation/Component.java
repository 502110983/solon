package org.noear.solon.annotation;

import java.lang.annotation.*;

/**
 * 通用 组件
 *
 * <pre><code>
 * //注解在类上
 * @Component
 * public class DemoBean{
 *     @Inject
 *     DataSource db1;
 * }
 * </code></pre>
 *
 * @author noear
 * @since 1.2
 * */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    @Note("注册名称")
    String value() default ""; //as bean.name

    @Note("标签，用于快速查找")
    String tag() default "";

    @Note("特性，用于辅助配置")
    String attrs() default "";

    @Note("同时注册类型，仅当名称非空时有效")
    boolean typed() default false;

    boolean remoting() default false; //是否开始远程服务
}
