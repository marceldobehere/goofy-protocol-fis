package com.masl.goofy_protocol_core.crypto.connected.request;

import java.time.Instant;

// TODO: implement basic Validator (using Instant.now() and a Set with a max size)
public interface SignedRequestValidator {
    boolean isUniqueIdValid(long uniqueId);
    boolean isValidUntilValid(Instant validUntil);

    boolean invalidateUniqueId(long uniqueId);
}
