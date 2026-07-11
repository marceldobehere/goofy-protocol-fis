package com.masl.goofy_protocol_fis_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBucketQuotasDto {
    private Long maxBucketSize;
    private Long maxItemSize;
    private Integer maxItemCount;
    private Integer maxUniquePermissionCount;

    private Long currentBucketSize;
    private Integer currentItemCount;
}
