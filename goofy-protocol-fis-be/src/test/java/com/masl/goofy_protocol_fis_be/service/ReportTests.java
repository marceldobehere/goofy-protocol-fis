package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.dto.request.GeneralReportDto;
import com.masl.goofy_protocol_fis_be.integration.IsolatedTestConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("service")
@SpringBootTest
@ActiveProfiles({"test", "tests-shared"})
@ContextConfiguration(initializers = IsolatedTestConfig.class)
public class ReportTests {
    private final GeneralReportService reportService;

    @Autowired
    public ReportTests(GeneralReportService reportService) {
        this.reportService = reportService;
    }

    @ParameterizedTest(name = "testReport(haveHandle={0})")
    @ValueSource(booleans = {false, true})
    void testReport(boolean haveHandle) {
        String randomUUID = UUID.randomUUID().toString();
        GeneralReportDto reportDto = new GeneralReportDto();
        reportDto.setTitle("Test Report");
        reportDto.setDescription("This is a test report.");
        reportDto.setContact(randomUUID);

        String handle = haveHandle ? "crazy_handle_123" : null;
        reportService.submitReport(reportDto, handle);
        var report = reportService.getAllUnresolvedReports()
            .stream().filter(r -> r.getContact().equals(randomUUID))
            .findFirst().orElse(null);
        assertThat(report).isNotNull();
        assertThat(report.getTitle()).isEqualTo(reportDto.getTitle());
        assertThat(report.getDescription()).isEqualTo(reportDto.getDescription());
        assertThat(report.getOptionalHandle()).isEqualTo(handle);
    }
}
