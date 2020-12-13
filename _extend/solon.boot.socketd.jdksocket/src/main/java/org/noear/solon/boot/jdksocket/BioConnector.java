package org.noear.solon.boot.jdksocket;

import org.noear.solon.Utils;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.message.Message;
import org.noear.solon.core.message.Session;
import org.noear.solon.core.util.ThrowableUtil;
import org.noear.solon.extend.socketd.ListenerProxy;
import org.noear.solon.extend.socketd.Connector;

import java.net.Socket;
import java.net.URI;

class BioConnector implements Connector<Socket> {
    URI uri;

    public BioConnector(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public Socket open(Session session) {
        try {
            Socket socket = new Socket(uri.getHost(), uri.getPort());

            startReceive(session, socket);

            return socket;
        } catch (Exception ex) {
            throw ThrowableUtil.throwableWrap(ex);
        }
    }

    void startReceive(Session session, Socket socket) {
        Utils.pools.submit(() -> {
            while (true) {
                Message message = BioReceiver.receive(socket);

                if (message != null) {
                    try {
                        ListenerProxy.getGlobal().onMessage(session, message, false);
                    } catch (Throwable ex) {
                        EventBus.push(ex);
                    }
                } else {
                    break;
                }
            }
        });
    }
}
