package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.properties.GeneralProperties;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @FisEndpoint(summary = "Redirects to the Frontend URL (should be static)")
    public ResponseEntity index() {
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .location(URI.create(generalProperties.getFrontendUrl()))
                .build();
    }
}
