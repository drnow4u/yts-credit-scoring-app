package com.yolt.creditscoring.controller.admin.clienttoken;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenEntity;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenRepository;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenStatus;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.admin.clienttoken.ClientTokenController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ClientTokenControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;
    
    @Autowired
    private ClientTokenRepository clientTokenRepository;

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
        userJourneyRepository.deleteAll();
        clientTokenRepository.deleteAll();
    }

    @Test
    void shouldCreateClientToken() throws Exception {
        // When
        ResultActions perform = mvc.perform(post(CREATE_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                            {
                                                "name": "%s",
                                                "permissions": ["%s"]
                                            }
                                        """,
                                SOME_CLIENT_JWT_NAME,
                                ClientTokenPermission.INVITE_USER)
                )
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientToken", notNullValue()));

        Iterable<ClientTokenEntity> result = clientTokenRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result).extracting("clientId", "status", "signedPublicKeyId")
                .contains(tuple(SOME_CLIENT_ID, ClientTokenStatus.ACTIVE, SOME_JWT_PUBLIC_KEY_ID));
        assertThat(result).flatExtracting("permissions")
                .contains(ClientTokenPermission.INVITE_USER);
    }

    @Test
    void shouldListAllClientTokens() throws Exception {
        // Given
        OffsetDateTime currentDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        String currentDateTimeString = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        String currentDateTimePlusDayString = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        String currentDateTimeMinusDayString = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

        List<ClientTokenEntity> givenClientTokenEntities = List.of(
                ClientTokenEntity.builder()
                        .jwtId(UUID.fromString("a2ecf9a2-85c4-47b7-bd83-54c3ee28c2df"))
                        .name("First Token")
                        .signedPublicKeyId(SOME_JWT_PUBLIC_KEY_ID)
                        .clientId(SOME_CLIENT_ID)
                        .createdAdminEmail(SOME_CLIENT_ADMIN_EMAIL)
                        .status(ClientTokenStatus.ACTIVE)
                        .createdDate(currentDateTime)
                        .expirationDate(currentDateTime.plusDays(1))
                        .permissions(List.of(ClientTokenPermission.INVITE_USER))
                        .build(),
                ClientTokenEntity.builder()
                        .jwtId(UUID.fromString("44efc0c4-c914-4c55-acd7-30e3468baaa3"))
                        .name("Second Token")
                        .signedPublicKeyId(SOME_JWT_PUBLIC_KEY_ID)
                        .clientId(SOME_CLIENT_ID)
                        .createdAdminEmail(SOME_CLIENT_ADMIN_EMAIL)
                        .status(ClientTokenStatus.ACTIVE)
                        .createdDate(currentDateTime.minusDays(1))
                        .lastAccessedDate(currentDateTime)
                        .expirationDate(currentDateTime.plusDays(1))
                        .permissions(List.of(ClientTokenPermission.DOWNLOAD_REPORT))
                        .build(),
                ClientTokenEntity.builder()
                        .jwtId(UUID.randomUUID())
                        .name("Third Token for different client")
                        .signedPublicKeyId(SOME_JWT_PUBLIC_KEY_ID)
                        .clientId(SOME_CLIENT_ID_2)
                        .createdAdminEmail(SOME_CLIENT_ADMIN_EMAIL)
                        .status(ClientTokenStatus.ACTIVE)
                        .createdDate(currentDateTime)
                        .expirationDate(currentDateTime.plusDays(2))
                        .lastAccessedDate(SOME_FIXED_TEST_DATE)
                        .permissions(List.of(ClientTokenPermission.DOWNLOAD_REPORT))
                        .build()
        );
        clientTokenRepository.saveAll(givenClientTokenEntities);


        // When
        ResultActions result = mvc.perform(get(CREATE_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].id", is("a2ecf9a2-85c4-47b7-bd83-54c3ee28c2df")))
                .andExpect(jsonPath("$.[0].name", is("First Token")))
                .andExpect(jsonPath("$.[0].creationDate", is(currentDateTimeString)))
                .andExpect(jsonPath("$.[0].expiryDate", is(currentDateTimePlusDayString)))
                .andExpect(jsonPath("$.[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$.[1].id", is("44efc0c4-c914-4c55-acd7-30e3468baaa3")))
                .andExpect(jsonPath("$.[1].name", is("Second Token")))
                .andExpect(jsonPath("$.[1].creationDate", is(currentDateTimeMinusDayString)))
                .andExpect(jsonPath("$.[1].expiryDate", is(currentDateTimePlusDayString)))
                .andExpect(jsonPath("$.[1].lastUsed", is(currentDateTimeString)))
                .andExpect(jsonPath("$.[1].status", is("ACTIVE")));
    }

    @Test
    void shouldGetAllClientTokenStatuses() throws Exception {
        // When
        ResultActions result = mvc.perform(get(LIST_TOKEN_PERMISSIONS_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$.[0]", is("INVITE_USER")))
                .andExpect(jsonPath("$.[1]", is("DOWNLOAD_REPORT")))
                .andExpect(jsonPath("$.[2]", is("DELETE_USER")));
    }

    @Test
    void shouldRevokeToken() throws Exception {
        // Given
        UUID jwtId = UUID.fromString("a2ecf9a2-85c4-47b7-bd83-54c3ee28c2df");

        clientTokenRepository.save(ClientTokenEntity.builder()
                .jwtId(jwtId)
                .name("First Token")
                .signedPublicKeyId(SOME_JWT_PUBLIC_KEY_ID)
                .clientId(SOME_CLIENT_ID)
                .createdAdminEmail(SOME_CLIENT_ADMIN_EMAIL)
                .status(ClientTokenStatus.ACTIVE)
                .createdDate(SOME_TEST_DATE)
                .expirationDate(SOME_TEST_DATE.plusDays(1))
                .permissions(List.of(ClientTokenPermission.INVITE_USER))
                .build());

        // When
        ResultActions result = mvc.perform(put(REVOKE_TOKEN_ENDPOINT, jwtId)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().isOk());

        assertThat(clientTokenRepository.findById(jwtId).get().getStatus()).isEqualTo(ClientTokenStatus.REVOKED);

    }

}
