package org.noear.solon.core.util;

import org.noear.solon.Utils;
import org.noear.solon.core.JarClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class ResourceUtil {
    /**
     * 获取资源URL集
     *
     * @param name 资源名称
     */
    public static Enumeration<URL> getResources(String name) throws IOException {
        return getResources(JarClassLoader.global(), name); //XUtil.class.getClassLoader().getResources(name);
    }

    public static Enumeration<URL> getResources(ClassLoader classLoader, String name) throws IOException {
        return classLoader.getResources(name); //XUtil.class.getClassLoader().getResources(name);
    }

    /**
     * 获取资源URL
     *
     * @param name 资源名称
     */
    public static URL getResource(String name) {
        return getResource(JarClassLoader.global(), name); //XUtil.class.getResource(name);
    }

    public static URL getResource(ClassLoader classLoader, String name) {
        return classLoader.getResource(name); //XUtil.class.getResource(name);
    }

    /**
     * 获取资源并转为String
     *
     * @param name 资源名称
     * @param charset 编码
     * */
    public static String getResourceAsString(String name, String charset) {
        return getResourceAsString(JarClassLoader.global(), name, charset);
    }

    public static String getResourceAsString(ClassLoader classLoader, String name, String charset) {
        URL url = getResource(classLoader, name);
        if (url != null) {
            try {
                return Utils.getString(url.openStream(), charset);
            } catch (Exception ex) {
                throw ThrowableUtil.throwableWrap(ex);
            }
        } else {
            return null;
        }
    }
}
