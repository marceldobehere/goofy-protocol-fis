package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Endpoints relating to User Info")
public class UserEndpoint {
    @GetMapping("/test-guest")
    @FisEndpoint(summary = "Test Endpoint for Guests", description = "This endpoint can be accessed by anyone, even without authentication.")
    public String testGuest() {
        return "Hello, Guest";
    }

    @GetMapping("/test-outsider")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Test Endpoint for Outsiders", description = "This endpoint can be accessed by users with the role 'ROLE_OUTSIDE_ENTITY'.")
    public String testOutsider(@AuthenticationPrincipal GoofyAuthUser auth) {
        return "Hello, " + auth.getHandle() + " (Outsider)";
    }

    @GetMapping("/test-user")
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Test Endpoint for Registered Users", description = "This endpoint can be accessed by users with the role 'ROLE_REGISTERED_USER'.")
    public String testUser(@AuthenticationPrincipal GoofyAuthUser auth) {
        return "Hello, " + auth.getHandle() + " (User)";
    }

    @GetMapping("/test-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Test Endpoint for Admins", description = "This endpoint can be accessed by users with the role 'ROLE_ADMIN'.")
    public String testAdmin(@AuthenticationPrincipal GoofyAuthUser auth) {
        return "Hello, " + auth.getHandle() + " (Admin)";
    }
}
