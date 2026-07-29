package com.masl.goofy_protocol_fis_be.crypto;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import org.springframework.stereotype.Component;

@Component
public class FisHandleCrypto extends HandleCrypto {
    public FisHandleCrypto(HandleHelper handleHelper) {
        super(handleHelper);
    }
}
