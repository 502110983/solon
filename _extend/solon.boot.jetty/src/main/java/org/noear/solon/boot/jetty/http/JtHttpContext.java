package org.noear.solon.boot.jetty.http;

import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.core.*;
import org.noear.solon.Utils;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.SessionState;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.core.util.TextUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

public class JtHttpContext extends Context {
    private HttpServletRequest _request;
    private HttpServletResponse _response;
    protected Map<String,List<UploadedFile>> _fileMap;

    public JtHttpContext(HttpServletRequest request, HttpServletResponse response) {
        _request = request;
        _response = response;

        if(sessionState().replaceable() && Solon.global().enableSessionState()){
            sessionStateInit(new SessionState() {
                @Override
                public String sessionId() {
                    return _request.getRequestedSessionId();
                }

                @Override
                public Object sessionGet(String key) {
                    return _request.getSession().getAttribute(key);
                }

                @Override
                public void sessionSet(String key, Object val) {
                    _request.getSession().setAttribute(key,val);
                }
            });
        }


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
        return _request;
    }

    private String _ip;
    @Override
    public String ip() {
        if(_ip == null) {
            _ip = header("X-Forwarded-For");

            if (_ip == null) {
                _ip = _request.getRemoteAddr();
            }
        }

        return _ip;
    }

    @Override
    public String method() {
        return _request.getMethod();
    }

    @Override
    public String protocol() {
        return _request.getProtocol();
    }

    @Override
    public URI uri() {
        if(_uri == null) {
            _uri = URI.create(url());
        }

        return _uri;
    }
    private URI _uri;

    @Override
    public String path() {
        return uri().getPath();
    }

    @Override
    public String url() {
        return _request.getRequestURL().toString();
    }

    @Override
    public long contentLength() {
        return _request.getContentLength();
    }

    @Override
    public String contentType() {
        return _request.getContentType();
    }


    @Override
    public InputStream bodyAsStream() throws IOException {
        return _request.getInputStream();
    }

    @Override
    public String[] paramValues(String key){
        return  _request.getParameterValues(key);
    }

    @Override
    public String param(String key) {
        //要充许为字符串
        //默认不能为null
        return paramMap().get(key);
    }

    @Override
    public String param(String key, String def) {
        String temp = paramMap().get(key); //因为会添加参数，所以必须用这个

        if(TextUtil.isEmpty(temp)){
            return def;
        }else{
            return temp;
        }
    }


    private NvMap _paramMap;
    @Override
    public NvMap paramMap() {
        if (_paramMap == null) {
            _paramMap = new NvMap();

            Enumeration<String> names = _request.getParameterNames();

            while (names.hasMoreElements()) {
                String name = names.nextElement();
                String value = _request.getParameter(name);
                _paramMap.put(name, value);
            }
        }

        return _paramMap;
    }

    private Map<String, List<String>> _paramsMap;
    @Override
    public Map<String, List<String>> paramsMap() {
        if (_paramsMap == null) {
            _paramsMap = new LinkedHashMap<>();

            _request.getParameterMap().forEach((k, v) -> {
                _paramsMap.put(k, Arrays.asList(v));
            });
        }

        return _paramsMap;
    }

    @Override
    public List<UploadedFile> files(String key) throws Exception{
         if (isMultipartFormData()){
             List<UploadedFile> temp = _fileMap.get(key);
             if(temp == null){
                 return new ArrayList<>();
             }else{
                 return temp;
             }
         }  else {
             return new ArrayList<>();
         }
    }

    private NvMap _cookieMap;

    @Override
    public NvMap cookieMap() {
        if (_cookieMap == null) {
            _cookieMap = new NvMap();

            Cookie[] _cookies = _request.getCookies();

            if (_cookies != null) {
                for (Cookie c : _cookies) {
                    _cookieMap.put(c.getName(), c.getValue());
                }
            }
        }

        return _cookieMap;
    }

    @Override
    public NvMap headerMap() {
        if(_headerMap == null) {
            _headerMap = new NvMap();
            Enumeration<String> headers = _request.getHeaderNames();

            while (headers.hasMoreElements()) {
                String key = (String) headers.nextElement();
                String value = _request.getHeader(key);
                _headerMap.put(key, value);
            }
        }

        return _headerMap;
    }
    private NvMap _headerMap;



    //====================================

    @Override
    public Object response() {
        return _response;
    }

    @Override
    public void charset(String charset) {
        _response.setCharacterEncoding(charset);
        this.charset = Charset.forName(charset);
    }

    @Override
    protected void contentTypeDoSet(String contentType) {
        _response.setContentType(contentType);
    }


    @Override
    public OutputStream outputStream() throws IOException {
        return _response.getOutputStream();
    }

    @Override
    public void output(byte[] bytes) {
        try {
            OutputStream out = _response.getOutputStream();
            out.write(bytes);
        }catch (Throwable ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void output(InputStream stream)  {
        try {
            OutputStream out = _response.getOutputStream();

            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = stream.read(buff, 0, 100)) > 0) {
                out.write(buff, 0, rc);
            }
        }catch (Throwable ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void headerSet(String key, String val) {
        _response.setHeader(key,val);
    }

    @Override
    public void headerAdd(String key, String val) {
        _response.addHeader(key,val);
    }

    @Override
    public void cookieSet(String key, String val, String domain, String path, int maxAge) {
        Cookie c = new Cookie(key,val);

        if (TextUtil.isNotEmpty(path)) {
            c.setPath(path);
        }

        c.setMaxAge(maxAge);

        if (TextUtil.isNotEmpty(domain)) {
            c.setDomain(domain);
        }

        _response.addCookie(c);
    }

    @Override
    public void redirect(String url) {
        try {
            _response.sendRedirect(url);
        }catch (Throwable ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void redirect(String url, int code)  {
        statusSet(code);
        _response.setHeader("Location", url);
    }

    @Override
    public int status() {
        return _response.getStatus();
    }

    @Override
    public void statusSet(int status) {
        _response.setStatus(status);
    }

    @Override
    public void flush() throws IOException {
        outputStream().flush();
    }
}
