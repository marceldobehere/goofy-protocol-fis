package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

// TODO: Test
@Service
public class UserDbService {
    private static final Logger log = LoggerFactory.getLogger(UserDbService.class);

    @Getter
    private static UserDbService singleton;

    private final FileStorageService fileStorageService;

    public UserDbService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;

        singleton = this;
    }

    public void createEntry(ServiceEntry entry) throws IOException, SQLException {
        deleteEntry(entry); // Ensure no existing folder
        fileStorageService.createDbFolder(entry.getUuid());
        initDb(entry);
    }

    public void deleteEntry(ServiceEntry entry) throws IOException {
        fileStorageService.deleteDbFolder(entry.getUuid());
    }

    private void initDb(ServiceEntry entry) throws SQLException {
        Connection test = getConnection(entry.getUuid());
        if (test == null)
            throw new SQLException("Failed to create database connection for entry: " + entry.getUuid());

        // Check if everything works
        List<String> tables = getAllTables(test);
        log.info("Initialized database for entry: {}, tables: {}", entry.getUuid(), tables);

        test.close();
    }

    public Connection getConnection(String uuid) throws SQLException {
        Path dbFile = fileStorageService.getDbFolderPath(uuid).resolve("userData");
        String dbBase = dbFile.toAbsolutePath().toString();

        String url = "jdbc:h2:file:" + dbBase + ";DB_CLOSE_DELAY=-1" + ";AUTO_SERVER=TRUE";

        return DriverManager.getConnection(url, "sa", "");
    }

    public List<String> getAllTables(Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[] { "TABLE" })) {
            List<String> tables = new java.util.ArrayList<>();
            while (rs.next())
                tables.add(rs.getString("TABLE_NAME"));
            return tables;
        }
    }
}
