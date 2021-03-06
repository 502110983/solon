package org.noear.nami;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 执行通道
 * */
public interface NamiChannel {
    /**
     * 设用
     * */
    Result call(NamiConfig cfg, Method method, String action, String url, Map<String, String> headers, Map<String, Object> args) throws Throwable;
}
