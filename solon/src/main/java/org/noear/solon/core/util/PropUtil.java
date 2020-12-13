package org.noear.solon.core.util;

import org.noear.solon.core.PropsLoader;
import org.noear.solon.core.wrap.ClassWrap;

import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public class PropUtil {

    /**
     * 根据url加载配置集
     * */
    public static Properties loadProperties(URL url) {
        if(url == null){
            return null;
        }

        try {
            return PropsLoader.global().load(url);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 根据txt加载配置集
     * */
    public static Properties buildProperties(String txt) {
        try {
            return PropsLoader.global().build(txt);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 注入属性
     * */
    public static <T> T injectProperties(T bean, Properties propS) {
        ClassWrap.get(bean.getClass()).fill(bean, propS::getProperty, null);
        return bean;
    }



    /**
     * 将 source:Map 数据，绑定到 target:bean
     * */
    public static void bindTo(Map<String,String> source, Object target) {
        bindTo((k) -> source.get(k), target);
    }

    /**
     * 将 source:Properties 数据，绑定到 target:bean
     * */
    public static void bindTo(Properties source, Object target) {
        bindTo((k) -> source.getProperty(k), target);
    }

    /**
     * 将 source:((k)->v) 数据，绑定到 target:bean
     * */
    public static void bindTo(Function<String, String> source, Object target) {
        if (target == null) {
            return;
        }

        ClassWrap.get(target.getClass()).fill(target,source,null);
    }
}
