package org.noear.solon.extend.staticfiles;

import org.noear.solon.XApp;
import org.noear.solon.XUtil;
import org.noear.solon.core.*;
import org.noear.solon.web.XContext;
import org.noear.solon.web.XHandler;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.regex.Pattern;

class XResourceHandler implements XHandler {
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String LAST_MODIFIED = "Last-Modified";

    private String _debugBaseUri;
    private String _baseUri;
    private XStaticFiles staticFiles =  XStaticFiles.instance();
    private Pattern _rule;

    public XResourceHandler(String baseUri) {
        _baseUri = baseUri;

        if(XApp.cfg().isDebugMode()){
            String dirroot = XUtil.getResource("/").toString().replace("target/classes/", "");
            if(dirroot.startsWith("file:")) {
                _debugBaseUri = dirroot + "src/main/resources" + _baseUri;
            }
        }

        String expr = "(" + String.join("|", staticFiles.keySet()) + ")$";

        _rule = Pattern.compile(expr, Pattern.CASE_INSENSITIVE);
    }

    public URL getResource(String path) throws Exception{
        if(_debugBaseUri != null){
            URI uri = URI.create(_debugBaseUri+path);
            File file = new File(uri);

            if(file.exists()){
                return uri.toURL();
            }else{
                return null;
            }
        }else{
            return XUtil.getResource(_baseUri + path);
        }
    }

    @Override
    public void handle(XContext context) throws Exception {
        if (context.getHandled()) {
            return;
        }

        if(XMethod.GET.name.equals( context.method()) == false){
            return;
        }

        if(_rule.matcher(context.path()).find()==false){
            return;
        }

        String path = context.path();

        URL uri = getResource(path);

        if (uri == null) {
            return;
        } else {
            context.setHandled(true);

            String modified_since = context.header("If-Modified-Since");
            String modified_now = modified_time.toString();

            if (modified_since != null) {
                if (modified_since.equals(modified_now)) {
                    context.headerSet(CACHE_CONTROL, "max-age=6000");//单位秒
                    context.headerSet(LAST_MODIFIED, modified_now);
                    context.status(304);
                    return;
                }
            }

            int idx = path.lastIndexOf(".");
            if (idx > 0) {
                String suffix = path.substring(idx);
                String mime = staticFiles.get(suffix);

                if (mime != null) {
                    context.headerSet(CACHE_CONTROL, "max-age=6000");
                    context.headerSet(LAST_MODIFIED, modified_time.toString());
                    context.contentType(mime);
                }
            }

            context.status(200);
            context.output(uri.openStream());
        }
    }

    private static final Date modified_time = new Date();
}
