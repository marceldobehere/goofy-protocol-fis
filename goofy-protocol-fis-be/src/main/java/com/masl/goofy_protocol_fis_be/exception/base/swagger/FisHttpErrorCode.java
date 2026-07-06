package com.masl.goofy_protocol_fis_be.exception.base.swagger;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// This should be added to all FisExceptions to define the errorCode and or httpStatus
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FisHttpErrorCode {
    int httpStatus() default 0;
    int errorCode() default 0;
    String[] detailFields() default {};
}
