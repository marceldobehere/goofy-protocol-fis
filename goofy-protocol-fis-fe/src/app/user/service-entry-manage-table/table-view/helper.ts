'use client';

import {TableBasicQueryDto, TableColumnDto, TableWhereConditionPart} from "@/libs/dtos";

export function renderColToString(value: unknown, col: TableColumnDto): string {
    if (value === null || value === undefined) return "null";

    // If the DB layer already gives you a boolean/number/date object, keep it sane.
    const upperType = col.type;

    switch (upperType) {
        case "FIXED_STRING_N":
        case "VAR_STRING_N": {
            return String(value);
        }

        case "BOOLEAN": {
            // Accept common DB outputs: true/false, 1/0, "TRUE"/"FALSE"
            if (typeof value === "boolean") return value ? "TRUE" : "FALSE";
            const s = String(value).trim().toUpperCase();
            if (s === "1" || s === "TRUE") return "TRUE";
            if (s === "0" || s === "FALSE") return "FALSE";
            // fallback: preserve input best-effort
            return s;
        }

        case "TINYINT":
        case "SMALLINT":
        case "INT":
        case "BIGINT": {
            // Render as integer literal
            return String(value);
        }

        case "FLOAT":
        case "DOUBLE": {
            // Render as numeric literal
            return String(value);
        }

        case "DATE": {
            // Expect YYYY-MM-DD (or Date-like); render as SQL DATE literal
            if (value instanceof Date) {
                // toISOString is UTC; adjust if you care about local date boundaries.
                const iso = value.toISOString().slice(0, 10); // YYYY-MM-DD
                return `DATE '${iso}'`;
            }
            const s = String(value).trim();

            // If it already looks like DATE 'YYYY-MM-DD'
            if (/^DATE\s+/i.test(s)) return s;

            // If it's just YYYY-MM-DD
            return `DATE '${s.replace(/^'|'$/g, "")}'`;
        }

        case "TIME": {
            // Expect HH:mm:ss (or Date-like); render as SQL TIME literal
            if (value instanceof Date) {
                const hh = String(value.getHours()).padStart(2, "0");
                const mm = String(value.getMinutes()).padStart(2, "0");
                const ss = String(value.getSeconds()).padStart(2, "0");
                return `TIME '${hh}:${mm}:${ss}'`;
            }
            const s = String(value).trim();

            if (/^TIME\s+/i.test(s)) return s;

            return `TIME '${s.replace(/^'|'$/g, "")}'`;
        }

        default: {
            // Best-effort: render as string literal
            return String(value);
        }
    }
}

export function renderQuery(basicQuery: TableBasicQueryDto): string {
    const parts: string[] = [];

    if (basicQuery.where) {
        parts.push(`WHERE ${renderWhere(basicQuery.where)}`);
    }

    if (basicQuery.sortByCols && basicQuery.sortByCols.length > 0) {
        const orders = basicQuery.sortOrders ?? [];
        const sortItems: string[] = basicQuery.sortByCols.map((col, i) => {
            const order = orders[i] ?? "ASC";
            return `"${col}" ${order}`;
        });
        parts.push(`ORDER BY ${sortItems.join(", ")}`);
    }

    if (basicQuery.limit !== undefined && basicQuery.limit !== null) {
        parts.push(`LIMIT ${basicQuery.limit}`);
    }

    if (basicQuery.offset !== undefined && basicQuery.offset !== null) {
        parts.push(`OFFSET ${basicQuery.offset}`);
    }

    return parts.join(" ") || "SELECT *";
}

function renderWhere(wherePart: TableWhereConditionPart): string {
    switch (wherePart.type) {
        case "VAL": {
            const fakeColDto: TableColumnDto = {
                type: wherePart.valueType!,
                colName: "",
                constraints: []
            };
            return renderColToString(wherePart.value, fakeColDto);
        }

        case "COL": {
            const colName = wherePart.colName;
            // Keep consistent with the Java behavior: quote column names.
            // (No validation here because we don't have cols Map.)
            if (!colName) return `""`;
            return `"${colName}"`;
        }

        case "L_AND":
        case "L_OR": {
            const keyword = wherePart.type === "L_AND" ? " AND " : " OR ";
            const sub = wherePart.conditionParts ?? [];
            return "(" + sub.map(renderWhere).join(keyword) + ")";
        }

        case "L_NOT": {
            const sub = wherePart.conditionParts ?? [];
            if (sub.length !== 1) return `(NOT ${sub.map(renderWhere).join(" ")})`;
            return `(NOT ${renderWhere(sub[0])})`;
        }

        case "M_ADD":
        case "M_SUB":
        case "M_MUL":
        case "M_DIV":
        case "M_MOD": {
            const sub = wherePart.conditionParts ?? [];
            const [a, b] = sub;
            const op =
                wherePart.type === "M_ADD"
                    ? "+"
                    : wherePart.type === "M_SUB"
                        ? "-"
                        : wherePart.type === "M_MUL"
                            ? "*"
                            : wherePart.type === "M_DIV"
                                ? "/"
                                : "%";
            return `(${a ? renderWhere(a) : ""} ${op} ${b ? renderWhere(b) : ""})`;
        }

        case "C_EQ":
        case "C_NEQ":
        case "C_GT":
        case "C_GE":
        case "C_LT":
        case "C_LE":
        case "LIKE": {
            const sub = wherePart.conditionParts ?? [];
            const [a, b] = sub;
            const op =
                wherePart.type === "C_EQ"
                    ? "="
                    : wherePart.type === "C_NEQ"
                        ? "<>"
                        : wherePart.type === "C_GT"
                            ? ">"
                            : wherePart.type === "C_GE"
                                ? ">="
                                : wherePart.type === "C_LT"
                                    ? "<"
                                    : wherePart.type === "C_LE"
                                        ? "<="
                                        : "LIKE";
            return `(${a ? renderWhere(a) : ""} ${op} ${b ? renderWhere(b) : ""})`;
        }

        case "COALESCE": {
            const sub = wherePart.conditionParts ?? [];
            const [a, b] = sub;
            return `COALESCE(${a ? renderWhere(a) : ""}, ${b ? renderWhere(b) : ""})`;
        }

        case "M_FLOOR":
        case "M_CEIL":
        case "M_ABS": {
            const sub = wherePart.conditionParts ?? [];
            const [a] = sub;
            const func = wherePart.type === "M_FLOOR" ? "FLOOR" : wherePart.type === "M_CEIL" ? "CEIL" : "ABS";
            return `${func}(${a ? renderWhere(a) : ""})`;
        }
    }
}
