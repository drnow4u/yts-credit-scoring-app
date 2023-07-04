package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.client.model.*;
import com.yolt.creditscoring.service.client.onboarding.ClientAdminOnboarding;
import com.yolt.creditscoring.service.client.onboarding.ClientUpdate;
import com.yolt.creditscoring.service.client.onboarding.OnboardClient;
import com.yolt.creditscoring.service.client.onboarding.OnboardClientEmail;
import com.yolt.creditscoring.service.clientadmin.model.AuthProvider;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.*;

import static com.yolt.creditscoring.TestUtils.SOME_CLIENT_REDIRECT_URL;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.BDDAssertions.then;

@IntegrationTest
class ClientManagementUseCaseIT {

    @Autowired
    private ClientManagementUseCase clientManagementUseCase;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientAdminRepository clientAdminRepository;

    @Autowired
    private ClientEmailRepository clientEmailRepository;

    @Test
    @Transactional
    void shouldOnboardNewClient() throws IOException {
        // given
        UUID clientId = UUID.fromString("9280c8e1-d832-4f65-9d94-03e04b6068e6");

        OnboardClientEmail clientEmail1 = OnboardClientEmail.builder()
                .template("SOME_EMAIL_TEMPLATE_1")
                .subject("SOME_SUBJECT_1")
                .sender("Cashflow Analyser PL<no-reply-cashflow-analyser@yolt.com>")
                .build();
        OnboardClientEmail clientEmail2 = OnboardClientEmail.builder()
                .template("SOME_EMAIL_TEMPLATE_2")
                .subject("SOME_SUBJECT_2")
                .sender("Cashflow Analyser EN<no-reply-cashflow-analyser@yolt.com>")
                .build();

        OnboardClient onboardClient = OnboardClient.builder()
                .id(clientId)
                .name("Test Client")
                .clientEmails(Arrays.asList(clientEmail1, clientEmail2))
                .logo("binaryData")
                .siteTags("NL")
                .defaultLanguage(ClientLanguage.NL)
                .redirectUrl(SOME_CLIENT_REDIRECT_URL)
                .pDScoreFeatureToggle(false)
                .signatureVerificationFeatureToggle(true)
                .categoryFeatureToggle(true)
                .monthsFeatureToggle(true)
                .overviewFeatureToggle(true)
                .apiTokenFeatureToggle(true)
                .build();

        ClientAdminOnboarding clientAdmin1 = ClientAdminOnboarding.builder()
                .idpId("1234567890")
                .email("test1@yolt.com")
                .authProvider(AuthProvider.GITHUB)
                .build();

        ClientAdminOnboarding clientAdmin2 = ClientAdminOnboarding.builder()
                .idpId("0987654321")
                .email("test2@yolt.com")
                .authProvider(AuthProvider.GOOGLE)
                .build();

        // when
        clientManagementUseCase.onboardNewClient(onboardClient, clientAdmin1, clientAdmin2);
        // executing the method second time to make sure the client with given ID will only be onboarded once
        // this simulates a situation when two pods would execute this method on startup
        clientManagementUseCase.onboardNewClient(onboardClient, clientAdmin1, clientAdmin2);

        // then
        Optional<ClientEntity> client = clientRepository.findById(clientId);
        then(client)
                .get()
                .hasFieldOrPropertyWithValue("id", clientId)
                .hasFieldOrPropertyWithValue("name", "Test Client")
                .hasFieldOrPropertyWithValue("logo", onboardClient.getLogo())
                .hasFieldOrPropertyWithValue("siteTags", "NL")
                .hasFieldOrPropertyWithValue("defaultLanguage", ClientLanguage.NL)
                .hasFieldOrPropertyWithValue("redirectUrl", SOME_CLIENT_REDIRECT_URL)
                .hasFieldOrPropertyWithValue("pDScoreFeatureToggle", false)
                .hasFieldOrPropertyWithValue("isSignatureVerificationFeatureToggle", true)
                .hasFieldOrPropertyWithValue("categoryFeatureToggle", true)
                .hasFieldOrPropertyWithValue("monthsFeatureToggle", true)
                .hasFieldOrPropertyWithValue("overviewFeatureToggle", true)
                .hasFieldOrPropertyWithValue("apiTokenFeatureToggle", true);
        ;
        then(client.get().getClientEmails())
                .extracting("template", "subject", "sender")
                .contains(
                        tuple("SOME_EMAIL_TEMPLATE_1", "SOME_SUBJECT_1", "Cashflow Analyser PL<no-reply-cashflow-analyser@yolt.com>"),
                        tuple("SOME_EMAIL_TEMPLATE_2", "SOME_SUBJECT_2", "Cashflow Analyser EN<no-reply-cashflow-analyser@yolt.com>")
                );

        then(clientAdminRepository.findByIdpId("1234567890"))
                .get()
                .hasFieldOrPropertyWithValue("clientId", clientId)
                .hasFieldOrPropertyWithValue("email", "test1@yolt.com")
                .hasFieldOrPropertyWithValue("authProvider", AuthProvider.GITHUB)
                .hasFieldOrPropertyWithValue("idpId", "1234567890");

        then(clientAdminRepository.findByIdpId("0987654321"))
                .get()
                .hasFieldOrPropertyWithValue("clientId", clientId)
                .hasFieldOrPropertyWithValue("email", "test2@yolt.com")
                .hasFieldOrPropertyWithValue("authProvider", AuthProvider.GOOGLE)
                .hasFieldOrPropertyWithValue("idpId", "0987654321");
    }

