package org.noear.solon.core.handle;

import org.noear.solon.Utils;
import org.noear.solon.core.util.PrintUtil;
import org.noear.solon.core.util.ThrowableUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过 Render 管理员，以此实现多模板引擎处理
 *
 * @author noear
 * @since 1.0
 * */
public class RenderManager implements Render {

    private static final Map<String, Render> _mapping = new HashMap<>();
    private static final Map<String, Render> _lib = new HashMap<>();


    //默认渲染器
    private static Render _def = (d, c) -> {
        if (d != null) {
            c.output(d.toString());
        }
    };

    private RenderManager() {
    }

    //不能放上面
    public static Render global = new RenderManager();


    /**
     * 登记渲染器
     *
     * @param render 渲染器
     */
    public static void register(Render render) {
        _def = render;
        _lib.put(render.getClass().getSimpleName(), render);
        _lib.put(render.getClass().getName(), render);

        PrintUtil.blueln("solon:: view load:" + render.getClass().getSimpleName());
        PrintUtil.blueln("solon:: view load:" + render.getClass().getName());
    }

    /**
     * 印射后缀和渲染器的关系
     *
     * @param suffix 后缀（例：.ftl）
     * @param render 渲染器
     */
    public static void mapping(String suffix, Render render) {
        //suffix=.ftl
        _mapping.put(suffix, render);

        PrintUtil.blueln("solon:: view mapping: " + suffix + "=" + render.getClass().getSimpleName());
    }

    /**
     * 印射后缀和渲染器的关系
     *
     * @param suffix 后缀（例：.ftl）
     * @param clzName  渲染器类名
     */
    public static void mapping(String suffix, String clzName) {
        Render render = _lib.get(clzName);
        if (render == null) {
            PrintUtil.redln("solon:: " + clzName + " not exists!");
            return;
            //throw new RuntimeException(classSimpleName + " not exists!");
        }

        _mapping.put(suffix, render);

        PrintUtil.blueln("solon:: view mapping: " + suffix + "=" + clzName);
    }

    /**
     * 渲染并返回
     * */
    public static String renderAndReturn(ModelAndView modelAndView) {
        try {
            return global.renderAndReturn(modelAndView, Context.current());
        } catch (Throwable e) {
            throw ThrowableUtil.wrap(e);
        }
    }

    /**
     * 渲染并返回
     * */
    @Override
    public String renderAndReturn(Object data, Context ctx) throws Throwable {
        if (data instanceof ModelAndView) {
            ModelAndView mv = (ModelAndView) data;

            if (Utils.isNotEmpty(mv.view())) {
                //
                //如果有视图
                //
                int suffix_idx = mv.view().lastIndexOf(".");
                if (suffix_idx > 0) {
                    String suffix = mv.view().substring(suffix_idx);
                    Render render = _mapping.get(suffix);

                    if (render != null) {
                        //如果找到对应的渲染器
                        //
                        return render.renderAndReturn(mv, ctx);
                    }
                }

                //如果没有则用默认渲染器
                //
                return _def.renderAndReturn(mv, ctx);
            }

        }

        throw new IllegalArgumentException("RenderAndReturn: Only support ModelAndView data");
    }

    /**
     * 渲染
     *
     * @param data 数据
     * @param ctx 上下文
     */
    @Override
    public void render(Object data, Context ctx) throws Throwable {
        if (data instanceof ModelAndView) {
            ModelAndView mv = (ModelAndView) data;

            if (Utils.isEmpty(mv.view()) == false) {
                //
                //如果有视图
                //
                int suffix_idx = mv.view().lastIndexOf(".");
                if (suffix_idx > 0) {
                    String suffix = mv.view().substring(suffix_idx);
                    Render render = _mapping.get(suffix);

                    if (render != null) {
                        //如果找到对应的渲染器
                        //
                        render.render(mv, ctx);
                        return;
                    }
                }

                //如果没有则用默认渲染器
                //
                _def.render(mv, ctx);
                return;
            }
        }

        //@json
        //@type_json
        //@xml
        //@protobuf
        //
        //
        Render render = null;
        String mode = ctx.header("X-Serialization");

        if(Utils.isEmpty(mode)){
            mode = ctx.attr("@render");
        }

        if (Utils.isEmpty(mode) == false) {
            render = _mapping.get(mode);

            if (render == null) {
                ctx.headerSet("Solon.serialization.mode", "Not supported " + mode);
            }
        }

        if (render == null) {
            if (ctx.remoting()) {
                render = _mapping.get("@type_json");
            }
        }

        if (render == null) {
            render = _mapping.get("@json");
        }

        if (render != null) {
            render.render(data, ctx);
        } else {
            //最后只有 def
            //
            _def.render(data, ctx);
        }
    }
}
