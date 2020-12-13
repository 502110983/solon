package org.noear.solon.boot.jetty.http;


import org.noear.solon.Utils;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.util.TextUtil;

import java.net.URLDecoder;

/**
 * 为jetty DELETE,PATCH进行表单转码
 *
 * @author noear
 * @since 1.0
 * */
public class XFormContentFilter implements Handler {
    @Override
    public void handle(Context ctx) throws Throwable {
        String method = ctx.method();

        if ("DELETE".equals(method) || "PATCH".equals(method) || "PUT".equals(method)) {
            parseBodyTry(ctx);
        }
    }

    private void parseBodyTry(Context ctx) throws Exception {
        String ct = ctx.contentType();

        if (ct == null || ctx.paramMap().size() > 0) {
            return;
        }

        if (ct.startsWith("application/x-www-form-urlencoded") == false) {
            return;
        }

        if (TextUtil.isEmpty(ctx.body())) {
            return;
        }

        String[] ss = ctx.body().split("&");

        for (String s1 : ss) {
            String[] ss2 = s1.split("=");

            if (ss2.length == 2) {
                ctx.paramMap().put(ss2[0], URLDecoder.decode(ss2[1], "utf-8"));
            }
        }
    }
}
