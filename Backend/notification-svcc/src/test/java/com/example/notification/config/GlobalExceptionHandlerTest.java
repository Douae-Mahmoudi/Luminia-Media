package com.example.notification.config;

import com.example.notification.config.GlobalExceptionHandler;
import com.example.notification.dto.request.BroadcastRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();


    @Nested
    @DisplayName("handleValidation() — unitaire")
    class HandleValidationUnitTests {

        @Test
        @DisplayName("retourne le statut 400 Bad Request")
        void handleValidation_returns400() {
            MethodArgumentNotValidException ex = buildException("message", "Le message est obligatoire");

            ResponseEntity<Map<String, List<String>>> response = handler.handleValidation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("le body contient la clé 'errors'")
        void handleValidation_bodyContainsErrorsKey() {
            MethodArgumentNotValidException ex = buildException("message", "Le message est obligatoire");

            ResponseEntity<Map<String, List<String>>> response = handler.handleValidation(ex);

            assertThat(response.getBody()).isNotNull().containsKey("errors");
        }

        @Test
        @DisplayName("la liste contient le message d'erreur du champ invalide")
        void handleValidation_containsFieldErrorMessage() {
            MethodArgumentNotValidException ex = buildException("message", "Le message est obligatoire");

            List<String> errors = handler.handleValidation(ex).getBody().get("errors");

            assertThat(errors).containsExactly("Le message est obligatoire");
        }

        @Test
        @DisplayName("agrège les messages de plusieurs champs invalides")
        void handleValidation_aggregatesMultipleFieldErrors() {
            BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new BroadcastRequest(), "broadcastRequest");
            bindingResult.addError(new FieldError("broadcastRequest", "message", "Le message est obligatoire"));
            bindingResult.addError(new FieldError("broadcastRequest", "title",   "Le titre est obligatoire"));

            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            List<String> errors = handler.handleValidation(ex).getBody().get("errors");

            assertThat(errors)
                    .hasSize(2)
                    .containsExactlyInAnyOrder("Le message est obligatoire", "Le titre est obligatoire");
        }

        @Test
        @DisplayName("retourne une liste vide si aucune FieldError")
        void handleValidation_returnsEmptyListWhenNoFieldErrors() {
            BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new BroadcastRequest(), "broadcastRequest");

            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            List<String> errors = handler.handleValidation(ex).getBody().get("errors");

            assertThat(errors).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleValidation() — MockMvc")
    class HandleValidationMvcTests {

        @RestController
        static class FakeController {
            @PostMapping("/test/broadcast")
            public ResponseEntity<Void> broadcast(@Valid @RequestBody BroadcastRequest request) {
                return ResponseEntity.ok().build();
            }
        }

        private MockMvc mockMvc;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders
                    .standaloneSetup(new FakeController())
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();
        }

        @Test
        @DisplayName("retourne 400 quand le body est vide")
        void mvc_returns400WhenBodyEmpty() throws Exception {
            mockMvc.perform(post("/test/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("le body de la réponse contient la clé 'errors'")
        void mvc_responseBodyContainsErrorsKey() throws Exception {
            mockMvc.perform(post("/test/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("les messages de validation des champs manquants sont présents")
        void mvc_containsValidationMessages() throws Exception {
            mockMvc.perform(post("/test/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors.length()").value(2));
        }

        @Test
        @DisplayName("retourne 200 quand le body est valide")
        void mvc_returns200WhenBodyValid() throws Exception {
            BroadcastRequest validRequest = BroadcastRequest.builder()
                    .message("Contenu valide")
                    .title("Titre valide")
                    .build();

            mockMvc.perform(post("/test/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("retourne 400 avec message si seulement le titre est manquant")
        void mvc_returns400WhenTitleMissing() throws Exception {
            String body = """
                    {"message": "Contenu présent"}
                    """;

            mockMvc.perform(post("/test/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors.length()").value(1));
        }

        @Test
        @DisplayName("retourne 400 avec message si seulement le message est manquant")
        void mvc_returns400WhenMessageMissing() throws Exception {
            String body = """
                    {"title": "Titre présent"}
                    """;

            mockMvc.perform(post("/test/broadcast")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors.length()").value(1));
        }
    }

    private MethodArgumentNotValidException buildException(String field, String message) {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new BroadcastRequest(), "broadcastRequest");
        bindingResult.addError(new FieldError("broadcastRequest", field, message));
        return new MethodArgumentNotValidException(null, bindingResult);
    }
}
