package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

// TODO: Test
@Service
public class UserBucketService {
    private static final Logger log = LoggerFactory.getLogger(UserBucketService.class);

    private final FileStorageService fileStorageService;

    public UserBucketService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void createEntry(ServiceEntry entry) throws IOException {
        deleteEntry(entry); // Ensure no existing folder
        fileStorageService.createBucketFolder(entry.getUuid());
        log.info("Created bucket folder for entry: {}", entry.getUuid());
    }

    public void deleteEntry(ServiceEntry entry) throws IOException {
        fileStorageService.deleteBucketFolder(entry.getUuid());
    }

    public void deleteBucketEntry(ServiceEntry entry, String fileUuid) throws IOException {
        fileStorageService.deleteBucketFile(entry.getUuid(), fileUuid);
    }

    public void uploadBucketEntry(ServiceEntry entry, String fileUuid, byte[] body) throws IOException {
        fileStorageService.createBucketFile(entry.getUuid(), fileUuid, body);
    }

    public byte[] getBucketEntry(ServiceEntry entry, String fileUuid) throws IOException {
        return fileStorageService.getBucketFile(entry.getUuid(), fileUuid);
    }
}
