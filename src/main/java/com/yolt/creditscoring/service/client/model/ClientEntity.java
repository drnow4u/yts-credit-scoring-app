package com.yolt.creditscoring.service.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = ClientEntity.TABLE_NAME)
@Entity
public class ClientEntity {

    public static final String TABLE_NAME = "client";

    @Id
    private UUID id;

    @NotNull
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String logo;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ClientLanguage defaultLanguage;

    private String additionalTextReport;

    private String additionalTextConsent;

    @NotNull
    private String siteTags;

    private String redirectUrl;

    @Column(name = "pd_score_feature_toggle")
    private boolean pDScoreFeatureToggle;

    @Column(name = "signature_verification_feature_toggle")
    private boolean isSignatureVerificationFeatureToggle;

    private boolean categoryFeatureToggle;

    private boolean monthsFeatureToggle;

    private boolean overviewFeatureToggle;

    private boolean apiTokenFeatureToggle;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "client", cascade = CascadeType.ALL)
    private Set<ClientEmailEntity> clientEmails;
}
