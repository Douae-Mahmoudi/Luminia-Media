package com.example.notification.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppConfig")
class AppConfigTest {

    private final AppConfig appConfig = new AppConfig();

    @Test
    @DisplayName("restTemplate() retourne une instance non nulle")
    void restTemplate_returnsNonNull() {
        assertThat(appConfig.restTemplate()).isNotNull();
    }

    @Test
    @DisplayName("restTemplate() retourne bien un RestTemplate")
    void restTemplate_returnsCorrectType() {
        assertThat(appConfig.restTemplate()).isInstanceOf(RestTemplate.class);
    }

    @Test
    @DisplayName("restTemplate() retourne une nouvelle instance à chaque appel (sans proxy Spring)")
    void restTemplate_returnsNewInstanceEachCall() {
        RestTemplate first  = appConfig.restTemplate();
        RestTemplate second = appConfig.restTemplate();
        assertThat(first).isNotSameAs(second);
    }
}
