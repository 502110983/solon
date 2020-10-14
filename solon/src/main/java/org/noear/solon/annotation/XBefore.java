package org.noear.solon.annotation;

import org.noear.solon.web.XHandler;
import java.lang.annotation.*;

/**
 * 触发器：前置处理（仅争对 XController 和 XAction 的拦截器）
 *
 * @author noear
 * @since 1.0
 * */
@Inherited //要可继承
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface XBefore {
    Class<? extends XHandler>[] value();
}
