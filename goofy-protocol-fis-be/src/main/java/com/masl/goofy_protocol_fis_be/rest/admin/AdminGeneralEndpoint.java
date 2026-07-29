package com.masl.goofy_protocol_fis_be.rest.admin;

import com.masl.goofy_protocol_fis_be.dto.response.ReportEntryDto;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.GenericNotFound;
import com.masl.goofy_protocol_fis_be.service.GeneralReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: Test
@RestController
@RequestMapping("/api/admin/general")
@Tag(name = "General (Admin)", description = "General Admin Endpoints regarding the FIS")
public class AdminGeneralEndpoint {
    private final GeneralReportService generalReportService;

    public AdminGeneralEndpoint(GeneralReportService generalReportService) {
        this.generalReportService = generalReportService;
    }

    // Get all reports
    @GetMapping("/report")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Get All General Reports", description = "This Endpoint allows an admin to retrieve all general reports submitted to the FIS. <br> The response will be a list of report entries, each containing details such as ID, title, description, contact information, optional handle, creation timestamp, and resolution timestamp.")
    public List<ReportEntryDto> getAllReports() {
        return generalReportService.getAllReports().stream()
                .map(report -> new ReportEntryDto(
                        report.getId(),
                        report.getTitle(),
                        report.getDescription(),
                        report.getContact(),
                        report.getOptionalHandle(),
                        report.getCreatedAt(),
                        report.getResolvedAt()
                )).toList();
    }

    // Get all unresolved reports
    @GetMapping("/report/open")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Get All Unresolved General Reports", description = "This Endpoint allows an admin to retrieve all unresolved general reports submitted to the FIS. <br> The response will be a list of report entries that have not been marked as resolved, each containing details such as ID, title, description, contact information, optional handle, creation timestamp, and resolution timestamp (which will be null for unresolved reports).")
    public List<ReportEntryDto> getAllUnresolvedReports() {
        return generalReportService.getAllUnresolvedReports().stream()
                .map(report -> new ReportEntryDto(
                        report.getId(),
                        report.getTitle(),
                        report.getDescription(),
                        report.getContact(),
                        report.getOptionalHandle(),
                        report.getCreatedAt(),
                        report.getResolvedAt()
                )).toList();
    }

    // TODO: Get singular Report by id

    // TODO: Turn into general update method with custom notes
    // Resolve Report
    @PutMapping("/report/{id}/resolve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Resolve a General Report by ID", description = "This Endpoint allows an admin to mark a general report as resolved or unresolved by its ID. <br> The request body should contain a boolean value indicating the resolved status. <br> Example: `true` for resolved, `false` for unresolved.")
    public void resolveReport(@PathVariable Long id, @Valid @RequestBody Boolean resolved) throws GenericNotFound {
        generalReportService.setResolvedStatus(id, resolved);
    }
}
