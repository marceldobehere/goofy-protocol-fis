package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.dto.both.ServiceTableEntryDto;
import com.masl.goofy_protocol_fis_be.dto.both.TableColumnDto;
import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
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
        fileStorageService.deleteDbFolder(entry.getUuid());
        // TODO: Check DB Connections are closed / Close Server somehow
    }

    public synchronized void deleteTableEntry(ServiceEntry entry, String tableUuid) {
        // TODO: Implement
        // TODO: Check DB Connections are closed / Close Server somehow
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
                        parsedDefault = TableColumnDto.parseDefaultSqlValue(rawDefault, inferredType);
                    }

                    // Create actual DTO
                    columns.add(TableColumnDto.fromSqlData(colName, colType, colSize, constraints, parsedDefault));
                }
            }

            return columns;
        }
    }

    public void createTableEntry(ServiceEntry entry, ServiceTableEntryDto tableEntryDto) throws IOException, SQLException {
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

    public void insertIntoTable(ServiceEntry entry, String tableUuid, Map<String, Object> values) throws IOException, SQLException {
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
                    throw new SQLException("Column " + colName + " does not exist in table " + tableName);

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
}
