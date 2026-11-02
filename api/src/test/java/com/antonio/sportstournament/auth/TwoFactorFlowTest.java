package com.antonio.sportstournament.auth;

import com.antonio.sportstournament.email.EmailService;
import com.antonio.sportstournament.security.JwtService;
import com.antonio.sportstournament.user.Role;
import com.antonio.sportstournament.user.UserRepository;
import com.antonio.sportstournament.user.UserService;
import com.antonio.sportstournament.user.dto.UserCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Covers the email 2FA login flow and role-based 2FA enforcement. */
@SpringBootTest
@AutoConfigureMockMvc
class TwoFactorFlowTest {
    @Autowired MockMvc mockMvc;
    @Autowired UserService userService;
    @Autowired JwtService jwtService;
    @Autowired UserRepository userRepository;
    @Autowired TwoFactorChallengeRepository challengeRepository;

    @MockitoBean EmailService emailService;

    @BeforeEach
    void setUp() {
        challengeRepository.deleteAll();
        userRepository.deleteAll();
        reset(emailService);
    }

    @Test
    void managerWithoutTwoFactorLogsInDirectly() throws Exception {
        userService.create(new UserCreateRequest("Manager", "manager@example.com", "password", Role.TEAM_MANAGER));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"manager@example.com","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.twoFactorRequired").value(false))
                .andExpect(jsonPath("$.user.email").value("manager@example.com"))
                .andExpect(jsonPath("$.user.token").isNotEmpty());
        verifyNoInteractions(emailService);
    }

    @Test
    void organizerMustVerifyEmailCodeAndChallengeIsSingleUse() throws Exception {
        userService.create(new UserCreateRequest("Organizer", "organizer@example.com", "password", Role.ORGANIZER));

        String token = loginAndGetToken("organizer@example.com");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendLoginCode(eq("organizer@example.com"), eq("Organizer"), codeCaptor.capture(), anyInt());
        String code = codeCaptor.getValue();

        mockMvc.perform(post("/api/auth/verify-2fa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"challengeToken\":\"" + token + "\",\"code\":\"000000\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid code. 4 attempts left."));

        mockMvc.perform(post("/api/auth/verify-2fa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"challengeToken\":\"" + token + "\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("organizer@example.com"))
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/api/auth/verify-2fa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"challengeToken\":\"" + token + "\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fiveWrongCodesLockChallenge() throws Exception {
        userService.create(new UserCreateRequest("Organizer", "lock@example.com", "password", Role.ORGANIZER));
        String token = loginAndGetToken("lock@example.com");
        for (int attempt = 1; attempt <= 4; attempt++) {
            mockMvc.perform(post("/api/auth/verify-2fa").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"challengeToken\":\"" + token + "\",\"code\":\"000000\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Invalid code. " + (5 - attempt) + " attempts left."));
        }
        mockMvc.perform(post("/api/auth/verify-2fa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"challengeToken\":\"" + token + "\",\"code\":\"000000\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Too many wrong codes. Please log in again."));
    }

    @Test
    void organizerCannotDisableTwoFactor() throws Exception {
        var organizer = userService.create(
                new UserCreateRequest("Organizer", "protected@example.com", "password", Role.ORGANIZER));

        mockMvc.perform(put("/api/users/" + organizer.id() + "/two-factor")
                        .header("Authorization", "Bearer " + jwtService.generateToken("protected@example.com"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("2FA is required for Administrators and Organizers"));
    }

    private String loginAndGetToken(String email) throws Exception {
        return com.jayway.jsonpath.JsonPath.read(
                mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"" + email + "\",\"password\":\"password\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.twoFactorRequired").value(true))
                        .andExpect(jsonPath("$.user").doesNotExist())
                        .andExpect(jsonPath("$.maskedEmail").value(containsString("@")))
                        .andReturn().getResponse().getContentAsString(), "$.challengeToken");
    }
}
