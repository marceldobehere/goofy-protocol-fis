package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.INVALID_PUBLIC_KEY_HANDLE_MAPPING, detailFields={"handle", "pubSplitKey"}, description = "This means that a Public Key does not correctly map to the provided handle.")
public class NotMatchingPublicKey extends BaseClientFisException {
    public NotMatchingPublicKey(String handle, String pubSplitKey) {
        super("Invalid public key mapping for: " + handle, Map.of("handle", handle, "pubSplitKey", pubSplitKey));
    }
}
