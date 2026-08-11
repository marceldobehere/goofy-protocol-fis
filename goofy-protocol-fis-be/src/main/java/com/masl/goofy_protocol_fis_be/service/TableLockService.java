package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import com.masl.goofy_protocol_fis_be.exception.client.ServiceEntryNotFound;
import com.masl.goofy_protocol_fis_be.exception.client.ServiceTableLockInvalid;
import com.masl.goofy_protocol_fis_be.exception.client.ServiceTableLockRequestInvalid;
import com.masl.goofy_protocol_fis_be.exception.server.ServiceTableLocked;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.ServiceEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TableLockService {
    private static final Logger log = LoggerFactory.getLogger(TableLockService.class);

    private final ServiceEntryRepository serviceEntryRepository;
    private final QuotaService quotaService;

    private final ConcurrentHashMap<String, TableLockState> locks = new ConcurrentHashMap<>();

    public TableLockService(ServiceEntryRepository serviceEntryRepository, QuotaService quotaService) {
        this.serviceEntryRepository = serviceEntryRepository;
        this.quotaService = quotaService;
    }

    public String lockServiceTableEntry(String serviceUuid, String tableUuid, boolean readLock, boolean writeLock) throws ServiceEntryNotFound, ServiceTableLockRequestInvalid, ServiceTableLocked {
        ServiceEntry entry = findServiceEntry(serviceUuid);
        BaseQuotaProperties quotas = getServiceEntryQuotas(entry);
        log.debug(" 2> Locking table entry: serviceUuid={}, tableUuid={}, readLock={}, writeLock={}", serviceUuid, tableUuid, readLock, writeLock);

        TableLockState state = getTableLock(serviceUuid, tableUuid);
        if (readLock && writeLock) {
            synchronized (state.readMon) {
                if (state.isFree(state.readLock)) {
                    synchronized (state.writeMon) {
                        if (state.isFree(state.writeLock)) {
                            state.readLock = new LockState(quotas.getTable().getMaxLockDurationSeconds());
                            state.writeLock = new LockState(state.readLock.token, quotas.getTable().getMaxLockDurationSeconds());
                            log.debug("  3> Acquired read/write lock for table entry: serviceUuid={}, tableUuid={}, token={}", serviceUuid, tableUuid, state.readLock.token);
                            return state.readLock.token;
                        }
                    }
                }
            }
        } else if (readLock) {
            synchronized (state.readMon) {
                if (state.isFree(state.readLock)) {
                    state.readLock = new LockState(quotas.getTable().getMaxLockDurationSeconds());
                    log.debug("  3> Acquired read lock for table entry: serviceUuid={}, tableUuid={}, token={}", serviceUuid, tableUuid, state.readLock.token);
                    return state.readLock.token;
                }
            }
        } else if (writeLock) {
            synchronized (state.writeMon) {
                if (state.isFree(state.writeLock)) {
                    state.writeLock = new LockState(quotas.getTable().getMaxLockDurationSeconds());
                    log.debug("  3> Acquired write lock for table entry: serviceUuid={}, tableUuid={}, token={}", serviceUuid, tableUuid, state.writeLock.token);
                    return state.writeLock.token;
                }
            }
        } else {
            throw new ServiceTableLockRequestInvalid(tableUuid);
        }

        throw new ServiceTableLocked(tableUuid);
    }

    public void unlockServiceTableEntry(String serviceUuid, String tableUuid, String lockToken, boolean readLock, boolean writeLock) throws ServiceEntryNotFound, ServiceTableLockInvalid {
        findServiceEntry(serviceUuid);
        TableLockState state = getTableLock(serviceUuid, tableUuid);

        if (readLock)
            synchronized (state.readMon) {
                if (state.readLock == null || !state.readLock.token.equals(lockToken))
                    throw new ServiceTableLockInvalid(tableUuid, lockToken);
                log.debug("   4> Releasing read lock for table entry: serviceUuid={}, tableUuid={}, token={}", serviceUuid, tableUuid, lockToken);
                state.readLock = null;
            }

        if (writeLock)
            synchronized (state.writeMon) {
                if (state.writeLock == null || !state.writeLock.token.equals(lockToken))
                    throw new ServiceTableLockInvalid(tableUuid, lockToken);
                log.debug("   4> Releasing write lock for table entry: serviceUuid={}, tableUuid={}, token={}", serviceUuid, tableUuid, lockToken);
                state.writeLock = null;
            }
    }

    public void checkLockServiceTableEntry(String serviceUuid, String tableUuid, String optLockToken, boolean readPerm, boolean writePerm) throws ServiceEntryNotFound, ServiceTableLockInvalid, ServiceTableLocked {
        findServiceEntry(serviceUuid);
        TableLockState state = getTableLock(serviceUuid, tableUuid);
        if (readPerm && writePerm) {
            synchronized (state.readMon) {
                if (state.isAccessible(state.readLock, optLockToken)) {
                    synchronized (state.writeMon) {
                        if (state.isAccessible(state.writeLock, optLockToken))
                            return;
                    }
                }
            }
        } else if (readPerm) {
            synchronized (state.readMon) {
                if (state.isAccessible(state.readLock, optLockToken))
                    return;
            }
        } else if (writePerm) {
            synchronized (state.writeMon) {
                if (state.isAccessible(state.writeLock, optLockToken))
                    return;
            }
        }

        throw new ServiceTableLocked(tableUuid);
    }

    private static final class TableLockState {
        final String tableUuid;

        public TableLockState(String tableUuid) {
            this.tableUuid = tableUuid;
        }

        final Object readMon = new Object();
        LockState readLock = null;

        final Object writeMon = new Object();
        LockState writeLock = null;

        boolean isFree(LockState state) {
            return (state == null || state.expiresAt.isBefore(Instant.now()));
        }
        boolean isAccessible(LockState state, String token) throws ServiceTableLockInvalid {
            if (isFree(state))
                return true;
            if (token == null)
                return false;
            if (state.token.equals(token))
                return true;
            throw new ServiceTableLockInvalid(tableUuid, token);
        }
    }

    private static final class LockState {
        final String token;
        final Instant expiresAt;
        LockState(int lockDurationSeconds) {
            this.token = UUID.randomUUID().toString();
            this.expiresAt = Instant.now().plusSeconds(lockDurationSeconds);
        }
        LockState(String token, int lockDurationSeconds) {
            this.token = token;
            this.expiresAt = Instant.now().plusSeconds(lockDurationSeconds);
        }
    }

    private static String getLockKey(String serviceUuid, String tableUuid) {
        return serviceUuid + ":" + tableUuid;
    }

    private TableLockState getTableLock(String serviceUuid, String tableUuid) {
        return locks.computeIfAbsent(getLockKey(serviceUuid, tableUuid), _ -> new TableLockState(tableUuid));
    }

    private ServiceEntry findServiceEntry(String serviceUuid) throws ServiceEntryNotFound {
        ServiceEntry entry = serviceEntryRepository.findByUuid(serviceUuid);
        if (entry == null)
            throw new ServiceEntryNotFound(serviceUuid);
        return entry;
    }

    private BaseQuotaProperties getServiceEntryQuotas(ServiceEntry entry) {
        return quotaService.getUserQuotas(entry.getCreatedBy().getHandle());
    }
}
