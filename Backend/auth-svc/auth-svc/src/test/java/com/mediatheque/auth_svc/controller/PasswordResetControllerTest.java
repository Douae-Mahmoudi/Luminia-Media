package com.mediatheque.auth_svc.controller;

import com.mediatheque.auth_svc.config.JwtAuthFilter;
import com.mediatheque.auth_svc.dto.ForgotPasswordRequest;
import com.mediatheque.auth_svc.dto.ResetPasswordRequest;
import com.mediatheque.auth_svc.dto.VerifyCodeRequest;
import com.mediatheque.auth_svc.service.JwtService;
import com.mediatheque.auth_svc.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PasswordResetController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;



    @Test
    @WithMockUser
    void forgotPassword_success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@example.com");

        doNothing().when(passwordResetService).sendResetCode("user@example.com");

        mockMvc.perform(post("/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Code de vérification envoyé par email"));

        verify(passwordResetService).sendResetCode("user@example.com");
    }

    @Test
    @WithMockUser
    void forgotPassword_userNotFound_returns500() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("inconnu@example.com");

        doThrow(new RuntimeException("Aucun compte trouvé avec cet email"))
                .when(passwordResetService).sendResetCode("inconnu@example.com");

        mockMvc.perform(post("/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }



    @Test
    @WithMockUser
    void verifyCode_success() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.setEmail("user@example.com");
        request.setCode("394601");

        doNothing().when(passwordResetService).verifyCode("user@example.com", "394601");

        mockMvc.perform(post("/auth/verify-code")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Code valide"));

        verify(passwordResetService).verifyCode("user@example.com", "394601");
    }

    @Test
    @WithMockUser
    void verifyCode_wrongCode_returns500() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.setEmail("user@example.com");
        request.setCode("000000");

        doThrow(new RuntimeException("Code incorrect"))
                .when(passwordResetService).verifyCode("user@example.com", "000000");

        mockMvc.perform(post("/auth/verify-code")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void verifyCode_expiredCode_returns500() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.setEmail("user@example.com");
        request.setCode("123456");

        doThrow(new RuntimeException("Code expiré"))
                .when(passwordResetService).verifyCode("user@example.com", "123456");

        mockMvc.perform(post("/auth/verify-code")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }



    @Test
    @WithMockUser
    void resetPassword_success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setCode("394601");
        request.setNewPassword("NouveauMdp123!");

        doNothing().when(passwordResetService)
                .resetPassword("user@example.com", "394601", "NouveauMdp123!");

        mockMvc.perform(post("/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Mot de passe réinitialisé avec succès"));

        verify(passwordResetService).resetPassword("user@example.com", "394601", "NouveauMdp123!");
    }

    @Test
    @WithMockUser
    void resetPassword_wrongCode_returns500() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setCode("000000");
        request.setNewPassword("NouveauMdp123!");

        doThrow(new RuntimeException("Code incorrect"))
                .when(passwordResetService).resetPassword("user@example.com", "000000", "NouveauMdp123!");

        mockMvc.perform(post("/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void resetPassword_expiredCode_returns500() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setCode("123456");
        request.setNewPassword("NouveauMdp123!");

        doThrow(new RuntimeException("Code expiré"))
                .when(passwordResetService).resetPassword("user@example.com", "123456", "NouveauMdp123!");

        mockMvc.perform(post("/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}