package com.masl.goofy_protocol_fis_be.repository;

import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Transactional
@Repository
public interface ServiceEntryRepository extends JpaRepository<ServiceEntry, String> {
    long countAllByLinkedIdentity_Handle(String linkedIdentityHandle);
    List<ServiceEntry> findAllByLinkedIdentity_Handle(String linkedIdentityHandle);

    ServiceEntry findByUuid(String uuid);
    ServiceEntry findByUuid_AndLinkedIdentity_Handle(String uuid, String linkedIdentityHandle);
    void deleteByUuid_AndLinkedIdentity_Handle(String uuid, String linkedIdentityHandle);
}
