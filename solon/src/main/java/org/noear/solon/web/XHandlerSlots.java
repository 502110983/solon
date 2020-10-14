package org.noear.solon.web;

import org.noear.solon.core.XMethod;

/**
 * 通用处理接口接收槽
 * */
public interface XHandlerSlots {
    default void before(String expr, XMethod method, int index, XHandler handler){}
    default void after(String expr, XMethod method, int index, XHandler handler){};
    void add(String expr, XMethod method, XHandler handler);
}
