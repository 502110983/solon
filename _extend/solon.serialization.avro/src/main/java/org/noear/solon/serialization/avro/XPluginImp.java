package org.noear.solon.serialization.avro;

import org.noear.solon.XApp;
import org.noear.solon.core.XBridge;
import org.noear.solon.core.XPlugin;

public class XPluginImp implements XPlugin {
    public static boolean output_meta = false;

    @Override
    public void start(XApp app) {
        output_meta = app.prop().getInt("solon.output.meta", 0) > 0;

        //XRenderManager.register(render);
        XBridge.renderMapping("@avro", new AvroRender());
    }
}
