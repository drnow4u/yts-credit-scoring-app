package com.yolt.creditscoring.service.yoltapi.service;

import com.yolt.creditscoring.controller.user.site.SiteViewDTO;
import com.yolt.creditscoring.service.creditscore.model.AccountReference;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.service.yoltapi.http.YoltHttpClient;
import com.yolt.creditscoring.service.yoltapi.http.model.*;
import com.yolt.creditscoring.service.yoltapi.webclient.ClientAuthenticationMeans;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class YoltFetchDataServiceTest {

    @Mock
    private PrivateKey tlsPrivateKey;

    @Mock
    private X509Certificate tlsCertificate;

    @Mock
    private KeyStore keyStore;

    @Mock
    private PrivateKey signingPrivateKey;

    @Mock
    private YoltHttpClient yoltHttpClient;

    @InjectMocks
    private YoltFetchDataService yoltFetchDataService;

    @Test
    void shouldFetchSitesDependantOnTag() throws Exception {
        // Given
        ClientAuthenticationMeans authenticationMean = getClientAuthenticationMeans();

        ClientSiteEntity clientSiteEntity = new ClientSiteEntity();
        clientSiteEntity.setName("SOME_NL_BANK");
        clientSiteEntity.setId(UUID.fromString("9970f107-5ea6-4607-add1-55cf2324bed6"));
        given(yoltHttpClient.getClientSite(authenticationMean, "NL")).willReturn(new ClientSiteEntity[]{clientSiteEntity});

        ClientSiteEntity clientSiteEntity2 = new ClientSiteEntity();
        clientSiteEntity2.setName("SOME_GB_BANK");
        clientSiteEntity2.setId(UUID.fromString("8e0ab02b-b3b0-439d-826a-b5a9e182bb01"));
        given(yoltHttpClient.getClientSite(authenticationMean, "GB")).willReturn(new ClientSiteEntity[]{clientSiteEntity, clientSiteEntity2});

        // When
        List<SiteViewDTO> results = yoltFetchDataService.fetchSites(authenticationMean, "NL,GB,PL");

        // Then
        then(yoltHttpClient).should().getClientSite(authenticationMean, "NL");
        then(yoltHttpClient).should().getClientSite(authenticationMean, "GB");
        then(yoltHttpClient).should().getClientSite(authenticationMean, "PL");

        assertThat(results).extracting("id", "name")
                .containsExactly(tuple(UUID.fromString("9970f107-5ea6-4607-add1-55cf2324bed6"), "SOME_NL_BANK"),
                        tuple(UUID.fromString("8e0ab02b-b3b0-439d-826a-b5a9e182bb01"), "SOME_GB_BANK"));
    }

    @Test
    void shouldCorrectlyMapAccount() throws Exception {
        // Given
        ClientAuthenticationMeans authenticationMean = getClientAuthenticationMeans();

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(UUID.fromString("68c3e456-0ca3-4d13-90f4-cc73c17f76cb"));
        accountDTO.setBalance(new BigDecimal("5000"));
        accountDTO.setCurrency(CurrencyEnum.EUR);
        accountDTO.setStatus(AccountStatusEnum.ENABLED);
        accountDTO.setType(TypeEnum.CURRENT_ACCOUNT);
        accountDTO.setUsage(UsageEnum.PRIVATE);
        accountDTO.setLastDataFetchTime(SOME_FIXED_TEST_DATE);

        AccountReferencesDTO accountReferencesDTO = new AccountReferencesDTO();
        accountReferencesDTO.setIban("NL79ABNA12345678901");
        accountReferencesDTO.setBban("79ABNA12345678901");
        accountReferencesDTO.setSortCodeAccountNumber("9455762838");
        accountReferencesDTO.setMaskedPan("1234 **** **** 5678");
        accountDTO.setAccountReferences(accountReferencesDTO);

        CurrentAccountDTO currentAccountDTO = new CurrentAccountDTO();
        currentAccountDTO.setCreditLimit(new BigDecimal("1000"));
        accountDTO.setCurrentAccount(currentAccountDTO);

        given(yoltHttpClient.accounts(authenticationMean, SOME_YOLT_USER_ID)).willReturn(new AccountDTO[]{accountDTO});

        // When
        List<CreditScoreAccountDTO> results = yoltFetchDataService.accounts(authenticationMean, SOME_YOLT_USER_ID);

        // Then
        assertThat(results).hasSize(1);
        CreditScoreAccountDTO csa = results.get(0);
        assertThat(csa.getId()).isEqualTo(UUID.fromString("68c3e456-0ca3-4d13-90f4-cc73c17f76cb"));
        assertThat(csa.getBalance()).isEqualTo(new BigDecimal("5000"));
        assertThat(csa.getLastDataFetchTime()).isEqualTo(SOME_FIXED_TEST_DATE);
        assertThat(csa.getCurrency()).isEqualTo("EUR");
        assertThat(csa.getStatus()).isEqualTo("ENABLED");
        assertThat(csa.getType()).isEqualTo("CURRENT_ACCOUNT");
        assertThat(csa.getUsage()).isEqualTo("PRIVATE");
        assertThat(csa.getAccountReference().getIban()).isEqualTo("NL79ABNA12345678901");
        assertThat(csa.getAccountReference().getBban()).isEqualTo("79ABNA12345678901");
        assertThat(csa.getAccountReference().getSortCodeAccountNumber()).isEqualTo("9455762838");
        assertThat(csa.getAccountReference().getMaskedPan()).isEqualTo("1234 **** **** 5678");
        assertThat(csa.getCreditLimit()).isEqualTo(new BigDecimal("-1000"));
    }

    @Test
    void shouldReturnEmptyAccountReferenceObjectIfAccountReferenceWillBeNull() throws Exception {
        // Given
        ClientAuthenticationMeans authenticationMean = getClientAuthenticationMeans();

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(UUID.fromString("68c3e456-0ca3-4d13-90f4-cc73c17f76cb"));
        accountDTO.setBalance(new BigDecimal("5000"));
        accountDTO.setCurrency(CurrencyEnum.EUR);
        accountDTO.setStatus(AccountStatusEnum.ENABLED);
        accountDTO.setType(TypeEnum.CURRENT_ACCOUNT);
        accountDTO.setUsage(UsageEnum.PRIVATE);
        AccountReferencesDTO accountReferencesDTO = new AccountReferencesDTO();
        accountDTO.setAccountReferences(accountReferencesDTO);

        given(yoltHttpClient.accounts(authenticationMean, SOME_YOLT_USER_ID)).willReturn(new AccountDTO[]{accountDTO});

        // When
        List<CreditScoreAccountDTO> results = yoltFetchDataService.accounts(authenticationMean, SOME_YOLT_USER_ID);

        // Then
        assertThat(results).hasSize(1);
        CreditScoreAccountDTO csa = results.get(0);
        assertThat(csa.getAccountReference()).isEqualTo(AccountReference.builder().build());
    }

    @Test
    void shouldCorrectlyMapCreditLimitForCreditCardAccount() throws Exception {
        // Given
        ClientAuthenticationMeans authenticationMean = getClientAuthenticationMeans();

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(UUID.fromString("68c3e456-0ca3-4d13-90f4-cc73c17f76cb"));
        accountDTO.setBalance(new BigDecimal("5000"));
        accountDTO.setCurrency(CurrencyEnum.EUR);
        accountDTO.setStatus(AccountStatusEnum.ENABLED);
        accountDTO.setType(TypeEnum.CREDIT_CARD);
        accountDTO.setUsage(UsageEnum.PRIVATE);

        AccountReferencesDTO accountReferencesDTO = new AccountReferencesDTO();
        accountReferencesDTO.setIban("NL79ABNA12345678901");
        accountDTO.setAccountReferences(accountReferencesDTO);

        CreditCardAccountDTO currentAccountDTO = new CreditCardAccountDTO();
        currentAccountDTO.setCreditLimit(new BigDecimal("1000"));
        accountDTO.setCreditCardAccount(currentAccountDTO);

        given(yoltHttpClient.accounts(authenticationMean, SOME_YOLT_USER_ID)).willReturn(new AccountDTO[]{accountDTO});

        // When
        List<CreditScoreAccountDTO> results = yoltFetchDataService.accounts(authenticationMean, SOME_YOLT_USER_ID);

        // Then
        assertThat(results).hasSize(1);
        CreditScoreAccountDTO csa = results.get(0);
        assertThat(csa.getId()).isEqualTo(UUID.fromString("68c3e456-0ca3-4d13-90f4-cc73c17f76cb"));
        assertThat(csa.getBalance()).isEqualTo(new BigDecimal("5000"));
        assertThat(csa.getCurrency()).isEqualTo("EUR");
        assertThat(csa.getStatus()).isEqualTo("ENABLED");
        assertThat(csa.getType()).isEqualTo("CREDIT_CARD");
        assertThat(csa.getUsage()).isEqualTo("PRIVATE");
        assertThat(csa.getAccountReference().getIban()).isEqualTo("NL79ABNA12345678901");
        assertThat(csa.getCreditLimit()).isEqualTo(new BigDecimal("-1000"));
    }

    @Test
    void shouldCorrectlyMapAccountWhenSomeFieldsNotRequiredFieldsWillBeNull() throws Exception {
        // Given
        ClientAuthenticationMeans authenticationMean = getClientAuthenticationMeans();

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(UUID.fromString("68c3e456-0ca3-4d13-90f4-cc73c17f76cb"));
        accountDTO.setBalance(new BigDecimal("5000"));
        accountDTO.setCurrency(CurrencyEnum.EUR);
        accountDTO.setStatus(AccountStatusEnum.ENABLED);
        accountDTO.setType(TypeEnum.CURRENT_ACCOUNT);
        accountDTO.setUsage(null);
        accountDTO.setCurrentAccount(null);
        accountDTO.setCreditCardAccount(null);

        AccountReferencesDTO accountReferencesDTO = new AccountReferencesDTO();
        accountReferencesDTO.setIban("NL79ABNA12345678901");
        accountDTO.setAccountReferences(accountReferencesDTO);

        given(yoltHttpClient.accounts(authenticationMean, SOME_YOLT_USER_ID)).willReturn(new AccountDTO[]{accountDTO});

        // When
        List<CreditScoreAccountDTO> results = yoltFetchDataService.accounts(authenticationMean, SOME_YOLT_USER_ID);

        // Then
        assertThat(results).hasSize(1);
        CreditScoreAccountDTO csa = results.get(0);
        assertThat(csa.getId()).isEqualTo(UUID.fromString("68c3e456-0ca3-4d13-90f4-cc73c17f76cb"));
        assertThat(csa.getBalance()).isEqualTo(new BigDecimal("5000"));
        assertThat(csa.getCurrency()).isEqualTo("EUR");
        assertThat(csa.getStatus()).isEqualTo("ENABLED");
        assertThat(csa.getType()).isEqualTo("CURRENT_ACCOUNT");
        assertThat(csa.getUsage()).isNull();
        assertThat(csa.getAccountReference().getIban()).isEqualTo("NL79ABNA12345678901");
        assertThat(csa.getCreditLimit()).isNull();
    }

    private ClientAuthenticationMeans getClientAuthenticationMeans() {
        ClientAuthenticationMeans authenticationMean = ClientAuthenticationMeans.builder()
                .clientId(SOME_YOLT_CLIENT_ID)
                .requestTokenPublicKeyId(UUID.fromString("1a5cee11-0bd6-4e86-806f-45c913ad7aa1"))
                .redirectUrlId(UUID.fromString("f6856ba7-0fb0-44b4-85a5-b72a229710cd"))
                .signingPrivateKey(signingPrivateKey)
                .accessToken("Fake access token")
                .build();
        return authenticationMean;
    }
}
