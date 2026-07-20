package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import com.masl.goofy_protocol_fis_be.entity.UserQuotas;
import com.masl.goofy_protocol_fis_be.exception.client.ServiceEntryNotFound;
import com.masl.goofy_protocol_fis_be.exception.client.ServiceTableLockInvalid;
import com.masl.goofy_protocol_fis_be.exception.client.ServiceTableLockRequestInvalid;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.ServiceEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.UserQuotasRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Test
@Service
public class TableLockService {
    private static final Logger log = LoggerFactory.getLogger(TableLockService.class);

    private final ServiceEntryRepository serviceEntryRepository;
    private final UserQuotasRepository userQuotasRepository;
    private final BaseQuotaProperties baseQuotaProperties;

    private final ConcurrentHashMap<String, TableLockState> locks = new ConcurrentHashMap<>();

    public TableLockService(ServiceEntryRepository serviceEntryRepository, UserQuotasRepository userQuotasRepository, BaseQuotaProperties baseQuotaProperties) {
        this.serviceEntryRepository = serviceEntryRepository;
        this.userQuotasRepository = userQuotasRepository;
        this.baseQuotaProperties = baseQuotaProperties;
    }

    public String lockServiceTableEntry(String serviceUuid, String tableUuid, boolean readLock, boolean writeLock) throws ServiceEntryNotFound, ServiceTableLockRequestInvalid {
        ServiceEntry entry = findServiceEntry(serviceUuid);
        BaseQuotaProperties quotas = getServiceEntryQuotas(entry);
        log.debug("Locking table entry: serviceUuid={}, tableUuid={}, readLock={}, writeLock={}", serviceUuid, tableUuid, readLock, writeLock);

        TableLockState state = getTableLock(serviceUuid, tableUuid);
        if (readLock && writeLock) {
            while (true) {
                synchronized (state.readMon) {
                    if (state.isFree(state.readLock)) {
                        synchronized (state.writeMon) {
                            if (state.isFree(state.writeLock)) {
                                state.readLock = new LockState(quotas.getTable().getMaxLockDurationSeconds());
                                state.writeLock = new LockState(state.readLock.token, quotas.getTable().getMaxLockDurationSeconds());
                                log.debug("Acquired read/write lock for table entry: serviceUuid={}, tableUuid={}, token={}", serviceUuid, tableUuid, state.readLock.token);
                                return state.readLock.token;
                            }
                        }
                    }
                }
                shortSleep();
            }
        } else if (readLock) {
            while (true) {
                synchronized (state.readMon) {
                    if (state.isFree(state.readLock)) {
                        state.readLock = new LockState(quotas.getTable().getMaxLockDurationSeconds());
                        log.debug("Acquired read lock for table entry: serviceUuid={}, tableUuid={}, token={}", serviceUuid, tableUuid, state.readLock.token);
                        return state.readLock.token;
                    }
                }
                shortSleep();
            }
        } else if (writeLock) {
            while (true) {
                synchronized (state.writeMon) {
                    if (state.isFree(state.writeLock)) {
                        state.writeLock = new LockState(quotas.getTable().getMaxLockDurationSeconds());
                        log.debug("Acquired write lock for table entry: serviceUuid={}, tableUuid={}, token={}", serviceUuid, tableUuid, state.writeLock.token);
                        return state.writeLock.token;
                    }
                }
                shortSleep();
            }
        } else {
            throw new ServiceTableLockRequestInvalid(tableUuid);
        }
    }

    public void unlockServiceTableEntry(String serviceUuid, String tableUuid, String lockToken, boolean readLock, boolean writeLock) throws ServiceEntryNotFound, ServiceTableLockInvalid {
        findServiceEntry(serviceUuid);
        TableLockState state = getTableLock(serviceUuid, tableUuid);

        if (readLock)
            synchronized (state.readMon) {
                if (state.readLock == null || !state.readLock.token.equals(lockToken))
                    throw new ServiceTableLockInvalid(tableUuid, lockToken);
                log.debug("Releasing read lock for table entry: serviceUuid={}, tableUuid={}, token={}", serviceUuid, tableUuid, lockToken);
                state.readLock = null;
            }

        if (writeLock)
            synchronized (state.writeMon) {
                if (state.writeLock == null || !state.writeLock.token.equals(lockToken))
                    throw new ServiceTableLockInvalid(tableUuid, lockToken);
                log.debug("Releasing write lock for table entry: serviceUuid={}, tableUuid={}, token={}", serviceUuid, tableUuid, lockToken);
                state.writeLock = null;
            }
    }

    public void checkLockServiceTableEntry(String serviceUuid, String tableUuid, String optLockToken, boolean readPerm, boolean writePerm) throws ServiceEntryNotFound, ServiceTableLockInvalid {
        findServiceEntry(serviceUuid);
        TableLockState state = getTableLock(serviceUuid, tableUuid);
        if (readPerm && writePerm) {
            while (true) {
                synchronized (state.readMon) {
                    if (state.isAccessible(state.readLock, optLockToken)) {
                        synchronized (state.writeMon) {
                            if (state.isAccessible(state.writeLock, optLockToken))
                                return;
                        }
                    }
                }
                shortSleep();
            }
        } else if (readPerm) {
            while (true) {
                synchronized (state.readMon) {
                    if (state.isAccessible(state.readLock, optLockToken))
                        return;
                }
                shortSleep();
            }
        } else if (writePerm) {
            while (true) {
                synchronized (state.writeMon) {
                    if (state.isAccessible(state.writeLock, optLockToken))
                        return;
                }
                shortSleep();
            }
        }
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

    private void shortSleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.info("Sleep interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
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
        UserQuotas quotas = userQuotasRepository.findByUserHandle(entry.getCreatedBy().getHandle());
        return UserQuotas.getUserQuotas(quotas, baseQuotaProperties);
    }
}
