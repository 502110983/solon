package org.noear.solon.extend.validation;

import org.noear.solon.Utils;
import org.noear.solon.annotation.Note;
import org.noear.solon.core.handle.Action;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.handle.Result;
import org.noear.solon.core.util.ThrowableUtil;
import org.noear.solon.extend.validation.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证管理器
 *
 * 只支持XController 和 XAction
 *
 * @author noear
 * @since 1.0
 * */
public class ValidatorManager implements Handler {
    private static ValidatorManager global = new ValidatorManager();

    public static ValidatorManager global() {
        return global;
    }

    public static void globalSet(ValidatorManager global) {
        if (global != null) {
            ValidatorManager.global = global;
        }
    }

    public static void setNoRepeatLock(NoRepeatLock lock){
        NoRepeatLockImp.globalSet(lock);
    }

    public static void setWhitelistChecker(WhitelistChecker checker){
        WhitelistCheckerImp.globalSet(checker);
    }


    protected final Map<Class<? extends Annotation>, Validator> validMap = new HashMap<>();
    protected ValidatorFailureHandler failureHandler;

    public ValidatorManager() {
        failureHandler = new ValidatorFailureHandlerImp();
        initialize();
    }

    public ValidatorManager(ValidatorFailureHandler handler) {
        if (handler == null) {
            this.failureHandler = new ValidatorFailureHandlerImp();
        } else {
            this.failureHandler = handler;
        }

        initialize();
    }

    public void onFailure(ValidatorFailureHandler handler) {
        if (handler != null) {
            this.failureHandler = handler;
        }
    }

    protected void initialize() {
        register(Date.class, DateValidator.instance);

        register(DecimalMax.class, DecimalMaxValidator.instance);
        register(DecimalMin.class, DecimalMinValidator.instance);

        register(Email.class, EmailValidator.instance);

        register(Max.class, MaxValidator.instance);
        register(Min.class, MinValidator.instance);

        register(NoRepeatSubmit.class, NoRepeatSubmitValidator.instance);

        register(NotBlank.class, NotBlankValidator.instance);
        register(NotEmpty.class, NotEmptyValidator.instance);
        register(NotNull.class, NotNullValidator.instance);
        register(NotZero.class, NotZeroValidator.instance);

        register(Null.class, NullValidator.instance);
        register(Numeric.class, NumericValidator.instance);

        register(Pattern.class, PatternValidator.instance);
        register(Length.class, LengthValidator.instance);
        register(Whitelist.class, WhitelistValidator.instance);
    }

    /**
     * 清除所有验证器
     * */
    @Note("清除所有验证器")
    public void clear() {
        validMap.clear();
    }

    /**
     * 移除某个类型的验证器
     * */
    @Note("移除某个类型的验证器")
    public <T extends Annotation> void remove(Class<T> type){
        validMap.remove(type);
    }

    /**
     * 注册验证器
     * */
    @Note("注册验证器")
    public <T extends Annotation> void register(Class<T> type, Validator<T> validator) {
        validMap.put(type, validator);
    }


    @Override
    public void handle(Context ctx) throws Throwable {
        Action action = ctx.action();

        if (action != null) {
            validate(ctx, action);
        }
    }

    protected void validate(Context ctx, Action action) throws Throwable {
        StringBuilder tmp = new StringBuilder();

        for (Annotation anno : action.bean().annotations()) {
            if (validateDo(ctx, anno, null, tmp)) {
                return;
            }
        }

        for (Annotation anno : action.method().getAnnotations()) {
            if (validateDo(ctx, anno, null, tmp)) {
                return;
            }
        }

        for (Parameter para : action.method().getParameters()) {
            for (Annotation anno : para.getAnnotations()) {
                if (validateDo(ctx, anno, para.getName(), tmp)) {
                    return;
                }
            }
        }
    }

    protected boolean validateDo(Context ctx, Annotation anno, String name, StringBuilder tmp){
        if (ctx.getHandled()) {
            return true;
        }

        Validator valid = validMap.get(anno.annotationType());

        if (valid != null) {
            tmp.setLength(0);
            Result rst = valid.validate(ctx, anno, name, tmp);

            if (rst.getCode() != 1) {
                if (this.failureDo(ctx, anno, rst, valid.message(anno))) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean failureDo(Context ctx, Annotation ano, Result result, String message){
        return failureHandler.onFailure(ctx,ano,result,message);
    }

    static class ValidatorFailureHandlerImp implements ValidatorFailureHandler {

        @Override
        public boolean onFailure(Context ctx, Annotation ano, Result rst, String message) {
            ctx.setHandled(true);
            ctx.statusSet(400);
            try {
                if (Utils.isEmpty(message)) {
                    if(Utils.isEmpty(rst.getDescription())){
                        message = new StringBuilder(100)
                                .append("@")
                                .append(ano.annotationType().getSimpleName())
                                .append(" verification failed")
                                .toString();
                    }else{
                        message = new StringBuilder(100)
                                .append("@")
                                .append(ano.annotationType().getSimpleName())
                                .append(" verification failed: ")
                                .append(rst.getDescription())
                                .toString();
                    }
                }

                ctx.render(Result.failure(400, message));
            } catch (Throwable ex) {
                throw ThrowableUtil.wrap(ex);
            }

            return true;
        }
    }
}
