package org.noear.solon.extend.sessionstate.local;

import org.noear.solon.Utils;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.SessionState;
import org.noear.solon.core.util.TextUtil;


/**
 * 它会是个单例，不能有上下文数据
 * */
public class LocalSessionState implements SessionState {
    public final static String SESSIONID_KEY = "SOLONID";

    public final static String SESSIONID_MD5() {
        return SESSIONID_KEY + "2";
    }

    public final static String SESSIONID_encrypt = "&L8e!@T0";

    private final ScheduledStore _store;

    private LocalSessionState() {
        if (XServerProp.session_timeout > 0) {
            _expiry = XServerProp.session_timeout;
        }

        if (XServerProp.session_state_domain != null) {
            _domain = XServerProp.session_state_domain;
        }

        _store = new ScheduledStore(_expiry);

    }

    public static LocalSessionState create(){
        return new LocalSessionState();
    }

    //
    // cookies control
    //
    private int _expiry = 60 * 60 * 2;
    private String _domain = null;

    public String cookieGet(String key) {
        return Context.current().cookie(key);
    }

    public  void   cookieSet(String key, String val) {
        Context ctx = Context.current();

        if (XServerProp.session_state_domain_auto) {
            if (_domain != null) {
                if(ctx.uri().getHost().indexOf(_domain) < 0){ //非安全域
                    ctx.cookieSet(key, val, null, _expiry);
                    return;
                }
            }
        }

        ctx.cookieSet(key, val, _domain, _expiry);
    }



    //
    // session control
    //


    @Override
    public boolean replaceable() {
        return false;
    }

    @Override
    public String sessionId() {
        String _sessionId = Context.current().attr("sessionId", null);

        if (_sessionId == null) {
            _sessionId = sessionId_get();
            Context.current().attrSet("sessionId", _sessionId);
        }

        return _sessionId;
    }

    private String sessionId_get() {
        String skey = cookieGet(SESSIONID_KEY);
        String smd5 = cookieGet(SESSIONID_MD5());

        if (TextUtil.isEmpty(skey) == false && TextUtil.isEmpty(smd5) == false) {
            if (EncryptUtil.md5(skey + SESSIONID_encrypt).equals(smd5)) {
                return skey;
            }
        }

        skey = IDUtil.guid();
        cookieSet(SESSIONID_KEY, skey);
        cookieSet(SESSIONID_MD5(), EncryptUtil.md5(skey + SESSIONID_encrypt));
        return skey;
    }

    @Override
    public Object sessionGet(String key) {
        return _store.get(sessionId(), key);
    }

    @Override
    public void sessionSet(String key, Object val) {
        _store.put(sessionId(), key, val);
    }

    @Override
    public void sessionClear() {
        _store.remove(sessionId());
    }

    @Override
    public void sessionRefresh() {
        String skey = cookieGet(SESSIONID_KEY);

        if (TextUtil.isEmpty(skey) == false) {
            cookieSet(SESSIONID_KEY, skey);
            cookieSet(SESSIONID_MD5(), EncryptUtil.md5(skey + SESSIONID_encrypt));

            _store.delay(sessionId());
        }
    }

    public static final int SESSION_STATE_PRIORITY = 1;
    @Override
    public int priority() {
        return SESSION_STATE_PRIORITY;
    }
}
