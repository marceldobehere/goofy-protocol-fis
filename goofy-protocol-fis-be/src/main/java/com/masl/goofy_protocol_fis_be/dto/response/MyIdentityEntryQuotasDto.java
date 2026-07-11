package com.masl.goofy_protocol_fis_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyIdentityEntryQuotasDto {
    private Integer maxEntryCount;
    private Long currentEntryCount;
}
