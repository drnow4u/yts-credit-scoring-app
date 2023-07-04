package com.yolt.creditscoring.service.email;

import com.yolt.creditscoring.service.email.model.InvitationEmailData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
public class EmailStubService {

    private final WebClient webClient;

    private final List<UUID> stubbedClientIds;

    EmailStubService(WebClient.Builder webClientBuilder,
                     @Value("${yolt.stubs.base-url:https://stubs/stubs}") String stubsBaseUrl,
                     @Value("${credit-scoring.email.stubbed-client-ids:}") List<UUID> stubbedClientIds) {
        this.webClient = webClientBuilder.baseUrl(stubsBaseUrl).build();
        this.stubbedClientIds = stubbedClientIds;
    }

    void sendInvitationForUser(UUID clientId, UUID userId, InvitationEmailData invitationEmailData) {
        webClient.put()
                .uri("/cashflow-analyser/clients/{clientId}/users/{userId}/invite", clientId, userId)
                .bodyValue(new CfaStubInviteDTO(invitationEmailData.getRedirectUrl()))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    boolean shouldBeStubbed(UUID clientId) {
        return stubbedClientIds.contains(clientId);
    }

    private record CfaStubInviteDTO(String redirectUrl) {
    }
}
