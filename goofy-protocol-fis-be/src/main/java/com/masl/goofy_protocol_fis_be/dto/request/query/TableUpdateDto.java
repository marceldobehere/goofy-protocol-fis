package com.masl.goofy_protocol_fis_be.dto.request.query;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableUpdateDto {
    @NotNull
    private String[] colNames;
    @NotNull
    private Object[] colValues;

    private TableBasicQueryDto basicQuery;
}
