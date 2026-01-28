package com.consistencyapp.backend;

import com.consistencyapp.backend.dto.auth.RegisterRequest;
import com.consistencyapp.backend.repository.user.AppUserRepository;
import com.consistencyapp.backend.security.UserRole;
import com.consistencyapp.backend.security.jwt.JwtService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AdminAccessIntegrationTest extends AbstractIntegrationTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void adminPing_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminPing_userToken_returns403_adminToken_returns200() throws Exception {
        var registerReq = new RegisterRequest("roleuser@example.com", "password123", "RoleUser");

        var registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andReturn();

        String body = registerResult.getResponse().getContentAsString();
        String userToken = objectMapper.readTree(body).get("accessToken").asText();

        mockMvc.perform(get("/api/admin/ping")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        var user = appUserRepository.findByEmailIgnoreCase("roleuser@example.com").orElseThrow();
        String adminToken = jwtService.createAccessToken(user, List.of(UserRole.USER, UserRole.ADMIN));

        mockMvc.perform(get("/api/admin/ping")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }
}
