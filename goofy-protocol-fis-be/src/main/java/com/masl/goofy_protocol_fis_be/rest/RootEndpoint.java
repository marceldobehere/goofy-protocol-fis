package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.properties.GeneralProperties;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/")
@Hidden
public class RootEndpoint {
    private final GeneralProperties generalProperties;

    public RootEndpoint(GeneralProperties generalProperties) {
        this.generalProperties = generalProperties;
    }

    @GetMapping
    @FisEndpoint(summary = "Redirects to the Frontend URL (should be static) <br>Also appends the `overrideBackendUrl` automatically, so the Frontend talks to the correct Backend")
    public ResponseEntity<String> index(@RequestHeader(name = "Host", required = false) String host1, @RequestHeader(name = "host", required = false) String host2) {
        System.out.println("NEW ROOT REQUEST for: " + host1 + " - " + host2);

        URI redirectUri = UriComponentsBuilder
                .fromUriString(generalProperties.getFrontendUrl())
                .queryParam("overrideBackendUrl", generalProperties.getUrl())
                .build(false)
                .toUri();

        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .location(redirectUri)
                .build();
    }
}
