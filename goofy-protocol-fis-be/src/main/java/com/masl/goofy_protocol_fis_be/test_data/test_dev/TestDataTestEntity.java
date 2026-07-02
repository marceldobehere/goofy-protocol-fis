package com.masl.goofy_protocol_fis_be.test_data.test_dev;

import com.masl.goofy_protocol_fis_be.entity.TestEntity;
import com.masl.goofy_protocol_fis_be.repository.TestRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@Profile({"dev","test"})
public class TestDataTestEntity {
    private static final Logger log = LoggerFactory.getLogger(TestDataTestEntity.class);

    private final TestRepository testRepository;

    public TestDataTestEntity(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @PostConstruct
    public void init() {
        if (testRepository.count() > 0) {
            log.info("> Test data for TestEntity already exists, skipping seeding.");
            return;
        }

        log.info("> Seeding test data for TestEntity...");
        for (int i = 0; i < 10; i++) {
            TestEntity entity = new TestEntity();
            entity.setData("Test: " + i);
            testRepository.save(entity);
        }

        log.info("> Test data for TestEntity seeding completed.");
    }
}