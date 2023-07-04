package com.yolt.creditscoring.service.legaldocument;

import com.yolt.creditscoring.service.legaldocument.model.DocumentType;
import com.yolt.creditscoring.service.legaldocument.model.LegalDocument;
import com.yolt.creditscoring.service.legaldocument.model.LegalDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LegalDocumentService {

    private final LegalDocumentRepository legalDocumentRepository;

    public LegalDocument getCurrentTermsAndConditions() {
        return legalDocumentRepository.findFirstByDocumentTypeOrderByVersionDesc(DocumentType.TERMS_AND_CONDITIONS);
    }

    public LegalDocument getCurrentPrivacyPolicy() {
        return legalDocumentRepository.findFirstByDocumentTypeOrderByVersionDesc(DocumentType.PRIVACY_POLICY);
    }
}
