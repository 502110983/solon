package org.noear.solon.test;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.core.Aop;
import org.noear.solon.core.util.ThrowableUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SolonJUnit4ClassRunner extends BlockJUnit4ClassRunner {
    public SolonJUnit4ClassRunner(Class<?> klass) throws InitializationError {
        super(klass);

        SolonTest anno = klass.getAnnotation(SolonTest.class);
        String[] debugArgs = new String[]{"-debug=1"};


        if (anno != null) {

            try {
                Method main = getMain(anno);

                if (main != null && Modifier.isStatic(main.getModifiers())) {
                    if (anno.debug()) {
                        main.invoke(null, new Object[]{debugArgs});
                    } else {
                        main.invoke(null, new Object[0]);
                    }
                } else {
                    if (anno.debug()) {
                        Solon.start(anno.value(), debugArgs);
                    } else {
                        Solon.start(anno.value(), new String[]{});
                    }
                }
            } catch (Throwable ex) {
                ThrowableUtil.throwableUnwrap(ex).printStackTrace();
            }


            //延迟秒数
            if (anno.delay() > 0) {
                try {
                    Thread.sleep(anno.delay() * 1000);
                } catch (Exception ex) {

                }
            }
        } else {
            Solon.start(klass, debugArgs);
        }

    }

    private Method getMain(SolonTest anno) {
        try {
            return anno.value().getMethod("main", String[].class);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    protected Object createTest() throws Exception {
        Object tmp = super.createTest();
        Aop.inject(tmp);
        return tmp;
    }
}
