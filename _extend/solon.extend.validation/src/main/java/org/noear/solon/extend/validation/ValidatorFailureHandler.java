package org.noear.solon.extend.validation;

import org.noear.solon.annotation.XNote;
import org.noear.solon.web.XContext;
import org.noear.solon.web.XResult;

import java.lang.annotation.Annotation;

/**
 * 失败处理者
 *
 * @author noear
 * @since 1.0
 * */
public interface ValidatorFailureHandler {
    /**
     * @return 是否停止后续检查器
     */
    @XNote("@return 是否停止后续检查器")
    boolean onFailure(XContext ctx, Annotation ano, XResult result, String message);
}
