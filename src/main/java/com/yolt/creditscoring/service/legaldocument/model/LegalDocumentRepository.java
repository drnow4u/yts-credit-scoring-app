package com.yolt.creditscoring.service.legaldocument.model;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface LegalDocumentRepository extends CrudRepository<LegalDocument, UUID> {
    
    LegalDocument findFirstByDocumentTypeOrderByVersionDesc(DocumentType documentType);
}
