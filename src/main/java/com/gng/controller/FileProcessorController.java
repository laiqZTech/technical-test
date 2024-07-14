package com.gng.controller;

import com.gng.model.Outcome;
import com.gng.service.FileProcessorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileProcessorController {

    private final FileProcessorService fileProcessorService;

    @Value("${feature.flag.validation:true}")
    private boolean validationEnabled;

    public FileProcessorController(FileProcessorService fileProcessorService) {
        this.fileProcessorService = fileProcessorService;
    }

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processFile(@RequestParam("file") MultipartFile file) {
        try {
            List<Outcome> outcomes = fileProcessorService.processFile(file, validationEnabled);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=OutcomeFile.json");
            return ResponseEntity.ok().headers(headers).body(outcomes);
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
