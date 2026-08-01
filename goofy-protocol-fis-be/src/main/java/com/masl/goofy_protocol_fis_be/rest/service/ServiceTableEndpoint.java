package com.masl.goofy_protocol_fis_be.rest.service;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.dto.both.ServiceTableEntryDto;
import com.masl.goofy_protocol_fis_be.dto.both.TableColumnDto;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableBasicQueryDto;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableMultiRowInsertDto;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableSelectDto;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableUpdateDto;
import com.masl.goofy_protocol_fis_be.dto.response.ServiceDbQuotasDto;
import com.masl.goofy_protocol_fis_be.dto.response.ServiceTableQueryResultDto;
import com.masl.goofy_protocol_fis_be.dto.response.ServiceTableQuotasDto;
import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import com.masl.goofy_protocol_fis_be.entity.ServiceTableEntry;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.*;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.ServiceEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.ServiceTableEntryRepository;
import com.masl.goofy_protocol_fis_be.service.QuotaService;
import com.masl.goofy_protocol_fis_be.service.TableLockService;
import com.masl.goofy_protocol_fis_be.service.UserDbService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/service-table")
@Tag(name = "Service Table Access", description = "Endpoints related to accessing Tables.")
public class ServiceTableEndpoint {
    private final ServiceTableEntryRepository tableEntryRepository;
    private final ServiceEntryRepository serviceEntryRepository;
    private final QuotaService quotaService;
    private final UserDbService userDbService;
    private final TableLockService tableLockService;

    public ServiceTableEndpoint(ServiceTableEntryRepository tableEntryRepository, ServiceEntryRepository serviceEntryRepository, QuotaService quotaService, UserDbService userDbService, TableLockService tableLockService) {
        this.tableEntryRepository = tableEntryRepository;
        this.serviceEntryRepository = serviceEntryRepository;
        this.quotaService = quotaService;
        this.userDbService = userDbService;
        this.tableLockService = tableLockService;
    }


    // --- IDENTITY ONLY ---


    // Get DB Quota and Stats (Count & Size)
    @GetMapping("/{idHandle}/{serviceUuid}/quotas")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets the DB Quotas and Stats", description = "Gets the DB Quotas and Stats (Count & Size).")
    public ServiceDbQuotasDto getDbQuotas(@PathVariable String idHandle, @PathVariable String serviceUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        checkServiceEntryAccessPermissions(entry, auth);
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        // Get Stats
        List<ServiceTableEntry> entries = tableEntryRepository.findAllByLinkedIdentity_Handle_AndLinkedServiceEntry_Uuid(idHandle, serviceUuid);
        int tableCount = entries.size();

        // Get DB Size
        Long currDbSize;
        try {
            currDbSize = userDbService.getDbSize(entry);
        } catch (IOException e) {
            throw new ServiceEntryNotFound(serviceUuid);
        }

        return new ServiceDbQuotasDto(
                tableCount,
                currDbSize,
                userQuotas.getTable().getMaxTables(),
                userQuotas.getTable().getMaxDbSize(),
                userQuotas.getTable().getMaxFieldSize(),
                userQuotas.getTable().getMaxCols(),
                userQuotas.getTable().getMaxRows(),
                userQuotas.getTable().getMaxPermissionCount(),
                userQuotas.getTable().getMaxLockDurationSeconds(),
                userQuotas.getTableQuery().getMaxQueryLength(),
                userQuotas.getTableQuery().getMaxConditionCount(),
                userQuotas.getTableQuery().getMaxResultCount(),
                userQuotas.getGeneral().getMaxNameSize()
        );
    }


    // --- OUTSIDE ENTITIES ---


    // TODO: Get all Table Names / Get all Tables for a name for a service uuid & table uuid
    // TODO: Make Table names unique?

