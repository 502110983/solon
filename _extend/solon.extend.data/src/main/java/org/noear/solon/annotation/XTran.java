package org.noear.solon.annotation;

import org.noear.solon.transaction.TranIsolation;
import org.noear.solon.transaction.TranPolicy;
import org.noear.solon.extend.data.around.TranInvokeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author noear
 * @since 1.0.20
 * */
@XAround(value = TranInvokeHandler.class, index = -7)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XTran {
    /**
     * 事务传导策略
     * */
    TranPolicy policy() default TranPolicy.required;

    /*
    * 事务隔离等级
    * */
    TranIsolation isolation() default TranIsolation.unspecified;

    /**
     * 只读事务
     * */
    boolean readOnly() default false;
}