    @Test
    @Transactional
    void shouldNotOnboardNewClientBecauseWrongSenderEmail() {
        // given
        UUID clientId = UUID.fromString("9280c8e1-d832-4f65-9d94-03e04b6068e6");

        OnboardClientEmail clientEmail1 = OnboardClientEmail.builder()
                .template("SOME_EMAIL_TEMPLATE_1")
                .subject("SOME_SUBJECT_1")
                .sender("Invalid email address PL")
                .build();
        OnboardClientEmail clientEmail2 = OnboardClientEmail.builder()
                .template("SOME_EMAIL_TEMPLATE_2")
                .subject("SOME_SUBJECT_2")
                .sender("Invalid email address EN")
                .build();

        OnboardClient onboardClient = OnboardClient.builder()
                .id(clientId)
                .name("Test Client")
                .clientEmails(Arrays.asList(clientEmail1, clientEmail2))
                .logo("binaryData")
                .siteTags("NL")
                .defaultLanguage(ClientLanguage.NL)
                .pDScoreFeatureToggle(false)
                .signatureVerificationFeatureToggle(true)
                .categoryFeatureToggle(true)
                .monthsFeatureToggle(true)
                .overviewFeatureToggle(true)
                .apiTokenFeatureToggle(true)
                .build();

        ClientAdminOnboarding clientAdmin1 = ClientAdminOnboarding.builder()
                .idpId("1234567890")
                .email("test1@yolt.com")
                .authProvider(AuthProvider.GITHUB)
                .build();

        ClientAdminOnboarding clientAdmin2 = ClientAdminOnboarding.builder()
                .idpId("0987654321")
                .email("test2@yolt.com")
                .authProvider(AuthProvider.GOOGLE)
                .build();

        // when
        Throwable thrown = catchThrowable(() -> clientManagementUseCase.onboardNewClient(onboardClient, clientAdmin1, clientAdmin2));

        // Then
        assertThat(thrown).isInstanceOf(ConstraintViolationException.class);
        assertThat(thrown.getMessage()).contains("must be a well-formed e-mail address optionally with user name");
    }

    @Test
    void shouldRollbackAllChangesIfThereWasAnErrorWhenSavingDataToDatabase() {
        // given
        UUID clientId = UUID.fromString("9280c8e1-d832-4f65-9d94-03e04b6068e6");

        OnboardClient onboardClient = OnboardClient.builder()
                .id(clientId)
                .name("Test Client")
                .clientEmails(new ArrayList<>())
                .siteTags("NL")
                .defaultLanguage(ClientLanguage.NL)
                .pDScoreFeatureToggle(false)
                .signatureVerificationFeatureToggle(false)
                .categoryFeatureToggle(true)
                .monthsFeatureToggle(true)
                .overviewFeatureToggle(true)
                .apiTokenFeatureToggle(true)
                .build();

        ClientAdminOnboarding clientAdmin = ClientAdminOnboarding.builder()
                .idpId("1234567890")
                .email("test1@yolt.com")
                .authProvider(AuthProvider.GITHUB)
                .build();

        ClientAdminOnboarding clientAdminWithTheSameIdpIdThatShouldCauseError = ClientAdminOnboarding.builder()
                .idpId("1234567890")
                .email("test2@yolt.com")
                .authProvider(AuthProvider.GOOGLE)
                .build();

        try {
            // when
            clientManagementUseCase.onboardNewClient(onboardClient, clientAdmin, clientAdminWithTheSameIdpIdThatShouldCauseError);
        } catch (Exception e) {
            // ignore for test
        } finally {
            // then
            then(clientRepository.findById(clientId)).isEmpty();
            then(clientAdminRepository.findByIdpId("1234567890")).isEmpty();
        }
    }

