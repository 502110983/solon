package org.noear.solon.boot.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.noear.solon.core.message.Message;
import org.noear.solon.core.message.Session;
import org.noear.solon.extend.socketd.ListenerProxy;

@ChannelHandler.Sharable
public class NioServerProcessor extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Session session = _SocketSession.get(ctx.channel());
        ListenerProxy.getGlobal().onMessage(session, msg, false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        Session session = _SocketSession.get(ctx.channel());
        ListenerProxy.getGlobal().onOpen(session);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        Session session = _SocketSession.get(ctx.channel());
        ListenerProxy.getGlobal().onClose(session);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Session session = _SocketSession.get(ctx.channel());
        ListenerProxy.getGlobal().onError(session, cause);

        //cause.printStackTrace();
        ctx.close();
    }
}