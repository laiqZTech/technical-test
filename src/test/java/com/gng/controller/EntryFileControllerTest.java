package com.gng.controller;

import com.gng.entity.FileRequestLog;
import com.gng.model.Outcome;
import com.gng.repo.FileRequestLogRepository;
import com.gng.service.EntryFileService;
import com.gng.service.IPValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EntryFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EntryFileService entryFileService;

    @MockBean
    private IPValidationService ipValidationService;

    @Autowired
    private FileRequestLogRepository fileRequestLogRepository;

    @Autowired
    private EntryFileController entryFileController;

    @BeforeEach
    void setup() {
        fileRequestLogRepository.deleteAll();
        ReflectionTestUtils.setField(entryFileController, "validationEnabled", true);
    }

    @Test
    public void testProcessFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt",
                MediaType.TEXT_PLAIN_VALUE, "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1\n".getBytes());

        List<Outcome> outcomes = List.of(
                new Outcome("John Smith", "Rides A Bike", 12.1)
        );

        when(entryFileService.processFile(file, true)).thenReturn(outcomes);

        String expectedJson = "[{\"Name\":\"John Smith\",\"Transport\":\"Rides A Bike\",\"Top Speed\":12.1}]";

        mockMvc.perform(multipart("/entry/process").file(file))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void testProcessFile_InvalidFileStructure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt",
                MediaType.TEXT_PLAIN_VALUE, "invalid-structure\n".getBytes());

        when(entryFileService.processFile(file, true)).thenThrow(new RuntimeException("Invalid file structure: invalid-structure"));

        mockMvc.perform(multipart("/entry/process").file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("Invalid file structure: invalid-structure"));
    }

    @Test
    public void testProcessFile_ShouldSkipValidation_WhenFeatureFlagIsDisabled() throws Exception {
        // Override property for this specific test
        ReflectionTestUtils.setField(entryFileController, "validationEnabled", false);

        String content = "invalid-structure\n";

        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt", "text/plain", content.getBytes());

        List<Outcome> outcomes = List.of(new Outcome("invalid-structure", "unknown", 0.0));

        when(entryFileService.processFile(file, false)).thenReturn(outcomes);

        String expectedJson = "[{\"Name\":\"invalid-structure\",\"Transport\":\"unknown\",\"Top Speed\":0.0}]";

        mockMvc.perform(multipart("/entry/process")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=OutcomeFile.json"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }

    @Test
    @Transactional
    @DirtiesContext
    public void testFileUploadAndLogging() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt",
                MediaType.TEXT_PLAIN_VALUE, "Content\n".getBytes());

        List<Outcome> outcomes = List.of(new Outcome("Name", "Transport", 123.45));

        when(entryFileService.processFile(file, true)).thenReturn(outcomes);

        String expectedJson = "[{\"Name\":\"Name\",\"Transport\":\"Transport\",\"Top Speed\":123.45}]";

        mockMvc.perform(multipart("/entry/process").file(file))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        // Verify that a FileRequestLog entry was created
        assertEquals(1, fileRequestLogRepository.count());
        FileRequestLog logEntry = (FileRequestLog) fileRequestLogRepository.findAll().get(0);
        assertNotNull(logEntry.getTimestamp());
        assertEquals("/entry/process", logEntry.getUri());
    }
}

