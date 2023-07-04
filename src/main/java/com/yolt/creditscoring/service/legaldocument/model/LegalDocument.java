package com.yolt.creditscoring.service.legaldocument.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = LegalDocument.TABLE_NAME)
public class LegalDocument {

    public static final String TABLE_NAME = "legal_document";

    @Id
    private UUID id;

    @Lob
    @NotNull
    @Type(type = "org.hibernate.type.TextType")
    private String content;

    @NotNull
    private Integer version;

    /**
     * Date when content of document was the last time updated by lawyers
     */
    @NotNull
    private LocalDate creationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
}
