package com.masl.goofy_protocol_fis_be.dto.both;

import com.masl.goofy_protocol_fis_be.config.CacheDuration;
import com.masl.goofy_protocol_fis_be.entity.FieldSize;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBucketEntryDto {
    @NotEmpty
    @Size(max = FieldSize.GENERIC_CODE_LEN)
    private String fileUuid;

    @NotEmpty
    @Size(max = FieldSize.SHORT_TEXT_LEN)
    private String contentType;

    @NotEmpty
    @Size(max = FieldSize.SHORT_TEXT_LEN)
    private String filename;

    @NotNull
    private CacheDuration cacheDuration;

    private Long contentSize;

    private Instant createdAt;

    @NotNull
    private String[] handlesWithReadPerms;

    @NotNull
    private String[] handlesWithWritePerms;
}
