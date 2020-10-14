package org.noear.solon.extend.data;

import org.noear.solon.XApp;
import org.noear.solon.cache.CacheService;
import org.noear.solon.core.*;

public class XPluginImp implements XPlugin {
    @Override
    public void start(XApp app) {
        if (app.enableTransaction()) {
            XBridge.tranExecutorSet(TranExecutorImp.global);
        }

        if (app.enableCaching()) {
            CacheLib.cacheServiceAddIfAbsent("", CacheServiceDefault.instance);

            app.onEvent(BeanWrap.class, new CacheEventListener());
        }

        Aop.context().beanOnloaded(() -> {
            if (Aop.has(CacheService.class) == false) {
                Aop.wrapAndPut(CacheService.class, CacheServiceDefault.instance);
            }
        });
    }
}
