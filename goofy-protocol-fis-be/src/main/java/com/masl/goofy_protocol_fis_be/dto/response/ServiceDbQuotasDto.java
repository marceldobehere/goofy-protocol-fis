package com.masl.goofy_protocol_fis_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDbQuotasDto {
    private Integer currTableCount;
    private Long currDbSize;

    private Integer maxTableCount;
    private Long maxDbSize;

    private Long maxFieldSize;
    private Integer maxColumnCount;
    private Integer maxRowCount;

    private Integer maxUniquePermissionCount;
    private Integer maxLockDurationSeconds;

    private Long maxQueryLength;
    private Integer maxConditionCount;
    private Integer maxResultCount;
    private Integer generalMaxNameSize;
}
