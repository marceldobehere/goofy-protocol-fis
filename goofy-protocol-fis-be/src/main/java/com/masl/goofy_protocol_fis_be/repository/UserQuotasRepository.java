package com.masl.goofy_protocol_fis_be.repository;

import com.masl.goofy_protocol_fis_be.entity.UserQuotas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQuotasRepository extends JpaRepository<UserQuotas, String> {
    UserQuotas findByUserHandle(String userHandle);
}
