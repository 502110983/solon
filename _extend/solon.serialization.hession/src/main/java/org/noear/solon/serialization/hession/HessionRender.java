package org.noear.solon.serialization.hession;

import com.caucho.hessian.io.Hessian2Output;
import org.noear.solon.web.ModelAndView;
import org.noear.solon.web.XContext;
import org.noear.solon.web.XRender;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

//不要要入参，方便后面多视图混用
//
public class HessionRender implements XRender {

    @Override
    public void render(Object obj, XContext ctx) throws Throwable {
        if (XPluginImp.output_meta) {
            ctx.headerSet("solon.serialization", "HessionRender");
        }

        ctx.contentType("application/hessian");

        if (obj instanceof ModelAndView) {
            ctx.output(serializeDo(new LinkedHashMap((Map) obj)));
        } else {
            ctx.output(serializeDo(obj));
        }
    }

    private byte[] serializeDo(Object obj) throws Throwable {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Hessian2Output ho = new Hessian2Output(out);
        ho.writeObject(obj);
        ho.close();

        return out.toByteArray();
    }
}
