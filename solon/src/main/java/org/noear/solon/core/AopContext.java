package org.noear.solon.core;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.annotation.*;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.event.EventListener;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.handle.HandlerLoader;
import org.noear.solon.core.message.Listener;
import org.noear.solon.core.util.PropUtil;
import org.noear.solon.core.util.TextUtil;
import org.noear.solon.core.wrap.ClassWrap;
import org.noear.solon.core.wrap.FieldWrap;
import org.noear.solon.core.wrap.MethodWrap;
import org.noear.solon.core.wrap.VarGather;
import org.noear.solon.ext.BiConsumerEx;
import org.noear.solon.core.util.ResourceScaner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/**
 * Aop 上下文（不直接使用；由 Aop 提供 AopContext 的手动使用模式）
 *
 * 主要实现两个动作：
 * 1.bean 构建
 * 2.bean 注入（字段 或 参数）
 *
 * @author noear
 * @since 1.0
 * */
public class AopContext extends BeanContainer {

    public AopContext() {
        initialize();
    }

    /**
     * ::初始化（独立出 initialize，方便重写）
     */
    protected void initialize() {

        //注册 @Configuration 构建器
        beanBuilderAdd(Configuration.class, (clz, bw, anno) -> {
            Inject typeInj = clz.getAnnotation(Inject.class);
            if (typeInj != null && TextUtil.isNotEmpty(typeInj.value())) {
                if (typeInj.value().startsWith("${")) {
                    PropUtil.injectProperties(bw.raw(), Solon.cfg().getPropByExpr(typeInj.value()));
                }
            }

            for (Method m : ClassWrap.get(bw.clz()).getMethods()) {
                Bean m_an = m.getAnnotation(Bean.class);

                if (m_an != null) {
                    MethodWrap mWrap = MethodWrap.get(m);

                    //有参数的bean，采用线程池处理；所以需要锁等待
                    //
                    tryBuildBean(m_an, mWrap, bw, (p1) -> {
                        Inject tmp = p1.getAnnotation(Inject.class);
                        if (tmp == null) {
                            return null;
                        } else {
                            return tmp.value();
                        }
                    });
                }
            }

            //添加bean形态处理
            addBeanShape(clz, bw);

            //尝试导入
            for (Annotation a1 : clz.getAnnotations()) {
                if (anno instanceof Import) {
                    beanImport((Import) anno);
                } else {
                    beanImport(anno.annotationType().getAnnotation(Import.class));
                }
            }

            //注册到容器 //XConfiguration 不进入二次注册
            //beanRegister(bw,bw.name(),bw.typed());
        });

        //注册 @Component 构建器
        beanBuilderAdd(Component.class, (clz, bw, anno) -> {
            bw.nameSet(anno.value());
            bw.tagSet(anno.tag());
            bw.attrsSet(anno.attrs());
            bw.typedSet(anno.typed());

            //添加bean形态处理
            addBeanShape(clz, bw);

            //设置remoting状态
            bw.remotingSet(anno.remoting());

            //注册到容器
            beanRegister(bw, anno.value(), anno.typed());

            //如果是remoting状态，转到 Solon 路由器
            if (bw.remoting()) {
                HandlerLoader bww = new HandlerLoader(bw);
                if (bww.mapping() != null) {
                    //
                    //如果没有xmapping，则不进行web注册
                    //
                    bww.load(Solon.global());
                }
            }
        });

        //注册 @Controller 构建器
        beanBuilderAdd(Controller.class, (clz, bw, anno) -> {
            new HandlerLoader(bw).load(Solon.global());
        });

        //注册 @Inject 构建器
        beanInjectorAdd(Inject.class, ((fwT, anno) -> {
            beanInject(fwT, anno.value());
        }));

        //注册 @ListenEndpoint 构建器
        beanBuilderAdd(ServerEndpoint.class, (clz, wrap, anno) -> {
            if (Listener.class.isAssignableFrom(clz)) {
                Listener l = wrap.raw();
                Solon.global().router().add(anno.value(), anno.method(), l);
            }
        });
    }

