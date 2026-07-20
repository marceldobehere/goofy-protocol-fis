package com.masl.goofy_protocol_fis_be.entity;

import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class UserQuotas {
    // Primary Key based on the User
    @Id
    @Column(nullable = false, name = "handle", length = FieldSize.HANDLE_LEN)
    private String handle;

    // User reference
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(name = "handle", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // General
    @Column
    private Integer generalMaxNameSize;

    // Identity
    @Column
    private Integer identityMaxEntries;
    @Column
    private Integer identityMaxServiceEntries;

    // Table
    @Column
    private Long tableMaxDbSize;
    @Column
    private Long tableMaxFieldSize;
    @Column
    private Integer tableMaxTables;
    @Column
    private Integer tableMaxCols;
    @Column
    private Integer tableMaxRows;
    @Column
    private Integer tableMaxPermissionCount;
    @Column
    private Integer tableMaxLockDurationSeconds;

    // TableQuery
    @Column
    private Long tableQueryMaxQueryLength;
    @Column
    private Integer tableQueryMaxConditionCount;
    @Column
    private Integer tableQueryMaxResultCount;

    // Bucket
    @Column
    private Long bucketMaxBucketSize;
    @Column
    private Long bucketMaxItemSize;
    @Column
    private Integer bucketMaxItemCount;
    @Column
    private Integer bucketMaxPermissionCount;

    // Helper Functions

    public static BaseQuotaProperties getUserQuotas(UserQuotas quotas, BaseQuotaProperties base) {
        if (quotas == null)
            return base;
        return quotas._getUserQuotas(base);
    }

    private static <T> T valueOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    private BaseQuotaProperties _getUserQuotas(BaseQuotaProperties base) {
        BaseQuotaProperties userQuotas = new BaseQuotaProperties();

        // Create Object
        BaseQuotaProperties.General general = new BaseQuotaProperties.General();
        BaseQuotaProperties.Identity identity = new BaseQuotaProperties.Identity();
        BaseQuotaProperties.Table table = new BaseQuotaProperties.Table();
        BaseQuotaProperties.TableQuery tableQuery = new BaseQuotaProperties.TableQuery();
        BaseQuotaProperties.Bucket bucket = new BaseQuotaProperties.Bucket();

        // Attach
        userQuotas.setGeneral(general);
        userQuotas.setIdentity(identity);
        userQuotas.setTable(table);
        userQuotas.setTableQuery(tableQuery);
        userQuotas.setBucket(bucket);

        // General
        general.setMaxNameSize(valueOrDefault(generalMaxNameSize, base.getGeneral().getMaxNameSize()));

        // Identity
        identity.setMaxEntries(valueOrDefault(identityMaxEntries, base.getIdentity().getMaxEntries()));
        identity.setMaxServiceEntries(valueOrDefault(identityMaxServiceEntries, base.getIdentity().getMaxServiceEntries()));

        // Table
        table.setMaxDbSize(valueOrDefault(tableMaxDbSize, base.getTable().getMaxDbSize()));
        table.setMaxFieldSize(valueOrDefault(tableMaxFieldSize, base.getTable().getMaxFieldSize()));
        table.setMaxTables(valueOrDefault(tableMaxTables, base.getTable().getMaxTables()));
        table.setMaxCols(valueOrDefault(tableMaxCols, base.getTable().getMaxCols()));
        table.setMaxRows(valueOrDefault(tableMaxRows, base.getTable().getMaxRows()));
        table.setMaxPermissionCount(valueOrDefault(tableMaxPermissionCount, base.getTable().getMaxPermissionCount()));
        table.setMaxLockDurationSeconds(valueOrDefault(tableMaxLockDurationSeconds, base.getTable().getMaxLockDurationSeconds()));

        // TableQuery
        tableQuery.setMaxQueryLength(valueOrDefault(tableQueryMaxQueryLength, base.getTableQuery().getMaxQueryLength()));
        tableQuery.setMaxConditionCount(valueOrDefault(tableQueryMaxConditionCount, base.getTableQuery().getMaxConditionCount()));
        tableQuery.setMaxResultCount(valueOrDefault(tableQueryMaxResultCount, base.getTableQuery().getMaxResultCount()));

        // Bucket
        bucket.setMaxBucketSize(valueOrDefault(bucketMaxBucketSize, base.getBucket().getMaxBucketSize()));
        bucket.setMaxItemSize(valueOrDefault(bucketMaxItemSize, base.getBucket().getMaxItemSize()));
        bucket.setMaxItemCount(valueOrDefault(bucketMaxItemCount, base.getBucket().getMaxItemCount()));
        bucket.setMaxPermissionCount(valueOrDefault(bucketMaxPermissionCount, base.getBucket().getMaxPermissionCount()));

        return userQuotas;
    }
}
