package org.noear.solon.boot.smartsocket;

import org.noear.solon.socketx.XMessage;

import org.noear.solon.extend.xsocket.XMessageUtils;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;


/**
 * /date/xxx/sss\n...
 *
 * */
public class AioProtocol implements Protocol<XMessage> {

    @Override
    public XMessage decode(ByteBuffer buffer, AioSession session) {
        if(buffer.remaining() < Integer.BYTES){
            return null;
        }

        buffer.mark();

        XMessage tmp = XMessageUtils.decode(buffer);

        if (tmp == null) {
            buffer.reset();
        }

        return tmp;
    }
}

