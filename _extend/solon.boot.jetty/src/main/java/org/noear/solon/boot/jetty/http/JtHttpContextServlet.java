package org.noear.solon.boot.jetty.http;

import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.boot.jetty.XPluginImp;
import org.noear.solon.boot.jetty.XServerProp;
import org.noear.solon.core.event.EventBus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JtHttpContextServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JtHttpContext context = new JtHttpContext(request, response);
        context.contentType("text/plain;charset=UTF-8");

        if (XServerProp.output_meta) {
            context.headerSet("solon.boot", XPluginImp.solon_boot_ver());
        }

        try {
            Solon.global().handle(context);

            if (context.getHandled() == false || context.status() == 404) {
                response.setStatus(404);
            }
        } catch (Throwable ex) {
            EventBus.push(ex);
            response.setStatus(500);

            if (Solon.cfg().isDebugMode()) {
                ex.printStackTrace(response.getWriter());
            }
        }
    }
}
