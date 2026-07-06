package com.masl.goofy_protocol_fis_be.exception.base.swagger;

import io.swagger.v3.oas.annotations.Operation;

import java.lang.annotation.*;

// This should be added to all endpoints to allow proper swagger documentation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation
public @interface FisEndpoint {
    String summary() default "";
    String description() default "";
}