    /**
     * 添加bean的不同形态
     * */
    private void addBeanShape(Class<?> clz, BeanWrap bw) {
        //Plugin
        if (Plugin.class.isAssignableFrom(clz)) {
            //如果是插件，则插入
            Solon.global().plug(bw.raw());
            return;
        }

        //EventListener
        if (EventListener.class.isAssignableFrom(clz)) {
            addEventListener(clz, bw);
            return;
        }

        //LoadBalance.Factory
        if (LoadBalance.Factory.class.isAssignableFrom(clz)) {
            Bridge.upstreamFactorySet(bw.raw());
        }

        //Handler
        if (Handler.class.isAssignableFrom(clz)) {
            Mapping mapping = clz.getAnnotation(Mapping.class);
            if (mapping != null) {
                Handler handler = bw.raw();
                Solon.global().add(mapping,handler);
            }
        }
    }

    //添加事件监听
    private void addEventListener(Class<?> clz, BeanWrap bw) {
        for (Type t1 : clz.getGenericInterfaces()) {
            if (t1 instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t1;
                if (pt.getRawType() == EventListener.class) {
                    Class<?> et = (Class<?>) pt.getActualTypeArguments()[0];
                    EventBus.subscribe(et, bw.raw());
                    return;
                }
            }
        }
    }

    //::注入

    /**
     * 为一个对象注入（可以重写）
     */
    public void beanInject(Object obj) {
        if (obj == null) {
            return;
        }

        ClassWrap clzWrap = ClassWrap.get(obj.getClass());

        //支持父类注入
        for (Map.Entry<String, FieldWrap> kv : clzWrap.getFieldAllWraps().entrySet()) {
            Annotation[] annS = kv.getValue().annoS;
            if (annS.length > 0) {
                VarHolder varH = kv.getValue().holder(obj);
                tryInject(varH, annS);
            }
        }
    }

    ////////////

    /**
     * 根据配置导入bean
     * */
    public void beanImport(Import anno) {
        if (anno != null) {
            for (Class<?> clz : anno.value()) {
                beanMake(clz);
            }

            for (String pkg : anno.scanPackages()) {
                beanScan(pkg);
            }

            for (Class<?> src : anno.scanPackageClasses()) {
                beanScan(src);
            }
        }
    }

    /**
     * ::扫描源下的所有 bean 及对应处理
     */
    public void beanScan(Class<?> source) {
        //确定文件夹名
        if (source.getPackage() != null) {
            beanScan(source.getClassLoader(), source.getPackage().getName());
        }
    }

    /**
     * ::扫描源下的所有 bean 及对应处理
     */
    public void beanScan(String basePackage) {
        beanScan(JarClassLoader.global(), basePackage);
    }

    /**
     * ::扫描源下的所有 bean 及对应处理
     */
    public void beanScan(ClassLoader classLoader, String basePackage) {
        if (TextUtil.isEmpty(basePackage)) {
            return;
        }

        if (classLoader == null) {
            return;
        }

        String dir = basePackage.replace('.', '/');

        //扫描类文件并处理（采用两段式加载，可以部分bean先处理；剩下的为第二段处理）
        ResourceScaner.scan(classLoader, dir, n -> n.endsWith(".class"))
                .stream().sorted(Comparator.comparing(s -> s.length())).forEach(name -> {
            String className = name.substring(0, name.length() - 6);

            Class<?> clz = Utils.loadClass(classLoader, className.replace("/", "."));
            if (clz != null) {
                tryCreateBean(clz);
            }
        });
    }

    /**
     * ::制造当前 bean 及对应处理
     */
    public BeanWrap beanMake(Class<?> clz) {
        //包装
        BeanWrap bw = wrap(clz, null);

        tryCreateBean(bw);

        //尝试入库
        putWrap(clz, bw);

        return bw;
    }


    ////////////////////////////////////////////////////
    //
    //

    /**
     * 尝试为bean注入
     */
    protected void tryInject(VarHolder varH, Annotation[] annS) {
        for (Annotation a : annS) {
            BeanInjector bi = beanInjectors.get(a.annotationType());
            if (bi != null) {
                bi.doInject(varH, a);
            }
        }
    }

