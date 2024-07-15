package com.gng.service;

import com.gng.model.Outcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EntryFileServiceTest {

    private EntryFileService entryFileService;

    @BeforeEach
    void setUp() {
        entryFileService = new EntryFileService();
    }

    @Test
    void testProcessFile_ValidFile() throws IOException {
        String content = "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1\n" +
                "3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5\n" +
                "1afb6f5d-a7c2-4311-a92d-974f3180ff5e|3X3D35|Jenny Walters|Likes Avocados|Rides A Scooter|8.5|15.3\n";

        MultipartFile file = new MockMultipartFile("file", "EntryFile.txt", "text/plain", content.getBytes());

        List<Outcome> outcomes = entryFileService.processFile(file, true);

        assertEquals(3, outcomes.size());

        // Validate the first line
        Outcome firstOutcome = outcomes.get(0);
        assertEquals("John Smith", firstOutcome.getName());
        assertEquals("Rides A Bike", firstOutcome.getTransport());
        assertEquals(12.1, firstOutcome.getTopSpeed());

        // Validate the second line
        Outcome secondOutcome = outcomes.get(1);
        assertEquals("Mike Smith", secondOutcome.getName());
        assertEquals("Drives an SUV", secondOutcome.getTransport());
        assertEquals(95.5, secondOutcome.getTopSpeed());

        // Validate the third line
        Outcome thirdOutcome = outcomes.get(2);
        assertEquals("Jenny Walters", thirdOutcome.getName());
        assertEquals("Rides A Scooter", thirdOutcome.getTransport());
        assertEquals(15.3, thirdOutcome.getTopSpeed());
    }

    @Test
    void testProcessFile_InvalidFormat() {
        String content = "Invalid Line Format";
        MultipartFile file = new MockMultipartFile("file", "EntryFile.txt", "text/plain", content.getBytes());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            entryFileService.processFile(file, true);
        }, "Should throw exception on invalid format");


        String expectedMessage = "Invalid format - Not 7 fields required : Invalid Line Format";
        String actualMessage = thrown.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testProcessFile_ValidationDisabled() throws IOException {

        String content = "Invalid Line Format";
        MultipartFile file = new MockMultipartFile("file", "EntryFile.txt", "text/plain", content.getBytes());

        List<Outcome> outcomes = entryFileService.processFile(file, false);

        // Ensure that an invalid structure is still processed when validation is disabled
        assertNotNull(outcomes);
        assertEquals(1, outcomes.size(), "Should return one outcome despite invalid format");
        assertEquals("unknown", outcomes.get(0).getTransport());
    }

    /*
    @Test
    void processFile_ShouldSkipValidation_WhenFeatureFlagIsDisabled() throws IOException {
        String content = "invalid-structure\n";

        MultipartFile file = new MockMultipartFile("file", "EntryFile.txt", "text/plain", content.getBytes());

        List<Outcome> outcomes = fileProcessorService.processFile(file, false);

        // Ensure that an invalid structure is still processed when validation is disabled
        assertNotNull(outcomes);
        assertEquals(1, outcomes.size());
        assertEquals("invalid-structure", outcomes.get(0).getName());
    }*/
}
