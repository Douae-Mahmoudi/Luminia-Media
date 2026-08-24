package com.mediatheque.auth_svc.config;

import com.mediatheque.auth_svc.service.AuthService;
import com.mediatheque.auth_svc.service.JwtService;
import com.mediatheque.auth_svc.service.PasswordResetService;
import com.mediatheque.auth_svc.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private JwtAuthFilter jwtAuthFilter;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private AuthenticationProvider authenticationProvider;
    @MockitoBean private UserService userService;
    @MockitoBean private AuthService authService;
    @MockitoBean private PasswordResetService passwordResetService;

    @Test
    void authRegister_shouldBePublic() throws Exception {
        mockMvc.perform(post("/auth/register"))
                .andExpect(status().isOk());
    }

    @Test
    void authLogin_shouldBePublic() throws Exception {
        mockMvc.perform(post("/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerUi_shouldBePublic() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void apiDocs_shouldBePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void adminRoute_shouldBeAccessible_withMockedFilter() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    void anyProtectedRoute_shouldBeAccessible_withMockedFilter() throws Exception {
        mockMvc.perform(get("/some/protected/route"))
                .andExpect(status().isOk());
    }
}