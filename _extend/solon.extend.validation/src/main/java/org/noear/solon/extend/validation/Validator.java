package org.noear.solon.extend.validation;

import org.noear.solon.web.XContext;
import org.noear.solon.web.XResult;

import java.lang.annotation.Annotation;

/**
 *
 * @author noear
 * @since 1.0
 * */
@FunctionalInterface
public interface Validator<T extends Annotation> {
    default String message(T anno) {
        return "";
    }

    XResult validate(XContext ctx, T anno, String name, StringBuilder tmp);
}