    @GetMapping("/{idHandle}/{serviceUuid}/quotas/{tableUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets the Table Quotas and Stats", description = "Gets the Table Quotas and Stats (Count & Size).")
    public ServiceTableQuotasDto getTableQuotasEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableNotFound, ServiceTableSqlError {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryReadPermissions(entry, tableEntry, auth);
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        // Get Stats
        List<ServiceTableEntry> entries = tableEntryRepository.findAllByLinkedIdentity_Handle_AndLinkedServiceEntry_Uuid(idHandle, serviceUuid);
        int currTableCount = entries.size();

        // Column Count
        int currColumnCount;
        try {
            currColumnCount = userDbService.getTableColumnCount(entry, tableUuid);
        } catch (SQLException e) {
            throw new ServiceTableSqlError(tableUuid, e.getMessage());
        }

        // Row Count
        int currRowCount;
        try {
            currRowCount = (int)userDbService.getTableRowCount(entry, tableUuid);
        } catch (SQLException e) {
            throw new ServiceTableSqlError(tableUuid, e.getMessage());
        }

        // Return Data
        return new ServiceTableQuotasDto(
                currTableCount,
                currColumnCount,
                currRowCount,
                userQuotas.getTable().getMaxTables(),
                userQuotas.getTable().getMaxCols(),
                userQuotas.getTable().getMaxRows(),
                userQuotas.getTable().getMaxFieldSize(),
                userQuotas.getTable().getMaxPermissionCount()
        );
    }

    // Get Table Entry Config (Name, Read Access, Write Access, Size, Timestamp, Columns, Schema Version, Row Count, ...)
    @GetMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets a Table Entry", description = "Get the Table Entry for a specific Table UUID")
    public ServiceTableEntryDto getTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableNotFound, ServiceTableLockInvalid, ServiceTableSqlError {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, true, false);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryReadPermissions(entry, tableEntry, auth);

