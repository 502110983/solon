package org.noear.solon.extend.socketd.protocol;

import org.noear.solon.extend.socketd.MessageFlag;
import org.noear.solon.core.message.Message;
import org.noear.solon.extend.socketd.MessageUtils;
import org.noear.solon.extend.socketd.protocol.util.GzipUtil;

import java.nio.ByteBuffer;

/**
 * 安全协议
 *
 * @author noear
 * @since 1.2
 * */
public class MessageProtocolCompress implements MessageProtocol {
    protected MessageProtocol baseProtocol = MessageProtocolBase.instance;

    public MessageProtocolCompress() {

    }

    public MessageProtocolCompress(MessageProtocol baseProtocol) {
        this.baseProtocol = baseProtocol;
    }

    /**
     * 是否充许压缩
     */
    public boolean allowCompress(int byteSize) {
        return (byteSize > 1024);
    }

    /**
     * 压缩
     */
    public byte[] compress(byte[] bytes) throws Exception {
        return GzipUtil.compress(bytes);
    }

    /**
     * 解压
     */
    public byte[] uncompress(byte[] bytes) throws Exception {
        return GzipUtil.uncompress(bytes);
    }

    @Override
    public ByteBuffer encode(Message message) throws Exception {
        ByteBuffer buffer = baseProtocol.encode(message);

        if (allowCompress(buffer.array().length)) {
            byte[] bytes = compress(buffer.array());
            message = MessageUtils.wrapContainer(bytes);

            buffer = baseProtocol.encode(message);
        }

        return buffer;
    }

    @Override
    public Message decode(ByteBuffer buffer) throws Exception {
        Message message = baseProtocol.decode(buffer);

        if (message.flag() == MessageFlag.container) {
            byte[] bytes = uncompress(message.body());
            buffer = ByteBuffer.wrap(bytes);

            message = baseProtocol.decode(buffer);
        }

        return message;
    }
}
