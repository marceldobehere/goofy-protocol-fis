package com.masl.goofy_protocol_fis_be.repository;

import com.masl.goofy_protocol_fis_be.entity.ServiceBucketEntry;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Transactional
@Repository
public interface ServiceBucketEntryRepository extends JpaRepository<ServiceBucketEntry, String> {
    long countAllByLinkedIdentity_Handle(String linkedIdentityHandle);
    List<ServiceBucketEntry> findAllByLinkedIdentity_Handle(String linkedIdentityHandle);

    ServiceBucketEntry findByFileUuid_AndLinkedIdentity_Handle(String fileUuid, String linkedIdentityHandle);
    void deleteByFileUuid_AndLinkedIdentity_Handle(String fileUuid, String linkedIdentityHandle);
}
