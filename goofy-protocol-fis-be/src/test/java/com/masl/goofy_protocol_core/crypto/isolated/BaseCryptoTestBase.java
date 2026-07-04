package com.masl.goofy_protocol_core.crypto.isolated;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeAll;

import java.security.Security;

public class BaseCryptoTestBase {
    @BeforeAll
    static void init() {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastlePQCProvider());
    }
}
