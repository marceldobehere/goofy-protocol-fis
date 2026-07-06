package com.masl.goofy_protocol_fis_be.exception.base.swagger;

import java.util.Map;

// This is the internal FisErrorDto used to generate the Swagger Schema
public record FisErrorDto (
        int errorCode,
        String message,
        Map<String,Object> details
) {}
