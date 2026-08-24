package com.example.notification.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")

class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest  req   = invocation.getArgument(0);
            HttpServletResponse res   = invocation.getArgument(1);
            FilterChain         chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }



    @Test
    void actuator_shouldBePermitAll_notUnauthorized() throws Exception {
        int status = mockMvc.perform(get("/actuator/health"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }


    @Test
    void swaggerUi_shouldBePermitAll_notUnauthorized() throws Exception {
        int status = mockMvc.perform(get("/swagger-ui/index.html"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }


    @Test
    void swaggerUiHtml_shouldBePermitAll_notUnauthorized() throws Exception {
        int status = mockMvc.perform(get("/swagger-ui.html"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }


    @Test
    void apiDocs_shouldBePermitAll_notUnauthorized() throws Exception {
        int status = mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void adminEndpoint_withoutAuth_shouldBeDenied() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoint_withRoleUser_shouldBeAllowed() throws Exception {
        int status = mockMvc.perform(get("/api/admin/users"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoint_withRoleAdmin_shouldBeAllowed() throws Exception {
        int status = mockMvc.perform(get("/api/admin/settings"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }



    @Test
    void notificationsEndpoint_withoutAuth_shouldBeDenied() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void notificationsSubPath_withoutAuth_shouldBeDenied() throws Exception {
        mockMvc.perform(get("/api/notifications/123"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void notificationsEndpoint_withRoleUser_shouldBeAllowed() throws Exception {
        int status = mockMvc.perform(get("/api/notifications"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void notificationsSubPath_withRoleAdmin_shouldBeAllowed() throws Exception {
        int status = mockMvc.perform(get("/api/notifications/456"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }



    @Test
    void unknownEndpoint_withoutAuth_shouldBeDenied() throws Exception {
        mockMvc.perform(get("/some/other/route"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser
    void unknownEndpoint_withAuth_shouldNotBeDeniedByAuth() throws Exception {
        int status = mockMvc.perform(get("/some/other/route"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }



    @Test
    @WithMockUser
    void session_shouldBeStateless() throws Exception {
        var response = mockMvc.perform(get("/some/route"))
                .andReturn().getResponse();

        assertThat(response.getCookies())
                .noneMatch(c -> "JSESSIONID".equals(c.getName()));
    }



    @Test
    @WithMockUser(roles = "USER")
    void csrf_shouldBeDisabled_postNotRejectedWith403() throws Exception {
        int status = mockMvc.perform(
                post("/api/notifications")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andReturn().getResponse().getStatus();

        assertThat(status).isNotEqualTo(403);
    }
}
