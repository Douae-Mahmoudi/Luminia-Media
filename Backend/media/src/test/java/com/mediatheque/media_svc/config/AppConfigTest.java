package com.mediatheque.media_svc.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    private final AppConfig appConfig = new AppConfig();

    @Test
    void restTemplate_shouldNotBeNull() {
        RestTemplate restTemplate = appConfig.restTemplate();
        assertNotNull(restTemplate);
    }

    @Test
    void restTemplate_shouldReturnNewInstance() {
        RestTemplate first = appConfig.restTemplate();
        RestTemplate second = appConfig.restTemplate();
        assertNotNull(first);
        assertNotNull(second);
    }
}
