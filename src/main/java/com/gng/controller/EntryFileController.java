package com.gng.controller;

import com.gng.entity.FileRequestLog;
import com.gng.model.Outcome;
import com.gng.repo.FileRequestLogRepository;
import com.gng.service.EntryFileService;
import com.gng.service.IPValidationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controller class to upload and process Entry.txt
 */
@RestController
@RequestMapping("/entry")
public class EntryFileController {

    private final Logger logger = LoggerFactory.getLogger(EntryFileController.class);

    private final EntryFileService entryFileService;

    private final IPValidationService ipValidationService;

    private final FileRequestLogRepository fileRequestLogRepository;

    @Value("${entry.file.flag.validation:true}")
    private boolean validationEnabled;

    public EntryFileController(EntryFileService entryFileService, IPValidationService ipValidationService,
                               FileRequestLogRepository fileRequestLogRepository) {
        this.entryFileService = entryFileService;
        this.ipValidationService = ipValidationService;
        this.fileRequestLogRepository = fileRequestLogRepository;
    }


    /**
     * Endpoint to upload and process Entry file and log the request.
     *
     * @param entryFile file to process
     * @param request   HttpServletRequest to capture request attributes
     * @return ResponseEntity<?>
     */
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processFile(@RequestParam("file") MultipartFile entryFile, HttpServletRequest request) {
        logger.info("Processing file {} where validation flag is {}", entryFile.getOriginalFilename(), validationEnabled);
        LocalDateTime startTime = LocalDateTime.now();
        FileRequestLog fileRequestLog = createRequestLog(request, startTime);
        try {
            ipValidationService.validateIp(fileRequestLog.getIpAddress());
            List<Outcome> outcomes = entryFileService.processFile(entryFile, validationEnabled);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=OutcomeFile.json");
            return ResponseEntity.ok().headers(headers).body(outcomes);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } finally {
            fileRequestLog.setTimeLapsed(Duration.between(startTime, LocalDateTime.now()).toMillis());
            logger.debug("Saving request file  {}", fileRequestLog);
            fileRequestLogRepository.save(fileRequestLog);
        }
    }

    private FileRequestLog createRequestLog(HttpServletRequest request, LocalDateTime startTime) {
        FileRequestLog fileRequestLog = new FileRequestLog();
        fileRequestLog.setId(UUID.randomUUID().toString());
        fileRequestLog.setUri(request.getRequestURI());
        fileRequestLog.setTimestamp(startTime);
        fileRequestLog.setIpAddress(request.getRemoteAddr());
        return fileRequestLog;
    }

}
