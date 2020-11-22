package com.baiwang.moirai.annotation;

import com.baiwang.cloud.logaop.annotation.SystemOperationLog;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@SystemOperationLog()
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserCenterOperationLog {
    /**
     * 业务代码（如果是存储到es会作为_type使用）
     */
    @AliasFor(annotation = SystemOperationLog.class, attribute = "businessCode")
    String businessCode() default "userOperLog";

    /**
     * 操作
     */
    @AliasFor(annotation = SystemOperationLog.class, attribute = "action")
    String action() default "";

    /**
     * 说明
     */
    @AliasFor(annotation = SystemOperationLog.class, attribute = "description")
    String description() default "";

    /**
     * 是否记录入参
     */
    @AliasFor(annotation = SystemOperationLog.class, attribute = "logParam")
    boolean logParam() default true;

    /**
     * 是否记录返回值
     */
    @AliasFor(annotation = SystemOperationLog.class, attribute = "logReturnVal")
    boolean logReturnVal() default true;

    /**
     * 业务模块名称
     */
    @AliasFor(annotation = SystemOperationLog.class, attribute = "moduleName")
    String moduleName() default "";
}