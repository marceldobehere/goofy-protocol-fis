package com.masl.goofy_protocol_fis_be.dto.both;

import com.masl.goofy_protocol_fis_be.entity.FieldSize;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTableEntryDto {
    @NotBlank
    @Size(max = FieldSize.GENERIC_CODE_LEN)
    private String tableUuid;

    @NotBlank
    @Size(max = FieldSize.SHORT_TEXT_LEN)
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Use only a-z, 0-9, and underscore (_)")
    private String tableName;

    // Schema
    private Integer schemaVersion;
    private TableColumnDto[] columns;

    private Instant createdAt;

    @NotNull
    private String[] handlesWithReadPerms;

    @NotNull
    private String[] handlesWithWritePerms;
}
