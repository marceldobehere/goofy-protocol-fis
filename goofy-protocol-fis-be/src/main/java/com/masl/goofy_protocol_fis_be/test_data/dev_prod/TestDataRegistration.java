package com.masl.goofy_protocol_fis_be.test_data.dev_prod;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
@Profile({"dev", "prod"})
public class TestDataRegistration implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(TestDataRegistration.class);

    // TODO: Maybe switch to @PostConstruct too
    @Transactional
    @Override
    public void run(String... args) {
        // TODO: Implement future logic to create one admin registration code & log it if none exist yet (fresh setup)
    }
}
