package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(httpStatus = 413, errorCode = AllClientErrorCodes.CONTENT_TOO_LARGE, description = "This means that the content/payload sent to the server is too large.")
public class ContentTooLarge extends BaseClientFisException {
    public ContentTooLarge() {
        super("Content/Payload too large");
    }
}
