package org.noear.solon.web;

import org.noear.solon.core.MethodWrap;

/**
 * 函数执行器
 * */
public interface XActionExecutor {
    /**
     * 是否匹配
     *
     * @param ctx 上下文
     * @param ct 内容类型
     * */
    boolean matched(XContext ctx, String ct);

    /**
     * 执行
     *
     * @param ctx 上下文
     * @param obj 控制器
     * @param mWrap 函数包装器
     * */
    Object execute(XContext ctx, Object obj, MethodWrap mWrap) throws Throwable;
}
