package org.noear.solon.extend.quartz;

import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.core.Aop;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.util.ThrowableUtil;

import java.util.Properties;

public class XPluginImp implements Plugin {
    @Override
    public void start(SolonApp app) {
        if(app.source().getAnnotation(EnableQuartz.class) == null){
            return;
        }

        try {
            JobManager.init();
        } catch (Exception ex) {
            throw ThrowableUtil.throwableWrap(ex);
        }

        Aop.context().beanBuilderAdd(Quartz.class, (clz, bw, anno) -> {
            String cronx = anno.cron7x();
            String name = anno.name();
            boolean enable = anno.enable();

            if (Utils.isNotEmpty(name)) {
                Properties prop = Solon.cfg().getProp("solon.quartz." + name);

                if (prop.size() > 0) {
                    String cronxTmp = prop.getProperty("cron7x");
                    String enableTmp = prop.getProperty("enable");

                    if ("false".equals(enableTmp)) {
                        enable = false;
                    }

                    if (Utils.isNotEmpty(cronxTmp)) {
                        cronx = cronxTmp;
                    }
                }
            }

            JobManager.doAddBean(name, cronx, enable, bw);
        });

        Aop.context().beanOnloaded(() -> {
            try {
                JobManager.start();
            } catch (Exception ex) {
                throw ThrowableUtil.throwableWrap(ex);
            }
        });
    }

    @Override
    public void stop() throws Throwable {
        JobManager.stop();
    }
}
