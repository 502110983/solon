package org.noear.solon.extend.validation.annotation;

import org.noear.solon.web.XContext;
import org.noear.solon.web.XResult;
import org.noear.solon.extend.validation.Validator;

/**
 *
 * @author noear
 * @since 1.0
 * */
public class WhitelistValidator implements Validator<Whitelist> {
    public static final WhitelistValidator instance = new WhitelistValidator();

    @Override
    public String message(Whitelist anno) {
        return anno.message();
    }

    @Override
    public XResult validate(XContext ctx, Whitelist anno, String name, StringBuilder tmp) {
        if (WhitelistCheckerImp.global().check(anno, ctx)) {
            return XResult.succeed();
        } else {
            return XResult.failure();
        }
    }
}
