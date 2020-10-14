package org.noear.solon.serialization.fastjson;

import org.noear.solon.XApp;
import org.noear.solon.core.XBridge;
import org.noear.solon.core.XPlugin;

public class XPluginImp implements XPlugin {
    public static boolean output_meta = false;

    @Override
    public void start(XApp app) {
        output_meta = app.prop().getInt("solon.output.meta", 0) > 0;

        //XRenderManager.register(render);
        XBridge.renderMapping("@json", new FastjsonRender(false));
        XBridge.renderMapping("@type_json", new FastjsonRender(true));

        //支持Json内容类型执行
        XBridge.actionExecutorAdd(new FastjsonJsonActionExecutor());
    }
}
