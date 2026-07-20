package com.masl.goofy_protocol_fis_be.dto.request.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableBasicQuery {
    // Where Statement
    private TableWhereConditionPart where;

    // Sort By
    private String[] sortByCols;
    private SortOrder[] sortOrders;

    // Optional Limit
    private Integer limit;

    // Optional Offset
    private Integer offset;

    public enum SortOrder {
        ASC,
        DESC
    }
}
