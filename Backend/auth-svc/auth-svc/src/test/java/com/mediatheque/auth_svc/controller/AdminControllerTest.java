package com.mediatheque.auth_svc.controller;

import com.mediatheque.auth_svc.config.JwtAuthFilter;
import com.mediatheque.auth_svc.config.SecurityConfig;
import com.mediatheque.auth_svc.dto.UserProfileDto;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

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


    private UserProfileDto buildDto(Long id, String email, String role) {
        return UserProfileDto.builder()
                .id(id)
                .email(email)
                .firstName("Alice")
                .lastName("Dupont")
                .role(role)
                .build();
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturn200_whenAdmin() throws Exception {
        List<UserProfileDto> users = List.of(
                buildDto(1L, "alice@example.com", "USER"),
                buildDto(2L, "bob@example.com", "ADMIN")
        );
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[1].email").value("bob@example.com"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers();
    }

    @Test
    void getAllUsers_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnEmptyList_whenNoUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[]"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_shouldReturn200_whenAdmin() throws Exception {
        UserProfileDto updated = buildDto(1L, "alice@example.com", "ADMIN");
        when(userService.updateUserRole(1L, "ADMIN")).thenReturn(updated);

        mockMvc.perform(patch("/admin/users/1/role")
                        .param("role", "ADMIN")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        verify(userService).updateUserRole(1L, "ADMIN");
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUserRole_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(patch("/admin/users/1/role")
                        .param("role", "ADMIN")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUserRole(any(), any());
    }

    @Test
    void updateUserRole_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(patch("/admin/users/1/role")
                        .param("role", "ADMIN")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUserRole(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_shouldReturn200_withRoleUser() throws Exception {
        UserProfileDto updated = buildDto(1L, "alice@example.com", "USER");
        when(userService.updateUserRole(1L, "USER")).thenReturn(updated);

        mockMvc.perform(patch("/admin/users/1/role")
                        .param("role", "USER")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).updateUserRole(1L, "USER");
    }
}