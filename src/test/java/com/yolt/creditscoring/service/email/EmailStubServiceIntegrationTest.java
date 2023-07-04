package com.yolt.creditscoring.service.email;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.client.ClientEmailDTO;
import com.yolt.creditscoring.service.email.model.InvitationEmailData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

@IntegrationTest
@TestPropertySource(properties = "credit-scoring.email.stubbed-client-ids=" + EmailStubServiceIntegrationTest.STUBBED_CLIENT_ID)
class EmailStubServiceIntegrationTest {

    static final String STUBBED_CLIENT_ID = "4b16a07f-f409-4289-8bfb-93186a0c9a2f";

    @Autowired
    private EmailStubService emailStubService;

    @BeforeEach
    void beforeEach() {
        WireMock.resetAllRequests();
    }

    @Test
    void shouldDetermineWhetherToStubBasedOnConfiguredStubbedClientIds() {
        assertThat(emailStubService.shouldBeStubbed(UUID.fromString(STUBBED_CLIENT_ID))).isEqualTo(true);
        assertThat(emailStubService.shouldBeStubbed(randomUUID())).isEqualTo(false);
    }

    @Test
    void shouldSendInvitationsToStubs() {
        UUID clientId = randomUUID();
        UUID userId = randomUUID();
        InvitationEmailData invitationEmailData = InvitationEmailData.builder()
                .clientEmail(mock(ClientEmailDTO.class))
                .recipientEmail("irrelev@nt2.com")
                .clientLogoUrl("https://some.url")
                .redirectUrl("https://some-redirect-url.com")
                .userName("John Doe")
                .build();

        String expectedUrl = "/cashflow-analyser/clients/%s/users/%s/invite".formatted(clientId, userId);

        stubFor(put(urlEqualTo(expectedUrl)).willReturn(ok()));

        emailStubService.sendInvitationForUser(clientId, userId, invitationEmailData);

        verify(1,
                putRequestedFor(urlEqualTo(expectedUrl))
                        .withRequestBody(equalToJson("{ \"redirectUrl\": \"" + invitationEmailData.getRedirectUrl() + "\"}"))
        );
    }
}
