package com.gng.service;

import com.gng.model.IPValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Service to validate incoming request IP.
 *
 */
@Service
public class IPValidationService {

    private final Logger logger = LoggerFactory.getLogger(IPValidationService.class);

    public static final String[] BLOCKED_COUNTRIES = {"CN", "ES", "US"};
    public static final String[] BLOCKED_ISP = {"AWS", "GCP", "Azure"};

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public IPValidationService(RestTemplate restTemplate,
                               @Value("${ip.api.base.url:http://ip-api.com}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void validateIp(String ip) throws IllegalAccessException {
        logger.info("Validating IP: {} ", ip);
        String url = baseUrl + "/json/" + ip;
        ResponseEntity<IPValidationResponse> response = restTemplate.getForEntity(url, IPValidationResponse.class);
        IPValidationResponse ipInfo = response.getBody();

        if (ipInfo == null) {
            throw new IllegalAccessException("IP information could not be retrieved");
        }

        if (Arrays.asList(BLOCKED_COUNTRIES).contains(ipInfo.getCountryCode())) {
            throw new IllegalAccessException("Access denied from country: " + ipInfo.getCountry());
        }

        if (Arrays.asList(BLOCKED_ISP).contains(ipInfo.getIsp())) {
            throw new IllegalAccessException("Access denied from ISP: " + ipInfo.getIsp());
        }
    }
}