    @Test
    @Transactional
    void shouldAddClientAdminToExistingClient() {
        // Given
        UUID existingClientId = UUID.fromString("0b4cee11-0bd6-4e86-806f-45c913ad7bd5");

        ClientAdminOnboarding clientAdmin1 = ClientAdminOnboarding.builder()
                .idpId("1234567890")
                .email("test1@yolt.com")
                .authProvider(AuthProvider.GITHUB)
                .build();

        ClientAdminOnboarding clientAdmin2 = ClientAdminOnboarding.builder()
                .idpId("0987654321")
                .email("test2@yolt.com")
                .authProvider(AuthProvider.GOOGLE)
                .build();

        // When
        clientManagementUseCase.addNewClientAdminToClient(existingClientId, clientAdmin1, clientAdmin2);
        // executing the method second time to make sure that given client admins will be only added once
        // this simulates a situation when two pods would execute this method on startup
        clientManagementUseCase.addNewClientAdminToClient(existingClientId, clientAdmin1, clientAdmin2);

        // Then
        then(clientAdminRepository.findByIdpId("1234567890"))
                .get()
                .hasFieldOrPropertyWithValue("clientId", existingClientId)
                .hasFieldOrPropertyWithValue("email", "test1@yolt.com")
                .hasFieldOrPropertyWithValue("authProvider", AuthProvider.GITHUB)
                .hasFieldOrPropertyWithValue("idpId", "1234567890");

        then(clientAdminRepository.findByIdpId("0987654321"))
                .get()
                .hasFieldOrPropertyWithValue("clientId", existingClientId)
                .hasFieldOrPropertyWithValue("email", "test2@yolt.com")
                .hasFieldOrPropertyWithValue("authProvider", AuthProvider.GOOGLE)
                .hasFieldOrPropertyWithValue("idpId", "0987654321");
    }

    @Test
    @Transactional
    void shouldRemoveClientAdminFromClient() {
        // Given
        UUID existingClientId = UUID.fromString("0b4cee11-0bd6-4e86-806f-45c913ad7bd5");

        ClientAdmin clientAdmin = new ClientAdmin();
        clientAdmin.setClientId(existingClientId);
        clientAdmin.setId(UUID.randomUUID());
        clientAdmin.setIdpId("1234567890");
        clientAdmin.setEmail("test1@yolt.com");
        clientAdmin.setAuthProvider(AuthProvider.GITHUB);
        clientAdminRepository.save(clientAdmin);

        // When
        clientManagementUseCase.removeClientAdminFromClient(existingClientId, "1234567890");
        // executing the method second time to make sure that there will be no error when the user would be deleted twice
        // this simulates a situation when two pods would execute this method on startup
        clientManagementUseCase.removeClientAdminFromClient(existingClientId, "1234567890");

        // Then
        then(clientAdminRepository.findByIdpId("1234567890")).isEmpty();
    }

