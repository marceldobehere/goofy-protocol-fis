package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.dto.both.ServiceTableEntryDto;
import com.masl.goofy_protocol_fis_be.dto.both.TableColumnDto;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableBasicQueryDto;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableSelectDto;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableUpdateDto;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableWhereConditionPart;
import com.masl.goofy_protocol_fis_be.dto.response.ServiceTableQueryResultDto;
import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

// TODO: Test
@Service
public class UserDbService {
    private static final Logger log = LoggerFactory.getLogger(UserDbService.class);

    private final FileStorageService fileStorageService;

    public UserDbService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public synchronized void createEntry(ServiceEntry entry) throws IOException, SQLException {
        deleteEntry(entry); // Ensure no existing folder
        fileStorageService.createDbFolder(entry.getUuid());
        initDb(entry);
    }

    public synchronized void deleteEntry(ServiceEntry entry) throws IOException {
        log.info("Deleting DB folder for entry: {}", entry.getUuid());
        fileStorageService.deleteDbFolder(entry.getUuid());
        // TODO: Check DB Connections are closed / Close Server somehow
    }

    // TODO: Test
    public synchronized void deleteTableEntry(ServiceEntry entry, String tableUuid) throws SQLException {
        try (Connection conn = getConnection(entry.getUuid())) {
            String tableName = getDbTableNameFromTableUuid(tableUuid);

            // Execute
            try (Statement statement = conn.createStatement()) {
                log.info("Deleting DB Table {} for entry {}", tableName, entry.getUuid());
                statement.execute("DROP TABLE \"" + tableName + "\"");
            }
        }
    }

    public Long getDbSize(ServiceEntry entry) throws IOException {
        Path dbFile = fileStorageService.getDbFolderPath(entry.getUuid()).resolve("userData.mv.db");
        File file = dbFile.toFile();
        if (!file.exists())
            throw new IOException("Database file does not exist for entry: " + entry.getUuid());
        return file.length();
    }

    private String getDbTableNameFromTableUuid(String tableUuid) {
        return "table_" + tableUuid.replace("-", "_").toLowerCase(Locale.ROOT);
    }

    private synchronized void initDb(ServiceEntry entry) throws SQLException {
        try (Connection conn = getConnection(entry.getUuid())) {
            // Check if everything works
            List<String> tables = getAllTables(conn);
            log.info("Initialized database for entry: {}, tables: {}", entry.getUuid(), tables);
        }
    }

    public Connection getConnection(String uuid) throws SQLException {
        Path dbFile = fileStorageService.getDbFolderPath(uuid).resolve("userData");
        String dbBase = dbFile.toAbsolutePath().toString();

        String url = "jdbc:h2:file:" + dbBase; // + ";AUTO_SERVER=TRUE";
        return DriverManager.getConnection(url, "sa", "");
    }

