package com.yolt.creditscoring.service.legaldocument.service;

import com.yolt.creditscoring.service.legaldocument.LegalDocumentService;
import com.yolt.creditscoring.service.legaldocument.model.DocumentType;
import com.yolt.creditscoring.service.legaldocument.model.LegalDocument;
import com.yolt.creditscoring.service.legaldocument.model.LegalDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LegalDocumentServiceTest {

    @Mock
    private LegalDocumentRepository legalDocumentRepository;

    @InjectMocks
    private LegalDocumentService legalDocumentService;

    @Test
    void shouldGetCurrentTermsAndConditions() {
        // Given
        given(legalDocumentRepository
                .findFirstByDocumentTypeOrderByVersionDesc(DocumentType.TERMS_AND_CONDITIONS))
                .willReturn(SOME_T_AND_C);

        // When
        LegalDocument termsAndConditions = legalDocumentService.getCurrentTermsAndConditions();

        // Then
        assertThat(termsAndConditions.getId()).isEqualTo(SOME_T_AND_C.getId());
        assertThat(termsAndConditions.getContent()).isEqualTo(SOME_T_AND_C.getContent());
    }

    @Test
    void shouldGetCurrentPrivatePolicy() {
        // Given
        given(legalDocumentRepository
                .findFirstByDocumentTypeOrderByVersionDesc(DocumentType.PRIVACY_POLICY))
                .willReturn(SOME_PRIVACY_POLICY);

        // When
        LegalDocument privacyPolicy = legalDocumentService.getCurrentPrivacyPolicy();

        // Then
        assertThat(privacyPolicy.getId()).isEqualTo(SOME_PRIVACY_POLICY.getId());
        assertThat(privacyPolicy.getContent()).isEqualTo(SOME_PRIVACY_POLICY.getContent());
    }
}
