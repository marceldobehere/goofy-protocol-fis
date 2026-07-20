package com.masl.goofy_protocol_fis_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTableQuotasDto {
    private Integer currTableCount;
    private Integer currColumnCount;
    private Integer currRowCount;

    private Integer maxTableCount;
    private Integer maxColumnCount;
    private Integer maxRowCount;

    private Long maxFieldSize;
    private Integer maxUniquePermissionCount;
}
