package com.masl.goofy_protocol_fis_be.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "goofy.quota.base")
@Data
public class BaseQuotaProperties {
    private General general = new General();
    private Identity identity = new Identity();
    private Table table = new Table();
    private TableQuery tableQuery = new TableQuery();
    private Bucket bucket = new Bucket();

    @Data
    public static class General {
        private Integer maxNameSize;
    }

    @Data
    public static class Identity {
        private Integer maxEntries;
        private Integer maxServiceEntries;
    }

    @Data
    public static class Table {
        private Long maxDbSize;
        private Long maxFieldSize;
        private Integer maxTables;
        private Integer maxCols;
        private Integer maxRows;
        private Integer maxPermissionCount;
        private Integer maxLockDurationSeconds;
    }

    @Data
    public static class TableQuery {
        private Long maxQueryLength;
        private Integer maxConditionCount;
        private Integer maxResultCount;
    }

    @Data
    public static class Bucket {
        private Long maxBucketSize;
        private Long maxItemSize;
        private Integer maxItemCount;
        private Integer maxPermissionCount;
    }
}
