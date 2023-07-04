package com.yolt.creditscoring.service.client;

import com.yolt.creditscoring.exception.ClientEmailConfigurationNotFoundException;
import com.yolt.creditscoring.exception.ClientNotFoundException;
import com.yolt.creditscoring.service.client.model.ClientEmailEntity;
import com.yolt.creditscoring.service.client.model.ClientEmailRepository;
import com.yolt.creditscoring.service.client.model.ClientEntity;
import com.yolt.creditscoring.service.client.model.ClientRepository;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.TogglesDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class ClientStorageService {

    private final ClientRepository clientRepository;
    private final ClientEmailRepository clientEmailRepository;

    public String getClientAdditionalReportTextBasedOnClientId(@NonNull UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId))
                .getAdditionalTextReport();
    }

    public @Valid ClientEmailDTO getClientEmailById(@NonNull UUID clientEmailId) {
        ClientEmailEntity clientEmailEntity = clientEmailRepository.findById(clientEmailId)
                .orElseThrow(() -> new ClientEmailConfigurationNotFoundException(clientEmailId));

        return mapToClientEmailDTO(clientEmailEntity);
    }

    public @Valid ClientEmailDTO getOnlyClientEmailByClientId(@NonNull UUID clientId) {
        List<ClientEmailEntity> clientEmails = clientEmailRepository.findByClient_Id(clientId);

        if (clientEmails.isEmpty()) {
            // This is a client configuration problem.
            throw new IllegalStateException("No client templates were found for client: " + clientId);
        }

        if (clientEmails.size() > 1) {
            // This is a client configuration problem.
            throw new IllegalStateException("Client has more then one email template, client id: " + clientId);
        }

        return mapToClientEmailDTO(clientEmails.get(0));
    }

    public List<@Valid ClientEmailDTO> getAllClientEmailTemplates(@NonNull UUID clientId) {
        List<ClientEmailEntity> clientEmails = clientEmailRepository.findByClient_Id(clientId);

        if (clientEmails.isEmpty()) {
            throw new ClientEmailConfigurationNotFoundException("No client templates were found for client: " + clientId);
        }

        return clientEmails.stream().map(this::mapToClientEmailDTO).toList();
    }

    public boolean checkIfClientHasPDFeatureEnabled(@NonNull UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId))
                .isPDScoreFeatureToggle();
    }

    public boolean hasSignatureVerificationFeature(@NonNull UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId))
                .isSignatureVerificationFeatureToggle();
    }

    public boolean hasApiTokenFeature(@NonNull UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId))
                .isApiTokenFeatureToggle();
    }

    public @Valid TogglesDTO getFeatureToggles(@NonNull UUID clientId) {
        return clientRepository.findById(clientId)
                .map(clientEntity -> TogglesDTO.builder()
                        .categoryFeatureToggle(clientEntity.isCategoryFeatureToggle())
                        .monthsFeatureToggle(clientEntity.isMonthsFeatureToggle())
                        .overviewFeatureToggle(clientEntity.isOverviewFeatureToggle())
                        .apiTokenFeatureToggle(clientEntity.isApiTokenFeatureToggle())
                        .estimateFeatureToggle(clientEntity.isPDScoreFeatureToggle())
                        .build())
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    public @Valid ClientDTO getClientInformationBasedOnLoggedUser(@NonNull UUID clientId) {
        Optional<ClientEntity> client = clientRepository.findById(clientId);
        return client.map(this::mapClientToClientDTO)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    public String getSiteTagsForGivenClient(@NonNull UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId))
                .getSiteTags();
    }

    public @Valid ClientSettingsDTO getClientSettings(@NonNull UUID clientId) {
        return clientRepository.findById(clientId)
                .map(this::mapClientToSettingsDTO)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    public String getClientRedirectUrl(@NonNull UUID clientId) {
        return clientRepository.findById(clientId)
                .map(ClientEntity::getRedirectUrl)
                .orElse(StringUtils.EMPTY);
    }

    public byte[] getClientLogo(@NonNull UUID clientId) {
        return clientRepository.findById(clientId)
                .map(ClientEntity::getLogo)
                .map(it -> Base64.getDecoder().decode(it))
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    private ClientDTO mapClientToClientDTO(ClientEntity client) {
        return ClientDTO.builder()
                .name(client.getName())
                .logo(client.getLogo())
                .language(client.getDefaultLanguage().name().toLowerCase())
                .additionalTextConsent(client.getAdditionalTextConsent())
                .build();
    }

    private ClientSettingsDTO mapClientToSettingsDTO(ClientEntity client) {
        return ClientSettingsDTO.builder()
                .defaultLanguage(client.getDefaultLanguage())
                .signatureVerificationFeatureToggle(client.isSignatureVerificationFeatureToggle())
                .pDScoreFeatureToggle(client.isPDScoreFeatureToggle())
                .monthsFeatureToggle(client.isMonthsFeatureToggle())
                .overviewFeatureToggle(client.isOverviewFeatureToggle())
                .categoryFeatureToggle(client.isCategoryFeatureToggle())
                .apiTokenFeatureToggle(client.isApiTokenFeatureToggle())
                .build();
    }

    private ClientEmailDTO mapToClientEmailDTO(ClientEmailEntity clientEmailEntity) {
        return ClientEmailDTO.builder()
                .id(clientEmailEntity.getId())
                .subject(clientEmailEntity.getSubject())
                .template(clientEmailEntity.getTemplate())
                .title(clientEmailEntity.getTitle())
                .subtitle(clientEmailEntity.getSubtitle())
                .welcomeBox(clientEmailEntity.getWelcomeBox())
                .buttonText(clientEmailEntity.getButtonText())
                .summaryBox(clientEmailEntity.getSummaryBox())
                .websiteUrl(clientEmailEntity.getWebsiteUrl())
                .sender(clientEmailEntity.getSender())
                .build();
    }
}
