package com.masl.goofy_protocol_fis_be.repository;

import com.masl.goofy_protocol_fis_be.entity.LoginStorageEntry;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public interface LoginStorageEntryRepository extends JpaRepository<LoginStorageEntry, String> {
    void deleteAllByCreatedByHandle(String createdByHandle);
    LoginStorageEntry findByUsernameHash(String usernameHash);
}
