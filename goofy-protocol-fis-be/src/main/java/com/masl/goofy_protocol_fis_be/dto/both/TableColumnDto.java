package com.masl.goofy_protocol_fis_be.dto.both;

import com.masl.goofy_protocol_fis_be.entity.FieldSize;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableColumnDto {
    @NotBlank
    @Size(max = FieldSize.SHORT_TEXT_LEN)
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Use only a-z, 0-9, and underscore (_)")
    private String colName;

    @NotNull
    private Type type;

    private Integer typeSize;

    @NotNull
    private Set<Constraint> constraints;

    private Object defaultValue;


    public enum Type {
        FIXED_STRING_N, VAR_STRING_N, // (N, 1-MAX_FIELD_SIZE)
        BOOLEAN,
        TINYINT, SMALLINT, INT, BIGINT,
        NUMERIC_N /*(N, 0-100)*/, FLOAT, DOUBLE,
        DATE, TIME
    }

    public enum Constraint {
        NOT_NULL,
        UNIQUE,
        PRIMARY_KEY
    }

    public static int getTypeSize(Type type, Integer optTypeSize) {
        if (optTypeSize < 0)
            throw new IllegalArgumentException("Type size must be non-negative");

        return switch (type) {
            case FIXED_STRING_N -> optTypeSize * 2;
            case VAR_STRING_N -> optTypeSize * 2 + 2;
            case BOOLEAN -> 1;
            case TINYINT -> 1;
            case SMALLINT -> 2;
            case INT -> 4;
            case BIGINT -> 8;
            case NUMERIC_N -> 16;
            case FLOAT -> 4;
            case DOUBLE -> 8;
            case DATE -> 8;
            case TIME -> 8;
        };
    }

    public String toSqlTypeString() {
        return switch (type){
            case FIXED_STRING_N -> "CHAR(" + typeSize + ")";
            case VAR_STRING_N -> "VARCHAR(" + typeSize + ")";
            case BOOLEAN -> "BOOLEAN";
            case TINYINT -> "TINYINT";
            case SMALLINT -> "SMALLINT";
            case INT -> "INT";
            case BIGINT -> "BIGINT";
            case NUMERIC_N -> "NUMERIC(" + typeSize + ")";
            case FLOAT -> "FLOAT";
            case DOUBLE -> "DOUBLE";
            case DATE -> "DATE";
            case TIME -> "TIME";
        };
    }

    public static Type fromSqlTypeString(String sqlType) {
        String t = sqlType.trim().toUpperCase(Locale.ROOT);

        // Trim TYPE(...)
        int pIdx = t.indexOf('(');
        if (pIdx >= 0)
            t = t.substring(0, pIdx).trim();

        return switch (t) {
            case "CHAR", "CHARACTER" -> Type.FIXED_STRING_N;
            case "VARCHAR", "CHARACTER VARYING" -> Type.VAR_STRING_N;
            case "BOOLEAN", "BOOL" -> Type.BOOLEAN;

            case "TINYINT" -> Type.TINYINT;
            case "SMALLINT" -> Type.SMALLINT;
            case "INT", "INTEGER" -> Type.INT;
            case "BIGINT" -> Type.BIGINT;

            case "NUMERIC", "DECIMAL" -> Type.NUMERIC_N;

            case "REAL", "FLOAT" -> Type.FLOAT;
            case "DOUBLE", "DOUBLE PRECISION" -> Type.DOUBLE;

            case "DATE" -> Type.DATE;
            case "TIME" -> Type.TIME;

            default -> throw new IllegalArgumentException("Unknown SQL Type: " + sqlType);
        };
    }

    public String toSqlConstraintsString() {
        StringJoiner joiner = new StringJoiner(" ");
        for (Constraint constraint : constraints) {
            switch (constraint) {
                case NOT_NULL -> joiner.add("NOT NULL");
                case UNIQUE -> joiner.add("UNIQUE");
                case PRIMARY_KEY -> joiner.add("PRIMARY KEY");
            }
        }
        return joiner.toString();
    }

    public static void addValueToPreparedStatement(PreparedStatement statement, int index, Object value, Type type) throws SQLException {
        if (value == null) {
            statement.setNull(index, switch (type) {
                case FIXED_STRING_N, VAR_STRING_N -> java.sql.Types.VARCHAR;
                case BOOLEAN -> java.sql.Types.BOOLEAN;
                case TINYINT -> java.sql.Types.TINYINT;
                case SMALLINT -> java.sql.Types.SMALLINT;
                case INT -> java.sql.Types.INTEGER;
                case BIGINT -> java.sql.Types.BIGINT;
                case NUMERIC_N -> java.sql.Types.NUMERIC;
                case FLOAT -> java.sql.Types.FLOAT;
                case DOUBLE -> java.sql.Types.DOUBLE;
                case DATE -> java.sql.Types.DATE;
                case TIME -> java.sql.Types.TIME;
            });
            return;
        }

        switch (type) {
            case FIXED_STRING_N, VAR_STRING_N -> statement.setString(index, String.valueOf(value));

            case BOOLEAN -> {
                if (value instanceof Boolean b) statement.setBoolean(index, b);
                else if (value instanceof Number n) statement.setBoolean(index, n.intValue() != 0);
                else statement.setBoolean(index, Boolean.parseBoolean(String.valueOf(value)));
            }

            case TINYINT -> {
                if (value instanceof Number n) statement.setByte(index, n.byteValue());
                else statement.setByte(index, Byte.parseByte(String.valueOf(value)));
            }

            case SMALLINT -> {
                if (value instanceof Number n) statement.setShort(index, n.shortValue());
                else statement.setShort(index, Short.parseShort(String.valueOf(value)));
            }

            case INT -> {
                if (value instanceof Number n) statement.setInt(index, n.intValue());
                else statement.setInt(index, Integer.parseInt(String.valueOf(value)));
            }

            case BIGINT -> {
                if (value instanceof Number n) statement.setLong(index, n.longValue());
                else statement.setLong(index, Long.parseLong(String.valueOf(value)));
            }

            case NUMERIC_N -> {
                if (value instanceof java.math.BigDecimal bd) statement.setBigDecimal(index, bd);
                else if (value instanceof Number n) statement.setBigDecimal(index, java.math.BigDecimal.valueOf(n.doubleValue()));
                else statement.setBigDecimal(index, new java.math.BigDecimal(String.valueOf(value)));
            }

            case FLOAT -> {
                if (value instanceof Number n) statement.setFloat(index, n.floatValue());
                else statement.setFloat(index, Float.parseFloat(String.valueOf(value)));
            }

            case DOUBLE -> {
                if (value instanceof Number n) statement.setDouble(index, n.doubleValue());
                else statement.setDouble(index, Double.parseDouble(String.valueOf(value)));
            }

            case DATE -> {
                switch (value) {
                    case java.sql.Date d -> statement.setDate(index, d);
                    case java.time.LocalDate ld -> statement.setDate(index, java.sql.Date.valueOf(ld));
                    case java.util.Date ud -> statement.setDate(index, new java.sql.Date(ud.getTime()));
                    default ->
                            statement.setDate(index, java.sql.Date.valueOf(String.valueOf(value))); // expects yyyy-[m]m-[d]d
                }
            }

            case TIME -> {
                switch (value) {
                    case java.sql.Time t -> statement.setTime(index, t);
                    case java.time.LocalTime lt -> statement.setTime(index, java.sql.Time.valueOf(lt));
                    case java.util.Date ud -> statement.setTime(index, new java.sql.Time(ud.getTime()));
                    default ->
                            statement.setTime(index, java.sql.Time.valueOf(String.valueOf(value))); // expects HH:mm:ss[.f...]
                }
            }
        }
    }

    public void addDefaultValueToPreparedStatement(PreparedStatement statement, int index) throws SQLException {
        addValueToPreparedStatement(statement, index, defaultValue, type);
    }

    public static TableColumnDto fromSqlData(String colName, String colType, int colTypeSize, Set<Constraint> constraints, Object defaultValue) {
        TableColumnDto dto = new TableColumnDto();
        dto.setColName(colName);
        dto.setConstraints(constraints);
        dto.setTypeSize(colTypeSize);
        dto.setType(fromSqlTypeString(colType));
        dto.setDefaultValue(defaultValue);

        return dto;
    }

    public static Object parseDefaultSqlValue(String rawDefault, Type type) {
        if (rawDefault == null) return null;

        String s = rawDefault.trim();
        if (s.isEmpty()) return null;

        if (s.startsWith("'") && s.endsWith("'") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }

        return switch (type) {
            case FIXED_STRING_N, VAR_STRING_N -> s;

            case BOOLEAN -> {
                if (s.equalsIgnoreCase("TRUE")) yield true;
                if (s.equalsIgnoreCase("FALSE")) yield false;
                if (s.equals("1")) yield true;
                if (s.equals("0")) yield false;
                // fallback
                yield Boolean.parseBoolean(s);
            }

            case TINYINT -> Byte.parseByte(s);
            case SMALLINT -> Short.parseShort(s);
            case INT -> Integer.parseInt(s);
            case BIGINT -> Long.parseLong(s);

            case NUMERIC_N, FLOAT, DOUBLE -> {
                if (type == TableColumnDto.Type.NUMERIC_N) yield new java.math.BigDecimal(s);
                if (type == TableColumnDto.Type.FLOAT) yield Float.parseFloat(s);
                yield Double.parseDouble(s);
            }

            case DATE -> {
                // H2 often returns DATE '2026-01-01' or just '2026-01-01'
                if (s.toUpperCase(Locale.ROOT).startsWith("DATE ")) {
                    String inside = s.substring(5).trim();
                    if (inside.startsWith("'") && inside.endsWith("'")) {
                        inside = inside.substring(1, inside.length() - 1);
                    }
                    yield java.sql.Date.valueOf(inside);
                }
                // otherwise assume YYYY-MM-DD
                yield java.sql.Date.valueOf(s);
            }

            case TIME -> {
                // likely HH:mm:ss or TIME '...'
                if (s.toUpperCase(Locale.ROOT).startsWith("TIME ")) {
                    String inside = s.substring(5).trim();
                    if (inside.startsWith("'") && inside.endsWith("'")) {
                        inside = inside.substring(1, inside.length() - 1);
                    }
                    yield java.sql.Time.valueOf(inside);
                }
                yield java.sql.Time.valueOf(s);
            }
        };
    }
}
