package org.noear.solon;

import org.noear.solon.core.JarClassLoader;
import org.noear.solon.core.NvMap;
import org.noear.solon.core.util.PrintUtil;
import org.noear.solon.core.util.ThrowableUtil;
import org.noear.solon.ext.ConsumerEx;

/**
 * 应用管理中心
 *
 * <pre><code>
 * public class DemoApp{
 *     public static void main(String[] args){
 *         Solon.start(DemoApp.class, args);
 *     }
 * }
 * </code></pre>
 *
 * @author noear
 * @since 1.0
 * */
public class Solon {
    private static SolonApp global;

    /**
     * 全局实例
     */
    public static SolonApp global() {
        return global;
    }

    /**
     * 应用配置
     * */
    public static SolonProps cfg(){
        return global().cfg();
    }


    /**
     * 启动应用（全局只启动一个），执行序列
     *
     * <p>
     * 1.加载配置（约定：application.properties    为应用配置文件）
     * 2.加载自发现插件（约定：/solonplugin/*.properties 为插件配置文件）
     * 3.加载注解Bean（约定：@XBean,@XController,@XInterceptor 为bean）
     * 4.执行Bean加载事件（采用：注册事件的方式进行安需通知）
     */
    public static SolonApp start(Class<?> source, String[] args) {
        return start(source, args, null);
    }

    public static SolonApp start(Class<?> source, String[] args, ConsumerEx<SolonApp> initialize) {
        //1.初始化应用，加载配置
        NvMap argx = NvMap.from(args);
        return start(source, argx, initialize);
    }

    public static SolonApp start(Class<?> source, NvMap argx, ConsumerEx<SolonApp> initialize) {
        if (global != null) {
            return global;
        }

        //绑定类加载器
        JarClassLoader.bindingThread();

        //添加关闭勾子
        Runtime.getRuntime().addShutdownHook(new Thread(()->stop(false, 0)));

        long time_start = System.currentTimeMillis();
        PrintUtil.blueln("solon.App:: Start loading");

        //1.创建应用
        global = new SolonApp(source, argx);

        //2.1.内部初始化（顺序不能乱!）
        global.init();

        //2.2.自定义初始化
        if (initialize != null) {
            try {
                initialize.accept(global);
            } catch (Throwable ex) {
                throw ThrowableUtil.wrap(ex);
            }
        }

        //3.运行
        global.run();

        long time_end = System.currentTimeMillis();
        PrintUtil.blueln("solon.App:: End loading @" + (time_end - time_start) + "ms");

        return global;
    }

    /**
     * 停止服务（为web方式停止服务提供支持）
     */
    private static boolean _stopped;
    public static void stop() {
        stop(true, 0);
    }

    public static void stop(boolean exit, long delay) {
        if (global == null) {
            return;
        }

        _stopped = true;

        Utils.pools.submit(() -> {
            if (delay > 0) {
                Thread.sleep(delay);
            }

            global.cfg().plugs().forEach(p -> p.stop());
            global = null;

            if (exit) {
                System.exit(0);
            }
            return null;
        });
    }

    public static boolean stopped(){
        return _stopped;
    }
}
