package com.gng.service;

import com.gng.model.Entry;
import com.gng.model.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileProcessorService {

    private final Logger logger = LoggerFactory.getLogger(FileProcessorService.class);

    public List<Outcome> processFile(MultipartFile file, boolean validationEnabled) throws IOException {
        List<Outcome> outcomes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    if (validationEnabled) {
                        validateLine(line);
                    }
                    outcomes.add(parseLineToOutcome(line));
                } catch (Exception e) {
                    logger.error("Validation or parsing error on line: {}", line, e);
                    if (!validationEnabled) {
                        outcomes.add(new Outcome("Error processing line", "unknown", 0.0));
                    }else {
                        throw e;
                    }
                }
            }
        }
        return outcomes;
    }

    private Outcome parseLineToOutcome(String line) {
        String[] parts = line.split("\\|");
        Entry entry = new Entry(parts[0], parts[1], parts[2], parts[3], parts[4], Double.parseDouble(parts[5]), Double.parseDouble(parts[6]));
        return new Outcome(entry.getName(), entry.getTransport(), entry.getTopSpeed());
    }

    public void validateLine(String line) {
        String[] parts = line.split("\\|");

        // Validate the number of fields
        if (parts.length != 7) {
            throw new IllegalArgumentException(String.format("Invalid format - Not 7 fields required : %s", line));
        }

        // Validate UUID
        validateUUID(parts[0]);

        // Validate numeric fields for Average Speed and Top Speed
        validateDouble(parts[5], "Average Speed");
        validateDouble(parts[6], "Top Speed");
    }

    private void validateUUID(String uuidStr) {
        try {
            UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Invalid UUID format in field 1: %s", uuidStr));
        }
    }

    private void validateDouble(String value, String fieldName) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("%s is not a valid number: %s", fieldName, value));
        }
    }

}
