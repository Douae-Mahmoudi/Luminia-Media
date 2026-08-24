package com.mediatheque.auth_svc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediatheque.auth_svc.config.JwtAuthFilter;
import com.mediatheque.auth_svc.config.SecurityConfig;
import com.mediatheque.auth_svc.dto.ChangePasswordRequest;
import com.mediatheque.auth_svc.dto.UserProfileDto;
import com.mediatheque.auth_svc.exception.GlobalExceptionHandler;
import com.mediatheque.auth_svc.service.JwtService;
import com.mediatheque.auth_svc.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private AuthenticationProvider authenticationProvider;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer((InvocationOnMock invocation) -> {
            HttpServletRequest  req   = invocation.getArgument(0);
            HttpServletResponse res   = invocation.getArgument(1);
            FilterChain         chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private UserProfileDto buildProfile(String email, String role) {
        return UserProfileDto.builder()
                .id(1L)
                .email(email)
                .firstName("Alice")
                .lastName("Dupont")
                .role(role)
                .build();
    }

    private ChangePasswordRequest buildChangePasswordRequest(String current, String newPwd) {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword(current);
        req.setNewPassword(newPwd);
        return req;
    }


    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void getProfile_shouldReturn200_whenAuthenticated() throws Exception {
        UserProfileDto profile = buildProfile("alice@example.com", "USER");
        when(userService.getProfile("alice@example.com")).thenReturn(profile);

        mockMvc.perform(get("/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getProfile("alice@example.com");
    }

    @Test
    void getProfile_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getProfile(anyString());
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void getProfile_shouldReturn500_whenServiceThrows() throws Exception {
        when(userService.getProfile("alice@example.com"))
                .thenThrow(new RuntimeException("Utilisateur introuvable"));

        mockMvc.perform(get("/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService).getProfile("alice@example.com");
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getProfile_shouldReturn200_whenAdmin() throws Exception {
        UserProfileDto profile = buildProfile("admin@example.com", "ADMIN");
        when(userService.getProfile("admin@example.com")).thenReturn(profile);

        mockMvc.perform(get("/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).getProfile("admin@example.com");
    }



    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void updateProfile_shouldReturn200_whenAuthenticated() throws Exception {
        UserProfileDto request = buildProfile("alice@example.com", "USER");
        UserProfileDto updated = UserProfileDto.builder()
                .id(1L)
                .email("alice@example.com")
                .firstName("Alicia")
                .lastName("Martin")
                .role("USER")
                .build();

        when(userService.updateProfile(eq("alice@example.com"), any(UserProfileDto.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/auth/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alicia"))
                .andExpect(jsonPath("$.lastName").value("Martin"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        verify(userService).updateProfile(eq("alice@example.com"), any(UserProfileDto.class));
    }

    @Test
    void updateProfile_shouldReturn401_whenNotAuthenticated() throws Exception {
        UserProfileDto request = buildProfile("alice@example.com", "USER");

        mockMvc.perform(patch("/auth/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateProfile(anyString(), any());
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void updateProfile_shouldReturn400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(patch("/auth/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(anyString(), any());
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void updateProfile_shouldReturn415_whenContentTypeIsWrong() throws Exception {
        mockMvc.perform(patch("/auth/me")
                        .with(csrf())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not json"))
                .andExpect(status().isUnsupportedMediaType());

        verify(userService, never()).updateProfile(anyString(), any());
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void updateProfile_shouldReturn400_whenServiceThrowsIllegalArgument() throws Exception {
        UserProfileDto request = buildProfile("alice@example.com", "USER");

        when(userService.updateProfile(eq("alice@example.com"), any(UserProfileDto.class)))
                .thenThrow(new IllegalArgumentException("Données invalides"));

        mockMvc.perform(patch("/auth/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService).updateProfile(eq("alice@example.com"), any(UserProfileDto.class));
    }



    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void changePassword_shouldReturn200_whenRequestIsValid() throws Exception {
        ChangePasswordRequest request = buildChangePasswordRequest("OldPass123!", "NewPass456!");

        doNothing().when(userService).changePassword(eq("alice@example.com"), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/auth/me/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Mot de passe modifié avec succès"));

        verify(userService).changePassword(eq("alice@example.com"), any(ChangePasswordRequest.class));
    }

    @Test
    void changePassword_shouldReturn401_whenNotAuthenticated() throws Exception {
        ChangePasswordRequest request = buildChangePasswordRequest("OldPass123!", "NewPass456!");

        mockMvc.perform(post("/auth/me/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).changePassword(anyString(), any());
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void changePassword_shouldReturn400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/auth/me/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(anyString(), any());
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void changePassword_shouldReturn400_whenCurrentPasswordIsWrong() throws Exception {
        ChangePasswordRequest request = buildChangePasswordRequest("WrongPass!", "NewPass456!");

        doThrow(new IllegalArgumentException("Mot de passe actuel incorrect"))
                .when(userService).changePassword(eq("alice@example.com"), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/auth/me/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService).changePassword(eq("alice@example.com"), any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void changePassword_shouldReturn500_whenServiceThrowsRuntimeException() throws Exception {
        ChangePasswordRequest request = buildChangePasswordRequest("OldPass123!", "NewPass456!");

        doThrow(new RuntimeException("Erreur interne"))
                .when(userService).changePassword(eq("alice@example.com"), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/auth/me/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(userService).changePassword(eq("alice@example.com"), any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void changePassword_shouldReturn415_whenContentTypeIsWrong() throws Exception {
        mockMvc.perform(post("/auth/me/change-password")
                        .with(csrf())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not json"))
                .andExpect(status().isUnsupportedMediaType());

        verify(userService, never()).changePassword(anyString(), any());
    }
}
