package org.noear.solon.extend.validation.annotation;

import org.noear.solon.web.XContext;

/**
 *
 * @author noear
 * @since 1.0
 * */
public interface WhitelistChecker {
    boolean check(Whitelist anno, XContext ctx);
}
