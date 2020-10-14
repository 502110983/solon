package org.noear.solon.extend.xsocket;

import org.noear.solon.XApp;
import org.noear.solon.socket.XListener;
import org.noear.solon.socket.XMessage;
import org.noear.solon.web.XSession;

/**
 * XSocket 监听者代理
 *
 * @author noear
 * @since 1.0
 * */
public class XListenerProxy implements XListener {
    //实例维护
    private static XListener global = new XListenerProxy();
    public static XListener getGlobal() {
        return global;
    }
    public static void setGlobal(XListener global) {
        XListenerProxy.global = global;
    }


    @Override
    public void onOpen(XSession session) {
        XListener sl = get(session);
        if (sl != null) {
            sl.onOpen(session);
        }
    }

    @Override
    public void onMessage(XSession session, XMessage message) {
        XListener sl = get(session);
        if (sl != null) {
            sl.onMessage(session, message);
        }
    }

    @Override
    public void onClose(XSession session) {
        XListener sl = get(session);
        if (sl != null) {
            sl.onClose(session);
        }
    }

    @Override
    public void onError(XSession session, Throwable error) {
        XListener sl = get(session);
        if (sl != null) {
            sl.onError(session, error);
        }
    }

    //获取监听器
    private XListener get(XSession s) {
        return XApp.global().router().matchOne(s);
    }
}
