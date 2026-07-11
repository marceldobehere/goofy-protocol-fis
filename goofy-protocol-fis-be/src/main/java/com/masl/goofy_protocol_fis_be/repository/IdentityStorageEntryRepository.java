package com.masl.goofy_protocol_fis_be.repository;

import com.masl.goofy_protocol_fis_be.entity.IdentityStorageEntry;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Transactional
@Repository
public interface IdentityStorageEntryRepository extends JpaRepository<IdentityStorageEntry, String> {
    long countAllByCreatedByHandle(String createdByHandle);
    List<IdentityStorageEntry> findAllByCreatedByHandle(String createdByHandle);

    IdentityStorageEntry findByHandle(String handle);
    IdentityStorageEntry findByCreatedByHandle_AndHandle(String createdByHandle, String handle);
    void deleteByCreatedByHandle_AndHandle(String createdByHandle, String handle);
}
