package org.noear.solon.boot.jdkhttp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.noear.solon.XApp;
import org.noear.solon.event.XEventBus;

public class JdkHttpContextHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            handleDo(exchange);
        } catch (Throwable ex) {
            //context 初始化时，可能会出错
            //
            XEventBus.push( ex);
        } finally {
            exchange.close();
        }
    }

    private void handleDo(HttpExchange exchange) {
        JdkHttpContext context = new JdkHttpContext(exchange); //这里可能会有异常

        try {
            //初始化好后，再处理；异常时，可以获取上下文
            //
            context.contentType("text/plain;charset=UTF-8");

            if (XServerProp.output_meta) {
                context.headerSet("solon.boot", XPluginImp.solon_boot_ver());
            }

            XApp.global().tryHandle(context);

            if (context.getHandled() && context.status() != 404) {
                context.commit();
            } else {
                context.status(404);
                context.commit();
            }

        } catch (Throwable err) {
            XEventBus.push(err);
        }
    }
}
