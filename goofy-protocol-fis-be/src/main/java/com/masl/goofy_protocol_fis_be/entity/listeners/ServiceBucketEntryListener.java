package com.masl.goofy_protocol_fis_be.entity.listeners;

import com.masl.goofy_protocol_fis_be.entity.ServiceBucketEntry;
import com.masl.goofy_protocol_fis_be.service.UserBucketService;
import jakarta.persistence.PreRemove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ServiceBucketEntryListener {
    private static final Logger log = LoggerFactory.getLogger(ServiceBucketEntryListener.class);

    private final UserBucketService userBucketService;

    public ServiceBucketEntryListener(UserBucketService userBucketService) {
        this.userBucketService = userBucketService;
        log.debug("ServiceBucketEntryListener initialized");
    }

    @PreRemove
    public void deleteEntryHandler(ServiceBucketEntry entry) throws IOException {
        log.info("Deleting ServiceBucketEntry with UUID: {}", entry.getFileUuid());
        userBucketService.deleteBucketEntry(entry.getLinkedServiceEntry(), entry.getFileUuid());
    }
}
