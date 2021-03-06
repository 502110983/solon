package org.noear.solon.extend.aspect;

import org.noear.solon.SolonApp;
import org.noear.solon.core.Aop;
import org.noear.solon.core.Plugin;
import org.noear.solon.extend.aspect.annotation.Dao;
import org.noear.solon.extend.aspect.annotation.Service;

public class XPluginImp implements Plugin {
    @Override
    public void start(SolonApp app) {
        Aop.context().beanBuilderAdd(Dao.class, (clz, bw, anno) -> {
            bw.proxySet(BeanProxyImp.global());

            Aop.context().beanRegister(bw, "", true);
        });

        Aop.context().beanBuilderAdd(Service.class, (clz, bw, anno) -> {
            bw.proxySet(BeanProxyImp.global());

            Aop.context().beanRegister(bw, "", true);
        });
    }
}
