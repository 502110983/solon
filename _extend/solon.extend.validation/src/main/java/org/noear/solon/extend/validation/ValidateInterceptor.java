package org.noear.solon.extend.validation;

import org.noear.solon.web.XContext;
import org.noear.solon.web.XHandler;

/**
 *
 * @author noear
 * @since 1.0
 * */
public class ValidateInterceptor implements XHandler {

    @Override
    public void handle(XContext ctx) throws Throwable {
        if (ValidatorManager.global() != null) {
            ValidatorManager.global().handle(ctx);
        }
    }
}