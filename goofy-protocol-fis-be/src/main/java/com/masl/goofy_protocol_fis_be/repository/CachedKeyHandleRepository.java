package com.masl.goofy_protocol_fis_be.repository;

import com.masl.goofy_protocol_fis_be.entity.CachedKeyHandleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CachedKeyHandleRepository extends JpaRepository<CachedKeyHandleEntry, String> {
}
