package org.noear.solon.web;

import org.noear.solon.core.ClassWrap;
import org.noear.solon.core.MethodWrap;
import org.noear.solon.core.TypeUtil;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XActionExecutorDefault implements XActionExecutor {
    @Override
    public boolean matched(XContext ctx, String ct) {
        return true;
    }

    @Override
    public Object execute(XContext ctx, Object obj, MethodWrap mWrap) throws Throwable {
        List<Object> args = buildArgs(ctx, mWrap.getParameters());
        return mWrap.invokeByAspect(obj, args.toArray());
    }


    /**
     * 构建执行参数
     */
    protected List<Object> buildArgs(XContext ctx, Parameter[] pSet) throws Exception {
        List<Object> args = new ArrayList<>(pSet.length);

        Object bodyObj = changeBody(ctx);

        //p 参数
        //pt 参数原类型
        for (int i = 0, len = pSet.length; i < len; i++) {
            Parameter p = pSet[i];
            Class<?> pt = p.getType();

            if (XContext.class.isAssignableFrom(pt)) {
                //如果是 XContext 类型，直接加入参数
                //
                args.add(ctx);
            } else if (XFile.class == pt) {
                //如果是文件
                //
                args.add(ctx.file(p.getName()));
            } else {
                Object tv = changeValue(ctx, p, i, pt, bodyObj);

                if (tv == null) {
                    //
                    // 如果是基类类型（int,long...），则抛出异常
                    //
                    if (pt.isPrimitive()) {
                        //如果是基本类型，则为给个默认值
                        //
                        if (pt == short.class) {
                            tv = (short) 0;
                        } else if (pt == int.class) {
                            tv = 0;
                        } else if (pt == long.class) {
                            tv = 0L;
                        } else if (pt == double.class) {
                            tv = 0d;
                        } else if (pt == float.class) {
                            tv = 0f;
                        } else if (pt == boolean.class) {
                            tv = false;
                        } else {
                            //
                            //其它类型不支持
                            //
                            throw new IllegalArgumentException("Please enter a valid parameter @" + p.getName());
                        }
                    }
                }

                args.add(tv);
            }
        }

        return args;
    }

    /**
     * 尝试将body转换为特定对象
     */
    protected Object changeBody(XContext ctx) throws Exception {
        return null;
    }

    /**
     * 尝试将值按类型转换
     */
    protected Object changeValue(XContext ctx, Parameter p, int pi, Class<?> pt, Object bodyObj) throws Exception {
        String pn = p.getName();    //参数名
        String pv = ctx.param(pn);  //参数值
        Object tv = null;


        if (pv == null) {
            //
            // 没有从ctx.param 直接找到值
            //
            if (XFile.class == pt) {
                //1.如果是 XFile 类型
                tv = ctx.file(pn);
            } else {
                //$name 的变量，从attr里找
                if (pn.startsWith("$")) {
                    tv = ctx.attr(pn);
                } else {
                    if (pt.getName().startsWith("java.") || pt.isArray() || pt.isPrimitive()) {
                        //如果是java基础类型，则为null（后面统一地 isPrimitive 做处理）
                        //
                        tv = null;
                    } else {
                        //尝试转为实体
                        tv = changeEntityDo(ctx, pn, pt);
                    }
                }
            }
        } else {
            //如果拿到了具体的参数值，则开始转换
            tv = TypeUtil.changeOfCtx(p, pt, pn, pv, ctx);
        }

        return tv;
    }

    /**
     * 尝试将值转换为实体
     */
    private Object changeEntityDo(XContext ctx, String name, Class<?> type) throws Exception {
        ClassWrap clzW = ClassWrap.get(type);

        Map<String, String> map = ctx.paramMap();
        Object obj = type.newInstance();

        if (map.size() > 0) {
            clzW.fill(obj, map::get, ctx);
        }

        return obj;
    }
}