    private List<String> getAllTables(Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[] { "TABLE" })) {
            List<String> tables = new ArrayList<>();
            while (rs.next())
                tables.add(rs.getString("TABLE_NAME"));
            return tables;
        }
    }

    public long getTableRowCount(ServiceEntry entry, String tableUuid) throws SQLException {
        try (Connection conn = getConnection(entry.getUuid())) {
            String tableName = getDbTableNameFromTableUuid(tableUuid);
            try (Statement statement = conn.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM \"" + tableName + "\"")) {
                if (rs.next())
                    return rs.getLong(1);
                return 0L;
            }
        }
    }

    public int getTableColumnCount(ServiceEntry entry, String tableUuid) throws SQLException {
        try (Connection conn = getConnection(entry.getUuid())) {
            String tableName = getDbTableNameFromTableUuid(tableUuid);
            int count = 0;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
                while (rs.next())
                    count++;
            }
            return count;
        }
    }

    // TODO: Cache this probably, and have the cache be invalidated if the table gets modified
    public List<TableColumnDto> getAllTableColumns(ServiceEntry entry, String tableUuid) throws SQLException {
        try (Connection conn = getConnection(entry.getUuid())) {
            String tableName = getDbTableNameFromTableUuid(tableUuid);

            // Primary Keys
            Set<String> pkCols = new HashSet<>();
            try (ResultSet rs = conn.getMetaData().getPrimaryKeys(null, null, tableName)) {
                while (rs.next())
                    pkCols.add(rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
            }

            // Unique Cols
            Set<String> uniqueCols = new HashSet<>();
            try (ResultSet rs = conn.getMetaData().getIndexInfo(null, null, tableName, true, false)) {
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    if (colName != null && !rs.getBoolean("NON_UNIQUE"))
                        uniqueCols.add(colName.toLowerCase(Locale.ROOT));
                }
            }

            // Columns
            List<TableColumnDto> columns = new ArrayList<>();
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT);
                    String colType = rs.getString("TYPE_NAME").toUpperCase(Locale.ROOT);
                    int colSize = rs.getInt("COLUMN_SIZE");
                    boolean nullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;

                    // Constraint
                    Set<TableColumnDto.Constraint> constraints = new HashSet<>();
                    if (pkCols.contains(colName))
                        constraints.add(TableColumnDto.Constraint.PRIMARY_KEY);
                    if (uniqueCols.contains(colName))
                        constraints.add(TableColumnDto.Constraint.UNIQUE);
                    if (!nullable)
                        constraints.add(TableColumnDto.Constraint.NOT_NULL);

                    // Default (H2 / JDBC metadata)
                    Object parsedDefault = null;
                    String rawDefault = rs.getString("COLUMN_DEF");
                    if (rawDefault != null) {
                        TableColumnDto.Type inferredType = TableColumnDto.fromSqlTypeString(colType);
                        parsedDefault = TableColumnDto.parseRawStringSqlValue(rawDefault, inferredType);
                    }

                    // Create actual DTO
                    columns.add(TableColumnDto.fromSqlData(colName, colType, colSize, constraints, parsedDefault));
                }
            }

            return columns;
        }
    }

    public void createTableEntry(ServiceEntry entry, ServiceTableEntryDto tableEntryDto) throws SQLException {
        try (Connection conn = getConnection(entry.getUuid())) {
            String tableName = getDbTableNameFromTableUuid(tableEntryDto.getTableUuid());

            // Create Query String
            StringBuilder createQuery = new StringBuilder();
            createQuery.append("CREATE TABLE \"").append(tableName).append("\" (");
            StringJoiner colDefs = new StringJoiner(", ");
            for (TableColumnDto colDto : tableEntryDto.getColumns()) {
                StringJoiner colDef = new StringJoiner(" ");
                colDef.add("\"" + colDto.getColName() + "\"");
                colDef.add(colDto.toSqlTypeString());
                colDef.add(colDto.toSqlConstraintsString());
                if (colDto.getDefaultValue() != null)
                    colDef.add("DEFAULT " + colDto.defaultToSqlValueString());

                colDefs.add(colDef.toString());
            }
            createQuery.append(colDefs);
            createQuery.append(");");

            // Execute
            try (Statement statement = conn.createStatement()) {
                statement.execute(createQuery.toString());
            }
        }
    }

    public void insertIntoTable(ServiceEntry entry, String tableUuid, Map<String, Object> values) throws SQLException {
        try (Connection conn = getConnection(entry.getUuid())) {
            String tableName = getDbTableNameFromTableUuid(tableUuid);

            // TODO: Optimize the Col retrieval in the future
            // Get Table Columns and Insert Entries
            Map<String, TableColumnDto> cols = getAllTableColumns(entry, tableUuid).stream().collect(Collectors.toMap(TableColumnDto::getColName, col -> col));
            var entries = values.entrySet().stream().toList();

            // Columns and Values
            StringJoiner colDefs = new StringJoiner(", ");
            StringJoiner valDefs = new StringJoiner(", ");
            for (var insertEntry : entries) {
                String colName = insertEntry.getKey();
                TableColumnDto colDto = cols.get(colName);
                if (colDto == null)
                    throw new SQLException("Column " + colName + " does not exist in table " + tableUuid);

                colDefs.add("\"" + colName + "\"");
                valDefs.add("?");
            }

            // Create Query String
            String insertQuery = "INSERT INTO \"" + tableName + "\" (" + colDefs + ") VALUES (" + valDefs + ");";

            // Execute
            try (PreparedStatement statement = conn.prepareStatement(insertQuery)) {
                // Fill out Prepared Statement
                int pIndex = 1;
                for (var insertEntry : entries) {
                    String colName = insertEntry.getKey();
                    Object value = insertEntry.getValue();
                    TableColumnDto colDto = cols.get(colName);

                    TableColumnDto.addValueToPreparedStatement(statement, pIndex++, value, colDto.getType());
                }

                statement.executeUpdate();
            }
        }
    }

    private record PrepStatementColValue(TableColumnDto.Type type, Object value) {}

    private static String createWherePart(TableWhereConditionPart wherePart, Map<String, TableColumnDto> cols, List<PrepStatementColValue> newValues) throws SQLException {
        switch (wherePart.getType()) {
            case VAL -> {
                newValues.add(new PrepStatementColValue(wherePart.getValueType(), wherePart.getValue()));
                return "?";
            }
            case COL -> {
                String colName = wherePart.getColName();
                if (!cols.containsKey(colName))
                    throw new SQLException("Column " + colName + " does not exist in the table");
                return "\"" + colName + "\"";
            }
            case L_AND, L_OR -> {
                String keyword = wherePart.getType() == TableWhereConditionPart.Type.L_AND ? " AND " : " OR ";
                StringJoiner joiner = new StringJoiner(keyword);
                for (var part : wherePart.getConditionParts())
                    joiner.add(createWherePart(part, cols, newValues));
                return "(" + joiner + ")";
            }
            case L_NOT -> {
                if (wherePart.getConditionParts().length != 1)
                    throw new SQLException("NOT condition must have exactly one sub-condition");
                return "(NOT " + createWherePart(wherePart.getConditionParts()[0], cols, newValues) + ")";
            }
            case M_ADD, M_SUB, M_MUL, M_DIV, M_MOD -> {
                if (wherePart.getConditionParts().length != 2)
                    throw new SQLException(wherePart.getType() + " condition must have exactly two sub-conditions");
                String op = switch (wherePart.getType()) {
                    case M_ADD -> "+";
                    case M_SUB -> "-";
                    case M_MUL -> "*";
                    case M_DIV -> "/";
                    case M_MOD -> "%";
                    default -> throw new SQLException("Unknown math operation: " + wherePart.getType());
                };
                return "(" + createWherePart(wherePart.getConditionParts()[0], cols, newValues) + " " + op + " " + createWherePart(wherePart.getConditionParts()[1], cols, newValues) + ")";
            }
            case C_EQ, C_NEQ, C_GT, C_GE, C_LT, C_LE, LIKE -> {
                if (wherePart.getConditionParts().length != 2)
                    throw new SQLException(wherePart.getType() + " condition must have exactly two sub-conditions");
                String op = switch (wherePart.getType()) {
                    case C_EQ -> "=";
                    case C_NEQ -> "<>";
                    case C_GT -> ">";
                    case C_GE -> ">=";
                    case C_LT -> "<";
                    case C_LE -> "<=";
                    case LIKE -> "LIKE";
                    default -> throw new SQLException("Unknown comparison operation: " + wherePart.getType());
                };
                return "(" + createWherePart(wherePart.getConditionParts()[0], cols, newValues) + " " + op + " " + createWherePart(wherePart.getConditionParts()[1], cols, newValues) + ")";
            }
            case COALESCE -> {
                if (wherePart.getConditionParts().length != 2)
                    throw new SQLException("COALESCE condition must have exactly two sub-conditions");
                return "COALESCE(" + createWherePart(wherePart.getConditionParts()[0], cols, newValues) + ", " + createWherePart(wherePart.getConditionParts()[1], cols, newValues) + ")";
            }
            case M_FLOOR, M_CEIL, M_ABS -> {
                if (wherePart.getConditionParts().length != 1)
                    throw new SQLException(wherePart.getType() + " condition must have exactly one sub-condition");
                String func = switch (wherePart.getType()) {
                    case M_FLOOR -> "FLOOR";
                    case M_CEIL -> "CEIL";
                    case M_ABS -> "ABS";
                    default -> throw new SQLException("Unknown math function: " + wherePart.getType());
                };
                return func + "(" + createWherePart(wherePart.getConditionParts()[0], cols, newValues) + ")";
            }
            default -> throw new SQLException("Unknown where condition type: " + wherePart.getType());
        }
    }

    private static PreparedStatement createWhereStatement(Connection conn, String firstPart, List<PrepStatementColValue> prevValues, Map<String, TableColumnDto> cols, TableBasicQueryDto basicQuery, BaseQuotaProperties userQuota) throws SQLException {
        StringBuilder fullQueryBuilder = new StringBuilder();
        fullQueryBuilder.append(firstPart);

        // TODO: Check against Query Quotas
        List<PrepStatementColValue> newValues = new ArrayList<>();
        if (basicQuery != null) {
            // Where
            if (basicQuery.getWhere() != null) {
                fullQueryBuilder.append(" WHERE ");
                fullQueryBuilder.append(createWherePart(basicQuery.getWhere(), cols, newValues));
            }

            // Sort By
            if (basicQuery.getSortByCols() != null && basicQuery.getSortByCols().length > 0) {
                // TODO: Check for cols and order being identical
                // TODO: Check for col existence
                fullQueryBuilder.append(" ORDER BY ");
                StringJoiner sortJoiner = new StringJoiner(", ");
                for (int i = 0; i < basicQuery.getSortByCols().length; i++) {
                    String colName = basicQuery.getSortByCols()[i];
                    TableBasicQueryDto.SortOrder order =  basicQuery.getSortOrders()[i];
                    sortJoiner.add("\"" + colName + "\" " + order.name());
                }
                fullQueryBuilder.append(sortJoiner);
            }

            // TODO: Add Default Limit if it doesnt exist and if it does exist the use min of limit and quota limit
            // Limit
            if (basicQuery.getLimit() != null) {
                // TODO: Bounds Check
                fullQueryBuilder.append(" LIMIT ?");
                newValues.add(new PrepStatementColValue(TableColumnDto.Type.INT, basicQuery.getLimit()));
            }


            // Offset
            if (basicQuery.getOffset() != null) {
                // TODO: Bounds Check
                fullQueryBuilder.append(" OFFSET ?");
                newValues.add(new PrepStatementColValue(TableColumnDto.Type.INT, basicQuery.getOffset()));
            }
        }
        fullQueryBuilder.append(";");

        // Create. Populate and return statement
        PreparedStatement statement = conn.prepareStatement(fullQueryBuilder.toString());
        int pIndex = 1;
        for (var val : prevValues)
            TableColumnDto.addValueToPreparedStatement(statement, pIndex++, val.value, val.type);
        for (var val : newValues)
            TableColumnDto.addValueToPreparedStatement(statement, pIndex++, val.value, val.type);
        return statement;
    }

    private static ServiceTableQueryResultDto fromResultSet(ResultSet rs, List<TableColumnDto> resultCols) throws SQLException {
        ServiceTableQueryResultDto res = new ServiceTableQueryResultDto();
        res.setColNames(resultCols.stream().map(TableColumnDto::getColName).toArray(String[]::new));
        res.setColTypes(resultCols.stream().map(TableColumnDto::getType).toArray(TableColumnDto.Type[]::new));

        // Read Results
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 0; i < resultCols.size(); i++) {
                TableColumnDto colDto = resultCols.get(i);
                // TODO: potentially update to use the correct datatype from the start?
                String rawVal = rs.getString(i + 1);
                Object value = TableColumnDto.parseRawStringSqlValue(rawVal, colDto.getType());
                row.add(value);
            }
            rows.add(row);
        }
        res.setRows(rows.stream().map(List::toArray).toArray(Object[][]::new));
        return res;
    }

    public ServiceTableQueryResultDto queryTable(ServiceEntry entry, String tableUuid, TableSelectDto select, BaseQuotaProperties userQuotas) throws SQLException {
        try (Connection conn = getConnection(entry.getUuid())) {
            String tableName = getDbTableNameFromTableUuid(tableUuid);

            // TODO: Optimize the Col retrieval in the future
            Map<String, TableColumnDto> cols = getAllTableColumns(entry, tableUuid).stream().collect(Collectors.toMap(TableColumnDto::getColName, col -> col));
            List<TableColumnDto> resultCols = new ArrayList<>();

            // If cols empty, use all cols
            if (select.getColNames().length == 0) {
                select.setColNames(cols.keySet().toArray(new String[0]));
            }

            // Columns
            StringJoiner colDefs = new StringJoiner(", ");
            for (var colName : select.getColNames()) {
                TableColumnDto colDto = cols.get(colName);
                if (colDto == null)
                    throw new SQLException("Column " + colName + " does not exist in table " + tableUuid);
                resultCols.add(colDto);
                colDefs.add("\"" + colName + "\"");
            }

            // Create Query String
            String selectQuery = "SELECT " + colDefs + " FROM \"" + tableName + "\"";

            try (PreparedStatement statement = createWhereStatement(conn, selectQuery, List.of(), cols, select.getBasicQuery(), userQuotas)) {
                ResultSet rs = statement.executeQuery();
                return fromResultSet(rs, resultCols);
            }
        }
    }

    public int updateQueryTable(ServiceEntry entry, String tableUuid, TableUpdateDto updateDto, BaseQuotaProperties userQuotas) throws SQLException {
        try (Connection conn = getConnection(entry.getUuid())) {
            String tableName = getDbTableNameFromTableUuid(tableUuid);

            // TODO: Optimize the Col retrieval in the future
            Map<String, TableColumnDto> cols = getAllTableColumns(entry, tableUuid).stream().collect(Collectors.toMap(TableColumnDto::getColName, col -> col));

            if (updateDto.getColNames().length == 0)
                throw new SQLException("No columns specified for update");

            // Columns
            StringJoiner updateDefs = new StringJoiner(", ");
            List<PrepStatementColValue> updateVals = new ArrayList<>();
            for (int i = 0; i < updateDto.getColNames().length; i++) {
                TableColumnDto colDto = cols.get(updateDto.getColNames()[i]);
                if (colDto == null)
                    throw new SQLException("Column " + updateDto.getColNames()[i] + " does not exist in table " + tableUuid);
                updateVals.add(new PrepStatementColValue(colDto.getType(), updateDto.getColValues()[i]));
                updateDefs.add("\"" + updateDto.getColNames()[i] + "\" = ?");
            }

            // Create Query String
            String updateQuery = "UPDATE \"" + tableName + "\" SET " + updateDefs;

            try (PreparedStatement statement = createWhereStatement(conn, updateQuery, updateVals, cols, updateDto.getBasicQuery(), userQuotas)) {
                return statement.executeUpdate();
            }
        }
    }

    public int deleteQueryTable(ServiceEntry entry, String tableUuid, TableBasicQueryDto queryDto, BaseQuotaProperties userQuotas) throws SQLException {
        try (Connection conn = getConnection(entry.getUuid())) {
            String tableName = getDbTableNameFromTableUuid(tableUuid);

            // TODO: Optimize the Col retrieval in the future
            Map<String, TableColumnDto> cols = getAllTableColumns(entry, tableUuid).stream().collect(Collectors.toMap(TableColumnDto::getColName, col -> col));

            // Create Delete Query String
            String deleteQuery = "DELETE FROM \"" + tableName + "\"";

            try (PreparedStatement statement = createWhereStatement(conn, deleteQuery, List.of(), cols, queryDto,userQuotas)) {
                return statement.executeUpdate();
            }
        }
    }
}
