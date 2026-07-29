package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.INVALID_JSON, detailFields = {"json"}, description = "The provided JSON is invalid and cannot be parsed.")
public class InvalidJson extends BaseClientFisException {
    public InvalidJson(String json) {
        super("Invalid JSON: " + json, Map.of("json", json));
    }
}
