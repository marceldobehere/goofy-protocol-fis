package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.dto.request.GeneralReportDto;
import com.masl.goofy_protocol_fis_be.dto.response.GeneralInfoDto;
import com.masl.goofy_protocol_fis_be.properties.GeneralProperties;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

// TODO: Document API
@RestController
@RequestMapping("/api/general")
public class GeneralEndpoint {
    private static final Logger log = LoggerFactory.getLogger(GeneralEndpoint.class);
    private final GeneralProperties generalProperties;

    public GeneralEndpoint(GeneralProperties generalProperties) {
        this.generalProperties = generalProperties;
    }

    @GetMapping("/status")
    public String status() {
        return "FIS Backend is running!";
    }

    @GetMapping("/info")
    public GeneralInfoDto info() {
        return new GeneralInfoDto(
                generalProperties.getFrontendUrl(),
                generalProperties.getUrl(),
                generalProperties.getName(),
                generalProperties.getDescription(),
                generalProperties.getVersion(),

                // TODO: Implement Repository/File & Service for FIS Identity & Load here
                "<PUB_KEY>",
                "<HANDLE>"
        );
    }

    @GetMapping("/contact")
    public String contact() {
        return generalProperties.getContact();
    }

    // TODO: Rate Limit
    @PostMapping("/report")
    public void report(@Valid @RequestBody GeneralReportDto report) {
        log.info("Received Report: {}", report);
        // TODO: Implement
        // TODO: Check how Invalid Requests get treated, possibly add Handler to GlobalExceptionHandler
    }
}
