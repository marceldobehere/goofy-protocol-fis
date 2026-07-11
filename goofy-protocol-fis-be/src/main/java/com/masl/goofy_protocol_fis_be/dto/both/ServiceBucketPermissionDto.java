package com.masl.goofy_protocol_fis_be.dto.both;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBucketPermissionDto {
    @NotNull
    private String[] handlesWithReadPerms;
    @NotNull
    private String[] handlesWithWritePerms;
}
