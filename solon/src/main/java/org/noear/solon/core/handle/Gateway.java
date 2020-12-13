package org.noear.solon.core.handle;

import org.noear.solon.Utils;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Note;
import org.noear.solon.core.*;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.PathUtil;
import org.noear.solon.core.util.TextUtil;
import org.noear.solon.ext.RunnableEx;
import org.noear.solon.ext.DataThrowable;

import java.util.HashMap;
import java.util.Map;


/**
 * 本地网关
 * 提供容器，重新组织处理者运行；只支持HASH路由
 *
 * <pre><code>
 * @Mapping("/*")
 * @Controller
 * public class ApiGateway extends Gateway {
 *     @Override
 *     protected void register() {
 *         before(StartHandler.class);   //添加前置拦截器，开始计时+记录请求日志
 *         before(IpHandler.class);      //添加前置拦截器，检查IP白名单
 *
 *         after(EndHandler.class);      //添加后置拦截器，结束计时+记录输出日志+记录接口性能
 *
 *         add(DemoService.class, true); //添加接口
 *     }
 * }
 * </code></pre>
 *
 * @author noear
 * @since 1.0
 * */
public abstract class Gateway extends HandlerAide implements Handler, Render {
    private Handler _def;
    private final Map<String, Handler> _main = new HashMap<>();
    private final String _path;
    private Mapping _mapping;

    public Gateway() {
        super();
        _mapping = this.getClass().getAnnotation(Mapping.class);
        if (_mapping == null) {
            throw new RuntimeException("No XMapping!");
        }

        _path = _mapping.value();

        //默认为404错误输出
        _def = (c) -> c.status(404);

        register();
    }

    /**
     * 注册相关接口与拦截器
     */
    @Note("注册相关接口与拦截器")
    protected abstract void register();

    /**
     * 允许 Action Mapping 申明
     */
    @Note("允许 Action Mapping 申明")
    protected boolean allowActionMapping() {
        return true;
    }

    /**
     * 充许提前准备控制器
     * */
    @Note("充许提前准备控制器")
    protected boolean allowReadyController(){return true;}

    /**
     * 充许路径合并
     * */
    @Note("充许路径合并")
    protected boolean allowPathMerging(){return true;}


    /**
     * for XRender （用于接管 BeanWebWrap 和 XAction 的渲染）
     */
    @Override
    public void render(Object obj, Context c) throws Throwable {
        if (c.getRendered()) {
            return;
        }

        //最多一次渲染
        c.setRendered(true);

        c.result = obj;

        c.render(obj);
    }

    /**
     * for XHandler
     */
    @Override
    public void handle(Context c) throws Throwable {
        Handler m = findDo(c);
        Object obj = null;

        //m 不可能为 null；有 _def 打底
        if (m != null) {
            Boolean is_action = m instanceof Action;
            //预加载控制器，确保所有的处理者可以都可以获取控制器
            if (is_action) {
                if(allowReadyController()) {
                    //提前准备控制器?（通过拦截器产生的参数，需要懒加载）
                    obj = ((Action) m).bean().get();
                    c.attrSet("controller", obj);
                }

                c.attrSet("action", m);
            }

            handle0(c, m, obj, is_action);
        }
    }

    private void handle0(Context c, Handler m, Object obj, Boolean is_action) throws Throwable {
        /**
         * 1.保持与XAction相同的逻辑
         * */

        //前置处理（最多一次渲染）
        handleDo(c, () -> {
            for (Handler h : befores) {
                h.handle(c);
            }
        });

        //主处理（最多一次尝染）
        if (c.getHandled() == false) {
            handleDo(c, () -> {
                if (is_action) {
                    ((Action) m).invoke(c, obj);
                } else {
                    m.handle(c);
                }
            });
        } else {
            render(c.result, c);
        }

        //后置处理（确保不受前面的异常影响）
        for (Handler h : afters) {
            h.handle(c);
        }
    }

    protected void handleDo(Context c, RunnableEx runnable) throws Throwable {
        try {
            runnable.run();
        } catch (DataThrowable ex) {
            c.setHandled(true); //停止处理

            render(ex, c);
        } catch (Throwable ex) {
            c.setHandled(true); //停止处理

            c.attrSet("error", ex);
            render(ex, c);
            EventBus.push(ex);
        }
    }


    /**
     * 添加前置拦截器
     */
    @Note("添加前置拦截器")
    public <T extends Handler> void before(Class<T> interceptorClz) {
        super.before(Aop.get(interceptorClz));
    }


    /**
     * 添加后置拦截器
     */
    @Note("添加后置拦截器")
    public <T extends Handler> void after(Class<T> interceptorClz) {
        super.after(Aop.get(interceptorClz));
    }

    /**
     * 添加接口
     */
    @Note("添加接口")
    public void add(Class<?> beanClz) {
        if (beanClz != null) {
            BeanWrap bw = Aop.wrapAndPut(beanClz);

            add(bw, bw.remoting());
        }
    }

    /**
     * 添加接口（remoting ? 采用@json进行渲染）
     */
    @Note("添加接口")
    public void add(Class<?> beanClz, boolean remoting) {
        if (beanClz != null) {
            add(Aop.wrapAndPut(beanClz), remoting);
        }
    }

    @Note("添加接口")
    public void add(BeanWrap beanWp) {
        add(beanWp, beanWp.remoting());
    }

    /**
     * 添加接口（适用于，从Aop工厂遍历加入；或者把rpc代理包装成bw）
     */
    @Note("添加接口")
    public void add(BeanWrap beanWp, boolean remoting) {
        if (beanWp == null) {
            return;
        }

        HandlerLoader uw = new HandlerLoader(beanWp, _path, remoting, this, allowActionMapping());

        uw.load((path, m, h) -> {
            if (h instanceof Action) {
                Action h2 = (Action) h;

                if (TextUtil.isEmpty(h2.name())) {
                    _def = h2;
                } else {
                    add(h2.name(), h2);
                }
            }
        });
    }

    /**
     * 添加二级路径处理
     */
    @Note("添加二级路径处理")
    public void add(String path, Handler handler) {
        addDo(path, handler);
    }

    protected void addDo(String path, Handler handler) {
        //addPath 已处理 path1= null 的情况
        if(allowPathMerging()) {
            _main.put(PathUtil.mergePath(_path, path).toUpperCase(), handler);
        }else{
            _main.put(PathUtil.mergePath(null, path).toUpperCase(), handler);
        }
    }

    /**
     * 查找接口
     */
    protected Handler findDo(Context c) throws Throwable {
        Handler h = _main.get(c.pathAsUpper());

        if (h == null) {
            _def.handle(c);
            c.setHandled(true);
            return _def;
        } else {
            if (h instanceof Action) {
                c.attrSet("handler_name", ((Action) h).name());
            }
            return h;
        }
    }
}
