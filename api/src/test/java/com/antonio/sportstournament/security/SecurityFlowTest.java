package com.antonio.sportstournament.security;

import com.antonio.sportstournament.auth.TwoFactorChallengeRepository;
import com.antonio.sportstournament.email.EmailService;
import com.antonio.sportstournament.player.PlayerRepository;
import com.antonio.sportstournament.registration.RegistrationRepository;
import com.antonio.sportstournament.team.TeamRepository;
import com.antonio.sportstournament.tournament.Tournament;
import com.antonio.sportstournament.tournament.TournamentRepository;
import com.antonio.sportstournament.tournament.TournamentStatus;
import com.antonio.sportstournament.user.Role;
import com.antonio.sportstournament.user.UserRepository;
import com.antonio.sportstournament.user.UserService;
import com.antonio.sportstournament.user.dto.UserCreateRequest;
import com.antonio.sportstournament.user.dto.UserResponse;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end tests of the JWT filter chain, the role (URL) rules and the ownership rules. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityFlowTest {
    private static final String PASSWORD = "password";

    @Autowired MockMvc mockMvc;
    @Autowired UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired PlayerRepository playerRepository;
    @Autowired TournamentRepository tournamentRepository;
    @Autowired RegistrationRepository registrationRepository;
    @Autowired TwoFactorChallengeRepository challengeRepository;
    @Autowired JwtService jwtService;

    @MockitoBean EmailService emailService;

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        // children first, so the foreign keys never block the deletes
        registrationRepository.deleteAll();
        playerRepository.deleteAll();
        tournamentRepository.deleteAll();
        teamRepository.deleteAll();
        challengeRepository.deleteAll();
        userRepository.deleteAll();
        reset(emailService);
    }

    @Test
    void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value(containsString("Please log in again")));
    }

    @Test
    void tamperedOrGarbageTokenIsUnauthorized() throws Exception {
        createUser("Manager", "manager@example.com", Role.TEAM_MANAGER);
        String valid = jwtService.generateToken("manager@example.com");
        String tampered = valid.substring(0, valid.length() - 2) + (valid.endsWith("AA") ? "BB" : "AA");

        mockMvc.perform(get("/api/teams").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").isNotEmpty());
        mockMvc.perform(get("/api/teams").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenOfADeletedUserIsRejected() throws Exception {
        UserResponse manager = createUser("Manager", "gone@example.com", Role.TEAM_MANAGER);
        String token = jwtService.generateToken("gone@example.com");
        userRepository.deleteById(manager.id());

        mockMvc.perform(get("/api/teams").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void teamManagerLogsInAndCanReadTeams() throws Exception {
        createUser("Manager", "manager@example.com", Role.TEAM_MANAGER);
        String token = loginWithoutTwoFactor("manager@example.com");

        mockMvc.perform(get("/api/teams").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("manager@example.com"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void teamManagerCannotCreateTournament() throws Exception {
        createUser("Manager", "manager@example.com", Role.TEAM_MANAGER);
        String token = loginWithoutTwoFactor("manager@example.com");

        mockMvc.perform(post("/api/tournaments").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(tournamentJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("You do not have permission to do this."));
    }

    @Test
    void organizerWithEmailCodeCreatesTournamentAsOrganizer() throws Exception {
        UserResponse organizer = createUser("Organizer", "organizer@example.com", Role.ORGANIZER);

        String challenge = JsonPath.read(mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("organizer@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.twoFactorRequired").value(true))
                .andExpect(jsonPath("$.user").doesNotExist())
                .andReturn().getResponse().getContentAsString(), "$.challengeToken");
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendLoginCode(eq("organizer@example.com"), eq("Organizer"), code.capture(), anyInt());

        String token = JsonPath.read(mockMvc.perform(post("/api/auth/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"challengeToken\":\"" + challenge + "\",\"code\":\"" + code.getValue() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString(), "$.token");

        mockMvc.perform(post("/api/tournaments").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(tournamentJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizerId").value(organizer.id()));
    }

    @Test
    void teamManagerCannotEditAnotherManagersTeam() throws Exception {
        createUser("Manager A", "a@example.com", Role.TEAM_MANAGER);
        createUser("Manager B", "b@example.com", Role.TEAM_MANAGER);
        String tokenA = loginWithoutTwoFactor("a@example.com");
        String tokenB = loginWithoutTwoFactor("b@example.com");
        int teamOfB = createTeam(tokenB, "Team B");

        mockMvc.perform(put("/api/teams/" + teamOfB).header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"teamName\":\"Stolen\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Only the team's manager can change this team."));
        mockMvc.perform(delete("/api/teams/" + teamOfB).header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/teams/" + teamOfB).header("Authorization", bearer(tokenB))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"teamName\":\"Renamed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Renamed"));
    }

    @Test
    void teamManagerCannotRegisterAnotherManagersTeam() throws Exception {
        UserResponse organizer = createUser("Organizer", "organizer@example.com", Role.ORGANIZER);
        createUser("Manager A", "a@example.com", Role.TEAM_MANAGER);
        createUser("Manager B", "b@example.com", Role.TEAM_MANAGER);
        String tokenA = loginWithoutTwoFactor("a@example.com");
        String tokenB = loginWithoutTwoFactor("b@example.com");
        int teamOfB = createTeam(tokenB, "Team B");

        Tournament tournament = new Tournament();
        tournament.setName("Open Cup");
        tournament.setStartDate(LocalDate.now().plusDays(10));
        tournament.setEndDate(LocalDate.now().plusDays(12));
        tournament.setStatus(TournamentStatus.OPEN);
        tournament.setOrganizer(userRepository.findById(organizer.id()).orElseThrow());
        Long tournamentId = tournamentRepository.save(tournament).getId();

        String body = "{\"teamId\":" + teamOfB + "}";
        mockMvc.perform(post("/api/tournaments/" + tournamentId + "/registrations")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You can only register your own teams."));
        mockMvc.perform(post("/api/tournaments/" + tournamentId + "/registrations")
                        .header("Authorization", bearer(tokenB))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void onlyAdminCanListUsers() throws Exception {
        createUser("Manager", "manager@example.com", Role.TEAM_MANAGER);
        createUser("Admin", "admin@example.com", Role.ADMIN);
        String managerToken = loginWithoutTwoFactor("manager@example.com");
        // admins log in with an email code; the token is issued directly here
        String adminToken = jwtService.generateToken("admin@example.com");

        mockMvc.perform(get("/api/users").header("Authorization", bearer(managerToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/users").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void adminCannotDeleteOwnAccount() throws Exception {
        UserResponse admin = createUser("Admin", "admin@example.com", Role.ADMIN);
        String adminToken = jwtService.generateToken("admin@example.com");

        mockMvc.perform(delete("/api/users/" + admin.id()).header("Authorization", bearer(adminToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("You cannot delete your own account."));
    }

    @Test
    void loginAndRegisterWorkWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Manager\",\"email\":\"new@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("TEAM_MANAGER"));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("new@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.token").isNotEmpty());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void userCannotChangeAnotherUsersTwoFactorSetting() throws Exception {
        createUser("Manager", "manager@example.com", Role.TEAM_MANAGER);
        UserResponse other = createUser("Other", "other@example.com", Role.TEAM_MANAGER);
        String token = loginWithoutTwoFactor("manager@example.com");

        mockMvc.perform(put("/api/users/" + other.id() + "/two-factor").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You can only change your own security settings."));
    }

    private UserResponse createUser(String name, String email, Role role) {
        return userService.create(new UserCreateRequest(name, email, PASSWORD, role));
    }

    private String loginWithoutTwoFactor(String email) throws Exception {
        return JsonPath.read(mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.twoFactorRequired").value(false))
                .andReturn().getResponse().getContentAsString(), "$.user.token");
    }

    private int createTeam(String token, String name) throws Exception {
        return JsonPath.read(mockMvc.perform(post("/api/teams").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"teamName\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String credentials(String email) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}";
    }

    private static String tournamentJson() {
        return "{\"name\":\"Spring Cup\",\"startDate\":\"" + LocalDate.now().plusDays(5)
                + "\",\"endDate\":\"" + LocalDate.now().plusDays(7) + "\"}";
    }
}
