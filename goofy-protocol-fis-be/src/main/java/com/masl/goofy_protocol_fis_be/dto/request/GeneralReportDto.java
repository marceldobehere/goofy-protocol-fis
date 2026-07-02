package com.masl.goofy_protocol_fis_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralReportDto {
    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 200)
    private String contact;
}
