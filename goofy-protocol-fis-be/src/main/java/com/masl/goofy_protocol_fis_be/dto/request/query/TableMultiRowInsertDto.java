package com.masl.goofy_protocol_fis_be.dto.request.query;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableMultiRowInsertDto {
    @NotNull
    private String[] colNames;
    @NotNull
    private List<Object[]> rows;
}
