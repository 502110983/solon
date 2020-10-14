package org.noear.solon.socket;

import org.noear.solon.web.XSession;

/**
 * XSocket 监听器
 *
 * @author noear
 * @since 1.0
 * */
@FunctionalInterface
public interface XListener {

    default void onOpen(XSession session){}

    void onMessage(XSession session, XMessage message);

    default void onClose(XSession session){}

    default void onError(XSession session, Throwable error){}
}
