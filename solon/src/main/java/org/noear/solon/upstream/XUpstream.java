package org.noear.solon.upstream;

/**
 * 负载器
 * */
@FunctionalInterface
public interface XUpstream {

    /**
     * 获取节点
     * */
    String getServer();
}
