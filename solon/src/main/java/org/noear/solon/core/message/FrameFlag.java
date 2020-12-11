package org.noear.solon.core.message;

import org.noear.solon.annotation.Note;

/**
 * 帧标志
 *
 * @author noear
 * @since 1.2
 * */
public interface FrameFlag {
    /**
     * 容器包（用于加密承载）
     * */
    @Note("容器包（用于加密承载）")
    int container = 1;

    /**
     * 消息包
     * */
    @Note("消息包")
    int message = 10;
    /**
     * 心跳消息包
     * */
    @Note("心跳消息包")
    int heartbeat = 11;
    /**
     * 握手消息包
     * */
    @Note("握手消息包")
    int handshake = 12;
    /**
     * 响应体消息包
     * */
    @Note("响应体消息包")
    int response = 13;
}