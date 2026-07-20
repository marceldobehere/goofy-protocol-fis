package com.masl.goofy_protocol_fis_be.rest.service;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.dto.both.ServiceTableEntryDto;
import com.masl.goofy_protocol_fis_be.dto.both.TableColumnDto;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableBasicQuery;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableInsert;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableSelect;
import com.masl.goofy_protocol_fis_be.dto.request.query.TableUpdate;
import com.masl.goofy_protocol_fis_be.dto.response.ServiceDbQuotasDto;
import com.masl.goofy_protocol_fis_be.dto.response.ServiceTableQueryResultDto;
import com.masl.goofy_protocol_fis_be.dto.response.ServiceTableQuotasDto;
import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import com.masl.goofy_protocol_fis_be.entity.ServiceTableEntry;
import com.masl.goofy_protocol_fis_be.entity.UserQuotas;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.*;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.ServiceEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.ServiceTableEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.UserQuotasRepository;
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
    private final UserQuotasRepository userQuotasRepository;
    private final BaseQuotaProperties baseQuotaProperties;
    private final UserDbService userDbService;
    private final TableLockService tableLockService;

    public ServiceTableEndpoint(ServiceTableEntryRepository tableEntryRepository, ServiceEntryRepository serviceEntryRepository, UserQuotasRepository userQuotasRepository, BaseQuotaProperties baseQuotaProperties, UserDbService userDbService, TableLockService tableLockService) {
        this.tableEntryRepository = tableEntryRepository;
        this.serviceEntryRepository = serviceEntryRepository;
        this.userQuotasRepository = userQuotasRepository;
        this.baseQuotaProperties = baseQuotaProperties;
        this.userDbService = userDbService;
        this.tableLockService = tableLockService;
    }

    // TODO: Add access log table with the last x access entries, for example 10000, just like handle and serviec-uuid/table-uuid


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

    // TODO: Reset DB?


    // --- OUTSIDE ENTITIES ---


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
    @FisEndpoint(summary = "Sets a Table Entry", description = "Set the Table Entry for a specific Table UUID. <br>Note: The Columns can only be changed if the schema version provided is larger and provides default values for new non-null columns.")
    public void setTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @Valid @RequestBody ServiceTableEntryDto entryDto, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableNotFound, ServiceTableLockInvalid, ServiceTableInvalidMigration, ServiceTableQuotaExceeded {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, false, true);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);
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

            if (tableEntry.getSchemaVersion().equals(entryDto.getSchemaVersion())) {
                // TODO: Check if the schema is identical
                boolean identical = false;
                if (!identical)
                    throw new ServiceTableInvalidMigration(tableUuid, "Same Schema Version but different Columns, please increase the Schema Version to update the Columns");
            } else {
                // TODO: Implement Schema Update
                // Make sure that all new columns with a constraint or something have a default value
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

        // Unlock Table Entry
        tableLockService.unlockServiceTableEntry(serviceUuid, tableUuid, lockToken, readLock, writeLock);
    }

    // Create Table Entry (Default, no UUID) (will be private by default)
    @PutMapping("/{idHandle}/{serviceUuid}/entry")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Create a Table Entry", description = "Creates a Table Entry (Default, no UUID) (will be private by default). ")
    public ServiceTableEntryDto createTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @Valid @RequestBody ServiceTableEntryDto entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableQuotaExceeded, ServiceTableEntryInvalid, ServiceTableSqlError {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        checkServiceEntryAccessPermissions(entry, auth);
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
            if (colDto.getConstraints().contains(TableColumnDto.Constraint.NOT_NULL) && colDto.getDefaultValue() == null)
                throw new ServiceTableEntryInvalid("New NOT NULL Column must have a Default Value: " + colDto.getColName());
            if (colDto.getColName().length() > userQuotas.getGeneral().getMaxNameSize())
                throw new ServiceTableQuotaExceeded("generalMaxNameSize");
            if (TableColumnDto.getTypeSize(colDto.getType(), colDto.getTypeSize()) > userQuotas.getTable().getMaxFieldSize())
                throw new ServiceTableQuotaExceeded("tableMaxFieldSize");
        }

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

        // Create Table in DB
        try {
            userDbService.createTableEntry(entry, entryDto);
        } catch (IOException | SQLException e) {
            throw new ServiceTableSqlError(tableEntry.getTableUuid(), e.getMessage());
        }

        tableEntryRepository.save(tableEntry);
        return fromServiceTableEntry(tableEntry, true);
    }

    @PostMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}/rows")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Inserts a Row into a Table Entry")
    public void insertQueryTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @Valid @RequestBody TableInsert entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, false, true);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);

        // TODO: Implement
    }

    @DeleteMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}/rows")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Deletes Rows from a Table Entry based on a Query")
    public Integer deleteQueryTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @Valid @RequestBody TableBasicQuery entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, true, true);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);

        // TODO: Implement
        return 0;
    }

    @PutMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}/rows")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Updates Rows from a Table Entry based on a Query")
    public Integer updateQueryTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @Valid @RequestBody TableUpdate entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, true, true);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);

        // TODO: Implement
        return 0;
    }


    @PostMapping("/{idHandle}/{serviceUuid}/entry/{tableUuid}/query")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Selects data from a Table using a Select Query")
    public ServiceTableQueryResultDto updateQueryTableEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String tableUuid, @RequestHeader(name = "X-Lock-Token", required = false) String lockToken, @Valid @RequestBody TableSelect entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableNotFound {
        tableLockService.checkLockServiceTableEntry(serviceUuid, tableUuid, lockToken, true, false);
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceTableEntry tableEntry = findServiceTableEntry(idHandle, tableUuid);
        checkServiceTableEntryWritePermissions(entry, tableEntry, auth);

        // TODO: Implement
        return null;
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
        UserQuotas quotas = userQuotasRepository.findByUserHandle(entry.getCreatedBy().getHandle());
        return UserQuotas.getUserQuotas(quotas, baseQuotaProperties);
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
}
