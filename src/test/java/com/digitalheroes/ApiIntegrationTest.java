package com.digitalheroes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.digitalheroes.entity.Draw;
import com.digitalheroes.entity.DrawParticipant;
import com.digitalheroes.entity.ScoreMode;
import com.digitalheroes.entity.SubscriptionPlan;
import com.digitalheroes.entity.User;
import com.digitalheroes.repository.DrawParticipantRepository;
import com.digitalheroes.repository.DrawRepository;
import com.digitalheroes.repository.SubscriptionPlanRepository;
import com.digitalheroes.repository.UserRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionPlanRepository planRepository;

    @Autowired
    private DrawRepository drawRepository;

    @Autowired
    private DrawParticipantRepository drawParticipantRepository;

    @Test
    void completeDigitalHeroesFlowShouldWork() throws Exception {

        // =========================================================
        // 1. HEALTH CHECK
        // =========================================================

        mockMvc.perform(
                get("/api/health")
        )
        .andExpect(status().isOk());


        // =========================================================
        // 2. CREATE UNIQUE TEST USER
        // =========================================================

        String uniqueId = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        String email = "integration_" + uniqueId + "@example.com";
        String password = "Test@12345";
        String fullName = "Integration Test User " + uniqueId;

        String registerJson = """
                {
                    "email": "%s",
                    "password": "%s",
                    "fullName": "%s"
                }
                """.formatted(
                        email,
                        password,
                        fullName
                );

        MvcResult registerResult = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson)
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode registerResponse =
                objectMapper.readTree(
                        registerResult.getResponse().getContentAsString()
                );

        String userToken =
                registerResponse.get("accessToken").asText();

        Long userId =
                registerResponse.get("user").get("id").asLong();


        // =========================================================
        // 3. LOGIN USER
        // =========================================================

        String loginJson = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                        email,
                        password
                );

        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson)
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode loginResponse =
                objectMapper.readTree(
                        loginResult.getResponse().getContentAsString()
                );

        userToken =
                loginResponse.get("accessToken").asText();


        // =========================================================
        // 4. GET CURRENT USER
        // =========================================================

        mockMvc.perform(
                get("/api/auth/me")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(status().isOk());


        // =========================================================
        // 5. GET ACTIVE PLAN
        // =========================================================

        MvcResult plansResult = mockMvc.perform(
                get("/api/plans")
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode plans =
                objectMapper.readTree(
                        plansResult.getResponse().getContentAsString()
                );

        if (!plans.isArray() || plans.isEmpty()) {
            throw new AssertionError(
                    "No active subscription plans found"
            );
        }

        Long planId = plans.get(0)
                .get("id")
                .asLong();


        // =========================================================
        // 6. ACTIVATE SUBSCRIPTION
        // =========================================================

        mockMvc.perform(
                post("/api/subscriptions/activate/" + planId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(status().isOk());


        // =========================================================
        // 7. VERIFY ACTIVE SUBSCRIPTION
        // =========================================================

        MvcResult subscriptionResult = mockMvc.perform(
                get("/api/subscriptions/me")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode subscriptions =
                objectMapper.readTree(
                        subscriptionResult
                                .getResponse()
                                .getContentAsString()
                );

        if (!subscriptions.isArray()
                || subscriptions.isEmpty()) {

            throw new AssertionError(
                    "Subscription was not created"
            );
        }


        // =========================================================
        // 8. CREATE EXACTLY 5 SCORES
        // =========================================================

        LocalDate today = LocalDate.now();

        int[] scores = {
                10,
                15,
                20,
                25,
                30
        };

        for (int i = 0; i < scores.length; i++) {

            String scoreJson = """
                    {
                        "scoreValue": %d,
                        "scoreDate": "%s"
                    }
                    """.formatted(
                            scores[i],
                            today.minusDays(i)
                    );

            mockMvc.perform(
                    post("/api/scores")
                            .header(
                                    "Authorization",
                                    "Bearer " + userToken
                            )
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .content(scoreJson)
            )
            .andExpect(status().isOk());
        }


        // =========================================================
        // 9. VERIFY EXACTLY 5 SCORES
        // =========================================================

        MvcResult scoresResult = mockMvc.perform(
                get("/api/scores")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode scoreResponse =
                objectMapper.readTree(
                        scoresResult
                                .getResponse()
                                .getContentAsString()
                );

        if (!scoreResponse.isArray()
                || scoreResponse.size() != 5) {

            throw new AssertionError(
                    "Expected exactly 5 scores but found "
                            + scoreResponse.size()
            );
        }


        // =========================================================
        // 10. LOGIN ADMIN
        // =========================================================

        String adminEmail =
                "admin@digitalheroes.local";

        String adminPassword =
                "Admin@12345";

        String adminLoginJson = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                        adminEmail,
                        adminPassword
                );

        MvcResult adminLoginResult = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(adminLoginJson)
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode adminLoginResponse =
                objectMapper.readTree(
                        adminLoginResult
                                .getResponse()
                                .getContentAsString()
                );

        String adminToken =
                adminLoginResponse
                        .get("accessToken")
                        .asText();


        // =========================================================
        // 11. SIMULATE DRAW
        // =========================================================

        LocalDate drawDate =
                LocalDate.now().plusDays(1);

        String simulateJson = """
                {
                    "drawPeriod": "%s",
                    "mode": "ALGORITHMIC"
                }
                """.formatted(drawDate);

        MvcResult simulateResult = mockMvc.perform(
                post("/api/admin/draws/simulate")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(simulateJson)
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode simulatedDraw =
                objectMapper.readTree(
                        simulateResult
                                .getResponse()
                                .getContentAsString()
                );

        Long drawId =
                simulatedDraw.get("id").asLong();

        if (!"SIMULATED".equals(
                simulatedDraw.get("status").asText())) {

            throw new AssertionError(
                    "Draw was not simulated"
            );
        }


        // =========================================================
        // 12. LOAD DRAW FROM DATABASE
        // =========================================================

        Draw draw = drawRepository
                .findById(drawId)
                .orElseThrow(() ->
                        new AssertionError(
                                "Simulated draw not found"
                        )
                );


        // =========================================================
        // 13. LOAD PARTICIPANT
        // =========================================================

        List<DrawParticipant> participants =
                drawParticipantRepository
                        .findByDrawId(drawId);

        DrawParticipant participant =
                participants.stream()
                        .filter(p ->
                                p.getUser()
                                        .getId()
                                        .equals(userId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new AssertionError(
                                        "Test user was not eligible "
                                                + "for the draw"
                                )
                        );


        // =========================================================
        // 14. MAKE DRAW NUMBERS DETERMINISTIC
        //
        // User scores:
        // 10, 15, 20, 25, 30
        //
        // Draw numbers:
        // 10, 15, 20, 41, 42
        //
        // Therefore matchCount = 3
        // =========================================================

        draw.setDrawnNumbers(
                new Short[]{
                        (short) 10,
                        (short) 15,
                        (short) 20,
                        (short) 41,
                        (short) 42
                }
        );

        drawRepository.saveAndFlush(draw);


        // =========================================================
        // 15. PUBLISH DRAW
        // =========================================================

        MvcResult publishResult = mockMvc.perform(
                post("/api/admin/draws/" + drawId + "/publish")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode publishedDraw =
                objectMapper.readTree(
                        publishResult
                                .getResponse()
                                .getContentAsString()
                );

        if (!"PUBLISHED".equals(
                publishedDraw.get("status").asText())) {

            throw new AssertionError(
                    "Draw was not published"
            );
        }


        // =========================================================
        // 16. GET USER WINNERS
        // =========================================================

        MvcResult winnersResult = mockMvc.perform(
                get("/api/winners/me")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode winners =
                objectMapper.readTree(
                        winnersResult
                                .getResponse()
                                .getContentAsString()
                );

        if (!winners.isArray()
                || winners.isEmpty()) {

            throw new AssertionError(
                    "Expected a winner but no winner was created"
            );
        }

        JsonNode winner =
                winners.get(0);

        Long winnerId =
                winner.get("id").asLong();


        // =========================================================
        // 17. UPLOAD WINNER PROOF
        // =========================================================

        MockMultipartFile proof =
                new MockMultipartFile(
                        "file",
                        "proof.png",
                        MediaType.IMAGE_PNG_VALUE,
                        "fake-test-image"
                                .getBytes(StandardCharsets.UTF_8)
                );

        mockMvc.perform(
                multipart(
                        "/api/winners/" + winnerId + "/proof"
                )
                .file(proof)
                .header(
                        "Authorization",
                        "Bearer " + userToken
                )
        )
        .andExpect(status().isOk());


        // =========================================================
        // 18. ADMIN APPROVES WINNER
        // =========================================================

        String verificationJson = """
                {
                    "approved": true,
                    "notes": "Integration test approval"
                }
                """;

        MvcResult verifyResult = mockMvc.perform(
                post(
                        "/api/admin/winners/"
                                + winnerId
                                + "/verify"
                )
                .header(
                        "Authorization",
                        "Bearer " + adminToken
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .content(verificationJson)
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode verifiedWinner =
                objectMapper.readTree(
                        verifyResult
                                .getResponse()
                                .getContentAsString()
                );

        if (!"APPROVED".equals(
                verifiedWinner
                        .get("verificationStatus")
                        .asText()
        )) {

            throw new AssertionError(
                    "Winner was not approved"
            );
        }


        // =========================================================
        // 19. GET PAYOUT ID
        // =========================================================

        Long payoutId =
                verifiedWinner
                        .get("payoutId")
                        .asLong();

        if (payoutId == null || payoutId <= 0) {

            throw new AssertionError(
                    "Payout was not created after approval"
            );
        }


        // =========================================================
        // 20. MARK PAYOUT AS PAID
        // =========================================================

        MvcResult paidResult = mockMvc.perform(
                post(
                        "/api/admin/payouts/"
                                + payoutId
                                + "/paid"
                )
                .header(
                        "Authorization",
                        "Bearer " + adminToken
                )
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode paidResponse =
                objectMapper.readTree(
                        paidResult
                                .getResponse()
                                .getContentAsString()
                );

        if (!"PAID".equals(
                paidResponse.get("status").asText()
        )) {

            throw new AssertionError(
                    "Payout was not marked as PAID"
            );
        }


        // =========================================================
        // 21. FINAL VERIFICATION
        // =========================================================

        MvcResult finalWinnerResult = mockMvc.perform(
                get("/api/winners/me")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(status().isOk())
        .andReturn();

        JsonNode finalWinners =
                objectMapper.readTree(
                        finalWinnerResult
                                .getResponse()
                                .getContentAsString()
                );

        JsonNode finalWinner = null;

        for (JsonNode w : finalWinners) {
            if (w.get("id").asLong() == winnerId) {
                finalWinner = w;
                break;
            }
        }

        if (finalWinner == null) {
            throw new AssertionError("Final winner not found");
        }

        if (!"APPROVED".equals(
                finalWinner
                        .get("verificationStatus")
                        .asText()
        )) {

            throw new AssertionError(
                    "Final winner verification is not APPROVED"
            );
        }

        if (!"PAID".equals(
                finalWinner
                        .get("payoutStatus")
                        .asText()
        )) {

            throw new AssertionError(
                    "Final payout status is not PAID"
            );
        }
    }
}