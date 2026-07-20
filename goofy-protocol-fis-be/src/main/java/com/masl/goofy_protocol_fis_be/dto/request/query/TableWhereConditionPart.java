package com.masl.goofy_protocol_fis_be.dto.request.query;

import com.masl.goofy_protocol_fis_be.dto.both.TableColumnDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableWhereConditionPart {
    private Type type;

    // RAW VALUE
    private Object value;
    private TableColumnDto.Type valueType;

    // COLUMN
    private String colName;

    // CONDITION
    private TableWhereConditionPart[] conditionParts;


    @Getter
    public enum Type {
        VAL(InputCount.NONE, ExpressionType.NONE, ExpressionType.ANY_VALUE),
        COL(InputCount.NONE, ExpressionType.NONE, ExpressionType.ANY_VALUE),

        M_ADD(InputCount.TWO, ExpressionType.ANY_NUMBER, ExpressionType.ANY_NUMBER),
        M_SUB(InputCount.TWO, ExpressionType.ANY_NUMBER, ExpressionType.ANY_NUMBER),
        M_MUL(InputCount.TWO, ExpressionType.ANY_NUMBER, ExpressionType.ANY_NUMBER),
        M_DIV(InputCount.TWO, ExpressionType.ANY_NUMBER, ExpressionType.ANY_NUMBER),
        M_MOD(InputCount.TWO, ExpressionType.ANY_NUMBER, ExpressionType.ANY_NUMBER),

        M_FLOOR(InputCount.ONE, ExpressionType.ANY_NUMBER, ExpressionType.ANY_NUMBER),
        M_CEIL(InputCount.ONE, ExpressionType.ANY_NUMBER, ExpressionType.ANY_NUMBER),
        M_ABS(InputCount.ONE, ExpressionType.ANY_NUMBER, ExpressionType.ANY_NUMBER),

        L_AND(InputCount.MANY, ExpressionType.ANY_BOOLEAN, ExpressionType.ANY_BOOLEAN),
        L_OR(InputCount.MANY, ExpressionType.ANY_BOOLEAN, ExpressionType.ANY_BOOLEAN),
        L_NOT(InputCount.ONE, ExpressionType.ANY_BOOLEAN, ExpressionType.ANY_BOOLEAN),

        C_EQ(InputCount.TWO, ExpressionType.ANY, ExpressionType.ANY_BOOLEAN),
        C_NEQ(InputCount.TWO, ExpressionType.ANY, ExpressionType.ANY_BOOLEAN),
        C_GT(InputCount.TWO, ExpressionType.ANY_NUMBER, ExpressionType.ANY_BOOLEAN),
        C_GE(InputCount.TWO, ExpressionType.ANY_NUMBER, ExpressionType.ANY_BOOLEAN),
        C_LT(InputCount.TWO, ExpressionType.ANY_NUMBER, ExpressionType.ANY_BOOLEAN),
        C_LE(InputCount.TWO, ExpressionType.ANY_NUMBER, ExpressionType.ANY_BOOLEAN),

        COALESCE(InputCount.TWO, ExpressionType.ANY, ExpressionType.ANY_VALUE),

        LIKE(InputCount.TWO, ExpressionType.ANY_VALUE, ExpressionType.ANY_VALUE);

        private final InputCount inputCount;
        private final ExpressionType inputTypes;
        private final ExpressionType outputType;

        Type(InputCount inputCount, ExpressionType inputTypes, ExpressionType outputType) {
            this.inputCount = inputCount;
            this.inputTypes = inputTypes;
            this.outputType = outputType;
        }

        public enum InputCount {
            NONE,
            ONE,
            TWO,
            MANY
        }

        public enum ExpressionType {
            NONE,
            ANY_VALUE,
            ANY_NUMBER, // Should also accept ANY_VALUE
            ANY_BOOLEAN, // Should also accept ANY_VALUE
            ANY
        }
    }
}
