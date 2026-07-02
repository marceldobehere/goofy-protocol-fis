package com.masl.goofy_protocol_fis_be.repository;

import com.masl.goofy_protocol_fis_be.entity.RegistrationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationCodeRepository extends JpaRepository<RegistrationCode, String> {
    RegistrationCode findByCodeAndUsedAtIsNull(String code);
    List<RegistrationCode> findAllByUsedAtIsNotNull();
    List<RegistrationCode> findAllByUsedAtIsNull();
}
