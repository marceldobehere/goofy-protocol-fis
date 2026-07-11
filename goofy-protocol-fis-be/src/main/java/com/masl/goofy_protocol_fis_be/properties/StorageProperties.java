package com.masl.goofy_protocol_fis_be.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goofy.storage")
@Data
public class StorageProperties {
    private Boolean useTempDir;
    private Boolean createDirectories;
    private String baseUserDatabasesPath;
    private String baseUserBucketsPath;
}
