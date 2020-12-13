package org.noear.solon.core.util;

import org.noear.solon.Utils;
import org.noear.solon.annotation.Param;
import org.noear.solon.core.handle.Context;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * 类型转换工具
 *
 * @author noear
 * @since 1.0
 * */
public class ConvertUtil {
    private static final SimpleDateFormat DATE_DEF_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * 转换 context 的值
     *
     * @param element 目标注解元素
     * @param type 目标类型
     * @param key 变量名
     * @param val 值
     * @param ctx 通用上下文
     * */
    public static Object to(AnnotatedElement element, Class<?> type, String key, String val, Context ctx) throws ClassCastException{
        if (String.class == (type)) {
            return val;
        }

        if (val.length() == 0) {
            return null;
        }

        Object rst = tryTo(type, val);

        if (rst != null) {
            return rst;
        }

        if (Date.class == (type) && element != null) {
            Param xd = element.getAnnotation(Param.class);
            SimpleDateFormat format = null;

            if (xd != null && TextUtil.isEmpty(xd.value()) == false) {
                format = new SimpleDateFormat(xd.value());
            } else {
                format = DATE_DEF_FORMAT;
            }

            try {
                return format.parse(val);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }

        if (type.isArray()) {
            if (ctx == null) {
                return null;
            } else {
                String[] ary = ctx.paramValues(key);
                if (ary == null) {
                    return null;
                }

                int len = ary.length;

                if (is(String[].class, type)) {
                    return ary;
                } else if (is(short[].class, type)) {
                    short[] ary2 = new short[len];
                    for (int i = 0; i < len; i++) {
                        ary2[i] = Short.parseShort(ary[i]);
                    }
                    return ary2;
                } else if (is(int[].class, type)) {
                    int[] ary2 = new int[len];
                    for (int i = 0; i < len; i++) {
                        ary2[i] = Integer.parseInt(ary[i]);
                    }
                    return ary2;
                } else if (is(long[].class, type)) {
                    long[] ary2 = new long[len];
                    for (int i = 0; i < len; i++) {
                        ary2[i] = Long.parseLong(ary[i]);
                    }
                    return ary2;
                } else if (is(float[].class, type)) {
                    float[] ary2 = new float[len];
                    for (int i = 0; i < len; i++) {
                        ary2[i] = Float.parseFloat(ary[i]);
                    }
                    return ary2;
                } else if (is(double[].class, type)) {
                    double[] ary2 = new double[len];
                    for (int i = 0; i < len; i++) {
                        ary2[i] = Double.parseDouble(ary[i]);
                    }
                    return ary2;
                } else if (is(Object[].class, type)) {
                    Class<?> c = type.getComponentType();
                    Object[] ary2 = (Object[]) Array.newInstance(c, len);
                    for (int i = 0; i < len; i++) {
                        ary2[i] = tryTo(c, ary[i]);
                    }
                    return ary2;
                }
            }
        }


        throw new ClassCastException("不支持类型:" + type.getName());
    }

    /**
     * 转换 properties 的值
     *
     * @param type 目标类型
     * @param val 属性值
     * */
    public static Object to(Class<?> type, String val) throws ClassCastException{
        if (String.class == (type)) {
            return val;
        }

        if (val.length() == 0) {
            return null;
        }

        Object rst = tryTo(type, val);

        if (rst != null) {
            return rst;
        }

        if (Date.class == (type)) {
            try {
                return DATE_DEF_FORMAT.parse(val);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }


        throw new ClassCastException("不支持类型:" + type.getName());
    }

    /**
     * 转换 string 值
     *
     * @param type 目标类型
     * @param val 值
     * */
    public static Object tryTo(Class<?> type, String val) {
        if (Short.class == type || type == Short.TYPE) {
            return Short.parseShort(val);
        }

        if (Integer.class == type || type == Integer.TYPE) {
            return Integer.parseInt(val);
        }

        if (Long.class == type || type == Long.TYPE) {
            return Long.parseLong(val);
        }

        if (Double.class == type || type == Double.TYPE) {
            return Double.parseDouble(val);
        }

        if (Float.class == type || type == Float.TYPE) {
            return Float.parseFloat(val);
        }

        if (Boolean.class == type || type == Boolean.TYPE) {
            return Boolean.parseBoolean(val);
        }

        if (LocalDate.class == type) {
            //as "2007-12-03", not null
            return LocalDate.parse(val);
        }

        if (LocalTime.class == type) {
            //as "10:15:30", not null
            return LocalTime.parse(val);
        }

        if (LocalDateTime.class == type) {
            //as "2007-12-03T10:15:30", not null
            return LocalDateTime.parse(val);
        }

        if (BigDecimal.class == type) {
            return new BigDecimal(val);
        }

        if (BigInteger.class == type) {
            return new BigInteger(val);
        }

        return null;
    }

    /**
     * 检测类型是否相同
     *
     * @param s 源类型
     * @param t 目标类型
     * */
    private static boolean is(Class<?> s, Class<?> t){
        return s.isAssignableFrom(t);
    }

}
