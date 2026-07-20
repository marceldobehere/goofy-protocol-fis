package com.masl.goofy_protocol_fis_be.repository;

import com.masl.goofy_protocol_fis_be.entity.ServiceTableEntry;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Transactional
@Repository
public interface ServiceTableEntryRepository extends JpaRepository<ServiceTableEntry, String> {
    long countAllByLinkedIdentity_Handle_AndLinkedServiceEntry_Uuid(String linkedIdentityHandle, String linkedServiceUuid);
    List<ServiceTableEntry> findAllByLinkedIdentity_Handle_AndLinkedServiceEntry_Uuid(String linkedIdentityHandle, String linkedServiceUuid);

    ServiceTableEntry findByTableUuid_AndLinkedIdentity_Handle(String tableUuid, String linkedIdentityHandle);
    void deleteByTableUuid_AndLinkedIdentity_Handle(String tableUuid, String linkedIdentityHandle);
}
