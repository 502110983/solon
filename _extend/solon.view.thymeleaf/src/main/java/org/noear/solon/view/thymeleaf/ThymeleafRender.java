package org.noear.solon.view.thymeleaf;

import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.core.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.Render;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.ext.SupplierEx;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ThymeleafRender implements Render {
    private static ThymeleafRender _global;

    public static ThymeleafRender global() {
        if (_global == null) {
            _global = new ThymeleafRender();
        }

        return _global;
    }


    private TemplateEngine _engine = new TemplateEngine();

    private Map<String, Object> _sharedVariable = new HashMap<>();

    private String _baseUri = "/WEB-INF/view/";

    public ThymeleafRender() {
        String baseUri = Solon.cfg().get("slon.mvc.view.prefix");

        if (Utils.isEmpty(baseUri) == false) {
            _baseUri = baseUri;
        }

        if (Solon.cfg().isDebugMode()) {
            forDebug();
        } else {
            forRelease();
        }


        try {
            Solon.global().shared().forEach((k, v) -> {
                setSharedVariable(k, v);
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Solon.global().onSharedAdd((k, v) -> {
            setSharedVariable(k, v);
        });
    }

    private void forDebug() {
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
                FileTemplateResolver _loader = new FileTemplateResolver();
                _loader.setPrefix(dir.getAbsolutePath() + File.separatorChar);
                _loader.setTemplateMode(TemplateMode.HTML);
                _loader.setCacheable(false);//必须为false
                _loader.setCharacterEncoding("utf-8");
                _loader.setCacheTTLMs(Long.valueOf(3600000L));

                _engine.setTemplateResolver(_loader);
            } else {
                //如果没有找到文件，则使用发行模式
                //
                forRelease();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void forRelease() {
        ClassLoaderTemplateResolver _loader = new ClassLoaderTemplateResolver(JarClassLoader.global());

        _loader.setPrefix(_baseUri);
        _loader.setTemplateMode(TemplateMode.HTML);
        _loader.setCacheable(true);
        _loader.setCharacterEncoding("utf-8");
        _loader.setCacheTTLMs(Long.valueOf(3600000L));

        _engine.setTemplateResolver(_loader);
    }


    public void setSharedVariable(String name, Object obj) {
        _sharedVariable.put(name, obj);
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

    public void render_mav(ModelAndView mv, Context ctx, SupplierEx<OutputStream> outputStream) throws Throwable {
        if (ctx.contentTypeNew() == null) {
            ctx.contentType("text/html;charset=utf-8");
        }

        if (XPluginImp.output_meta) {
            ctx.headerSet("solon.view", "ThymeleafRender");
        }

        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariables(_sharedVariable);
        context.setVariables(mv);


        PrintWriter writer = new PrintWriter(outputStream.get());
        _engine.process(mv.view(), context, writer);
        writer.flush();
    }
}
