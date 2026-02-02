package com.consistencyapp.backend;

import com.consistencyapp.backend.dto.auth.LoginRequest;
import com.consistencyapp.backend.dto.auth.RegisterRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_login_currentUser_happyPath() throws Exception {
        var registerReq = new RegisterRequest(
                "Jack@Example.com",
                "password123",
                "Jack",
                "Jack"
        );

        var registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").isNumber())
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.email").value("jack@example.com")) // normalized
                .andExpect(jsonPath("$.user.displayName").value("Jack"))
                .andReturn();

        JsonNode registerJson = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String accessToken = registerJson.get("accessToken").asText();
        long userId = registerJson.get("user").get("id").asLong();

        mockMvc.perform(get("/api/users/current")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("jack@example.com"))
                .andExpect(jsonPath("$.displayName").value("Jack"));

        var loginReq = new LoginRequest("jack@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("jack@example.com"))
                .andExpect(jsonPath("$.user.displayName").value("Jack"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        var req = new RegisterRequest("dup@example.com", "password123", "Dup_User", "Dup User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        var registerReq = new RegisterRequest("wrongpass@example.com", "password123", "wp_user", "WP");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON
                        )
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        var loginReq = new LoginRequest("wrongpass@example.com", "password999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void currentUser_missingToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/current"))
                .andExpect(status().isUnauthorized());
    }
}
