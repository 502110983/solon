package org.noear.solon.web;

import org.noear.solon.XUtil;
import org.noear.solon.annotation.*;
import org.noear.solon.core.Aop;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.XMethod;
import org.noear.solon.function.ConsumerEx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Web Bean 包装
 * */
public class BeanWebWrap extends XHandlerAide {
    protected BeanWrap _bw;
    protected XRender _render;
    protected boolean _allowMapping;

    protected boolean _poi_main = true;
    protected XMapping c_map;
    protected String c_path;
    protected boolean c_remoting;

    public BeanWebWrap main(boolean poi_main) {
        _poi_main = poi_main;
        return this;
    }

    public BeanWebWrap(BeanWrap wrap) {
        c_map = wrap.clz().getAnnotation(XMapping.class);

        if (c_map == null) {
            initDo(wrap, null, wrap.remoting(), null, true);
        } else {
            initDo(wrap, c_map.value(), wrap.remoting(), null, true);
        }
    }

    public BeanWebWrap(BeanWrap wrap, String mapping) {
        initDo(wrap, mapping, wrap.remoting(), null, true);
    }

    public BeanWebWrap(BeanWrap wrap, String mapping, boolean remoting) {
        initDo(wrap, mapping, remoting, null, true);
    }

    public BeanWebWrap(BeanWrap wrap, String mapping, boolean remoting, XRender render, boolean allowMapping) {
        initDo(wrap, mapping, remoting, render, allowMapping);
    }

    private void initDo(BeanWrap wrap, String mapping, boolean remoting, XRender render, boolean allowMapping) {
        _bw = wrap;
        _render = render;
        _allowMapping = allowMapping;

        if (mapping != null) {
            c_path = mapping;
        }

        c_remoting = remoting;
    }

    /**
     * mapping expr
     */
    public String mapping() {
        return c_path;
    }

    /**
     * 加载 XAction 到目标容器
     *
     * @param slots 接收加载结果的容器（槽）
     */
    public void load(XHandlerSlots slots) {
        load(c_remoting, slots);
    }

    /**
     * 加载 XAction 到目标容器
     *
     * @param all   加载全部函数（一般 remoting 会全部加载）
     * @param slots 接收加载结果的容器（槽）
     */
    public void load(boolean all, XHandlerSlots slots) {
        if (XHandler.class.isAssignableFrom(_bw.clz())) {
            loadHandlerDo(slots);
        } else {
            loadActionDo(slots, all || c_remoting);
        }
    }

    /**
     * 加载处理
     */
    protected void loadHandlerDo(XHandlerSlots slots) {
        if (c_map == null) {
            throw new RuntimeException(_bw.clz().getName() + " No @XMapping!");
        }

        for (XMethod m1 : c_map.method()) {
            if (_poi_main) {
                slots.add(c_map.value(), m1, _bw.raw());
            } else {
                if (c_map.after()) {
                    slots.after(c_map.value(), m1, c_map.index(), _bw.raw());
                } else {
                    slots.before(c_map.value(), m1, c_map.index(), _bw.raw());
                }
            }
        }
    }


    /**
     * 加载 XAction 处理
     */
    protected void loadActionDo(XHandlerSlots slots, boolean all) {
        String m_path;

        if (c_path == null) {
            c_path = "";
        }

        loadControllerAide();

        XMethod[] m_method;
        XMapping m_map;
        int m_index = 0;

        //只支持public函数为XAction
        for (Method method : _bw.clz().getDeclaredMethods()) {
            m_map = method.getAnnotation(XMapping.class);

            m_index = 0;

            //构建path and method
            if (m_map != null) {
                m_path = m_map.value();
                m_method = m_map.method();
                m_index = m_map.index();
            } else {
                m_path = method.getName();
                if (c_map == null) {
                    m_method = new XMethod[]{XMethod.HTTP};
                } else {
                    m_method = c_map.method();
                }
            }

            //如果是service，method 就不需要map
            if (m_map != null || all) {
                String newPath = XUtil.mergePath(c_path, m_path);

                XAction action = createAction(_bw, _poi_main, method, m_map, newPath, c_remoting);

                loadActionAide(method, action);

                for (XMethod m1 : m_method) {
                    if (_poi_main) {
                        slots.add(newPath, m1, action);
                    } else {
                        if (m_map.after()) {
                            slots.after(newPath, m1, m_index, action);
                        } else {
                            slots.before(newPath, m1, m_index, action);
                        }
                    }
                }
            }
        }
    }


    protected void loadControllerAide() {
        for (Annotation anno : _bw.clz().getAnnotations()) {
            if (anno instanceof XBefore) {
                addDo(((XBefore) anno).value(), (b) -> this.before(Aop.get(b)));
            } else if (anno instanceof XAfter) {
                addDo(((XAfter) anno).value(), (f) -> this.after(Aop.get(f)));
            } else {
                for (Annotation anno2 : anno.annotationType().getAnnotations()) {
                    if (anno2 instanceof XBefore) {
                        addDo(((XBefore) anno2).value(), (b) -> this.before(Aop.get(b)));
                    } else if (anno2 instanceof XAfter) {
                        addDo(((XAfter) anno2).value(), (f) -> this.after(Aop.get(f)));
                    }
                }
            }
        }
    }

    protected void loadActionAide(Method method, XAction action) {
        for (Annotation anno : method.getAnnotations()) {
            if (anno instanceof XBefore) {
                addDo(((XBefore) anno).value(), (b) -> action.before(Aop.get(b)));
            } else if (anno instanceof XAfter) {
                addDo(((XAfter) anno).value(), (f) -> action.after(Aop.get(f)));
            } else {
                for (Annotation anno2 : anno.annotationType().getAnnotations()) {
                    if (anno2 instanceof XBefore) {
                        addDo(((XBefore) anno2).value(), (b) -> action.before(Aop.get(b)));
                    } else if (anno2 instanceof XAfter) {
                        addDo(((XAfter) anno2).value(), (f) -> action.after(Aop.get(f)));
                    }
                }
            }
        }
    }

    /**
     * 构建 XAction
     */
    protected XAction createAction(BeanWrap bw, boolean poi_main, Method method, XMapping mp, String path, boolean remoting) {
        if (_allowMapping) {
            return new XAction(bw, this, poi_main, method, mp, path, remoting, _render);
        } else {
            return new XAction(bw, this, poi_main, method, null, path, remoting, _render);
        }
    }

    /**
     * 附加触发器（前后置处理）
     */
    private static <T> void addDo(T[] ary, ConsumerEx<T> fun) {
        if (ary != null) {
            for (T t : ary) {
                try {
                    fun.accept(t);
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
