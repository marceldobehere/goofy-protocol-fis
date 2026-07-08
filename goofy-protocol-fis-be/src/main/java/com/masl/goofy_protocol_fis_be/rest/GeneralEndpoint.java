package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCryptoType;
import com.masl.goofy_protocol_core.crypto.isolated.symm.SymmCryptoType;
import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.dto.request.GeneralReportDto;
import com.masl.goofy_protocol_fis_be.dto.response.GeneralInfoDto;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.properties.GeneralProperties;
import com.masl.goofy_protocol_fis_be.service.GeneralReportService;
import com.masl.goofy_protocol_fis_be.test_data.test_dev_prod.TestDataKeypair;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/general")
@Tag(name = "General", description = "General Endpoints regarding the FIS")
public class GeneralEndpoint {
    private final GeneralProperties generalProperties;
    private final GeneralReportService generalReportService;
    private final TestDataKeypair testDataKeypair;

    public GeneralEndpoint(GeneralProperties generalProperties, GeneralReportService generalReportService, TestDataKeypair testDataKeypair) {
        this.generalProperties = generalProperties;
        this.generalReportService = generalReportService;
        this.testDataKeypair = testDataKeypair;
    }

    @GetMapping("/status")
    @FisEndpoint(summary = "Get the Status of the FIS Backend. Mostly just `XYZ is running!`")
    public String status() {
        return "FIS Backend is running!";
    }

    @GetMapping("/info")
    @FisEndpoint(summary = "Get General Information about the FIS Backend", description = "The information includes: the FIS Name, Description, Version, Public Split Key and the supported Asymmetric/Symmetric Crypto Types.")
    public GeneralInfoDto info() {
        return new GeneralInfoDto(
                generalProperties.getFrontendUrl(),
                generalProperties.getUrl(),
                generalProperties.getDomain(),
                generalProperties.getName(),
                generalProperties.getDescription(),
                generalProperties.getVersion(),
                testDataKeypair.getServerKeypair().pub().serialize(),
                testDataKeypair.getServerHandle(),
                Arrays.stream(AsymmCryptoType.values()).map(AsymmCryptoType::name).toList(),
                Arrays.stream(SymmCryptoType.values()).map(SymmCryptoType::name).toList()
        );
    }

    @GetMapping("/contact")
    @FisEndpoint(summary = "Get Contact Information for the Instance Owner. Can be signed by the user")
    public String contact() {
        return generalProperties.getContact();
    }

    // TODO: Rate Limit
    @PostMapping("/report")
    @FisEndpoint(summary = "Report a General Issue to the Instance Owner. Can be signed by the user")
    public void report(@Valid @RequestBody GeneralReportDto report, @AuthenticationPrincipal GoofyAuthUser auth) {
        String optHandle = auth != null ? auth.getHandle() : null;
        generalReportService.submitReport(report, optHandle);
    }
}
