package com.gng.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@SpringBootTest
@ActiveProfiles("test")
public class IPValidationServiceTest {

    private WireMockServer wireMockServer;

    @Autowired
    private IPValidationService ipValidationService;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.resetAll();
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testIpAccessAllowed() {
        stubFor(get(urlEqualTo("/json/123.123.123.123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody("{\"country\":\"Canada\",\"countryCode\":\"CA\",\"isp\":\"Telus\"}")));

        assertDoesNotThrow(() -> ipValidationService.validateIp("123.123.123.123"));
    }

    @Test
    void testIpAccessBlockedByCountry() {
        stubFor(get(urlEqualTo("/json/123.123.123.124"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"country\":\"China\",\"countryCode\":\"CN\",\"isp\":\"China Telecom\"}")));

        Exception exception = assertThrows(IllegalAccessException.class,
                () -> ipValidationService.validateIp("123.123.123.124"));
        assertTrue(exception.getMessage().contains("Access denied from country: China"));
    }

    @Test
    void testIpAccessBlockedByISP() {
        stubFor(get(urlEqualTo("/json/123.123.123.125"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"country\":\"Germany\",\"countryCode\":\"DE\",\"isp\":\"AWS\"}")));

        Exception exception = assertThrows(IllegalAccessException.class,
                () -> ipValidationService.validateIp("123.123.123.125"));
        assertEquals("Access denied from ISP: AWS", exception.getMessage());
    }
}
