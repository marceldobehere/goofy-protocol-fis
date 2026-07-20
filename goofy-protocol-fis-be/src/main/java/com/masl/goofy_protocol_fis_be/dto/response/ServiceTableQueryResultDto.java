package com.masl.goofy_protocol_fis_be.dto.response;

import com.masl.goofy_protocol_fis_be.dto.both.TableColumnDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTableQueryResultDto {
    @NotNull
    private String[] colNames;
    @NotNull
    private TableColumnDto.Type[] colTypes;
    @NotNull
    private Object[][] rows;
}
