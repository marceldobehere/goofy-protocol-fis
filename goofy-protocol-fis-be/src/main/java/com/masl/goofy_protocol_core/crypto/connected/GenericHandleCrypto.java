package com.masl.goofy_protocol_core.crypto.connected;

import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCrypto;

public interface GenericHandleCrypto {
    // Verification
    boolean verifyKeyAndHandle(String pubSplitKey, String handle);
    boolean verifyKeyAndHandle(AsymmCrypto.AsymmPubKeyPair pubSplitKey, String handle);

    // Keypair to Handle Derivation
    String deriveHandle(String pubSplitKey);
    String deriveHandle(AsymmCrypto.AsymmPubKeyPair pubSplitKey);

    // Handle to Keypair Lookup
    String getPublicSplitKeyFromHandle(String handle);
}
