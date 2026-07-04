package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.IsolatedHandleHelper;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;
import com.masl.goofy_protocol_fis_be.dto.request.GeneralReportDto;
import com.masl.goofy_protocol_fis_be.integration.IsolatedTestConfig;
import com.masl.goofy_protocol_fis_be.repository.GeneralReportRepository;
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
    private static final String INVALID_CODE = "bruh 123";

    private final GeneralReportService reportService;
    private final GeneralReportRepository reportRepository;

    private final GlobAsymmCrypto asymmCrypto = new GlobAsymmCrypto();
    private final HandleCrypto handleCrypto = new HandleCrypto(new IsolatedHandleHelper());

    @Autowired
    public ReportTests(GeneralReportService reportService, GeneralReportRepository reportRepository) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
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
