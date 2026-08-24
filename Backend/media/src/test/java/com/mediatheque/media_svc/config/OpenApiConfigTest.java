package com.mediatheque.media_svc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    void openAPI_shouldNotBeNull() {
        OpenAPI openAPI = openApiConfig.openAPI();
        assertNotNull(openAPI);
    }

    @Test
    void openAPI_shouldHaveCorrectTitle() {
        OpenAPI openAPI = openApiConfig.openAPI();
        assertEquals("Media Service API", openAPI.getInfo().getTitle());
    }

    @Test
    void openAPI_shouldHaveCorrectVersion() {
        OpenAPI openAPI = openApiConfig.openAPI();
        assertEquals("1.0", openAPI.getInfo().getVersion());
    }

    @Test
    void openAPI_shouldHaveCorrectDescription() {
        OpenAPI openAPI = openApiConfig.openAPI();
        assertEquals("Gestion des médias — BOOK, FILM, GAME, PODCAST",
                openAPI.getInfo().getDescription());
    }

    @Test
    void openAPI_shouldHaveBearerSecurityScheme() {
        OpenAPI openAPI = openApiConfig.openAPI();
        SecurityScheme scheme = openAPI.getComponents()
                .getSecuritySchemes()
                .get("Bearer");

        assertNotNull(scheme);
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
    }

    @Test
    void openAPI_shouldHaveSecurityRequirement() {
        OpenAPI openAPI = openApiConfig.openAPI();
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
        assertTrue(openAPI.getSecurity().get(0).containsKey("Bearer"));
    }
}
