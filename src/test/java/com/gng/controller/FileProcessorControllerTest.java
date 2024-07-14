package com.gng.controller;

import com.gng.model.Outcome;
import com.gng.service.FileProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileProcessorController.class)
public class FileProcessorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileProcessorService fileProcessorService;

    @Autowired
    private FileProcessorController fileProcessorController;

    @BeforeEach
    void setup() {
        // Default setup, override in individual tests if needed
        ReflectionTestUtils.setField(fileProcessorController, "validationEnabled", true);
    }

    @Test
    public void testProcessFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt",
                MediaType.TEXT_PLAIN_VALUE, "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1\n".getBytes());

        List<Outcome> outcomes = List.of(
                new Outcome("John Smith", "Rides A Bike", 12.1)
        );

        Mockito.when(fileProcessorService.processFile(file, true)).thenReturn(outcomes);

        String expectedJson = "[{\"Name\":\"John Smith\",\"Transport\":\"Rides A Bike\",\"Top Speed\":12.1}]";

        mockMvc.perform(multipart("/files/process").file(file))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void testProcessFile_InvalidFileStructure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt",
                MediaType.TEXT_PLAIN_VALUE, "invalid-structure\n".getBytes());

        Mockito.when(fileProcessorService.processFile(file, true)).thenThrow(new RuntimeException("Invalid file structure: invalid-structure"));

        mockMvc.perform(multipart("/files/process").file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("Invalid file structure: invalid-structure"));
    }

    @Test
    public void testProcessFile_ShouldSkipValidation_WhenFeatureFlagIsDisabled() throws Exception {
        // Override property for this specific test
        ReflectionTestUtils.setField(fileProcessorController, "validationEnabled", false);

        String content = "invalid-structure\n";

        MockMultipartFile file = new MockMultipartFile("file", "EntryFile.txt", "text/plain", content.getBytes());

        List<Outcome> outcomes = List.of(new Outcome("invalid-structure", "unknown", 0.0));

        Mockito.when(fileProcessorService.processFile(file, false)).thenReturn(outcomes);

        String expectedJson = "[{\"Name\":\"invalid-structure\",\"Transport\":\"unknown\",\"Top Speed\":0.0}]";

        mockMvc.perform(multipart("/files/process")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=OutcomeFile.json"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }
}
