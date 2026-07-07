package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.dto.request.RegistrationRequestDto;
import com.masl.goofy_protocol_fis_be.dto.response.RegisterStatusDto;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.HandleAlreadyRegistered;
import com.masl.goofy_protocol_fis_be.exception.client.InvalidRegisterCode;
import com.masl.goofy_protocol_fis_be.exception.client.RegistrationCodeAlreadyUsed;
import com.masl.goofy_protocol_fis_be.exception.client.RegistrationNotAllowed;
import com.masl.goofy_protocol_fis_be.properties.RegisterProperties;
import com.masl.goofy_protocol_fis_be.service.RegistrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
@Tag(name = "Registration", description = "Endpoints relating to Registration of Users")
public class RegistrationEndpoint {
    private final RegisterProperties registerProperties;
    private final RegistrationService registrationService;

    public RegistrationEndpoint(RegisterProperties registerProperties, RegistrationService registrationService) {
        this.registerProperties = registerProperties;
        this.registrationService = registrationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Attempt Registration", description = "To register, a registration code is required.")
    public String register(@Valid @RequestBody String code, @AuthenticationPrincipal GoofyAuthUser auth) throws RegistrationNotAllowed, InvalidRegisterCode, HandleAlreadyRegistered, RegistrationCodeAlreadyUsed {
        if (!registerProperties.getRegistrationsAllowed())
            throw new RegistrationNotAllowed();

        registrationService.attemptRegistration(code, auth);
        return "Successfully registered user with handle: " + auth.getHandle();
    }

    @GetMapping("/status")
    @FisEndpoint(summary = "Get Registration Status")
    public RegisterStatusDto registrationsAllowed() {
        return new RegisterStatusDto(
                registerProperties.getRegistrationsAllowed(),
                registerProperties.getCheckMethod()
        );
    }

    @GetMapping("/valid")
    @FisEndpoint(summary = "Check if a Registration Code is Valid")
    public boolean isRegistrationCodeValid(@RequestParam String code) {
        return registrationService.isCodeValid(code);
    }

    // TODO: Rate Limit
    @PostMapping("/request")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Request a Registration Code")
    public void requestRegistrationCode(@Valid @RequestBody RegistrationRequestDto requestDto) throws RegistrationNotAllowed {
        if (!registerProperties.getRegistrationsAllowed())
            throw new RegistrationNotAllowed();
        registrationService.submitRegistrationRequest(requestDto);
    }
}
