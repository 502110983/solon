package org.noear.solon.boot.undertow.websocket;

import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;

public class UtWsConnectionCallback implements WebSocketConnectionCallback {
    UtWsChannelListener listener = new UtWsChannelListener();

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        listener.onConnect(channel);

        channel.getReceiveSetter().set(listener);
    }
}