        return fromServiceTableEntry(tableEntry, tableEntry.getLinkedIdentity().getHandle().equals(auth.getHandle()));
    }

    // Set Table Entry Config (Name, Read Access, Write Access, Schema Version, Columns, ...)
    @PutMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Sets a Table Entry", description = "Set the Table Entry for a specific Table UUID. <br>Note: The Columns can only be changed if the schema version provided is larger and provides default values for new non-null columns. <br>Also keep in mind that renaming a column acts as deleting the old column and adding a new one, leading to potential data loss if done carelessly!")
    public void setTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @Valid @RequestBody ServiceTableEntryDto entryDto, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableNotFound, ServiceTableLockInvalid, ServiceTableInvalidMigration, ServiceTableQuotaExceeded, ServiceTableSqlError {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, false, true);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);
        ServiceEntry.checkForRestrictedAccess(entry.getCreatedBy());
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        // Update Name
        if (!tableEntry.getTableName().equals(entryDto.getTableName())) {
            if (entryDto.getTableName().length() > userQuotas.getGeneral().getMaxNameSize())
                throw new ServiceTableQuotaExceeded("generalMaxNameSize");

            // TODO: Check if name already exists?
            tableEntry.setTableName(entryDto.getTableName());
        }

        // Schema Update
        if (entryDto.getColumns() != null || entryDto.getSchemaVersion() != null) {
            if (entryDto.getColumns() == null || entryDto.getSchemaVersion() == null)
                throw new ServiceTableInvalidMigration(tableUuid, "Columns and Schema Version required");
            if (tableEntry.getSchemaVersion() > entryDto.getSchemaVersion())
                throw new ServiceTableInvalidMigration(tableUuid, "New Schema Version cannot be lower than current Version");

            // Check Columns
            Set<String> colNames = new HashSet<>();
            for (TableColumnDto colDto : entryDto.getColumns()) {
                if (colNames.contains(colDto.getColName()))
                    throw new ServiceTableInvalidMigration(tableUuid, "Duplicate Column Name: " + colDto.getColName());
                else
                    colNames.add(colDto.getColName());
                if (colDto.getConstraints().contains(TableColumnDto.Constraint.NOT_NULL) && colDto.getDefaultValue() == null)
                    throw new ServiceTableInvalidMigration(tableUuid, "New NOT NULL Column must have a Default Value: " + colDto.getColName());
                if (colDto.getColName().length() > userQuotas.getGeneral().getMaxNameSize())
                    throw new ServiceTableQuotaExceeded("generalMaxNameSize");
                if (TableColumnDto.getTypeSize(colDto.getType(), colDto.getTypeSize()) > userQuotas.getTable().getMaxFieldSize())
                    throw new ServiceTableQuotaExceeded("tableMaxFieldSize");
            }

            var comparison = compareSchemas(tableEntry, entryDto);
            if (tableEntry.getSchemaVersion().equals(entryDto.getSchemaVersion())) {
                if (!comparison.identical)
                    throw new ServiceTableInvalidMigration(tableUuid, "Same Schema Version but different Columns, please increase the Schema Version to update the Columns");
            } else {
                if (comparison.identical)
                    throw new ServiceTableInvalidMigration(tableUuid, "Schema Version increased but Columns are identical, please only increase the Schema Version if you want to update the Columns");
                else {
                    try {
                        userDbService.updateTableEntrySchema(entry, entryDto, comparison);
                    } catch (SQLException e) {
                        throw new ServiceTableSqlError(tableUuid, e.getMessage());
                    }
                }
            }
        }

        // Perms
        if (tableEntry.getLinkedIdentity().getHandle().equals(auth.getHandle())) {
            if (entryDto.getHandlesWithReadPerms().length > userQuotas.getTable().getMaxPermissionCount())
                throw new ServiceTableQuotaExceeded("tableMaxPermissionCount");
            if (entryDto.getHandlesWithWritePerms().length > userQuotas.getTable().getMaxPermissionCount())
                throw new ServiceTableQuotaExceeded("tableMaxPermissionCount");

            tableEntry.setExtraReadPerms(new HashSet<>(Arrays.asList(entryDto.getHandlesWithReadPerms())));
            tableEntry.setExtraWritePerms(new HashSet<>(Arrays.asList(entryDto.getHandlesWithWritePerms())));
        }

        tableEntry.setLastUpdatedBy(auth.getHandle());
        tableEntry.setLastUpdatedAt(Instant.now());

        tableEntryRepository.save(tableEntry);
    }

    // Delete Table Entry
    @DeleteMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Deletes a Table Entry", description = "Deletes a Table Entry based on a specific Table UUID")
    public void deleteTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, false, true);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);
        ServiceEntry.checkForRestrictedAccess(entry.getCreatedBy());

        // Delete Table Entry
        tableEntryRepository.deleteByTableUuid_AndLinkedIdentity_Handle(tableUuid, idHandle);
    }

    // Get All Table Entries
    @GetMapping("/{idHandle}/{serviceUuid}/entry")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets all Table Entries", description = "Get all Table Entries")
    public List<ServiceTableEntryDto> getAllTableEntries(@PathVariable String idHandle, @PathVariable String serviceUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableSqlError {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        checkServiceEntryAccessPermissions(entry, auth);

        boolean perms = entry.getLinkedIdentity().getHandle().equals(auth.getHandle());
        List<ServiceTableEntryDto> dtos = new ArrayList<>();
        for (ServiceTableEntry tableEntry : entry.getServiceTableEntries())
            dtos.add(fromServiceTableEntry(tableEntry, perms));
        return dtos;
    }

    // Lock Table Entry
    @PostMapping("/{idHandle}/{serviceUuid}/lock/{tableUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Lock a Table Entry", description = "Locks a Table Entry based on a specific Table UUID and specific permissions (read / write) and returns the Lock Token. <br>Locks should be unlocked when you're done using them, but they can also time out after a maximum duration, you can check it in the Quotas.")
    public String lockTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestParam Boolean readLock, @RequestParam Boolean writeLock, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound, ServiceTableLockRequestInvalid {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, null, readLock, writeLock);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);
        ServiceEntry.checkForRestrictedAccess(entry.getCreatedBy());

        // Lock Table Entry
        return tableLockService.lockServiceTableEntry(serviceUuid, tableUuid, readLock, writeLock);
    }

    // Unlock Table Entry
    @PostMapping("/{idHandle}/{serviceUuid}/unlock/{tableUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Unlock a Table Entry", description = "Unlocks a Table Entry based on a specific Table UUID, the lockToken and the specific permissions (read / write).")
    public void unlockTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestParam Boolean readLock, @RequestParam Boolean writeLock, @RequestHeader(name = "X-Lock-Token") String lockToken, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, readLock, writeLock);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);
        ServiceEntry.checkForRestrictedAccess(entry.getCreatedBy());

        // Unlock Table Entry
        tableLockService.unlockServiceTableEntry(serviceUuid, tableUuid, lockToken, readLock, writeLock);
    }

    // Create Table Entry (Default, no UUID) (will be private by default)
    @PostMapping("/{idHandle}/{serviceUuid}/entry")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Create a Table Entry", description = "Creates a Table Entry (Default, no UUID) (will be private by default). <br> Tables may only have one primary key currently.")
    public ServiceTableEntryDto createTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @Valid @RequestBody ServiceTableEntryDto entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableQuotaExceeded, ServiceTableEntryInvalid, ServiceTableSqlError {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        checkServiceEntryAccessPermissions(entry, auth);
        ServiceEntry.checkForRestrictedAccess(entry.getCreatedBy());
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        // Get DB Size
        Long currDbSize;
        try {
            currDbSize = userDbService.getDbSize(entry);
        } catch (IOException e) {
            throw new ServiceEntryNotFound(serviceUuid);
        }

        // Check Basic Quotas
        if (currDbSize >= userQuotas.getTable().getMaxDbSize())
            throw new ServiceTableQuotaExceeded("tableMaxDbSize");
        if (entry.getServiceTableEntries().size() >= userQuotas.getTable().getMaxTables())
            throw new ServiceTableQuotaExceeded("tableMaxTables");

        // Check DTO
        if (entryDto.getSchemaVersion() == null || entryDto.getColumns() == null)
            throw new ServiceTableEntryInvalid("Missing Fields");
        if (entryDto.getSchemaVersion() < 0)
            throw new ServiceTableEntryInvalid("Invalid Schema Version");
        if (entryDto.getColumns().length < 1)
            throw new ServiceTableEntryInvalid("At least one Column is required");

        // Check Quotas
        if (entryDto.getColumns().length > userQuotas.getTable().getMaxCols())
            throw new ServiceTableQuotaExceeded("tableMaxCols");
        if (entryDto.getTableName().length() > userQuotas.getGeneral().getMaxNameSize())
            throw new ServiceTableQuotaExceeded("generalMaxNameSize");
        if (entryDto.getHandlesWithReadPerms().length > userQuotas.getTable().getMaxPermissionCount())
            throw new ServiceTableQuotaExceeded("tableMaxPermissionCount");
        if (entryDto.getHandlesWithWritePerms().length > userQuotas.getTable().getMaxPermissionCount())
            throw new ServiceTableQuotaExceeded("tableMaxPermissionCount");

        // Check Columns
        Set<String> colNames = new HashSet<>();
        for (TableColumnDto colDto : entryDto.getColumns()) {
            if (colNames.contains(colDto.getColName()))
                throw new ServiceTableEntryInvalid("Duplicate Column Name: " + colDto.getColName());
            else
                colNames.add(colDto.getColName());
            if (colDto.getColName().length() > userQuotas.getGeneral().getMaxNameSize())
                throw new ServiceTableQuotaExceeded("generalMaxNameSize");
            if (TableColumnDto.getTypeSize(colDto.getType(), colDto.getTypeSize()) > userQuotas.getTable().getMaxFieldSize())
                throw new ServiceTableQuotaExceeded("tableMaxFieldSize");
        }

        // TODO: Check if name already exists?

        // Create Table Entry
        ServiceTableEntry tableEntry = new ServiceTableEntry();
        tableEntry.setTableUuid(UUID.randomUUID().toString());
        tableEntry.setTableName(entryDto.getTableName());
        tableEntry.setSchemaVersion(entryDto.getSchemaVersion() != null ? entryDto.getSchemaVersion() : 1);
        tableEntry.setLinkedIdentity(entry.getLinkedIdentity());
        tableEntry.setLinkedServiceEntry(entry);
        tableEntry.setCreatedAt(Instant.now());
        tableEntry.setCreatedBy(auth.getHandle());
        tableEntry.setExtraReadPerms(new HashSet<>(Arrays.asList(entryDto.getHandlesWithReadPerms())));
        tableEntry.setExtraWritePerms(new HashSet<>(Arrays.asList(entryDto.getHandlesWithWritePerms())));

        entryDto.setTableUuid(tableEntry.getTableUuid());

        // Create Table in DB
        try {
            userDbService.createTableEntry(entry, entryDto);
        } catch (SQLException e) {
            throw new ServiceTableSqlError(tableEntry.getTableUuid(), e.getMessage());
        }

        tableEntryRepository.save(tableEntry);
        return fromServiceTableEntry(tableEntry, true);
    }

    @PostMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}/rows")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Inserts a Row into a Table Entry", description = "Inserts a Row into a Table Entry based on the provided data. <br> The data must match the table's schema and constraints. <br> The format is just a json object with the keys being the column names and values being the values")
    public void insertQueryTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @Valid @RequestBody Map<String, Object> insertFields, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound, ServiceTableQuotaExceeded, ServiceTableInsertEntryInvalid, ServiceTableSqlError {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, false, true);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);
        ServiceEntry.checkForRestrictedAccess(entry.getCreatedBy());
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        // Check insert Object
        if (insertFields.size() > userQuotas.getTable().getMaxCols())
            throw new ServiceTableInsertEntryInvalid("Too many columns in insert object");
        for (var insertEntry : insertFields.entrySet()) {
            if (insertEntry.getKey().length() > userQuotas.getGeneral().getMaxNameSize())
                throw new ServiceTableQuotaExceeded("generalMaxNameSize");
            if (!insertEntry.getKey().matches("^[a-z0-9_]+$"))
                throw new ServiceTableInsertEntryInvalid("Invalid Column Name: " + insertEntry.getKey());
            if (insertEntry.getValue() != null && (insertEntry.getValue() instanceof Map || insertEntry.getValue() instanceof List || insertEntry.getValue().getClass().isArray()))
                throw new ServiceTableInsertEntryInvalid("Invalid Column Value Type: " + insertEntry.getKey());
        }

        try {
            // Check Max Rows
            if (userDbService.getTableRowCount(entry, tableUuid) >= userQuotas.getTable().getMaxRows())
                throw new ServiceTableQuotaExceeded("tableMaxRows");

            // TODO: Check Max DB Size

            // Insert
            userDbService.insertIntoTable(entry, tableUuid, insertFields);
        } catch (SQLException e) {
            throw new ServiceTableSqlError(tableUuid, e.getMessage());
        }
    }

    @PostMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}/rows-bulk")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Bulk Insert Rows into a Table Entry", description = "Inserts multiple Rows into a Table Entry based on the provided data. <br> The data must match the table's schema and constraints.")
    public void insertBulkQueryTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @Valid @RequestBody TableMultiRowInsertDto insertDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound, ServiceTableQuotaExceeded, ServiceTableInsertEntryInvalid, ServiceTableSqlError {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, false, true);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);
        ServiceEntry.checkForRestrictedAccess(entry.getCreatedBy());
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        // Check insert Cols
        if (insertDto.getColNames().length > userQuotas.getTable().getMaxCols())
            throw new ServiceTableInsertEntryInvalid("Too many columns in insert object");
        for (var insertColName : insertDto.getColNames()) {
            if (insertColName.length() > userQuotas.getGeneral().getMaxNameSize())
                throw new ServiceTableQuotaExceeded("generalMaxNameSize");
            if (!insertColName.matches("^[a-z0-9_]+$"))
                throw new ServiceTableInsertEntryInvalid("Invalid Column Name: " + insertColName);
        }

        // Check insert Rows
        try {
            if (insertDto.getRows().size() + userDbService.getTableRowCount(entry, tableUuid) > userQuotas.getTable().getMaxRows())
                throw new ServiceTableInsertEntryInvalid("Too many rows in insert object");

            // TODO: Check Max DB Size

            for (var insertRow : insertDto.getRows()) {
                if (insertRow.length != insertDto.getColNames().length)
                    throw new ServiceTableInsertEntryInvalid("Row length does not match column length");
                for (var insertRowVal : insertRow) {
                    if (insertRowVal != null && (insertRowVal instanceof Map || insertRowVal instanceof List || insertRowVal.getClass().isArray()))
                        throw new ServiceTableInsertEntryInvalid("Invalid Column Value Type: " + insertRowVal);
                }
            }
        } catch (SQLException e) {
            throw new ServiceTableSqlError(tableUuid, e.getMessage());
        }

        try {
            // Insert Multiple
            userDbService.insertMultipleIntoTable(entry, tableUuid, insertDto);
        } catch (SQLException e) {
            throw new ServiceTableSqlError(tableUuid, e.getMessage());
        }
    }

    @DeleteMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}/rows")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Deletes Rows from a Table Entry based on a Query")
    public Integer deleteQueryTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @Valid @RequestBody TableBasicQueryDto deleteQuery, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound, ServiceTableSqlError {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, true, true);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);
        ServiceEntry.checkForRestrictedAccess(entry.getCreatedBy());
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        try {
            // Delete
            return userDbService.deleteQueryTable(entry, tableUuid, deleteQuery, userQuotas);
        } catch (SQLException e) {
            throw new ServiceTableSqlError(tableUuid, e.getMessage());
        }
    }

    @PutMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}/rows")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Updates Rows from a Table Entry based on a Query")
    public Integer updateQueryTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @Valid @RequestBody TableUpdateDto updateDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound, ServiceTableSqlError, ServiceTableQueryInvalid, ServiceTableQuotaExceeded {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, false, true);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);
        ServiceEntry.checkForRestrictedAccess(entry.getCreatedBy());
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        // TODO: Check Max DB Size

        // Check Update Object
        if (updateDto.getColNames().length > userQuotas.getTable().getMaxCols())
            throw new ServiceTableQueryInvalid("Too many columns in insert object");
        if (updateDto.getColNames().length != updateDto.getColValues().length)
            throw new ServiceTableQueryInvalid("Column Names and Values must have the same length");
        for (var updateEntry : updateDto.getColNames()) {
            if (updateEntry.length() > userQuotas.getGeneral().getMaxNameSize())
                throw new ServiceTableQuotaExceeded("generalMaxNameSize");
            if (!updateEntry.matches("^[a-z0-9_]+$"))
                throw new ServiceTableQueryInvalid("Invalid Column Name: " + updateEntry);
        }
        for (var updateValEntry : updateDto.getColValues())
            if (updateValEntry != null && (updateValEntry instanceof Map || updateValEntry instanceof List || updateValEntry.getClass().isArray()))
                throw new ServiceTableQueryInvalid("Invalid Column Value Type: " + updateValEntry);

        try {
            // Update
            return userDbService.updateQueryTable(entry, tableUuid, updateDto, userQuotas);
        } catch (SQLException e) {
            throw new ServiceTableSqlError(tableUuid, e.getMessage());
        }
    }


    @PostMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}/query")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Selects data from a Table using a Select Query.", description = "If you set the `colNames` Array to be empty, it will select all Columns.")
    public ServiceTableQueryResultDto queryTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @Valid @RequestBody TableSelectDto selectDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound, ServiceTableQuotaExceeded, ServiceTableQueryInvalid, ServiceTableSqlError {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, true, false);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryReadPermissions(entry, tableEntry, auth);
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        // Check Query Result Cols
        if (selectDto.getColNames().length > userQuotas.getTable().getMaxCols())
            throw new ServiceTableQueryInvalid("Too many columns in insert object");
        for (var insertEntry : selectDto.getColNames()) {
            if (insertEntry.length() > userQuotas.getGeneral().getMaxNameSize())
                throw new ServiceTableQuotaExceeded("generalMaxNameSize");
            if (!insertEntry.matches("^[a-z0-9_]+$"))
                throw new ServiceTableQueryInvalid("Invalid Column Name: " + insertEntry);
        }

        try {
            return userDbService.queryTable(entry, tableUuid, selectDto, userQuotas);
        } catch (SQLException e) {
            throw new ServiceTableSqlError(tableUuid, e.getMessage());
        }
    }


    // --- Helper Methods ---

    private ServiceTableEntryDto fromServiceTableEntry(ServiceTableEntry entry, boolean includePerms) throws ServiceTableSqlError {
        // Columns
        TableColumnDto[] columns;
        try {
            columns = userDbService.getAllTableColumns(entry.getLinkedServiceEntry(), entry.getTableUuid()).toArray(new TableColumnDto[0]);
        } catch (SQLException e) {
            throw new ServiceTableSqlError(entry.getTableUuid(), e.getMessage());
        }

        // Perms
        String[] readPerms = includePerms ? entry.getExtraReadPerms().toArray(new String[0]) : new String[0];
        String[] writePerms = includePerms ? entry.getExtraWritePerms().toArray(new String[0]) : new String[0];

        return new ServiceTableEntryDto(
            entry.getTableUuid(),
            entry.getTableName(),
                entry.getSchemaVersion(),
                columns,
                entry.getCreatedAt(),
                readPerms,
                writePerms
        );
    }

    private ServiceEntry findServiceEntry(String idHandle, String serviceUuid) throws ServiceEntryNotFound {
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(serviceUuid, idHandle);
        if (entry == null)
            throw new ServiceEntryNotFound(serviceUuid);
        return entry;
    }
    private ServiceTableEntry findServiceTableEntry(String idHandle, String tableUuid) throws ServiceTableNotFound {
        ServiceTableEntry tableEntry = tableEntryRepository.findByTableUuid_AndLinkedIdentity_Handle(tableUuid, idHandle);
        if (tableEntry == null)
            throw new ServiceTableNotFound(tableUuid);
        return tableEntry;
    }

    private BaseQuotaProperties getServiceEntryQuotas(ServiceEntry entry) {
        return quotaService.getUserQuotas(entry.getCreatedBy().getHandle());
    }

    private void checkServiceEntryAccessPermissions(ServiceEntry entry, GoofyAuthUser auth) throws ServiceEntryNotFound {
        if (!entry.getLinkedIdentity().getHandle().equals(auth.getHandle()) && !auth.getAdmin())
            throw new ServiceEntryNotFound(entry.getUuid());
    }

    private void checkServiceTableEntryReadPermissions(ServiceEntry entry, ServiceTableEntry tableEntry, GoofyAuthUser auth) throws ServiceEntryNotFound {
        if (!entry.getLinkedIdentity().getHandle().equals(auth.getHandle()) && !auth.getAdmin())
            if (!tableEntry.getExtraReadPerms().contains("*") && !tableEntry.getExtraReadPerms().contains(auth.getHandle()))
                throw new ServiceEntryNotFound(entry.getUuid());
    }

    private void checkServiceTableEntryWritePermissions(ServiceEntry entry, ServiceTableEntry tableEntry, GoofyAuthUser auth) throws ServiceEntryNotFound {
        if (!entry.getLinkedIdentity().getHandle().equals(auth.getHandle()) && !auth.getAdmin())
            if (!tableEntry.getExtraWritePerms().contains(auth.getHandle()))
                throw new ServiceEntryNotFound(entry.getUuid());
    }

    public record SchemaComparison(boolean identical, List<TableColumnDto> removedCols, List<TableColumnDto> addedCols, Set<TableColumnDto> updatedCols, TableColumnDto[] currCols) {}

    private boolean compCols(TableColumnDto first, TableColumnDto second) {
        boolean typeSizeEq = first.normalizeTypeSize() == second.normalizeTypeSize();

        boolean constraintsEq = TableColumnDto.normalizeConstraints(first.getConstraints())
                .equals(TableColumnDto.normalizeConstraints(second.getConstraints()));

        return first.getColName().equals(second.getColName()) &&
                first.getType() == second.getType() &&
                typeSizeEq &&
                constraintsEq &&
                Objects.equals(first.getDefaultValue(), second.getDefaultValue());
    }

    private SchemaComparison compareSchemas(ServiceTableEntry entry, ServiceTableEntryDto newSchema) throws ServiceTableSqlError {
        // Columns
        TableColumnDto[] currCols;
        try {
            currCols = userDbService.getAllTableColumns(entry.getLinkedServiceEntry(), entry.getTableUuid()).toArray(new TableColumnDto[0]);
        } catch (SQLException e) {
            throw new ServiceTableSqlError(entry.getTableUuid(), e.getMessage());
        }

        List<TableColumnDto> removedCols = new ArrayList<>();
        List<TableColumnDto> addedCols = new ArrayList<>();
        Set<TableColumnDto> updatedCols = new HashSet<>();

        // Go through current cols
        for (var col: currCols) {
            TableColumnDto foundCol = Arrays.stream(newSchema.getColumns()).filter(c -> c.getColName().equals(col.getColName())).findFirst().orElse(null);
            if (foundCol == null)
                removedCols.add(col);
            else if (!compCols(col, foundCol))
                updatedCols.add(foundCol);
        }

        // Go through new cols
        for (var col: newSchema.getColumns()) {
            TableColumnDto foundCol = Arrays.stream(currCols).filter(c -> c.getColName().equals(col.getColName())).findFirst().orElse(null);
            if (foundCol == null)
                addedCols.add(col);
        }

        return new SchemaComparison(
            removedCols.isEmpty() && addedCols.isEmpty() && updatedCols.isEmpty(),
            removedCols,
            addedCols,
            updatedCols,
            currCols
        );
    }
}
