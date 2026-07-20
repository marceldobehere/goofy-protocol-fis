package com.masl.goofy_protocol_fis_be.entity.listeners;

import com.masl.goofy_protocol_fis_be.entity.ServiceTableEntry;
import com.masl.goofy_protocol_fis_be.service.UserDbService;
import jakarta.persistence.PreRemove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceTableEntryListener {
    private static final Logger log = LoggerFactory.getLogger(ServiceTableEntryListener.class);

    private final UserDbService userDbService;

    public ServiceTableEntryListener(UserDbService userDbService) {
        this.userDbService = userDbService;
        log.debug("ServiceTableEntryListener initialized");
    }

    @PreRemove
    public void deleteEntryHandler(ServiceTableEntry entry) {
        log.info("Deleting ServiceTableEntry with UUID: {} and Name: {}", entry.getTableUuid(), entry.getTableName());
        userDbService.deleteTableEntry(entry.getLinkedServiceEntry(), entry.getTableUuid());
    }
}