    /**
     * 尝试生成 bean
     */
    protected void tryCreateBean(Class<?> clz) {
        tryCreateBean0(clz, (c, a) -> {
            //包装
            BeanWrap bw = this.wrap(clz, null);
            c.doBuild(clz, bw, a);
            //尝试入库
            this.putWrap(clz, bw);
        });
    }

    protected void tryCreateBean(BeanWrap bw) {
        tryCreateBean0(bw.clz(), (c, a) -> {
            c.doBuild(bw.clz(), bw, a);
        });
    }

    protected void tryCreateBean0(Class<?> clz, BiConsumerEx<BeanBuilder, Annotation> consumer) {
        Annotation[] annS = clz.getDeclaredAnnotations();

        if (annS.length > 0) {
            try {
                for (Annotation a : annS) {
                    BeanBuilder creator = beanBuilders.get(a.annotationType());
                    if (creator != null) {
                        consumer.accept(creator, a);
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 尝试构建 bean
     *
     * @param anno      bean 注解
     * @param mWrap     方法包装器
     * @param bw        bean 包装器
     * @param injectVal 参数注入
     */
    protected void tryBuildBean(Bean anno, MethodWrap mWrap, BeanWrap bw, Function<Parameter, String> injectVal) throws Exception {
        int size2 = mWrap.getParameters().length;

        if (size2 == 0) {
            //0.没有参数
            Object raw = mWrap.doIntercept(bw.raw(), new Object[]{});
            tryBuildBean0(mWrap, anno, raw);
        } else {
            //1.构建参数
            VarGather gather = new VarGather(size2, (args2) -> {
                try {
                    //
                    //变量收集完成后，会回调此处
                    //
                    Object raw = mWrap.doIntercept(bw.raw(), args2);
                    tryBuildBean0(mWrap, anno, raw);
                } catch (Throwable ex) {
                    EventBus.push(ex);
                }
            });

            //1.1.添加要收集的参数；并为参数注入（注入是异步的；全部完成后，VarGather 会回调）
            for (Parameter p1 : mWrap.getParameters()) {
                VarHolder p2 = gather.add(p1);
                beanInject(p2, injectVal.apply(p1));
            }
        }
    }

    protected void tryBuildBean0(MethodWrap mWrap, Bean anno, Object raw) {
        if (raw != null) {
            Class<?> beanClz = mWrap.getReturnType();
            Inject beanInj = mWrap.getAnnotation(Inject.class);

            BeanWrap m_bw = null;
            if (raw instanceof BeanWrap) {
                m_bw = (BeanWrap) raw;
            } else {
                if (beanInj != null && TextUtil.isEmpty(beanInj.value()) == false) {
                    if (beanInj.value().startsWith("${")) {
                        PropUtil.injectProperties(raw, Solon.cfg().getPropByExpr(beanInj.value()));
                    }
                }

                //动态构建的bean, 可通过广播进行扩展
                EventBus.push(raw);

                //动态构建的bean，都用新生成wrap（否则会类型混乱）
                m_bw = new BeanWrap(beanClz, raw);
                m_bw.attrsSet(anno.attrs());
            }

            m_bw.nameSet(anno.value());
            m_bw.tagSet(anno.tag());
            m_bw.typedSet(anno.typed());

            beanRegister(m_bw, anno.value(), anno.typed());

            //@XBean 动态产生的 beanWrap（含 name,tag,attrs），进行事件通知
            EventBus.push(m_bw);
        }
    }

    /////////

    //加载完成标志
    private boolean loadDone;
    //加载事件
    private Set<Runnable> loadEvents = new LinkedHashSet<>();

    //::bean事件处理

    /**
     * 添加bean加载完成事件
     */
    @Note("添加bean加载完成事件")
    public void beanOnloaded(Runnable fun) {
        loadEvents.add(fun);

        //如果已加载完成，则直接返回
        if (loadDone) {
            fun.run();
        }
    }

    /**
     * 完成加载时调用，会进行事件通知
     */
    public void beanLoaded() {
        loadDone = true;

        //执行加载事件（不用函数包装，是为了减少代码）
        loadEvents.forEach(f -> f.run());
    }
}
