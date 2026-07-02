package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.properties.GeneralProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/")
public class RootEndpoint {
    private final GeneralProperties generalProperties;

    public RootEndpoint(GeneralProperties generalProperties) {
        this.generalProperties = generalProperties;
    }

    @GetMapping
    public ResponseEntity index() {
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .location(URI.create(generalProperties.getFrontendUrl()))
                .build();
    }
}
