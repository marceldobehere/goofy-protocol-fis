package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.dto.request.GeneralReportDto;
import com.masl.goofy_protocol_fis_be.dto.response.GeneralInfoDto;
import com.masl.goofy_protocol_fis_be.properties.GeneralProperties;
import com.masl.goofy_protocol_fis_be.service.GeneralReportService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// TODO: Document API
@RestController
@RequestMapping("/api/general")
public class GeneralEndpoint {
    private final GeneralProperties generalProperties;
    private final GeneralReportService generalReportService;

    public GeneralEndpoint(GeneralProperties generalProperties, GeneralReportService generalReportService) {
        this.generalProperties = generalProperties;
        this.generalReportService = generalReportService;
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
    public void report(@Valid @RequestBody GeneralReportDto report, @AuthenticationPrincipal GoofyAuthUser auth) {
        String optHandle = auth != null ? auth.getHandle() : null;
        generalReportService.submitReport(report, optHandle);
    }
}
