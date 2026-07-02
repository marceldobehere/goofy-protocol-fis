package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.dto.response.RegisterStatusDto;
import com.masl.goofy_protocol_fis_be.exception.client.RegistrationNotAllowed;
import com.masl.goofy_protocol_fis_be.properties.RegisterProperties;
import com.masl.goofy_protocol_fis_be.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// TODO: Document API
// TODO: Test
@RestController
@RequestMapping("/api/register")
public class RegistrationEndpoint {
    private final RegisterProperties registerProperties;
    private final RegistrationService registrationService;

    public RegistrationEndpoint(RegisterProperties registerProperties, RegistrationService registrationService) {
        this.registerProperties = registerProperties;
        this.registrationService = registrationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    public String register(@Valid @RequestBody String code, @AuthenticationPrincipal GoofyAuthUser auth) {
        if (!registerProperties.getRegistrationsAllowed())
            throw new RegistrationNotAllowed();

        registrationService.attemptRegistration(code, auth);
        return "Successfully registered user with handle: " + auth.getHandle();
    }

    @GetMapping("/status")
    public RegisterStatusDto registrationsAllowed() {
        return new RegisterStatusDto(
                registerProperties.getRegistrationsAllowed(),
                registerProperties.getCheckMethod()
        );
    }

    // TODO: Rate Limit
    @PostMapping("/request")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    public void requestRegistrationCode(@Valid @RequestBody String requestMessage) {
        if (!registerProperties.getRegistrationsAllowed())
            throw new RegistrationNotAllowed();
        registrationService.requestRegistrationCode(requestMessage);
    }
}
