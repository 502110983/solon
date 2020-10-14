package org.noear.solon.boot.undertow.http;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.handlers.ServletRequestContext;
import org.noear.solon.XApp;
import org.noear.solon.boot.undertow.XPluginImp;
import org.noear.solon.event.XEventBus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * author : Yukai
 * Description : 基础handler
 **/
public class UtHttpHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        ServletRequestContext servletRequestContext = exchange.getAttachment(ServletRequestContext.ATTACHMENT_KEY);
        HttpServletRequest request = (HttpServletRequest) servletRequestContext.getServletRequest();
        HttpServletResponse response = (HttpServletResponse) servletRequestContext.getServletResponse();

        UtHttpContext context = new UtHttpContext(request, response);
        context.contentType("text/plain;charset=UTF-8");
        context.headerSet("solon.boot", XPluginImp.solon_boot_ver());

        try {
            if (exchange.getRequestURI() != null && !exchange.getRequestURI().endsWith(".jsp")) {
                XApp.global().tryHandle(context);
            }

            if (context.getHandled() == false || context.status() == 404) {
                exchange.setStatusCode(404);
            }
        } catch (Throwable ex) {
            XEventBus.push(ex);
            exchange.setStatusCode(500);

            if (XApp.cfg().isDebugMode()) {
                ex.printStackTrace(response.getWriter());
            }
        } finally {
            exchange.endExchange();
        }
    }
}
