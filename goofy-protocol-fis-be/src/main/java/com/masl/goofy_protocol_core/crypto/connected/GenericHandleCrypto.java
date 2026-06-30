package com.masl.goofy_protocol_core.crypto.connected;

import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCrypto;

public interface GenericHandleCrypto {
    String DEF_DOMAIN_SEPARATOR = "@";

    // Verification
    boolean verifyKeyAndHandle(String pubSplitKey, String handle);
    boolean verifyKeyAndHandle(AsymmCrypto.AsymmPubKeyPair pubSplitKey, String handle);

    // Keypair to Handle Derivation
    String deriveHandle(String pubSplitKey);
    String deriveHandle(AsymmCrypto.AsymmPubKeyPair pubSplitKey);

    // Handle & Domain Separation
    String stripPotentialDomainFromHandle(String handle);
    String getPotentialDomainFromHandle(String handle);

    // Handle to Keypair Lookup
    // NOTE: The handle might have a domain attached (See Spec)
    String getPublicSplitKeyFromHandle(String handle);

    // TODO: Look into Storing Domains for Handles too, additionally Updating them in the Cache / Other Storage, and handling the local ones or ones without a domain(?)
}
