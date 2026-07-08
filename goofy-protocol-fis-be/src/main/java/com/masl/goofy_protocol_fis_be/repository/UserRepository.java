package com.masl.goofy_protocol_fis_be.repository;

import com.masl.goofy_protocol_fis_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByHandle(String handle);
}
