package com.masl.goofy_protocol_fis_be.dto.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQuotasDto {
    // General
    private Integer generalMaxNameSize;

    // Identity
    private Integer identityMaxEntries;
    private Integer identityMaxServiceEntries;

    // Table
    private Long maxDbSize;
    private Long maxFieldSize;
    private Integer maxTableCount;
    private Integer maxColumnCount;
    private Integer maxRowCount;
    private Integer maxTableUniquePermissionCount;
    private Integer maxLockDurationSeconds;
    private Long maxQueryLength;
    private Integer maxConditionCount;
    private Integer maxResultCount;

    // Bucket
    private Long maxBucketSize;
    private Long maxBucketItemSize;
    private Integer maxBucketItemCount;
    private Integer maxBucketUniquePermissionCount;
}
