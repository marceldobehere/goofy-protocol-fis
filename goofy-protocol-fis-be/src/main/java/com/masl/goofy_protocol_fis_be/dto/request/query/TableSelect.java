package com.masl.goofy_protocol_fis_be.dto.request.query;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableSelect {
    @NotNull
    private String[] colNames;

    private TableBasicQuery basicQuery;
}
