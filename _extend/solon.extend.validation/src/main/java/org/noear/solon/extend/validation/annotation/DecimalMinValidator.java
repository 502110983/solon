package org.noear.solon.extend.validation.annotation;

import org.noear.solon.web.XContext;
import org.noear.solon.web.XResult;
import org.noear.solon.extend.validation.StringUtils;
import org.noear.solon.extend.validation.Validator;

/**
 *
 * @author noear
 * @since 1.0
 * */
public class DecimalMinValidator implements Validator<DecimalMin> {
    public static final DecimalMinValidator instance = new DecimalMinValidator();

    @Override
    public String message(DecimalMin anno) {
        return anno.message();
    }

    @Override
    public XResult validate(XContext ctx, DecimalMin anno, String name, StringBuilder tmp) {
        String val = ctx.param(name);

        if (StringUtils.isNumber(val) == false || Double.parseDouble(val) < anno.value()) {
            tmp.append(',').append(name);
        }

        if (tmp.length() > 1) {
            return XResult.failure(tmp.substring(1));
        } else {
            return XResult.succeed();
        }
    }
}
