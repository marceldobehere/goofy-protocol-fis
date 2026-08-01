package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(httpStatus = 404, errorCode = AllClientErrorCodes.GENERIC_NOT_FOUND, detailFields = {"id"}, description = "The provided ID does not exist.")
public class GenericNotFound extends BaseClientFisException {
    public GenericNotFound(Long id) {
        super("ID not found: " + id, Map.of("id", id));
    }
}
