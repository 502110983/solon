package webapp.utils;


import org.noear.nami.NamiChannel;
import org.noear.nami.NamiConfig;
import org.noear.nami.NamiException;
import org.noear.nami.Result;
import org.noear.solon.core.message.Message;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Socket 通道
 * */
public class SocketChannel implements NamiChannel {
    public static final SocketChannel instance = new SocketChannel();

    @Override
    public Result call(NamiConfig cfg, Method method, String action, String url, Map<String, String> headers, Map<String, Object> args) throws Throwable {
        byte[] message;
        switch (cfg.getEncoder().enctype()) {
            case application_json: {
                String json = (String) cfg.getEncoder().encode(args);
                message = json.getBytes("utf-8");
                break;
            }
            case application_hessian: {
                message = (byte[]) cfg.getEncoder().encode(args);
                break;
            }
            default: {
                throw new NamiException("SocketChannel not support encoder: " + cfg.getEncoder().enctype().name());
            }
        }

        synchronized (url.intern()) {
            Message msg = SocketUtils.send(url, message);

            return new Result(msg.getCharset(), msg.body());
        }
    }
}
