package com.masl.goofy_protocol_fis_be.auth;

import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.Principal;

@NoArgsConstructor
@AllArgsConstructor
public class GoofyAuthUser implements Principal {
    @Getter @Setter
    private String handle;

    @Getter @Setter
    private Boolean user;

    @Getter @Setter
    private Boolean admin;

    @Getter @Setter
    private SignedRequest signedRequest;

    @Override
    public String getName() {
        return handle;
    }
}
