package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.entity.ServiceConfigEntry;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

// TODO: Test
@Service
public class UserBucketService {
    private static final Logger log = LoggerFactory.getLogger(UserBucketService.class);

    @Getter
    private static UserBucketService singleton;

    private final FileStorageService fileStorageService;

    public UserBucketService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;

        singleton = this;
    }

    public void createEntry(ServiceConfigEntry entry) throws IOException {
        deleteEntry(entry); // Ensure no existing folder
        fileStorageService.createBucketFolder(entry.getUuid());
        log.info("Created bucket folder for entry: {}", entry.getUuid());
    }

    public void deleteEntry(ServiceConfigEntry entry) throws IOException {
        fileStorageService.deleteBucketFolder(entry.getUuid());
    }
}
