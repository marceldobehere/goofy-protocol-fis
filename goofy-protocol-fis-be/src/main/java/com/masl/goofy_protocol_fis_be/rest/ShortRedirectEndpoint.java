package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.properties.GeneralProperties;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/short")
@Tag(name = "Short Redirects", description = "Endpoints to get Short Frontend URL Redirects")
public class ShortRedirectEndpoint {
    private final GeneralProperties generalProperties;
    private final UserRepository userRepository;

    public ShortRedirectEndpoint(GeneralProperties generalProperties, UserRepository userRepository) {
        this.generalProperties = generalProperties;
        this.userRepository = userRepository;
    }

    @GetMapping("/bucket/{idHandle}/{serviceUuid}/{fileUuid}")
    @FisEndpoint(summary = "Redirects to the Frontend Bucket Viewer for a specific file in a Service Entry. <br> The URL is constructed based on the provided Identity Handle, Service UUID, and File UUID. <br> If the user is authenticated and has a custom frontend URL, it will be used instead of the default frontend URL.")
    public ResponseEntity<String> bucket(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String fileUuid, @AuthenticationPrincipal GoofyAuthUser auth) {
        String frontendUrl = generalProperties.getFrontendUrl();
        if (auth != null) {
            User user = userRepository.findByHandle(auth.getHandle());
            if (user.getCustomFrontendUrl() != null && !user.getCustomFrontendUrl().isBlank())
                frontendUrl = user.getCustomFrontendUrl();
        }


        URI redirectUri = UriComponentsBuilder
                .fromUriString(frontendUrl + "/guest/view")
                .queryParam("tempBackendUrl", generalProperties.getUrl())
                .fragment(idHandle + "@" + serviceUuid + "@" + fileUuid)
                .build(false)
                .toUri();

        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .location(redirectUri)
                .build();
    }
}
