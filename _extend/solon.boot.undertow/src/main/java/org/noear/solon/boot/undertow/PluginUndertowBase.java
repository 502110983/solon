package org.noear.solon.boot.undertow;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.util.DefaultClassIntrospector;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.boot.undertow.http.UtContainerInitializerProxy;
import org.noear.solon.core.util.ResourceUtil;

import javax.servlet.MultipartConfigElement;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

abstract class PluginUndertowBase {
    protected DeploymentInfo initDeploymentInfo() {
        MultipartConfigElement configElement = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));

        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(XPluginImp.class.getClassLoader())
                .setDeploymentName("solon")
                .setContextPath("/")
                .setDefaultEncoding(XServerProp.encoding_request)
                .setDefaultMultipartConfig(configElement)
                .setClassIntrospecter(DefaultClassIntrospector.INSTANCE);

        //添加容器初始器
        builder.addServletContainerInitializer(UtContainerInitializerProxy.info());
        builder.setEagerFilterInit(true);

        if (XServerProp.session_timeout > 0) {
            builder.setDefaultSessionTimeout(XServerProp.session_timeout);
        }

        return builder;
    }

    protected String getResourceRoot() throws FileNotFoundException {
        URL rootURL = getRootPath();
        if (rootURL == null) {
            throw new FileNotFoundException("Unable to find root");
        }
        String resURL = rootURL.toString();

        boolean isDebug = Solon.cfg().isDebugMode();
        if (isDebug && (resURL.startsWith("jar:") == false)) {
            int endIndex = resURL.indexOf("target");
            return resURL.substring(0, endIndex) + "src/main/resources/";
        }

        return "";
    }

    protected URL getRootPath() {
        URL root = ResourceUtil.getResource("/");
        if (root != null) {
            return root;
        }
        try {
            String path = ResourceUtil.getResource("").toString();
            if (path.startsWith("jar:")) {
                int endIndex = path.indexOf("!");
                path = path.substring(0, endIndex + 1) + "/";
            } else {
                return null;
            }
            return new URL(path);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
