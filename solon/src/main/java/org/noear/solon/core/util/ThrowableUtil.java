package org.noear.solon.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

public class ThrowableUtil {
    /**
     * 包装异常
     * */
    public static RuntimeException throwableWrap(Throwable ex){
        if(ex instanceof RuntimeException){
            return  (RuntimeException)ex;
        }else {
            return new RuntimeException(ex);
        }
    }

    /**
     * 解包异常
     * */
    public static Throwable throwableUnwrap(Throwable ex) {
        Throwable th = ex;

        while (true) {
            if (th instanceof RuntimeException) {
                if (th.getCause() != null) {
                    th = th.getCause();
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        while (true) {
            if (th instanceof InvocationTargetException) {
                th = ((InvocationTargetException) th).getTargetException();
            } else {
                break;
            }
        }

        return th;
    }

    public static boolean throwableHas(Throwable ex, Class<? extends Throwable> clz) {
        Throwable th = ex;

        while (true) {
            if (clz.isAssignableFrom(th.getClass())) {
                return true;
            }

            if (th.getCause() != null) {
                th = th.getCause();
            } else {
                break;
            }
        }

        while (true) {
            if (clz.isAssignableFrom(th.getClass())) {
                return true;
            }

            if (th instanceof InvocationTargetException) {
                th = ((InvocationTargetException) th).getTargetException();
            } else {
                break;
            }
        }

        return false;
    }

    /**
     * 获取异常的完整内容
     */
    public static String getFullStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }
}
