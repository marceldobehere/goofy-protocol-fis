package com.masl.goofy_protocol_fis_be.entity.listeners;

import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import com.masl.goofy_protocol_fis_be.service.UserBucketService;
import com.masl.goofy_protocol_fis_be.service.UserDbService;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PrePersist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;

@Component
public class ServiceEntryListener {
    private static final Logger log = LoggerFactory.getLogger(ServiceEntryListener.class);

    private final UserDbService userDbService;
    private final UserBucketService userBucketService;

    public ServiceEntryListener(UserDbService userDbService, UserBucketService userBucketService) {
        this.userDbService = userDbService;
        this.userBucketService = userBucketService;
        log.debug("ServiceEntryListener initialized");
    }

    @PrePersist
    public void initEntryHandler(ServiceEntry entry) throws IOException, SQLException {
        log.info("Creating ServiceEntry: {}", entry.getUuid());
        userDbService.createEntry(entry);
        userBucketService.createEntry(entry);
    }

    @PostRemove
    public void deleteEntryHandler(ServiceEntry entry) throws IOException {
        log.info("Deleting ServiceEntry: {}", entry.getUuid());
        userDbService.deleteEntry(entry);
        userBucketService.deleteEntry(entry);
    }
}
