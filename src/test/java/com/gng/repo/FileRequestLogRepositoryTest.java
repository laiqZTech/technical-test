package com.gng.repo;

import com.gng.entity.FileRequestLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class FileRequestLogRepositoryTest {

    @Autowired
    private FileRequestLogRepository fileRequestLogRepository;


    @BeforeEach
    void setup() {
        fileRequestLogRepository.deleteAll();
    }

    @Test
    @Transactional
    @DirtiesContext
    public void testSaveAndCount() {
        FileRequestLog log = new FileRequestLog();
        log.setId(UUID.randomUUID().toString()); // ensure ID is set if not generated
        log.setUri("/test-uri");
        log.setTimestamp(LocalDateTime.now());
        log.setHttpResponseCode(200);
        fileRequestLogRepository.save(log);

        assertEquals(1, fileRequestLogRepository.count(), "Should have one entry in the database.");
    }
}
