package org.noear.solon.boot.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.noear.solon.core.*;
import org.noear.solon.event.XEventBus;
import org.noear.solon.extend.xsocket.XListenerProxy;
import org.noear.solon.extend.xsocket.XSocketContextHandler;
import org.noear.solon.socketx.XListener;
import org.noear.solon.socketx.XMessage;
import org.noear.solon.web.XSession;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unchecked")
public class WsServer extends WebSocketServer {
    private Charset _charset = StandardCharsets.UTF_8;

    private XSocketContextHandler handler;
    private XListener listener;

    public WsServer(int port) {
        super(new InetSocketAddress(port));
        handler = new XSocketContextHandler(XMethod.WEBSOCKET);
        listener = XListenerProxy.getGlobal();
    }

    @Override
    public void onStart() {
        System.out.println("Solon.Server:Websocket onStart...");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake shake) {
        listener.onOpen(_SocketSession.get(conn));
    }

    @Override
    public void onClose(WebSocket conn, int i, String s, boolean b) {
        listener.onClose(_SocketSession.get(conn));

        _SocketSession.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String data) {
        try {
            XSession session = _SocketSession.get(conn);
            XMessage message = XMessage.wrap(conn.getResourceDescriptor(), data.getBytes(_charset));

            listener.onMessage(session, message);

            if (message.getHandled() == false) {
                handler.handle(session, message, true);
            }
        } catch (Throwable ex) {
            XEventBus.push(ex);
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer data) {
        try {
            XSession session = _SocketSession.get(conn);
            XMessage message = XMessage.wrap(conn.getResourceDescriptor(), data.array());

            listener.onMessage(session, message);

            if (message.getHandled() == false) {
                handler.handle(session, message, false);
            }
        } catch (Throwable ex) {
            XEventBus.push(ex);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        listener.onError(_SocketSession.get(conn), ex);
    }
}
