package org.noear.solon.view.beetl;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.FileResourceLoader;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.core.JarClassLoader;
import org.noear.solon.core.handle.Render;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.ext.SupplierEx;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URI;

public class BeetlRender implements Render {

    private static BeetlRender _global;

    public static BeetlRender global() {
        if (_global == null) {
            _global = new BeetlRender();
        }

        return _global;
    }


    Configuration cfg = null;
    GroupTemplate gt = null;

    private String _baseUri = "/WEB-INF/view/";

    //不要要入参，方便后面多视图混用
    //
    public BeetlRender() {

        try {
            cfg = Configuration.defaultConfiguration();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }


        String baseUri = Solon.cfg().get("slon.mvc.view.prefix");

        if (Utils.isEmpty(baseUri) == false) {
            _baseUri = baseUri;
        }


        if (Solon.cfg().isDebugMode()) {
            forDebug();
        } else {
            forRelease();
        }

        Solon.global().onSharedAdd((k, v) -> {
            setSharedVariable(k, v);
        });
    }

    private void forDebug() {
        //添加调试模式
        String dirroot = ResourceUtil.getResource("/").toString().replace("target/classes/", "");
        File dir = null;

        if (dirroot.startsWith("file:")) {
            String dir_str = dirroot + "src/main/resources" + _baseUri;
            dir = new File(URI.create(dir_str));
            if (!dir.exists()) {
                dir_str = dirroot + "src/main/webapp" + _baseUri;
                dir = new File(URI.create(dir_str));
            }
        }

        try {
            if (dir != null && dir.exists()) {
                FileResourceLoader loader = new FileResourceLoader(dir.getAbsolutePath(), "utf-8");
                gt = new GroupTemplate(loader, cfg);
            } else {
                forRelease();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void forRelease() {
        try {
            ClasspathResourceLoader loader = new ClasspathResourceLoader(JarClassLoader.global(), _baseUri);
            gt = new GroupTemplate(loader, cfg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void registerTag(String name, Class<?> tag) {
        try {
            gt.registerTag(name, tag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setSharedVariable(String name, Object value) {
        try {
            gt.getSharedVars().put(name, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void render(Object obj, Context ctx) throws Throwable {
        if (obj == null) {
            return;
        }

        if (obj instanceof ModelAndView) {
            render_mav((ModelAndView) obj, ctx, () -> ctx.outputStream());
        } else {
            ctx.output(obj.toString());
        }
    }

    @Override
    public String renderAndReturn(Object obj, Context ctx) throws Throwable {
        if (obj == null) {
            return null;
        }

        if (obj instanceof ModelAndView) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            render_mav((ModelAndView) obj, ctx, () -> outputStream);

            return outputStream.toString();
        } else {
            return obj.toString();
        }
    }

    private void render_mav(ModelAndView mv, Context ctx, SupplierEx<OutputStream> outputStream) throws Throwable {
        if (ctx.contentTypeNew() == null) {
            ctx.contentType("text/html;charset=utf-8");
        }

        if (XPluginImp.output_meta) {
            ctx.headerSet("solon.view", "BeetlRender");
        }

        Template template = gt.getTemplate(mv.view());
        template.binding(mv.model());
        template.renderTo(outputStream.get());
    }
}