    @Test
    @Transactional
    void shouldUpdateExistingClientData() throws IOException {
        // Given
        UUID existingClientId = UUID.fromString("0b4cee11-0bd6-4e86-806f-45c913ad7bd5");

        ClientUpdate clientUpdate = ClientUpdate.builder()
                .id(existingClientId)
                .logo("someBinaryData")
                .additionalTextConsent("New Consent Text")
                .additionalTextReport("New Report Text")
                .template("New Email Template")
                .siteTags("NL")
                .defaultLanguage(ClientLanguage.NL)
                .redirectUrl("https://client-redirect-new.com")
                .pDScoreFeatureToggle(true)
                .signatureVerificationFeatureToggle(false)
                .categoryFeatureToggle(true)
                .monthsFeatureToggle(true)
                .overviewFeatureToggle(true)
                .apiTokenFeatureToggle(true)
                .build();

        // When
        clientManagementUseCase.updateClientData(clientUpdate);
        // executing the method second time to make sure client was correctly updated
        // this simulates a situation when two pods would execute this method on startup
        clientManagementUseCase.updateClientData(clientUpdate);

        // Then
        then(clientRepository.findById(existingClientId))
                .get()
                .hasFieldOrPropertyWithValue("id", existingClientId)
                .hasFieldOrPropertyWithValue("name", "Some Client")
                .hasFieldOrPropertyWithValue("logo", clientUpdate.getLogo())
                .hasFieldOrPropertyWithValue("siteTags", "NL")
                .hasFieldOrPropertyWithValue("defaultLanguage", ClientLanguage.NL)
                .hasFieldOrPropertyWithValue("additionalTextConsent", "New Consent Text")
                .hasFieldOrPropertyWithValue("additionalTextReport", "New Report Text")
                .hasFieldOrPropertyWithValue("redirectUrl", "https://client-redirect-new.com")
                .hasFieldOrPropertyWithValue("pDScoreFeatureToggle", true)
                .hasFieldOrPropertyWithValue("isSignatureVerificationFeatureToggle", false)
                .hasFieldOrPropertyWithValue("categoryFeatureToggle", true)
                .hasFieldOrPropertyWithValue("monthsFeatureToggle", true)
                .hasFieldOrPropertyWithValue("overviewFeatureToggle", true)
                .hasFieldOrPropertyWithValue("apiTokenFeatureToggle", true);
    }

    @Test
    @Transactional
    void shouldAddExistingEmailConfiguration() {
        // Given
        UUID existingClientId = UUID.fromString("0b4cee11-0bd6-4e86-806f-45c913ad7bd5");

        OnboardClientEmail onboardClientEmail1 = OnboardClientEmail.builder()
                .template("Some_email_template_en")
                .subject("Some subject EN")
                .sender("Cashflow Analyser EN<no-reply-cashflow-analyser@yolt.com>")
                .build();

        OnboardClientEmail onboardClientEmail2 = OnboardClientEmail.builder()
                .template("Some_email_template_pl")
                .subject("Some subject PL")
                .sender("Cashflow Analyser PL<no-reply-cashflow-analyser@yolt.com>")
                .build();

        // When
        clientManagementUseCase.addClientEmailConfiguration(existingClientId, onboardClientEmail1, onboardClientEmail2);
        // executing the method second time to make sure client was correctly updated
        // this simulates a situation when two pods would execute this method on startup
        clientManagementUseCase.addClientEmailConfiguration(existingClientId, onboardClientEmail1, onboardClientEmail2);

        // Then
        then(clientRepository.findById(existingClientId).get().getClientEmails())
                .extracting("template", "subject", "sender")
                .contains(
                        tuple("UserInvitation_Test_Client", "Uitnodiging voor de Cashflow Analyser", "Cashflow Analyser <no-reply-cashflow-analyser@yolt.com>"),
                        tuple("Some_email_template_en", "Some subject EN", "Cashflow Analyser EN<no-reply-cashflow-analyser@yolt.com>"),
                        tuple("Some_email_template_pl", "Some subject PL", "Cashflow Analyser PL<no-reply-cashflow-analyser@yolt.com>")
                );

    }

    @Test
    @Transactional
    void shouldNotAddExistingEmailConfigurationBecauseWrongSenderEmail() {
        // Given
        UUID existingClientId = UUID.fromString("0b4cee11-0bd6-4e86-806f-45c913ad7bd5");

        OnboardClientEmail onboardClientEmail1 = OnboardClientEmail.builder()
                .template("Some_email_template_en")
                .subject("Invalid email address EN")
                .sender("Cashflow Analyser EN<no-reply-cashflow-analyser@yolt.com>")
                .build();

        OnboardClientEmail onboardClientEmail2 = OnboardClientEmail.builder()
                .template("Some_email_template_pl")
                .subject("Some subject PL")
                .sender("Invalid email address PL")
                .build();

        // When
        Throwable thrown = catchThrowable(() -> clientManagementUseCase.addClientEmailConfiguration(existingClientId, onboardClientEmail1, onboardClientEmail2));

        // Then
        assertThat(thrown).isInstanceOf(ConstraintViolationException.class);
        assertThat(thrown.getMessage()).contains("must be a well-formed e-mail address optionally with user name");
    }

