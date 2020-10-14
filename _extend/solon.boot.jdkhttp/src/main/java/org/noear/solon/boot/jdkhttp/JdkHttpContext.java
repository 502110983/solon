package org.noear.solon.boot.jdkhttp;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.noear.solon.XUtil;
import org.noear.solon.web.XContext;
import org.noear.solon.web.XFile;
import org.noear.solon.core.XMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

public class JdkHttpContext extends XContext {
    private HttpExchange _exchange;
    private Map<String, Object> _parameters;
    protected Map<String, List<XFile>> _fileMap;

    public JdkHttpContext(HttpExchange exchange) {
        _exchange = exchange;
        _parameters = (Map<String, Object>) _exchange.getAttribute("parameters");

        //文件上传需要
        if (isMultipart()) {
            try {
                _fileMap = new HashMap<>();
                MultipartUtil.buildParamsAndFiles(this);
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public Object request() {
        return _exchange;
    }

    private String _ip;

    @Override
    public String ip() {
        if (_ip == null) {
            _ip = header("X-Forwarded-For");

            if (_ip == null) {
                _ip = _exchange.getRemoteAddress().getAddress().getHostAddress();
            }
        }

        return _ip;
    }

    @Override
    public String method() {
        return _exchange.getRequestMethod();
    }

    @Override
    public String protocol() {
        return _exchange.getProtocol();
    }

    @Override
    public URI uri() {
        return URI.create(url());
    }

    @Override
    public String path() {
        return uri().getPath();
    }

    private String _url;

    @Override
    public String url() {
        if (_url == null) {
            _url = _exchange.getRequestURI().toString();

            if (_url != null && _url.startsWith("/")) {
                String host = header("Host");

                if (host == null) {
                    host = header(":authority");
                    String scheme = header(":scheme");

                    if (host == null) {
                        host = "localhost";
                    }

                    if (scheme != null) {
                        _url = "https://" + host + _url;
                    } else {
                        _url = scheme + "://" + host + _url;
                    }

                } else {
                    _url = "http://" + host + _url;
                }
            }
        }

        return _url;
    }

    @Override
    public long contentLength() {
        try {
            return bodyAsStream().available();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    @Override
    public String contentType() {
        return header("Content-Type");
    }

    @Override
    public InputStream bodyAsStream() throws IOException {
        return _exchange.getRequestBody();
    }

    @Override
    public String[] paramValues(String key) {
        List<String> list = paramsMap().get(key);
        if(list == null){
            return null;
        }

        return list.toArray(new String[list.size()]);
    }

    @Override
    public String param(String key) {
        //要充许为字符串
        //默认不能为null
        return paramMap().get(key);
    }

    @Override
    public String param(String key, String def) {
        try {
            String temp = paramMap().get(key);

            if (XUtil.isEmpty(temp)) {
                return def;
            } else {
                return temp;
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            return def;
        }
    }

    private XMap _paramMap;

    @Override
    public XMap paramMap() {
        if (_paramMap == null) {
            _paramMap = new XMap();

            _parameters.forEach((k, v) -> {
                if (v instanceof List) {
                    _paramMap.put(k, ((List<String>) v).get(0));
                } else {
                    _paramMap.put(k, (String) v);
                }
            });
        }

        return _paramMap;
    }

    private Map<String, List<String>> _paramsMap;
    @Override
    public Map<String, List<String>> paramsMap() {
        if(_paramsMap == null){
            _paramsMap = new LinkedHashMap<>();

            _parameters.forEach((k, v) -> {
                if (v instanceof List) {
                    _paramsMap.put(k, (List<String>)v);
                } else {
                    List<String> list = new ArrayList<>();
                    list.add((String) v);
                    _paramsMap.put(k, list);
                }
            });
        }

        return _paramsMap;
    }

    @Override
    public List<XFile> files(String key) throws Exception {
        if (isMultipartFormData()) {
            List<XFile> temp = _fileMap.get(key);
            if (temp == null) {
                return new ArrayList<>();
            } else {
                return temp;
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public XMap cookieMap() {
        if (_cookieMap == null) {
            _cookieMap = new XMap();

            String tmp = header("Cookie", "");
            String[] ss = tmp.split(";");
            for (String s : ss) {
                String[] kv = s.split("=");
                if (kv.length > 1) {
                    _cookieMap.put(kv[0].trim(), kv[1].trim());
                } else {
                    _cookieMap.put(kv[0].trim(), null);
                }
            }
        }

        return _cookieMap;
    }

    private XMap _cookieMap;

    @Override
    public XMap headerMap() {
        if (_headerMap == null) {
            _headerMap = new XMap();

            Headers headers = _exchange.getRequestHeaders();

            if (headers != null) {
                headers.forEach((k, l) -> {
                    if (l.size() > 0) {
                        _headerMap.put(k, l.get(0));
                    }
                });
            }
        }

        return _headerMap;
    }

    private XMap _headerMap;

    @Override
    public Object response() {
        return _exchange;
    }

    @Override
    protected void contentTypeDoSet(String contentType) {
        if (_charset != null) {
            if (contentType.indexOf(";") < 0) {
                headerSet("Content-Type", contentType + ";charset=" + _charset);
                return;
            }
        }

        headerSet("Content-Type", contentType);
    }

    @Override
    public void output(byte[] bytes){
        try {
            OutputStream out = outputStream();

            if(!_allows_write){
                return;
            }

            out.write(bytes);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void output(InputStream stream) {
        try {
            OutputStream out = outputStream();

            if(!_allows_write){
                return;
            }

            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = stream.read(buff, 0, 100)) > 0) {
                out.write(buff, 0, rc);
            }
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public OutputStream outputStream() throws IOException {
        sendHeaders();

        if (_allows_write) {
            return _exchange.getResponseBody();
        }else{
            return new ByteArrayOutputStream();
        }
    }

    @Override
    public void headerSet(String key, String val) {
        _exchange.getResponseHeaders().set(key, val);
    }

    @Override
    public void headerAdd(String key, String val) {
        _exchange.getResponseHeaders().add(key,val);
    }

    @Override
    public void cookieSet(String key, String val, String domain, String path, int maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(key).append("=").append(val).append(";");

        if (XUtil.isNotEmpty(path)) {
            sb.append("path=").append(path).append(";");
        }

        if (maxAge >= 0) {
            sb.append("max-age=").append(maxAge).append(";");
        }

        if (XUtil.isNotEmpty(domain)) {
            sb.append("domain=").append(domain.toLowerCase()).append(";");
        }

        _exchange.getResponseHeaders().add("Set-Cookie", sb.toString());
    }

    @Override
    public void redirect(String url) {
        redirect(url, 302);
    }

    @Override
    public void redirect(String url, int code) {
        try {
            headerSet("Location", url);
            statusSet(code);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int status() {
        return _status;
    }

    private int _status = 200;

    @Override
    public void statusSet(int status) {
        _status = status;
    }

    @Override
    public void flush() throws IOException {
        sendHeaders();

        if(_allows_write) {
            outputStream().flush();
        }
    }

    @Override
    protected void commit() throws IOException {
        sendHeaders();

        if(!_allows_write){
            outputStream().close();
        }
    }

    private boolean _allows_write = true;
    private boolean _headers_sent = false;
    private void sendHeaders() throws IOException{
        if(!_headers_sent) {
            _headers_sent = true;

            if("HEAD".equals(method())){
                _allows_write = false;
                _exchange.sendResponseHeaders(_status, -1);
            }else {
                _exchange.sendResponseHeaders(_status, 0);
            }
        }
    }
}