    @Test
    @Transactional
    void shouldOverrideExistingEmailConfiguration() {
        // Given
        UUID existingClientId = UUID.fromString("0b4cee11-0bd6-4e86-806f-45c913ad7bd5");
        final ClientEntity clientEntity = clientRepository.findById(existingClientId).get();
        final ClientEmailEntity clientEmailEntity = new ClientEmailEntity();
        clientEmailEntity.setId(UUID.fromString("fa07ae4c-4010-428a-86d3-13728b763190"));
        clientEmailEntity.setTemplate("Some_email_template_en");
        clientEmailEntity.setSender("Cashflow Analyser EN<no-reply-cashflow-analyser@yolt.com>");
        clientEmailEntity.setSubject("Some subject EN");
        clientEmailEntity.setClient(clientEntity);

        clientEntity.getClientEmails().add(clientEmailEntity);
        clientEmailRepository.save(clientEmailEntity);

        OnboardClientEmail onboardClientEmailOld = OnboardClientEmail.builder()
                .template(clientEmailEntity.getTemplate())
                .subject(clientEmailEntity.getSubject())
                .sender(clientEmailEntity.getSender())
                .build();

        OnboardClientEmail onboardClientEmailNew = OnboardClientEmail.builder()
                .template("Some_email_template_pl")
                .subject("Some subject PL")
                .sender("Cashflow Analyser PL<no-reply-cashflow-analyser@yolt.com>")
                .build();

        // When
        clientManagementUseCase.updateClientEmailConfiguration(existingClientId, onboardClientEmailOld, onboardClientEmailNew);
        // executing the method second time to make sure client was correctly updated
        // this simulates a situation when two pods would execute this method on startup
        clientManagementUseCase.updateClientEmailConfiguration(existingClientId, onboardClientEmailOld, onboardClientEmailNew);

        // Then
        then(clientRepository.findById(existingClientId).get().getClientEmails())
                .extracting("template", "subject", "sender")
                .containsOnly(
                        tuple("UserInvitation_Test_Client", "Uitnodiging voor de Cashflow Analyser", "Cashflow Analyser <no-reply-cashflow-analyser@yolt.com>"),
                        tuple("Some_email_template_pl", "Some subject PL", "Cashflow Analyser PL<no-reply-cashflow-analyser@yolt.com>")
                );
    }

    @Test
    @Transactional
    void shouldNotOverrideExistingEmailConfigurationBecauseWrongSenderEmail() {
        // Given
        UUID existingClientId = UUID.fromString("0b4cee11-0bd6-4e86-806f-45c913ad7bd5");
        final ClientEntity clientEntity = clientRepository.findById(existingClientId).get();
        final ClientEmailEntity clientEmailEntity = new ClientEmailEntity();
        clientEmailEntity.setId(UUID.fromString("fa07ae4c-4010-428a-86d3-13728b763190"));
        clientEmailEntity.setTemplate("Some_email_template_en");
        clientEmailEntity.setSender("Cashflow Analyser EN<no-reply-cashflow-analyser@yolt.com>");
        clientEmailEntity.setSubject("Some subject EN");
        clientEmailEntity.setClient(clientEntity);

        clientEntity.getClientEmails().add(clientEmailEntity);
        clientEmailRepository.save(clientEmailEntity);

        OnboardClientEmail onboardClientEmailOld = OnboardClientEmail.builder()
                .template(clientEmailEntity.getTemplate())
                .subject(clientEmailEntity.getSubject())
                .sender(clientEmailEntity.getSender())
                .build();

        OnboardClientEmail onboardClientEmailNew = OnboardClientEmail.builder()
                .template("Some_email_template_pl")
                .subject("Invalid email address PL")
                .sender("Cashflow Analyser PL")
                .build();

        // When
        Throwable thrown = catchThrowable(() -> clientManagementUseCase.updateClientEmailConfiguration(existingClientId, onboardClientEmailOld, onboardClientEmailNew));

        // Then
        assertThat(thrown).isInstanceOf(ConstraintViolationException.class);
        assertThat(thrown.getMessage()).contains("must be a well-formed e-mail address optionally with user name");
    }
}
